package com.boss.client.service.impl;

import cn.hutool.core.util.IdUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.boss.client.dao.*;
import com.boss.client.service.OrderService;
import com.boss.client.util.RedisUtil;
import com.boss.client.util.ShopeeUtil;
import com.boss.client.vo.OrderEscrowInfoVO;
import com.boss.client.vo.OrderEscrowItemVO;
import com.boss.client.vo.OrderEscrowVO;
import com.boss.client.vo.PageResult;
import com.boss.common.dto.ConditionDTO;
import com.boss.common.enities.*;
import com.boss.common.enums.OrderStatusEnum;
import com.boss.common.util.BeanCopyUtils;
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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.boss.common.constant.RedisPrefixConst.ORDER;
import static com.boss.common.constant.RedisPrefixConst.ORDER_ITEM_MODEL;

/**
 * @Description
 * @Author 罗宇航
 * @Date 2024/4/11
 */

@Service
@Slf4j
public class OrderServiceImpl extends ServiceImpl<OrderDao, Order> implements OrderService {

    @Autowired
    private ShopDao shopDao;

    @Autowired
    private OrderDao orderDao;

    @Autowired
    private OrderItemDao orderItemDao;

    @Autowired
    private EscrowInfoDao escrowInfoDao;

    @Autowired
    private EscrowItemDao escrowItemDao;

    @Autowired
    private PayoutInfoDao payoutDetailDao;

    @Autowired
    private ShopServiceImpl shopService;

    @Autowired
    private OrderItemServiceImpl orderItemService;

    @Autowired
    private CostServiceImpl costService;

    @Autowired
    private OrderStatusPushServiceImpl orderStatusPushService;

    @Autowired
    private OrderStatusPushDao orderStatusPushDao;

    @Autowired
    private CostDao costDao;

    @Autowired
    private RedisServiceImpl redisService;

    @Autowired
    @Qualifier("customThreadPool")
    private ThreadPoolExecutor customThreadPool;

    private final TransactionTemplate transactionTemplate;

    @Autowired
    public OrderServiceImpl(DataSourceTransactionManager transactionManager) {
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void refreshOrderByTime(long startTime, long endTime) {
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
                List<String> object = ShopeeUtil.getOrderList(accessToken, shopId, 0, new ArrayList<>(), pair[0], pair[1]);
                log.info(pair[0] + "---" + pair[1] + ":  " + object.size());
                orderSnList.addAll(object);
            }

            if (orderSnList == null || orderSnList.isEmpty()) {
                continue;
            }

            List<String> newOrderSnList = new ArrayList<>();
            for (int i = 0; i < orderSnList.size(); i += 50) {
                newOrderSnList.add(String.join(",", orderSnList.subList(i, Math.min(i + 50, orderSnList.size()))));
            }
            refreshBatchOrderBySn(newOrderSnList, shopId);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void refreshNewOrder() {
        List<Order> orders = orderDao.maxTimeList();
        String accessToken;
        for (Order order : orders) {
            long startTime = order.getCreateTime();
            long shopId = order.getShopId();
            long endTime = System.currentTimeMillis();
            accessToken = shopService.getAccessTokenByShopId(String.valueOf(shopId));

            List<String> orderSnList = new ArrayList<>();

            List<Long[]> result = CommonUtil.splitIntoEveryNDaysTimestamp(startTime, endTime, 14);
            for (Long[] pair : result) {
                List<String> object = ShopeeUtil.getOrderList(accessToken, shopId, 0, new ArrayList<>(), pair[0], pair[1]);
                log.info(pair[0] + "---" + pair[1] + ":  " + object.size());
                orderSnList.addAll(object);
            }

            if (orderSnList == null || orderSnList.isEmpty()) {
                continue;
            }

            List<String> newOrderSnList = new ArrayList<>();
            for (int i = 0; i < orderSnList.size(); i += 50) {
                newOrderSnList.add(String.join(",", orderSnList.subList(i, Math.min(i + 50, orderSnList.size()))));
            }

            refreshBatchOrderBySn(newOrderSnList, shopId);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void initOrder(long shopId) {
        // 获取Calendar实例并设置为当前时间
        Calendar calendar = Calendar.getInstance();

        // 方法1: 设置到今天的0时0分0秒
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        long startOfDayTimestamp = calendar.getTimeInMillis() / 1000; // 获取今天开始的时间戳

        // 方法2: 设置到今天的23时59分59秒
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        long endOfDayTimestamp = calendar.getTimeInMillis() / 1000; // 获取今天结束的时间戳

        String accessToken = shopService.getAccessTokenByShopId(String.valueOf(shopId));

        List<String> orderSnList = ShopeeUtil.getOrderList(accessToken, shopId, 0, new ArrayList<>(), startOfDayTimestamp, endOfDayTimestamp);

        if (orderSnList == null || orderSnList.isEmpty()) {
            return;
        }

        List<String> newOrderSnList = new ArrayList<>();
        for (int i = 0; i < orderSnList.size(); i += 50) {
            newOrderSnList.add(String.join(",", orderSnList.subList(i, Math.min(i + 50, orderSnList.size()))));
        }

        refreshBatchOrderBySn(newOrderSnList, shopId);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void refreshOrder(List<String> sns) {
        StringJoiner sj = new StringJoiner(",");
        for (int i = 0; i < sns.size(); i ++) {
            sj.add(String.valueOf(sns.get(i)));
        }
        List<Order> orders = orderDao.selectList(new QueryWrapper<Order>().select("order_sn", "shop_id").in("order_sn", sj.toString()));

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

            List<String> snList = new ArrayList<>();
            for (int i = 0; i < oldSnList.size(); i += 50) {
                snList.add(String.join(",", oldSnList.subList(i, Math.min(i + 50, oldSnList.size()))));
            }

            refreshOrderDetail(snList, shopId);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void refreshOrderByStatus(String... status) {
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

            List<String> snList = new ArrayList<>();
            for (int i = 0; i < oldSnList.size(); i += 50) {
                snList.add(String.join(",", oldSnList.subList(i, Math.min(i + 50, oldSnList.size()))));
            }

            refreshOrderDetail(snList, shopId);
        }
    }

    public void refreshOrderDetail(List<String> orderSnList, long shopId) {
        List<Order> ordertList = new CopyOnWriteArrayList<>();

        log.info("===订单发送请求及处理开始");
        long startTime =  System.currentTimeMillis();

        List<CompletableFuture<Void>> orderFutures = orderSnList.stream()
                .map(orderSn -> {
                    long finalShopId = shopId;
                    return CompletableFuture.runAsync(() -> {
                        String finalAccessToken = shopService.getAccessTokenByShopId(String.valueOf(finalShopId));
                        JSONObject orderObject = ShopeeUtil.getOrderDetail(finalAccessToken, finalShopId, orderSn);
                        if (orderObject.getString("error").contains("error") && orderObject == null && orderObject.getJSONObject("response") == null) {
                            return;
                        }
                        JSONArray orderArray = orderObject.getJSONObject("response").getJSONArray("order_list");
                        JSONObject orderDetailObject;
                        for (int j = 0; j < orderArray.size(); j++) {
                            orderDetailObject = orderArray.getJSONObject(j);
                            getOrderDetail(orderDetailObject, ordertList, finalShopId);
                        }
                    }, customThreadPool);
                }).collect(Collectors.toList());

        CompletableFuture.allOf(orderFutures.toArray(new CompletableFuture[0])).join();

        log.info("===订单发送请求并处理结束，耗时：{}秒", (System.currentTimeMillis() - startTime) / 1000);

        log.info("===开始订单数据落库");
        startTime =  System.currentTimeMillis();

        List<List<Order>> batchesOrderList = CommonUtil.splitListBatches(ordertList, 100);
        List<CompletableFuture<Void>> insertOrderFutures = new ArrayList<>();
        for (List<Order> batch : batchesOrderList) {
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                try {
                    transactionTemplate.executeWithoutResult(status -> {
                        this.saveOrUpdateBatch(batch);
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }, customThreadPool);

            insertOrderFutures.add(future);
        }
        CompletableFuture.allOf(insertOrderFutures.toArray(new CompletableFuture[0])).join();

        log.info("===订单数据落库结束，耗时：{}秒", (System.currentTimeMillis() - startTime) / 1000);

    }

    @Transactional(rollbackFor = Exception.class)
    public void refreshSingleOrderBySn(String orderSn, long shopId) {
        List<Order> ordertList = new CopyOnWriteArrayList<>();
        List<OrderItem> orderItemList =  new CopyOnWriteArrayList<>();

        log.info("===开始处理推送订单：{}", orderSn);
        long startTime =  System.currentTimeMillis();

        String token = shopService.getAccessTokenByShopId(String.valueOf(shopId));
        JSONObject orderObject = ShopeeUtil.getOrderDetail(token, shopId, orderSn);
        if (orderObject.getString("error").contains("error") && orderObject == null && orderObject.getJSONObject("response") == null) {
            return;
        }
        JSONArray orderArray = orderObject.getJSONObject("response").getJSONArray("order_list");
        JSONObject orderDetailObject;
        for (int j = 0; j < orderArray.size(); j++) {
            orderDetailObject = orderArray.getJSONObject(j);
            getOrderDetail(orderDetailObject, ordertList, shopId);
            getOrderItem(orderDetailObject, orderItemList);
        }

        this.saveOrUpdateBatch(ordertList);
        orderItemService.saveOrUpdateBatch(orderItemList);
    }

    public void refreshBatchOrderBySn(List<String> orderSnList, long shopId) {
        if (orderSnList.isEmpty()) {
            return;
        }

        List<Order> ordertList = new CopyOnWriteArrayList<>();
        List<OrderItem> orderItemList =  new CopyOnWriteArrayList<>();

        log.info("===订单发送请求及处理开始");
        long startTime =  System.currentTimeMillis();

        List<CompletableFuture<Void>> orderFutures = orderSnList.stream()
                .map(orderSn -> {
                    long finalShopId = shopId;
                    return CompletableFuture.runAsync(() -> {
                        String finalAccessToken = shopService.getAccessTokenByShopId(String.valueOf(finalShopId));
                        JSONObject orderObject = ShopeeUtil.getOrderDetail(finalAccessToken, finalShopId, orderSn);
                        if (orderObject.getString("error").contains("error") && orderObject == null && orderObject.getJSONObject("response") == null) {
                            return;
                        }
                        JSONArray orderArray = orderObject.getJSONObject("response").getJSONArray("order_list");
                        JSONObject orderDetailObject;
                        for (int j = 0; j < orderArray.size(); j++) {
                            orderDetailObject = orderArray.getJSONObject(j);
                            getOrderDetail(orderDetailObject, ordertList, finalShopId);
                            getOrderItem(orderDetailObject, orderItemList);
                        }
                    }, customThreadPool);
                }).collect(Collectors.toList());

        CompletableFuture.allOf(orderFutures.toArray(new CompletableFuture[0])).join();

        log.info("===订单发送请求并处理结束，耗时：{}秒", (System.currentTimeMillis() - startTime) / 1000);

        log.info("===开始订单数据落库");
        startTime =  System.currentTimeMillis();

        List<List<Order>> batchesOrderList = CommonUtil.splitListBatches(ordertList, 100);
        List<CompletableFuture<Void>> insertOrderFutures = new ArrayList<>();
        for (List<Order> batch : batchesOrderList) {
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                try {
                    transactionTemplate.executeWithoutResult(status -> {
                        this.saveOrUpdateBatch(batch);
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }, customThreadPool);

            insertOrderFutures.add(future);
        }
        CompletableFuture.allOf(insertOrderFutures.toArray(new CompletableFuture[0])).join();

        List<List<OrderItem>> batchesOrderItemList = CommonUtil.splitListBatches(orderItemList, 1000);
        List<CompletableFuture<Void>> insertOrderItemFutures = new ArrayList<>();
        for (List<OrderItem> batch : batchesOrderItemList) {
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                try {
                    transactionTemplate.executeWithoutResult(status -> {
                        orderItemService.saveOrUpdateBatch(batch);
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }, customThreadPool);
            insertOrderItemFutures.add(future);
        }
        CompletableFuture.allOf(insertOrderItemFutures.toArray(new CompletableFuture[0])).join();


        log.info("===订单数据落库结束，耗时：{}秒", (System.currentTimeMillis() - startTime) / 1000);

    }

    private void getOrderDetail(JSONObject orderObject, List<Order> ordertList, Long shopId) {
        String orderSn = orderObject.getString("order_sn");

        Order order = Order.builder()
                .id(IdUtil.getSnowflakeNextId())
                .shopId(shopId)
                .createTime(orderObject.getLong("create_time"))
                .updateTime(orderObject.getLong("update_time"))
                .orderSn(orderSn)
                .status(orderObject.getString("order_status"))
                .payTime(orderObject.getLong("pay_time"))
                .buyerUerId(orderObject.getLong("buyer_user_id"))
                .buyerUserName(orderObject.getString("buyer_username"))
                .cancelReason(orderObject.getString("cancel_reason"))
                .cancelBy(orderObject.getString("cancel_by"))
                .buyerCancelReason(orderObject.getString("buyer_cancel_reason"))
                .build();

        JSONArray packageArray = orderObject.getJSONArray("package_list");
        if (packageArray != null && !packageArray.isEmpty()) {
            order.setShippingCarrier(packageArray.getJSONObject(0).getString("shipping_carrier"));
        }
//        if (!"UNPAID".equals(order.getStatus()) && !"READY_TO_SHIP".equals(order.getStatus()) && !"PROCESSED".equals(order.getStatus()) && !"CANCELLED".equals(order.getStatus())) {
//            String token = shopService.getAccessTokenByShopId(String.valueOf(shopId));
//            JSONObject responseObject = ShopeeUtil.getTrackingNumber(token, shopId, orderSn).getJSONObject("response");
//            if (responseObject != null) {
//                order.setTrackingNumber(responseObject.getString("tracking_number"));
//            } else {
//                Object redisResult = redisService.get(ORDER + orderSn);
//                if (!Objects.isNull(redisResult)) {
//                    order.setTrackingNumber(JSONObject.parseObject(redisResult.toString()).getString("trackingNumber"));
//                }
//            }
//        }

        RedisUtil.judgeRedis(redisService, ORDER + orderSn, ordertList, order, Order.class);

    }

    private void getOrderItem(JSONObject orderObject, List<OrderItem> orderItemList) {
        JSONArray itemList = orderObject.getJSONArray("item_list");
        JSONObject itemObject;
        for (int i = 0; i < itemList.size(); i++) {
            itemObject = itemList.getJSONObject(i);

            String orderSn = orderObject.getString("order_sn");
            long itemId = itemObject.getLong("item_id");
            long modelId = itemObject.getLong("model_id");
            OrderItem orderItem = OrderItem.builder()
                    .id(IdUtil.getSnowflakeNextId())
                    .orderSn(orderSn)
                    .itemId(itemId)
                    .itemName(itemObject.getString("item_name"))
                    .itemSku(itemObject.getString("item_sku"))
                    .modelId(modelId)
                    .modelName(itemObject.getString("model_name"))
                    .modelSku(itemObject.getString("model_sku"))
                    .count(itemObject.getInteger("model_quantity_purchased"))
                    .promotionId(itemObject.getLong("promotion_id"))
                    .promotionType(itemObject.getString("promotion_type"))
                    .build();

            JSONObject imageInfoArray = itemObject.getJSONObject("image_info");
            if (imageInfoArray != null && imageInfoArray.size() > 0) {
                orderItem.setImageUrl(imageInfoArray.getString("image_url"));
            }

            RedisUtil.judgeRedis(redisService, ORDER_ITEM_MODEL + orderSn + "_" + itemId + "_" + modelId + "_" + i, orderItemList, orderItem, OrderItem.class);
        }

    }

    public PageResult<OrderEscrowVO> orderListByCondition(ConditionDTO condition) {
        // 查询分类数量
        Integer count = orderDao.orderCount(condition);
        if (count == 0) {
            return new PageResult<>();
        }
        // 分页查询分类列表
        List<OrderEscrowVO> orderEscrowVOList = orderDao.orderList(PageUtils.getLimitCurrent(), PageUtils.getSize(), condition).stream().map(orderEscrowVO -> {
            orderEscrowVO.setCreateTime(CommonUtil.timestamp2String((Long) orderEscrowVO.getCreateTime()));
            orderEscrowVO.setStatus(OrderStatusEnum.getDescByCode(orderEscrowVO.getStatus()));
            orderEscrowVO.setShopName(shopDao.selectOne(new QueryWrapper<Shop>().eq("shop_id", orderEscrowVO.getShopId())).getName());

            return orderEscrowVO;
        }).collect(Collectors.toList());
        return new PageResult<>(orderEscrowVOList, count);
    }

    public OrderEscrowInfoVO getOrderInfo(String orderSn) {

        Order order = orderDao.selectOne(new QueryWrapper<Order>().eq("order_sn", orderSn));

        OrderEscrowInfoVO orderEscrowInfoVO = BeanCopyUtils.copyObject(order, OrderEscrowInfoVO.class);
        orderEscrowInfoVO.setCreateTime(CommonUtil.timestamp2String(order.getCreateTime()));
        if (order.getPayTime() != null) {
            orderEscrowInfoVO.setPayTime(CommonUtil.timestamp2String(order.getPayTime()));
        }

        orderEscrowInfoVO.setStatus(OrderStatusEnum.getDescByCode(order.getStatus()));
        String status = orderStatusPushDao.maxTimeStatus(orderSn);
        if (status != null) {
            orderEscrowInfoVO.setStatus(OrderStatusEnum.getDescByCode(status));
        }

        orderEscrowInfoVO.setStatus(OrderStatusEnum.getDescByCode(order.getStatus()));

        orderEscrowInfoVO.setShopName(shopDao.selectOne(new QueryWrapper<Shop>().eq("shop_id", order.getShopId())).getName());

        EscrowInfo escrowInfo = escrowInfoDao.selectOne(new QueryWrapper<EscrowInfo>().eq("order_sn", orderSn));
        if (escrowInfo != null) {
            orderEscrowInfoVO.setBuyerTotalAmount(escrowInfo.getBuyerTotalAmount());
            orderEscrowInfoVO.setBuyerPaidShippingFee(escrowInfo.getBuyerPaidShippingFee());
            orderEscrowInfoVO.setActualShippingFee(escrowInfo.getActualShippingFee());
            orderEscrowInfoVO.setEscrowAmount(escrowInfo.getEscrowAmount());
        }
//        PayoutInfo payoutDetail = payoutDetailDao.selectOne(new QueryWrapper<PayoutInfo>().eq("order_sn", orderSn));
//        if (payoutDetail != null) {
//            orderEscrowInfoVO.setAdjustmentAmount(payoutDetail.getAdjustmentAmount());
//            orderEscrowInfoVO.setAdjustmentReason(payoutDetail.getScenario());
//            orderEscrowInfoVO.setAdjustmentRemark(payoutDetail.getRemark());
//        }

        // 初始化衣服数量 map
        Map<String, Integer> clothesCountMap = new HashMap<>();
        // 双面
        clothesCountMap.put("double", 0);
        for (SelectVO vo : costService.getCostType()) {
            clothesCountMap.put(vo.getValue(), 0);
        }


        List<OrderEscrowItemVO> orderEscrowItemVOList = orderItemDao.selectList(new QueryWrapper<OrderItem>().eq("order_sn", orderSn)).stream().map(
                orderItem -> {
                    OrderEscrowItemVO orderEscrowItemVO = BeanCopyUtils.copyObject(orderItem, OrderEscrowItemVO.class);

                    // 计算衣服数量
                    String clothesType = getClothesType(orderItem.getModelSku().toLowerCase());
                    if (clothesCountMap.get(clothesType) != null) {
                        clothesCountMap.put(clothesType, clothesCountMap.get(clothesType) + 1);
                    }

                    // 计算双面
                    String itemSku = orderItem.getItemSku();
                    if (!"notsure".equals(itemSku) && isEnglish(itemSku.substring(0, 1)) && isEnglish(itemSku.substring(1, 2))) {
                        clothesCountMap.put("double", clothesCountMap.get("double") + 1);
                    }

                    EscrowItem escrowItem = escrowItemDao.selectOne(new QueryWrapper<EscrowItem>()
                            .eq("order_sn", orderSn)
                            .eq("item_id", orderEscrowItemVO.getItemId())
                            .eq("model_id", orderEscrowItemVO.getModelId()));
                    if (escrowItem != null) {
                        orderEscrowItemVO.setOriginalPrice(escrowItem.getOriginalPrice());
                        orderEscrowItemVO.setSellingPrice(escrowItem.getSellingPrice());
                        orderEscrowItemVO.setDiscountedPrice(escrowItem.getDiscountedPrice());
                        orderEscrowItemVO.setSellerDiscount(escrowItem.getSellerDiscount());
                        orderEscrowItemVO.setActivityId(escrowItem.getActivityId());
                        orderEscrowItemVO.setActivityType(escrowItem.getActivityType());
                        orderEscrowItemVO.setCount(escrowItem.getCount());

                        // 计算成本利润
                        JSONObject costObject = getCostObject(clothesType, orderItem.getCreateTime());
                        // 成本
                        BigDecimal costPrice = costObject.getBigDecimal("cost");
                        // 利率
                        double rate = costObject.getDouble("rate");

                        BigDecimal cost = costPrice.multiply(BigDecimal.valueOf(orderEscrowItemVO.getCount())).divide(BigDecimal.valueOf(rate), 2, RoundingMode.HALF_UP);
                        BigDecimal oldPrice = orderEscrowItemVO.getSellerDiscount();
                        if (oldPrice.compareTo(BigDecimal.valueOf(0.0)) == 0) {
                            oldPrice = orderEscrowItemVO.getSellingPrice();
                        }
                        BigDecimal price = oldPrice.multiply(BigDecimal.valueOf(orderEscrowItemVO.getCount())).divide(BigDecimal.valueOf(rate), 2, RoundingMode.HALF_UP);
                        BigDecimal profit = price.subtract(cost);
                        float profitRate = 1;
                        if (price.compareTo(BigDecimal.valueOf(0.0)) != 0) {
                            profitRate = profit.divide(price, 2, RoundingMode.HALF_UP).floatValue();
                        }

                        orderEscrowItemVO.setCost(cost);
                        orderEscrowItemVO.setProfit(profit);
                        orderEscrowItemVO.setProfitRate(profitRate);
                    }

                    return orderEscrowItemVO;
                }
        ).collect(Collectors.toList());

        orderEscrowInfoVO.setOrderEscrowItemVOList(orderEscrowItemVOList);

        JSONArray clothesCounts = new JSONArray();
        for (String s : clothesCountMap.keySet()) {
            JSONObject object = new JSONObject();
            object.put("key", s);
            object.put("value", clothesCountMap.get(s));
            clothesCounts.add(object);
        }
        orderEscrowInfoVO.setClothesCountMap(clothesCounts);

        return orderEscrowInfoVO;
    }

    private static final Pattern ENGLISH_PATTERN = Pattern.compile("^[a-zA-Z]+$");
    private boolean isEnglish(String str) {
        if (str == null) {
            return false;
        }
        Matcher matcher = ENGLISH_PATTERN.matcher(str);
        return matcher.matches();
    }

    private JSONObject getCostObject(String type, LocalDateTime orderCreateTime) {
        BigDecimal costPrice = new BigDecimal(0.00);
        double rate = 1.00;
        QueryWrapper<Cost> costQueryWrapper = new QueryWrapper<>();
        costQueryWrapper.select("price", "start_time", "end_time", "exchange_rate").eq("type", type);
        List<Cost> costList = costDao.selectList(costQueryWrapper);
        for (Cost cost : costList) {
            if (orderCreateTime.isBefore(cost.getEndTime()) && orderCreateTime.isAfter(cost.getStartTime())) {
                costPrice = cost.getPrice();
                rate = cost.getExchangeRate();
                break;
            }
        }

        JSONObject object = new JSONObject();
        object.put("cost", costPrice);
        object.put("rate", rate);

        return object;
    }

    /**
     * 获取衣服类型
     * @param modelSku
     */
    private String getClothesType(String modelSku) {
        String type = "";
        if (modelSku.contains("t-shirt")) {
            // 如果包含 t-shirt 就直接添加
            type = "t-shirt";
        } else {
            String[] skuSplit = modelSku.substring(0, modelSku.indexOf('(')).split("-");
            if (skuSplit.length == 3) {
                // 如果只有三段，默认为 100%cotton
                type = "100%cotton";
            } else {
                // 其他直接获取第二个 - 的值
                type = skuSplit[1];
            }
        }
        return type;
    }

    @Override
    public List<SelectVO> getStatusSelect() {
        return OrderStatusEnum.getOrderStatusEnum();
    }
}
