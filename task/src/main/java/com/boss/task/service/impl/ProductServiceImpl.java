package com.boss.task.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.boss.common.enities.Category;
import com.boss.common.enities.Model;
import com.boss.common.enities.Product;
import com.boss.common.enities.Shop;
import com.boss.common.util.CommonUtil;
import com.boss.task.dao.*;
import com.boss.task.service.ProductService;
import com.boss.task.util.ShopeeUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

import static com.boss.common.constant.RedisPrefixConst.CATEGORY;
import static com.boss.common.enums.ProductStatusEnum.SELLER_DELETE;


/**
 * @Description
 * @Author 罗宇航
 * @Date 2024/4/11
 */

@Service
@Slf4j
public class ProductServiceImpl extends ServiceImpl<ProductDao, Product> implements ProductService {

    @Autowired
    private ShopDao shopDao;

    @Autowired
    private ProductDao productDao;

    @Autowired
    private OrderItemDao orderItemDao;

    @Autowired
    private CategoryDao categoryDao;

    @Autowired
    private ModelServiceImpl modelService;

    @Autowired
    private ShopServiceImpl shopService;

    @Autowired
    private RedisServiceImpl redisService;

    @Autowired
    private OperationLogDao operationLogDao;

    @Autowired
    @Qualifier("customThreadPool")
    private ThreadPoolExecutor customThreadPool;

    private final TransactionTemplate transactionTemplate;

    @Autowired
    public ProductServiceImpl(DataSourceTransactionManager transactionManager) {
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    /**
     * 授权后初始化产品信息
     * @param shopId
     */
//    @Transactional(rollbackFor = Exception.class)
    @Override
    public void initProduct(long shopId) {
        String accessToken = shopService.getAccessTokenByShopId(String.valueOf(shopId));
        String status = "&item_status=NORMAL&item_status=BANNED&item_status=UNLIST&item_status=REVIEWING";
        List<String> itemIds = ShopeeUtil.getProducts(accessToken, shopId, 0, new ArrayList<>(), status);

        if (itemIds == null || itemIds.isEmpty()) {
            return;
        }

        List<String> itemIdList = new ArrayList<>();
        for (int i = 0; i < itemIds.size(); i += 50) {
            itemIdList.add(String.join(",", itemIds.subList(i, Math.min(i + 50, itemIds.size()))));
        }

        refreshProductByItemId(itemIdList, shopId);
    }

    @Override
    public void refreshProductByTime(String startTimeStr, String endTimeStr) {
        long startTime = CommonUtil.string2Timestamp(startTimeStr);
        long endTime = CommonUtil.string2Timestamp(endTimeStr);
        // 遍历所有未冻结店铺获取 token 和 shopId
        QueryWrapper<Shop> shopQueryWrapper = new QueryWrapper<>();
        shopQueryWrapper.select("shop_id").eq("status", "1");
        List<Shop> shopList = shopDao.selectList(shopQueryWrapper);

        // 根据每个店铺的 token 和 shopId 获取订单
        long shopId;
        String accessToken;
        for (Shop shop : shopList) {
            shopId = shop.getShopId();
            accessToken = shopService.getAccessTokenByShopId(String.valueOf(shopId));

            List<String> itemIdList = new ArrayList<>();

            List<Long[]> result = CommonUtil.splitIntoEveryNDaysTimestamp(startTime, endTime, 14);

            for (Long[] pair : result) {
                List<String> object = ShopeeUtil.getProductsByTime(accessToken, shopId, 0, new ArrayList<>(), pair[0], pair[1]);
                log.info(pair[0] + "---" + pair[1] + ":  " + object.size());
                itemIdList.addAll(object);
            }

            if (itemIdList == null || itemIdList.isEmpty()) {
                continue;
            }

            List<String> newOrderSnList = new ArrayList<>();
            for (int i = 0; i < itemIdList.size(); i += 50) {
                newOrderSnList.add(String.join(",", itemIdList.subList(i, Math.min(i + 50, itemIdList.size()))));
            }

            refreshProductByItemId(itemIdList, shopId);
        }
    }

    /**
     * 根据状态刷新商品
     * @param status
     */
//    @Transactional(rollbackFor = Exception.class)
    @Override
    public void refreshProductByStatus(String status) {
        // 遍历所有未冻结店铺获取 token 和 shopId
        QueryWrapper<Shop> shopQueryWrapper = new QueryWrapper<>();
        shopQueryWrapper.select("shop_id").eq("status", "1");
        List<Shop> shopList = shopDao.selectList(shopQueryWrapper);

        long shopId;
        String accessToken;
        for (Shop shop : shopList) {
            shopId = shop.getShopId();
            accessToken = shopService.getAccessTokenByShopId(String.valueOf(shopId));

            List<String> itemIds = ShopeeUtil.getProducts(accessToken, shopId, 0, new ArrayList<>(), status);

            if (itemIds == null || itemIds.isEmpty()) {
                continue;
            }

            List<String> itemIdList = new ArrayList<>();
            for (int i = 0; i < itemIds.size(); i += 50) {
                itemIdList.add(String.join(",", itemIds.subList(i, Math.min(i + 50, itemIds.size()))));
            }

            refreshProductByItemId(itemIdList, shopId);
        }
    }

    /**
     * 刷新被删除商品状态
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void refreshDeletedProduct() {
        // 遍历所有未冻结店铺获取 token 和 shopId
        QueryWrapper<Shop> shopQueryWrapper = new QueryWrapper<>();
        shopQueryWrapper.select("shop_id").eq("status", "1");
        List<Shop> shopList = shopDao.selectList(shopQueryWrapper);

        long shopId;
        String accessToken;
        for (Shop shop : shopList) {
            shopId = shop.getShopId();
            accessToken = shopService.getAccessTokenByShopId(String.valueOf(shopId));

            String status = "&item_status=SELLER_DELETE";
            List<String> itemIds = ShopeeUtil.getProducts(accessToken, shopId, 0, new ArrayList<>(), status);
            if (itemIds != null && !itemIds.isEmpty()) {
                StringJoiner joiner = new StringJoiner(",");
                for (String itemId : itemIds) {
                    joiner.add(itemId);
                }
            }
            List<String> tempList = productDao.selectList(new QueryWrapper<Product>().select("item_id" ).in("item_id", itemIds))
                    .stream().map(p -> String.valueOf(p.getItemId())).collect(Collectors.toList());
            if (tempList != null && !tempList.isEmpty()) {
                StringJoiner joiner = new StringJoiner(",");
                for (String itemId : tempList) {
                    joiner.add(itemId);
                }
                productDao.update(new UpdateWrapper<Product>().set("status", SELLER_DELETE.getCode()).in("item_id", joiner));
            }

//            status = "&item_status=SHOPEE_DELETE";
//            itemIds = ShopeeUtil.getProducts(accessToken, shopId, 0, new ArrayList<>(), status);
//            if (itemIds != null && !itemIds.isEmpty()) {
//                StringJoiner joiner = new StringJoiner(",");
//                for (String itemId : itemIds) {
//                    joiner.add(itemId);
//                }
//            }
//            tempList = productDao.selectList(new QueryWrapper<Product>().select("item_id" ).in("item_id", itemIds))
//                    .stream().map(p -> String.valueOf(p.getItemId())).collect(Collectors.toList());
//            if (tempList != null && !tempList.isEmpty()) {
//                StringJoiner joiner = new StringJoiner(",");
//                for (String itemId : tempList) {
//                    joiner.add(itemId);
//                }
//                productDao.update(new UpdateWrapper<Product>().set("status", "SHOPEE_DELETE").in("item_id", joiner));
//            }
//
//            status = "&item_status=BANNED";
//            itemIds = ShopeeUtil.getProducts(accessToken, shopId, 0, new ArrayList<>(), status);
//            if (itemIds != null && !itemIds.isEmpty()) {
//                StringJoiner joiner = new StringJoiner(",");
//                for (String itemId : itemIds) {
//                    joiner.add(itemId);
//                }
//            }
//            tempList = productDao.selectList(new QueryWrapper<Product>().select("item_id" ).in("item_id", itemIds))
//                    .stream().map(p -> String.valueOf(p.getItemId())).collect(Collectors.toList());
//            if (tempList != null && !tempList.isEmpty()) {
//                StringJoiner joiner = new StringJoiner(",");
//                for (String itemId : tempList) {
//                    joiner.add(itemId);
//                }
//                productDao.update(new UpdateWrapper<Product>().set("status", "BANNED").in("item_id", joiner));
//            }
        }
    }

    public void refreshProductByItemId(List<String> itemIdList, long shopId) {
        // 根据每个店铺的 token 和 shopId 获取产品
        List<Product> productList = new CopyOnWriteArrayList<>();
        List<Model> modelList =  new CopyOnWriteArrayList<>();

        // 改为全局线程池和 futurelist
        // ExecutorService executor = Executors.newFixedThreadPool(Math.min(itemIdList.size(), Runtime.getRuntime().availableProcessors() + 1));
        log.info("===产品发送请求及处理开始");
        long startTime =  System.currentTimeMillis();

        List<CompletableFuture<Void>> productFutures = itemIdList.stream()
                .map(itemId -> {
                    long finalShopId = shopId;
                    return CompletableFuture.runAsync(() -> {
                        String accessToken = shopService.getAccessTokenByShopId(String.valueOf(finalShopId));
                        getProductDetail(itemId, accessToken, finalShopId, productList);
                    }, customThreadPool);
                }).collect(Collectors.toList());



        List<CompletableFuture<Void>> modelFutures = itemIdList.stream()
                .map(itemId -> {
                    long finalShopId = shopId;
                    return CompletableFuture.runAsync(() -> {
                        String accessToken = shopService.getAccessTokenByShopId(String.valueOf(finalShopId));
                        String[] splitIds = itemId.split(",");
                        for (String splitId : splitIds) {
                            modelService.getModel(Long.parseLong(splitId), accessToken, finalShopId, modelList);
                        }
                    }, customThreadPool);
                }).collect(Collectors.toList());

        CompletableFuture.allOf(productFutures.toArray(new CompletableFuture[0])).join();
        CompletableFuture.allOf(modelFutures.toArray(new CompletableFuture[0])).join();

        log.info("===产品发送请求并处理结束，耗时：{}秒", (System.currentTimeMillis() - startTime) / 1000);

        log.info("===开始产品数据落库");
        startTime = System.currentTimeMillis();


        List<List<Product>> splitProduct = CommonUtil.splitListBatches(productList, 100);
//        List<CompletableFuture<Void>> insertProductFutures = new ArrayList<>();
        for (List<Product> batch : splitProduct) {
            CompletableFuture.runAsync(() -> {
                try {
//                    TransactionTemplate transactionTemplate = new TransactionTemplate();
                    transactionTemplate.executeWithoutResult(status -> {
                        this.saveOrUpdateBatch(batch);
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }, customThreadPool);

//            insertProductFutures.add(future);
        }


        List<List<Model>> splitModel = CommonUtil.splitListBatches(modelList, 5000);
//        List<CompletableFuture<Void>> insertModelFutures = new ArrayList<>();
        for (List<Model> batch : splitModel) {
            CompletableFuture.runAsync(() -> {
                try {
//                    TransactionTemplate transactionTemplate = new TransactionTemplate();
                    transactionTemplate.executeWithoutResult(status -> {
                        modelService.saveOrUpdateBatch(batch);
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }, customThreadPool);

//            insertModelFutures.add(future);
        }

//        CompletableFuture.allOf(insertProductFutures.toArray(new CompletableFuture[0])).join();
//        CompletableFuture.allOf(insertModelFutures.toArray(new CompletableFuture[0])).join();

        log.info("===产品数据落库结束，耗时：{}秒", (System.currentTimeMillis() - startTime) / 1000);

    }

    private void getProductDetail(String itemIds, String token, long shopId, List<Product> productList) {
        try {
            JSONObject result = ShopeeUtil.getProductInfo(token, shopId, itemIds);

            if (result == null || result.getString("error").contains("error")) {
                return;
            }

            JSONArray itemArray = result.getJSONObject("response").getJSONArray("item_list");

            JSONObject itemObject;
            JSONArray imgIdArray;
            JSONArray imgUrlArray;
            for (int i = 0; i < itemArray.size(); i++) {
                itemObject = itemArray.getJSONObject(i);

                imgIdArray = itemObject.getJSONObject("image").getJSONArray("image_id_list");
                imgUrlArray = itemObject.getJSONObject("image").getJSONArray("image_url_list");

                long itemId = itemObject.getLong("item_id");
                Product product = Product.builder()
                        .id(itemId)
                        .shopId(shopId)
                        .itemId(itemId)
                        .itemName(itemObject.getString("item_name"))
                        .categoryId(itemObject.getLong("category_id"))
                        .createTime(itemObject.getLong("create_time"))
                        .updateTime(itemObject.getLong("update_time"))
                        .itemSku(itemObject.getString("item_sku"))
                        .mainImgUrl(imgUrlArray.getString(0))
                        .mainImgId(imgIdArray.getString(0))
                        .status(itemObject.getString("item_status"))
                        .build();

                imgIdArray = itemObject.getJSONObject("image").getJSONArray("image_id_list");
                if (imgIdArray.size() > 0) {
                    product.setMainImgId(imgIdArray.getString(0));
                }
                imgUrlArray = itemObject.getJSONObject("image").getJSONArray("image_url_list");
                if (imgUrlArray.size() > 0) {
                    product.setMainImgUrl(imgUrlArray.getString(0));
                }

                long categoryId = itemObject.getLong("category_id");
                Boolean flag = redisService.setnx(CATEGORY + categoryId, categoryId);
                if (flag) {
                    categoryDao.insert(Category.builder().id(categoryId).build());
                }


//                String judgeResult = RedisUtil.judgeRedis(redisService,PRODUCT_ITEM + itemId, productList, product, Product.class);
//                if (!"".equals(judgeResult)) {
//                    JSONArray diffArray = JSON.parseObject(judgeResult).getJSONArray("defectsList");
//                    if (diffArray.size() != 0) {
//                        StringJoiner joiner = new StringJoiner(",");
//                        OperationLog operationLog = new OperationLog();
//                        operationLog.setOptType(SYSTEM_LOG);
//                        for (int j = 0; j < diffArray.size(); j++) {
//                            String key = diffArray.getJSONObject(j).getJSONObject("travelPath").getString("abstractTravelPath");
//                            joiner.add(key.substring(key.indexOf(".") + 1, key.length()));
//                        }
//                        operationLog.setOptDesc("产品 " + itemId + " 字段发生变化：" + joiner.toString());
//                        operationLogDao.insert(operationLog);
//                    }
//                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateStatusByItemId(Long itemId, String status) {
        productDao.update(new UpdateWrapper<Product>().set("status", status).eq("id", itemId));
    }

}
