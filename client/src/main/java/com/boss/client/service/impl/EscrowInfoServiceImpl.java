package com.boss.client.service.impl;

import cn.hutool.core.util.IdUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.boss.client.dao.EscrowInfoDao;
import com.boss.client.dao.OrderDao;
import com.boss.client.dao.ShopDao;
import com.boss.client.service.EscrowInfoService;
import com.boss.client.util.CommonUtil;
import com.boss.client.util.ShopeeUtil;
import com.boss.common.enities.EscrowInfo;
import com.boss.common.enities.EscrowItem;
import com.boss.common.enities.Order;
import com.boss.common.enities.Shop;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

import static com.boss.common.constant.RedisPrefixConst.ESCROW;
import static com.boss.common.constant.RedisPrefixConst.ESCROW_ITEM_MODEL;

/**
 * @Description
 * @Author 罗宇航
 * @Date 2024/4/11
 */

@Service
@Slf4j
public class EscrowInfoServiceImpl extends ServiceImpl<EscrowInfoDao, EscrowInfo> implements EscrowInfoService {

    @Autowired
    private ShopDao shopDao;

    @Autowired
    private OrderDao orderDao;

    @Autowired
    private ShopServiceImpl shopService;

    @Autowired
    private EscrowItemServiceImpl escrowItemService;

    @Autowired
    private RedisServiceImpl redisService;

    @Autowired
    @Qualifier("customThreadPool")
    private ThreadPoolExecutor customThreadPool;

    private final TransactionTemplate transactionTemplate;

    @Autowired
    public EscrowInfoServiceImpl(DataSourceTransactionManager transactionManager) {
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void refreshEscrowByTime(String startTime, String endTime) {
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

            List<String> orderSnList = new ArrayList<>();

            List<Long[]> result = CommonUtil.splitIntoEveryNDaysTimestamp(startTime, endTime, 14);
            for (Long[] pair : result) {
                List<String> object = ShopeeUtil.getEscrowList(accessToken, shopId, 1, new ArrayList<>(), pair[0], pair[1]);
                log.info(pair[0] + "---" + pair[1] + ":  " + object.size());
                orderSnList.addAll(object);
            }

            if (orderSnList == null || orderSnList.isEmpty()) {
                continue;
            }

            List<List<String>> newOrderSnList = new ArrayList<>();
            for (int i = 0; i < orderSnList.size(); i += 20) {
                newOrderSnList.add(orderSnList.subList(i, Math.min(i + 20, orderSnList.size())));
            }

            refreshEscrowBySn(newOrderSnList, shopId);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void refreshOrderNoOnEscrow() {
        // 遍历所有未冻结店铺获取 token 和 shopId
        QueryWrapper<Shop> shopQueryWrapper = new QueryWrapper<>();
        shopQueryWrapper.select("shop_id").eq("status", "1");
        List<Shop> shopList = shopDao.selectList(shopQueryWrapper);

        // 根据每个店铺的 token 和 shopId 获取订单
        long shopId;
        for (Shop shop : shopList) {
            shopId = shop.getShopId();

            List<String> orderSnList = orderDao.getNonEscrowList(shopId);

            if (orderSnList == null || orderSnList.isEmpty()) {
                continue;
            }

            List<List<String>> newOrderSnList = new ArrayList<>();
            for (int i = 0; i < orderSnList.size(); i += 20) {
                newOrderSnList.add(orderSnList.subList(i, Math.min(i + 20, orderSnList.size())));
            }

            refreshEscrowBySn(newOrderSnList, shopId);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void refreshEscrowByStatus(String... status) {
        List<Order> orders = orderDao.selectList(new QueryWrapper<Order>().select("order_sn", "shop_id").notIn("status", status));

        if (orders.isEmpty()) {
            return;
        }

        Map<Long, List<String>> map = new HashMap<>();

        for (Order order : orders) {
            String orderSn = order.getOrderSn();
            long shop_id = order.getShopId();

            if (!map.containsKey(shop_id)) {
                List<String> temp = new ArrayList<>();
                temp.add(orderSn);
                map.put(shop_id, temp);
            } else {
                List<String> temp = map.get(shop_id);
                temp.add(orderSn);
                map.put(shop_id, temp);
            }
        }

        for (long shopId : map.keySet()) {
            List<String> oldSnList = map.get(shopId);

            List<List<String>> newOrderSnList = new ArrayList<>();
            for (int i = 0; i < oldSnList.size(); i += 20) {
                newOrderSnList.add(oldSnList.subList(i, Math.min(i + 20, oldSnList.size())));
            }

            refreshEscrowBySn(newOrderSnList, shopId);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void refreshEscrowBySn(List<List<String>> orderSnList, long shopId) {
        List<EscrowInfo> escrowInfoList =  new CopyOnWriteArrayList<>();
        List<EscrowItem> escrowItemList = new CopyOnWriteArrayList<>();

        log.info("===支付信息发送请求及处理开始");
        long startTime =  System.currentTimeMillis();

        List<CompletableFuture<Void>> escrowFutures = orderSnList.stream()
                .map(orderSn -> {
                    long finalShopId = shopId;
                    return CompletableFuture.runAsync(() -> {
//                        String[] splitOrderSns = orderSn.split(",");
//                        for (String sn : splitOrderSns) {
                        String finalAccessToken = shopService.getAccessTokenByShopId(String.valueOf(finalShopId));
                        JSONObject escrowResult = ShopeeUtil.getEscrowDetail(finalAccessToken, finalShopId, orderSn);
                        if (escrowResult == null || escrowResult.getString("error").contains("error")) {
                            return;
                        }
                        JSONArray escrowInfoArray = escrowResult.getJSONArray("response");
                        if (escrowInfoArray.isEmpty()) {
                            return;
                        }
                        for (int i = 0; i < escrowInfoArray.size(); i++) {
                            JSONObject escrowDetailObject = escrowInfoArray.getJSONObject(i).getJSONObject("escrow_detail");
                            JSONObject orderIncomeObject = escrowDetailObject.getJSONObject("order_income");
                            String sn = escrowDetailObject.getString("order_sn");
                            saveEscrowInfoByOrderSn(orderIncomeObject, sn, escrowInfoList);
                            saveEscrowItem(orderIncomeObject, sn, escrowItemList);
                        }

//                        }
                    }, customThreadPool);
                }).collect(Collectors.toList());


        CompletableFuture.allOf(escrowFutures.toArray(new CompletableFuture[0])).join();

        log.info("===支付信息发送请求并处理结束，耗时：{}秒", (System.currentTimeMillis() - startTime) / 1000);

        log.info("===开始支付信息数据落库");
        startTime =  System.currentTimeMillis();

        List<List<EscrowInfo>> batchesEscrowInfoList = CommonUtil.splitListBatches(escrowInfoList, 100);
        List<CompletableFuture<Void>> insertEscrowInfoFutures = new ArrayList<>();
        for (List<EscrowInfo> batch : batchesEscrowInfoList) {
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                try {
                    transactionTemplate.executeWithoutResult(status -> {
                        this.saveOrUpdateBatch(batch);
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }, customThreadPool);
            insertEscrowInfoFutures.add(future);
        }
        CompletableFuture.allOf(insertEscrowInfoFutures.toArray(new CompletableFuture[0])).join();

        List<List<EscrowItem>> batchesEscrowItemList = CommonUtil.splitListBatches(escrowItemList, 1000);
        List<CompletableFuture<Void>> insertEscrowItemFutures = new ArrayList<>();
        for (List<EscrowItem> batch : batchesEscrowItemList) {
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                try {
                    transactionTemplate.executeWithoutResult(status -> {
                        escrowItemService.saveOrUpdateBatch(batch);
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }, customThreadPool);
            insertEscrowItemFutures.add(future);
        }
        CompletableFuture.allOf(insertEscrowItemFutures.toArray(new CompletableFuture[0])).join();

        log.info("===支付信息数据落库结束，耗时：{}秒", (System.currentTimeMillis() - startTime) / 1000);

    }

    public void saveEscrowInfoByOrderSn(JSONObject orderIncomeObject, String orderSn, List<EscrowInfo> escrowInfoList) {

        EscrowInfo escrowInfo = EscrowInfo.builder()
                .id(IdUtil.getSnowflakeNextId())
                .orderSn(orderSn)
                .buyerUserName(orderIncomeObject.getString("buyer_user_name"))
                .buyerTotalAmount(orderIncomeObject.getBigDecimal("buyer_total_amount"))
                .buyerPaidShippingFee(orderIncomeObject.getBigDecimal("buyer_paid_shipping_fee"))
                .actualShippingFee(orderIncomeObject.getBigDecimal("actual_shipping_fee"))
                .escrowAmount(orderIncomeObject.getBigDecimal("escrow_amount"))
                .build();

//        if (orderIncomeObject.containsKey("order_adjustment")) {
//            JSONObject adjustmentObject = orderIncomeObject.getJSONArray("order_adjustment").getJSONObject(0);
//            escrowInfo.setAdjustmentAmount(adjustmentObject.getBigDecimal("amount"));
//            escrowInfo.setAdjustmentReason(adjustmentObject.getString("adjustment_reason"));
//        }

        CommonUtil.judgeRedis(redisService, ESCROW + orderSn, escrowInfoList, escrowInfo, EscrowInfo.class);
    }

    public void saveEscrowItem(JSONObject oderIncomeObject, String orderSn, List<EscrowItem> escrowItemList) {
        JSONArray items = oderIncomeObject.getJSONArray("items");

        JSONObject itemObject;
        for (int i = 0; i < items.size(); i++) {
            itemObject = items.getJSONObject(i);
            long itemId = itemObject.getLong("item_id");
            long modelId = itemObject.getLong("model_id");
            EscrowItem escrowItem = EscrowItem.builder()
                    .id(IdUtil.getSnowflakeNextId())
                    .orderSn(orderSn)
                    .itemId(itemId)
                    .itemName(itemObject.getString("item_name"))
                    .itemSku(itemObject.getString("item_sku"))
                    .modelId(modelId)
                    .modelName(itemObject.getString("model_name"))
                    .modelSku(itemObject.getString("model_sku"))
                    .count(itemObject.getInteger("quantity_purchased"))
                    .originalPrice(itemObject.getBigDecimal("original_price"))
                    .sellingPrice(itemObject.getBigDecimal("selling_price"))
                    .discountedPrice(itemObject.getBigDecimal("discounted_price"))
                    .sellerDiscount(itemObject.getBigDecimal("seller_discount"))
                    .activityId(itemObject.getLong("activity_id"))
                    .activityType(itemObject.getString("activity_type"))
                    .build();

            CommonUtil.judgeRedis(redisService, ESCROW_ITEM_MODEL + orderSn + "_" + itemId + "_" + modelId + "_" + i, escrowItemList, escrowItem, EscrowItem.class);

        }
    }

}
