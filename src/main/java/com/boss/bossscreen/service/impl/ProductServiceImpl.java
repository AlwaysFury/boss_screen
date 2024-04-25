package com.boss.bossscreen.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.boss.bossscreen.dao.OperationLogDao;
import com.boss.bossscreen.dao.ProductDao;
import com.boss.bossscreen.dao.ShopDao;
import com.boss.bossscreen.dto.ConditionDTO;
import com.boss.bossscreen.enities.Model;
import com.boss.bossscreen.enities.OperationLog;
import com.boss.bossscreen.enities.Product;
import com.boss.bossscreen.enities.Shop;
import com.boss.bossscreen.service.ProductService;
import com.boss.bossscreen.util.BeanCopyUtils;
import com.boss.bossscreen.util.CommonUtil;
import com.boss.bossscreen.util.PageUtils;
import com.boss.bossscreen.util.ShopeeUtil;
import com.boss.bossscreen.vo.PageResult;
import com.boss.bossscreen.vo.ProductInfoVO;
import com.boss.bossscreen.vo.ProductVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import java.util.concurrent.*;

import static com.boss.bossscreen.constant.OptTypeConst.SYSTEM_LOG;
import static com.boss.bossscreen.constant.RedisPrefixConst.PRODUCT_ITEM;

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
    private ModelServiceImpl modelService;

    @Autowired
    private ShopServiceImpl shopService;

    @Autowired
    private RedisServiceImpl redisService;

    @Autowired
    private OperationLogDao operationLogDao;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void saveOrUpdateProduct() {
        long startTime =  System.currentTimeMillis();
        // 遍历所有未冻结店铺获取 token 和 shopId
        QueryWrapper<Shop> shopQueryWrapper = new QueryWrapper<>();
        shopQueryWrapper.select("shop_id").eq("status", "1");
        List<Shop> shopList = shopDao.selectList(shopQueryWrapper);

        // 根据每个店铺的 token 和 shopId 获取产品
        List<Product> productList = new CopyOnWriteArrayList<>();
        List<Product> updateProList = new CopyOnWriteArrayList<>();
        List<Model> modelList =  new CopyOnWriteArrayList<>();
        List<Model> updateModelList = new CopyOnWriteArrayList<>();
        long shopId;
        String accessToken;
        List<String> itemIds;
        for (Shop shop : shopList) {
            shopId = shop.getShopId();
            accessToken = shopService.getAccessTokenByShopId(String.valueOf(shopId));

            itemIds = ShopeeUtil.getProducts(accessToken, shopId, 0, new ArrayList<>());

            if (itemIds.size() == 0) {
                continue;
            }

            List<String> itemIdList = new ArrayList<>();
            for (int i = 0; i < itemIds.size(); i += 50) {
                itemIdList.add(String.join(",", itemIds.subList(i, Math.min(i + 50, itemIds.size()))));
            }

            CountDownLatch productCountDownLatch = new CountDownLatch(itemIdList.size());
            // 开线程池，线程数量为要遍历的对象的长度
            ExecutorService productExecutor = Executors.newFixedThreadPool(itemIdList.size());

            CountDownLatch modelCountDownLatch = new CountDownLatch(itemIdList.size());
            // 开线程池，线程数量为要遍历的对象的长度
            ExecutorService modelExecutor = Executors.newFixedThreadPool(itemIdList.size());
            for (String itemId : itemIdList) {

                long finalShopId = shopId;

                CompletableFuture.runAsync(() -> {
                    try {
                        String finalAccessToken = shopService.getAccessTokenByShopId(String.valueOf(finalShopId));
                        getProductDetail(itemId, finalAccessToken, finalShopId, productList, updateProList);
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        productCountDownLatch.countDown();
                        System.out.println("productCountDownLatch===> " + productCountDownLatch);
                    }
                }, productExecutor);

                CompletableFuture.runAsync(() -> {
                    try {
                        String[] splitIds = itemId.split(",");
                        for (String splitId : splitIds) {
                            String finalAccessToken = shopService.getAccessTokenByShopId(String.valueOf(finalShopId));
                            modelService.getModel(Long.parseLong(splitId), finalAccessToken, finalShopId, modelList, updateModelList);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        modelCountDownLatch.countDown();
                        System.out.println("modelCountDownLatch===> " + modelCountDownLatch);
                    }
                }, modelExecutor);
            }

            try {
                productCountDownLatch.await();
                modelCountDownLatch.await();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

        }

        this.saveBatch(productList);
//        System.out.println("updateProList===>" + JSONArray.toJSONString(updateProList));
        this.updateBatchById(updateProList);
        modelService.saveBatch(modelList);
//        System.out.println("updateModelList===>" + JSONArray.toJSONString(updateModelList));
        modelService.updateBatchById(updateModelList);

        log.info("更新产品耗时： {}秒", (System.currentTimeMillis() - startTime) / 1000);
    }

    private void getProductDetail(String itemIds, String token, long shopId, List<Product> productList, List<Product> updateProList) {
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


                String judgeResult = CommonUtil.judgeRedis(redisService,PRODUCT_ITEM + itemId, productList, updateProList, product, Product.class);
                if (!"".equals(judgeResult)) {
                    JSONArray diffArray = JSON.parseObject(judgeResult).getJSONArray("defectsList");
                    if (diffArray.size() != 0) {
                        StringJoiner joiner = new StringJoiner(",");
                        OperationLog operationLog = new OperationLog();
                        operationLog.setOptType(SYSTEM_LOG);
                        operationLog.setStatus(1);
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
        List<ProductVO> productList = productDao.productList(PageUtils.getLimitCurrent(), PageUtils.getSize(), condition);
        return new PageResult<>(productList, count);
    }

    @Override
    public ProductInfoVO getProductInfo(Long itemId) {

        Product product = productDao.selectOne(new QueryWrapper<Product>().eq("item_id", itemId));

        ProductInfoVO productInfoVO = BeanCopyUtils.copyObject(product, ProductInfoVO.class);

        productInfoVO.setModelVOList(modelService.getModelVOListByItemId(itemId));

        return productInfoVO;
    }

    // todo 等级
}
