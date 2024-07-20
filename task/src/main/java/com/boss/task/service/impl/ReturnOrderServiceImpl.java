package com.boss.task.service.impl;

import cn.hutool.core.util.IdUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.boss.common.enities.Product;
import com.boss.common.enities.ReturnOrder;
import com.boss.common.enities.ReturnOrderItem;
import com.boss.common.enities.Shop;
import com.boss.common.util.CommonUtil;
import com.boss.task.dao.OperationLogDao;
import com.boss.task.dao.ProductDao;
import com.boss.task.dao.ReturnOrderDao;
import com.boss.task.dao.ShopDao;
import com.boss.task.service.ReturnOrderService;
import com.boss.task.util.RedisUtil;
import com.boss.task.util.ShopeeUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import static com.boss.common.constant.RedisPrefixConst.RETURN_ORDER;
import static com.boss.common.constant.RedisPrefixConst.RETURN_ORDER_ITEM_MODEL;

/**
 * @Description
 * @Author 罗宇航
 * @Date 2024/4/11
 */

@Service
@Slf4j
public class ReturnOrderServiceImpl extends ServiceImpl<ReturnOrderDao, ReturnOrder> implements ReturnOrderService {

    @Autowired
    private ShopDao shopDao;

    @Autowired
    private ProductDao productDao;

    @Autowired
    private ReturnOrderItemServiceImpl returnOrderItemService;

    @Autowired
    private ShopServiceImpl shopService;

    @Autowired
    private RedisServiceImpl redisService;


    @Autowired
    @Qualifier("customThreadPool")
    private ThreadPoolExecutor customThreadPool;

    private final TransactionTemplate transactionTemplate;

    @Autowired
    public ReturnOrderServiceImpl(DataSourceTransactionManager transactionManager) {
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void saveOrUpdateReturnOrder() {

        // 遍历所有未冻结店铺获取 token 和 shopId
        QueryWrapper<Shop> shopQueryWrapper = new QueryWrapper<>();
        shopQueryWrapper.select("shop_id").eq("status", "1");
        List<Shop> shopList = shopDao.selectList(shopQueryWrapper);

        // 根据每个店铺的 token 和 shopId 获取产品
        List<ReturnOrder> returnOrdertList = new CopyOnWriteArrayList<>();
//        List<ReturnOrder> updateReturnOrderList = new CopyOnWriteArrayList<>();
        List<ReturnOrderItem> returnOrderItemList =  new CopyOnWriteArrayList<>();
//        List<ReturnOrderItem> updateReturnOrderItemList = new CopyOnWriteArrayList<>();
        long shopId;
        String accessToken;
        JSONObject result;
        for (Shop shop : shopList) {
            shopId = shop.getShopId();
            accessToken = shopService.getAccessTokenByShopId(String.valueOf(shopId));

            result = ShopeeUtil.getReturnListByHttp(accessToken, shopId);

            if (result == null || result.getString("error").contains("error")) {
                continue;
            }

            JSONArray returnArray = result.getJSONObject("response").getJSONArray("return");

            if (returnArray.size() == 0) {
                continue;
            }

            CountDownLatch orderCountDownLatch = new CountDownLatch(returnArray.size());
            // 开线程池，线程数量为要遍历的对象的长度
            ExecutorService orderExecutor = Executors.newFixedThreadPool(returnArray.size());

            CountDownLatch itemCountDownLatch = new CountDownLatch(returnArray.size());
            // 开线程池，线程数量为要遍历的对象的长度
            ExecutorService itemExecutor = Executors.newFixedThreadPool(returnArray.size());
            for (int i = 0; i < returnArray.size(); i++) {
                JSONObject tempObject = returnArray.getJSONObject(i);

                CompletableFuture.runAsync(() -> {
                    try {
                        getReturnOrderDetail(tempObject, returnOrdertList);
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        orderCountDownLatch.countDown();
                        System.out.println("returnOrderCountDownLatch===> " + orderCountDownLatch);
                    }
                }, orderExecutor);

                CompletableFuture.runAsync(() -> {
                    try {
                        getReturnOrderItem(tempObject, returnOrderItemList);
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        itemCountDownLatch.countDown();
                        System.out.println("returnOrderItemCountDownLatch===> " + itemCountDownLatch);
                    }
                }, itemExecutor);
            }

        }

        List<List<ReturnOrder>> splitReturnOrder = CommonUtil.splitListBatches(returnOrdertList, 100);
        for (List<ReturnOrder> batch : splitReturnOrder) {
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                try {
                    transactionTemplate.executeWithoutResult(status -> {
                        this.saveOrUpdateBatch(batch);
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }, customThreadPool);
        }

        List<List<ReturnOrderItem>> splitReturnOrderItem = CommonUtil.splitListBatches(returnOrderItemList, 100);
        for (List<ReturnOrderItem> batch : splitReturnOrderItem) {
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                try {
                    transactionTemplate.executeWithoutResult(status -> {
                        returnOrderItemService.saveOrUpdateBatch(batch);
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }, customThreadPool);
        }
    }

    private void getReturnOrderDetail(JSONObject tempObject, List<ReturnOrder> returnOrdertList) {
        try {
            String returnSn = tempObject.getString("return_sn");
            ReturnOrder returnOrder = ReturnOrder.builder()
                    .id(IdUtil.getSnowflakeNextId())
                    .returnSn(returnSn)
                    .orderSn(tempObject.getString("order_sn"))
                    .reason(tempObject.getString("reason"))
                    .textReason(tempObject.getString("text_reason"))
                    .refundAmount(tempObject.getBigDecimal("refund_amount"))
                    .status(tempObject.getString("status"))
                    .createTime(tempObject.getLong("create_time"))
                    .updateTime(tempObject.getLong("update_time"))
                    .amountBeforeDiscount(tempObject.getBigDecimal("amount_before_discount"))
                    .build();

            RedisUtil.judgeRedis(redisService,RETURN_ORDER + returnSn, returnOrdertList, returnOrder, ReturnOrder.class);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void getReturnOrderItem(JSONObject tempObject, List<ReturnOrderItem> returnOrderItemList) {
        try {
            JSONArray itemArray = tempObject.getJSONArray("item");
            if (itemArray.size() == 0) {
                return;
            }
            String returnSn = tempObject.getString("return_sn");
            JSONObject itemObject;
            for (int i = 0; i < itemArray.size(); i++) {
                itemObject = itemArray.getJSONObject(i);
                long itemId = itemObject.getLong("item_id");
                long modelId = itemObject.getLong("model_id");
                ReturnOrderItem returnOrderItem = ReturnOrderItem.builder()
                        .id(IdUtil.getSnowflakeNextId())
                        .returnSn(returnSn)
                        .name(itemObject.getString("name"))
                        .itemId(itemId)
                        .itemSku(itemObject.getString("item_sku"))
                        .itemPrice(itemObject.getBigDecimal("item_price"))
                        .variationSku(itemObject.getString("variation_sku"))
                        .modelId(modelId)
                        .build();

                RedisUtil.judgeRedis(redisService,RETURN_ORDER_ITEM_MODEL + returnSn + "_" + itemId + "_" + modelId, returnOrderItemList, returnOrderItem, ReturnOrderItem.class);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
