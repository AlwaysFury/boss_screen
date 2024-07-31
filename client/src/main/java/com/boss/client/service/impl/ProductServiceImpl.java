package com.boss.client.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.boss.client.dao.*;
import com.boss.client.dto.ConditionDTO;
import com.boss.client.exception.BizException;
import com.boss.client.service.ProductOverviewService;
import com.boss.client.service.ProductService;
import com.boss.client.util.RedisUtil;
import com.boss.client.util.ShopeeUtil;
import com.boss.client.vo.PageResult;
import com.boss.client.vo.ProductExtraInfoVO;
import com.boss.client.vo.ProductInfoVO;
import com.boss.client.vo.ProductVO;
import com.boss.common.enities.*;
import com.boss.common.enums.ProductStatusEnum;
import com.boss.common.util.CommonUtil;
import com.boss.common.util.PageUtils;
import com.boss.common.vo.SelectVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

import static com.boss.common.constant.OptTypeConst.SYSTEM_LOG;
import static com.boss.common.constant.RedisPrefixConst.CATEGORY;
import static com.boss.common.constant.RedisPrefixConst.PRODUCT_ITEM;
import static com.boss.common.enums.TagTypeEnum.ITEM;

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
    private CategoryDao categoryDao;

    @Autowired
    private TagDao tagDao;

    @Autowired
    private ProductExtraInfoDao productExtraInfoDao;

    @Autowired
    private OrderItemServiceImpl orderItemService;

    @Autowired
    private ProductOverviewService productOverviewService;

    @Autowired
    private ProductExtraInfoServiceImpl productExtraInfoService;

    @Autowired
    private ModelServiceImpl modelService;

    @Autowired
    private GradeServiceImpl gradeService;

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
        for (List<Product> batch : splitProduct) {
            CompletableFuture.runAsync(() -> {
                try {
                    transactionTemplate.executeWithoutResult(status -> {
                        this.saveOrUpdateBatch(batch);
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }, customThreadPool);
        }


        List<List<Model>> splitModel = CommonUtil.splitListBatches(modelList, 5000);
        List<CompletableFuture<Void>> insertModelFutures = new ArrayList<>();
        for (List<Model> batch : splitModel) {
            CompletableFuture.runAsync(() -> {
                try {
                    transactionTemplate.executeWithoutResult(status -> {
                        modelService.saveOrUpdateBatch(batch);
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }, customThreadPool);

        }

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


                String judgeResult = RedisUtil.judgeRedis(redisService,PRODUCT_ITEM + itemId, productList, product, Product.class);
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

                    List<Tag> tagList = tagDao.getTagListByItemOrImgId(productVO.getId());
                    if (tagList != null) {
                        List<String> tagNameList = tagList.stream().map(Tag::getTagName).toList();
                        productVO.setTagNameList(tagNameList);
                    }

//                    productVO.setCreateTime(CommonUtil.timestamp2String((Long) productVO.getCreateTime()));
                    productVO.setStatus(ProductStatusEnum.getDescByCode(productVO.getStatus()));

                    return productVO;
                }).collect(Collectors.toList());

        return new PageResult<>(productList, count);
    }


    @Override
    public ProductInfoVO getProductInfo(Long itemId) {

        ProductInfoVO productInfoVO = productDao.getProductInfo(itemId);

        if (Objects.isNull(productInfoVO)) {
            throw new BizException("该产品不存在");
        }

        List<Tag> tagList = tagDao.getTagListByItemOrImgId(productInfoVO.getId());
        if (tagList != null) {
            List<String> tagNameList = tagList.stream().map(Tag::getTagName).toList();
            productInfoVO.setTagNameList(tagNameList);
        }

//        Object redisCategoryObj = redisService.get(CATEGORY + productInfoVO.getCategoryId());
//        if (redisCategoryObj != null) {
//            productInfoVO.setCategoryName(JSONObject.parseObject(redisCategoryObj.toString()).getString("display_category_name"));
//        }

        productInfoVO.setStatus(ProductStatusEnum.getDescByCode(productInfoVO.getStatus()));

        ProductExtraInfoVO productExtraInfoVO = productExtraInfoService.getProductExtraInfoByItemId(itemId);

        productInfoVO.setExtraInfo(productExtraInfoVO);

        Date nowDate = new Date();

        productExtraInfoVO.setSalesVolume3daysCount(orderItemService.countByCreateTimeRange(nowDate, -3, productInfoVO.getItemId(), "", ITEM.getCode()));
        productExtraInfoVO.setSalesVolume7daysCount(orderItemService.countByCreateTimeRange(nowDate, -7, productInfoVO.getItemId(), "", ITEM.getCode()));
        productExtraInfoVO.setSalesVolume15daysCount(orderItemService.countByCreateTimeRange(nowDate, -15, productInfoVO.getItemId(), "", ITEM.getCode()));
        productExtraInfoVO.setSalesVolume30daysCount(orderItemService.countByCreateTimeRange(nowDate, -30, productInfoVO.getItemId(), "", ITEM.getCode()));

//        productOverviewService.getProductOverviewInfoById(itemId);

        return productInfoVO;
    }

    @Override
    public List<SelectVO> getCategorySelect() {
        List<SelectVO> list = categoryDao.selectList(new QueryWrapper<Category>()).stream()
                .map(category -> SelectVO.builder().key(category.getId()).value(category.getName()).build()).collect(Collectors.toList());

        return list;
    }

    @Override
    public List<SelectVO> getStatusSelect() {
        return ProductStatusEnum.getProductStatusEnum();
    }

    @Override
    public List<String> getNewerSaleProductNames() {
        List<String> ids = productDao.getNewerSaleProductNames();
        if (ids == null || ids.size() == 0) {
            ids = new ArrayList<>();
        }
        return ids;
    }
}
