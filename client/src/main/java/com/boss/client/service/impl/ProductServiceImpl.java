package com.boss.client.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.thread.ExecutorBuilder;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.boss.client.dao.OperationLogDao;
import com.boss.client.dao.OrderItemDao;
import com.boss.client.dao.ProductDao;
import com.boss.client.dao.ShopDao;
import com.boss.client.service.ProductService;
import com.boss.client.util.CommonUtil;
import com.boss.client.util.ShopeeUtil;
import com.boss.client.vo.PageResult;
import com.boss.client.vo.ProductInfoVO;
import com.boss.client.vo.ProductVO;
import com.boss.common.vo.SelectVO;
import com.boss.common.dto.ConditionDTO;
import com.boss.common.enities.Model;
import com.boss.common.enities.OperationLog;
import com.boss.common.enities.Product;
import com.boss.common.enities.Shop;
import com.boss.common.enums.ProductStatusEnum;
import com.boss.common.util.BeanCopyUtils;
import com.boss.common.util.PageUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

import static com.boss.common.constant.OptTypeConst.SYSTEM_LOG;
import static com.boss.common.constant.RedisPrefixConst.*;

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
    @Transactional(rollbackFor = Exception.class)
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

    /**
     * 根据状态刷新商品
     * @param status
     */
    @Transactional(rollbackFor = Exception.class)
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
            if (!itemIds.isEmpty()) {
                for (String itemId : itemIds) {
                    productDao.update(new UpdateWrapper<Product>().set("status", "SELLER_DELETE").eq("item_id", itemId));
                }
            }

            status = "&item_status=SHOPEE_DELETE";
            itemIds = ShopeeUtil.getProducts(accessToken, shopId, 0, new ArrayList<>(), status);
            if (!itemIds.isEmpty()) {
                for (String itemId : itemIds) {
                    productDao.update(new UpdateWrapper<Product>().set("status", "SHOPEE_DELETE").eq("item_id", itemId));
                }
            }
        }
    }

    /**
     * 刷新指定商品
     * @param itemIds
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void refreshProducts(List<Long> itemIds) {
        StringJoiner sj = new StringJoiner(",");
        for (int i = 0; i < itemIds.size(); i ++) {
            sj.add(String.valueOf(itemIds.get(i)));
        }
        List<Product> products = productDao.selectList(new QueryWrapper<Product>().select("item_id", "shop_id").in("item_id", sj.toString()));

        Map<Long, List<String>> map = new HashMap<>();

        for (Product product : products) {
            long itemId = product.getItemId();
            long shop_id = product.getShopId();

            if (!map.containsKey(shop_id)) {
                List<String> temp = new ArrayList<>();
                temp.add(String.valueOf(itemId));
                map.put(shop_id, temp);
            } else {
                List<String> temp = map.get(shop_id);
                temp.add(String.valueOf(itemId));
                map.put(shop_id, temp);
            }
        }

        for (long shopId : map.keySet()) {
            List<String> oldItemList = map.get(shopId);

            List<String> itemIdList = new ArrayList<>();
            for (int i = 0; i < oldItemList.size(); i += 50) {
                itemIdList.add(String.join(",", oldItemList.subList(i, Math.min(i + 50, oldItemList.size()))));
            }

            refreshProductByItemId(itemIdList, shopId);
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
                    }, ExecutorBuilder.create().setCorePoolSize(itemIdList.size()).build());
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
                    }, ExecutorBuilder.create().setCorePoolSize(itemIdList.size()).build());
                }).collect(Collectors.toList());

        CompletableFuture.allOf(productFutures.toArray(new CompletableFuture[0])).join();
        CompletableFuture.allOf(modelFutures.toArray(new CompletableFuture[0])).join();

        log.info("===产品发送请求并处理结束，耗时：{}秒", (System.currentTimeMillis() - startTime) / 1000);

        log.info("===开始产品数据落库");
        startTime = System.currentTimeMillis();


        List<List<Product>> splitProduct = CommonUtil.splitListBatches(productList, 100);
        List<CompletableFuture<Void>> insertProductFutures = new ArrayList<>();
        for (List<Product> batch : splitProduct) {
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                try {
//                    TransactionTemplate transactionTemplate = new TransactionTemplate();
                    transactionTemplate.executeWithoutResult(status -> {
                        this.saveOrUpdateBatch(batch);
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }, ExecutorBuilder.create().setCorePoolSize(splitProduct.size()).build());

            insertProductFutures.add(future);
        }


        List<List<Model>> splitModel = CommonUtil.splitListBatches(modelList, 5000);
        List<CompletableFuture<Void>> insertModelFutures = new ArrayList<>();
        for (List<Model> batch : splitModel) {
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                try {
//                    TransactionTemplate transactionTemplate = new TransactionTemplate();
                    transactionTemplate.executeWithoutResult(status -> {
                        modelService.saveOrUpdateBatch(batch);
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }, ExecutorBuilder.create().setCorePoolSize(splitModel.size()).build());

            insertModelFutures.add(future);
        }

        CompletableFuture.allOf(insertProductFutures.toArray(new CompletableFuture[0])).join();
        CompletableFuture.allOf(insertModelFutures.toArray(new CompletableFuture[0])).join();

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


                String judgeResult = CommonUtil.judgeRedis(redisService,PRODUCT_ITEM + itemId, productList, product, Product.class);
                if (!"".equals(judgeResult)) {
                    JSONArray diffArray = JSON.parseObject(judgeResult).getJSONArray("defectsList");
                    if (diffArray.size() != 0) {
                        StringJoiner joiner = new StringJoiner(",");
                        OperationLog operationLog = new OperationLog();
                        operationLog.setOptType(SYSTEM_LOG);
                        for (int j = 0; j < diffArray.size(); j++) {
                            String key = diffArray.getJSONObject(j).getJSONObject("travelPath").getString("abstractTravelPath");
                            joiner.add(key.substring(key.indexOf(".") + 1, key.length()));
                        }
                        operationLog.setOptDesc("产品 " + itemId + " 字段发生变化：" + joiner.toString());
                        operationLogDao.insert(operationLog);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public PageResult<ProductVO> productListByCondition(ConditionDTO condition) {
        // 查询分类数量
        Integer count = productDao.productCount(condition);
        if (count == 0) {
            return new PageResult<>();
        }
        // 分页查询分类列表
        List<ProductVO> productList = productDao.productList(PageUtils.getLimitCurrent(), PageUtils.getSize(), condition)
                .stream().map(productVO -> {
                    Object redisCategoryObj = redisService.get(CATEGORY + productVO.getCategoryId());
                    if (redisCategoryObj != null) {
                        productVO.setCategoryName(JSONObject.parseObject(redisCategoryObj.toString()).getString("display_category_name"));
                    }
                    productVO.setCreateTime(CommonUtil.timestamp2String((Long) productVO.getCreateTime()));
                    productVO.setStatus(ProductStatusEnum.getDescByCode(productVO.getStatus()));

                    productVO.setShopName(shopDao.selectOne(new QueryWrapper<Shop>().select("name").eq("shop_id", productVO.getShopId())).getName());

                    // 设置销量
//                    Integer tempCount = orderItemDao.salesVolumeByItemId(productVO.getItemId());
//                    int salesVolume = tempCount == null ? 0 : tempCount;
//                    productVO.setSalesVolume(salesVolume);

                    Product product = BeanCopyUtils.copyObject(productVO, Product.class);
                    product.setCreateTime(CommonUtil.string2Timestamp(String.valueOf(productVO.getCreateTime())));
                    // 判断规则设置产品等级
                    productVO.setGrade(getGrade(product, productVO.getSalesVolume()));

                    return productVO;
                }).collect(Collectors.toList());
        return new PageResult<>(productList, count);
    }

    @Override
    public ProductInfoVO getProductInfo(Long itemId) {

        Product product = productDao.selectOne(new QueryWrapper<Product>().eq("item_id", itemId));

        ProductInfoVO productInfoVO = BeanCopyUtils.copyObject(product, ProductInfoVO.class);
        Object redisCategoryObj = redisService.get(CATEGORY + product.getCategoryId());
        if (redisCategoryObj != null) {
            productInfoVO.setCategoryName(JSONObject.parseObject(redisCategoryObj.toString()).getString("display_category_name"));
        }

        productInfoVO.setCreateTime(CommonUtil.timestamp2String(product.getCreateTime()));
        productInfoVO.setStatus(ProductStatusEnum.getDescByCode(product.getStatus()));

        productInfoVO.setShopName(shopDao.selectOne(new QueryWrapper<Shop>().eq("shop_id", product.getShopId())).getName());

        // 判断规则设置产品等级
        // 设置销量
        Integer tempCount = orderItemDao.salesVolumeByItemId(product.getItemId());
        int salesVolume = tempCount == null ? 0 : tempCount;
        productInfoVO.setGrade(getGrade(product, salesVolume));

        productInfoVO.setModelVOList(modelService.getModelVOListByItemId(itemId));

        return productInfoVO;
    }

    @Override
    public List<SelectVO> getCategorySelect() {
        List<SelectVO> list = new ArrayList<>();
        List<Product> categoryId = productDao.selectList(new QueryWrapper<Product>().select("category_id").groupBy("category_id"));
        for (Product product : categoryId) {
            Long id = product.getCategoryId();
            Object redisCategoryObj = redisService.get(CATEGORY + product.getCategoryId());
            if (redisCategoryObj == null) {
                continue;
            }
            String name = JSONObject.parseObject(redisCategoryObj.toString()).getString("display_category_name");
            SelectVO vo = SelectVO.builder()
                    .key(id)
                    .value(name).build();
            list.add(vo);
        }
        return list;
    }

    @Override
    public List<SelectVO> getStatusSelect() {
//        List<SelectVO> list = new ArrayList<>();
//        for (ProductStatusEnum statusEnum : ProductStatusEnum.values()) {
//            SelectVO vo = SelectVO.builder()
//                    .key(statusEnum.getCode())
//                    .value(statusEnum.getDesc()).build();
//            list.add(vo);
//        }
        return ProductStatusEnum.getProductStatusEnum();
    }


    public String getGrade(Product product, int salesVolume) {

        String grade = "";
        boolean allOrNot;
        JSONObject ruleData;
        // 满足规则次数
        int ruleCount = 0;
        Set<String> keys = redisService.keys(RULE + "*");
        for (String key : keys) {
            JSONObject object = JSONObject.parseObject(redisService.get(key).toString());

            ruleData = object.getJSONObject("ruleData");
            if (ruleData == null || ruleData.keySet().size() == 0) {
                continue;
            }
            grade = object.getString("grade");
            allOrNot = object.getBoolean("allOrNot");

            // true：全部满足
            // false：满足任一条件
            // 满足条件次数
            int count = getSatisfactionCount(product, ruleData, allOrNot, salesVolume);
            // 全部满足：满足条件次数 = 全部条件个数
            if (allOrNot && count == ruleData.keySet().size()) {
                ruleCount ++;
            } else if (!allOrNot && count > 0) {
                // 满足任一条件：满足条件次数 > 0
                ruleCount ++;
            }

            // 满足超过一个规则直接 break
            if (ruleCount > 1) {
                grade = "!";
                break;
            }
        }

        return grade;
    }

    private int getSatisfactionCount(Product product, JSONObject ruleData, boolean allOrNot, int salesVolume) {
        Date nowDate = new Date();

        int count = 0;

        if (ruleData.containsKey("itemId") && ruleData.getJSONObject("itemId") != null) {
            if (ruleData.getJSONObject("itemId").getString("value").equals(String.valueOf(product.getItemId()))) {
                count ++;
            }

            if (returnOrNot(allOrNot, count)) {
                return count;
            }
        }

        if (ruleData.containsKey("itemSku") && ruleData.getJSONObject("itemSku") != null) {
            if (ruleData.getJSONObject("itemSku").getString("value").equals(String.valueOf(product.getItemSku()))) {
                count ++;
            }

            if (returnOrNot(allOrNot, count)) {
                return count;
            }
        }

        if (ruleData.containsKey("categoryId") && ruleData.getJSONObject("categoryId") != null) {
            if (ruleData.getJSONObject("categoryId").getString("value").equals(String.valueOf(product.getCategoryId()))) {
                count ++;
            }

            if (returnOrNot(allOrNot, count)) {
                return count;
            }
        }

        if (ruleData.containsKey("status") && ruleData.getJSONObject("status") != null) {
            if (ruleData.getJSONObject("status").getString("value").equals(String.valueOf(product.getStatus()))) {
                count ++;
            }

            if (returnOrNot(allOrNot, count)) {
                return count;
            }
        }

        if (ruleData.containsKey("createTime") && ruleData.getJSONObject("createTime") != null) {
            JSONObject object = ruleData.getJSONObject("createTime");
            LocalDateTime createTime = CommonUtil.timestamp2LocalDateTime(product.getCreateTime());
            LocalDateTime startTime = CommonUtil.string2LocalDateTime(object.getString("startTime"));
            LocalDateTime endTime = CommonUtil.string2LocalDateTime(object.getString("endTime"));

            if ((createTime.isAfter(startTime) && createTime.isBefore(endTime)) || createTime.equals(startTime) || createTime.equals(endTime)) {
                count ++;
            }

            if (returnOrNot(allOrNot, count)) {
                return count;
            }
        }

        if (ruleData.containsKey("price") && ruleData.getJSONObject("price") != null) {
            JSONObject object = ruleData.getJSONObject("price");
            // 最小价格
            BigDecimal minPrice = new BigDecimal(object.getString("minPrice"));
            // 最大价格
            BigDecimal maxPrice = new BigDecimal(object.getString("maxPrice"));

            BigDecimal tempItemMinPrice = orderItemDao.itemMinPrice(product.getItemId());
            BigDecimal itemMinPrice = tempItemMinPrice == null ? new BigDecimal(0) : tempItemMinPrice;

            if (itemMinPrice.compareTo(minPrice) >= 0 && itemMinPrice.compareTo(maxPrice) <= 0) {
                count++;
            }

            if (returnOrNot(allOrNot, count)) {
                return count;
            }
        }

        if (ruleData.containsKey("salesVolume") && ruleData.getJSONObject("salesVolume") != null) {
            JSONObject object = ruleData.getJSONObject("salesVolume");
            int ruleValue = Integer.valueOf(object.getString("value"));
            String ruleType = object.getString("type");

            count = judgeIntegerRange(salesVolume, ruleValue, ruleType, count);

            if (returnOrNot(allOrNot, count)) {
                return count;
            }
        }

        if (ruleData.containsKey("salesVolumeOneDays") && ruleData.getJSONObject("salesVolumeOneDays") != null) {
            JSONObject object = ruleData.getJSONObject("salesVolumeOneDays");
            int ruleValue = Integer.valueOf(object.getString("value"));
            String date = object.getString("date");
            String ruleType = object.getString("type");

            long time = CommonUtil.string2Timestamp(date);

            Integer tempCount = orderItemDao.itemCountByCreateTime(product.getItemId(), time);
            int salesVolumeOneDaysCount = tempCount == null ? 0 : tempCount;

            count = judgeIntegerRange(salesVolumeOneDaysCount, ruleValue, ruleType, count);

            if (returnOrNot(allOrNot, count)) {
                return count;
            }
        }

        if (ruleData.containsKey("salesVolume7days") && ruleData.getJSONObject("salesVolume7days") != null) {
            JSONObject object = ruleData.getJSONObject("salesVolume7days");
            // 值
            int ruleValue = Integer.valueOf(object.getString("value"));
            // 符号类型
            String ruleType = object.getString("type");

            // 当前时间 - 7 天的时间戳
            long startTime = DateUtil.offsetDay(nowDate, -7).getTime();
            long endTime = nowDate.getTime();

            Integer tempCount = orderItemDao.itemCountByCreateTimeRange(product.getItemId(), startTime, endTime);
            int salesVolume7daysCount = tempCount == null ? 0 : tempCount;

            count = judgeIntegerRange(salesVolume7daysCount, ruleValue, ruleType, count);

            if (returnOrNot(allOrNot, count)) {
                return count;
            }
        }

        if (ruleData.containsKey("salesVolume30days") && ruleData.getJSONObject("salesVolume30days") != null) {
            JSONObject object = ruleData.getJSONObject("salesVolume30days");
            int ruleValue = Integer.valueOf(object.getString("value"));
            String ruleType = object.getString("type");

            // 当前时间 - 30 天的时间戳
            long startTime = DateUtil.offsetDay(nowDate, -30).getTime();
            long endTime = nowDate.getTime();

            Integer tempCount = orderItemDao.itemCountByCreateTimeRange(product.getItemId(), startTime, endTime);
            int salesVolume30daysCount = tempCount == null ? 0 : tempCount;

            count = judgeIntegerRange(salesVolume30daysCount, ruleValue, ruleType, count);

            if (returnOrNot(allOrNot, count)) {
                return count;
            }
        }

        return count;
    }

    private int judgeIntegerRange(int salesVolume, int ruleValue, String type, int count) {
        if ("=".equals(type) && salesVolume == ruleValue) {
            count++;
        } else if ("<=".equals(type) && salesVolume <= ruleValue) {
            count++;
        } else if (">=".equals(type) && salesVolume >= ruleValue) {
            count++;
        } else if (">".equals(type) && salesVolume > ruleValue) {
            count++;
        } else if ("<".equals(type) && salesVolume < ruleValue) {
            count++;
        }

        return count;
    }

    private boolean returnOrNot(boolean allOrNot, int count) {
        if (!allOrNot && count > 0) {
            return true;
        }
        return false;
    }
}
