package com.boss.task.service.impl;

import cn.hutool.core.util.IdUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.boss.common.enities.Order;
import com.boss.common.enities.OrderItem;
import com.boss.common.enities.Shop;
import com.boss.common.util.CommonUtil;
import com.boss.task.dao.OrderDao;
import com.boss.task.dao.OrderItemDao;
import com.boss.task.dao.ProductDao;
import com.boss.task.dao.ShopDao;
import com.boss.task.service.OrderService;
import com.boss.task.util.RedisUtil;
import com.boss.task.util.ShopeeUtil;
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

import static com.boss.common.constant.RedisPrefixConst.ORDER;
import static com.boss.common.enums.OrderStatusEnum.UNPAID;


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
    private ProductDao productDao;

    @Autowired
    private ShopServiceImpl shopService;

    @Autowired
    private OrderItemServiceImpl orderItemService;

    @Autowired
    private EscrowInfoServiceImpl escrowInfoService;

    @Autowired
    private TrackingInfoServiceImpl trackingInfoService;

    @Autowired
    private ProductOrImgTagServiceImpl productOrImgTagService;

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

//    @Transactional(rollbackFor = Exception.class)
    @Override
    public void refreshOrderByTime(String startTimeStr, String endTimeStr) {
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
            refreshBatchOrderBySn(newOrderSnList, shopId, 0);
        }
    }

//    @Transactional(rollbackFor = Exception.class)
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
        List<List<String>> escrowSnList = new ArrayList<>();
        for (int i = 0; i < orderSnList.size(); i += 50) {
            newOrderSnList.add(String.join(",", orderSnList.subList(i, Math.min(i + 50, orderSnList.size()))));
            escrowSnList.add(orderSnList.subList(i, Math.min(i + 50, orderSnList.size())));
        }

        refreshBatchOrderBySn(newOrderSnList, shopId, 0);
        escrowInfoService.refreshEscrowBySn(escrowSnList, shopId, 0);
        for (String sn : orderSnList) {
            trackingInfoService.saveTrackingInfoBySn(sn, shopId, "");
        }
    }

    @Override
    public void refreshOrderBySn(List<String> sns) {
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
            List<List<String>> escrowSnList = new ArrayList<>();
            for (int i = 0; i < oldSnList.size(); i += 50) {
                snList.add(String.join(",", oldSnList.subList(i, Math.min(i + 50, oldSnList.size()))));
                escrowSnList.add(oldSnList.subList(i, Math.min(i + 50, oldSnList.size())));
            }

            refreshBatchOrderBySn(snList, shopId, 2);
            escrowInfoService.refreshEscrowBySn(escrowSnList, shopId, 0);
            for (String sn : oldSnList) {
                trackingInfoService.saveTrackingInfoBySn(sn, shopId, "");
            }
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void refreshSingleOrderBySn(String orderSn, long shopId, String status) {
        List<Order> ordertList = new ArrayList<>();
        List<OrderItem> orderItemList =  new ArrayList<>();

        String token = shopService.getAccessTokenByShopId(String.valueOf(shopId));
        JSONObject orderObject = ShopeeUtil.getOrderDetail(token, shopId, orderSn);
        if (orderObject.getString("error").contains("error") || orderObject == null || orderObject.getJSONObject("response") == null) {
            return;
        }
        JSONArray orderArray = orderObject.getJSONObject("response").getJSONArray("order_list");
        JSONObject orderDetailObject;
        for (int j = 0; j < orderArray.size(); j++) {
            orderDetailObject = orderArray.getJSONObject(j);
            getOrderDetail(orderDetailObject, ordertList, shopId);
            // 取消就不用刷新订单货物了
            if (UNPAID.getCode().equals(status)) {
                getOrderItem(orderDetailObject, orderItemList, shopId);
            }
        }

        try {
            this.saveOrUpdateBatch(ordertList);
            orderItemService.saveBatch(orderItemList);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void refreshBatchOrderBySn(List<String> orderSnList, long shopId, int flag) {
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
                        if (orderObject.getString("error").contains("error") || orderObject == null || orderObject.getJSONObject("response") == null) {
                            return;
                        }
                        JSONArray orderArray = orderObject.getJSONObject("response").getJSONArray("order_list");
                        JSONObject orderDetailObject;
                        for (int j = 0; j < orderArray.size(); j++) {
                            orderDetailObject = orderArray.getJSONObject(j);
                            getOrderDetail(orderDetailObject, ordertList, finalShopId);
                            if (flag == 1) {
                                getOrderItem(orderDetailObject, orderItemList, finalShopId);
                            } else if (flag == 0) {
                                Long itemCount = orderItemDao.selectCount(new QueryWrapper<OrderItem>().eq("order_sn", orderObject.getString("order_sn")));
                                if (itemCount == 0) {
                                    getOrderItem(orderDetailObject, orderItemList, finalShopId);
                                }
                            }
                        }
                    }, customThreadPool);
                }).collect(Collectors.toList());

        CompletableFuture.allOf(orderFutures.toArray(new CompletableFuture[0])).join();

        log.info("===订单发送请求并处理结束，耗时：{}秒", (System.currentTimeMillis() - startTime) / 1000);

        log.info("===开始订单数据落库");
        startTime =  System.currentTimeMillis();

        List<List<Order>> batchesOrderList = CommonUtil.splitListBatches(ordertList, 100);
//        List<CompletableFuture<Void>> insertOrderFutures = new ArrayList<>();
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

//            insertOrderFutures.add(future);
        }
//        CompletableFuture.allOf(insertOrderFutures.toArray(new CompletableFuture[0])).join();

        List<List<OrderItem>> batchesOrderItemList = CommonUtil.splitListBatches(orderItemList, 1000);
//        List<CompletableFuture<Void>> insertOrderItemFutures = new ArrayList<>();
        for (List<OrderItem> batch : batchesOrderItemList) {
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                try {
                    transactionTemplate.executeWithoutResult(status -> {
                        orderItemService.saveBatch(batch);
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }, customThreadPool);
//            insertOrderItemFutures.add(future);
        }
//        CompletableFuture.allOf(insertOrderItemFutures.toArray(new CompletableFuture[0])).join();


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

        RedisUtil.judgeRedis(redisService, ORDER + orderSn, ordertList, order, Order.class);

    }

    private void getOrderItem(JSONObject orderObject, List<OrderItem> orderItemList, long shopId) {
        JSONArray itemList = orderObject.getJSONArray("item_list");
        String orderSn = orderObject.getString("order_sn");
        JSONObject itemObject;

        Set<Long> itemIdSet = new HashSet<>();
        for (int i = 0; i < itemList.size(); i++) {
            itemObject = itemList.getJSONObject(i);

            long itemId = itemObject.getLong("item_id");

            itemIdSet.add(itemId);

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
                    .shopId(shopId)
                    .build();

            JSONObject imageInfoArray = itemObject.getJSONObject("image_info");
            if (imageInfoArray != null && imageInfoArray.size() > 0) {
                orderItem.setImageUrl(imageInfoArray.getString("image_url"));
            }

            orderItemList.add(orderItem);

//            RedisUtil.judgeRedis(redisService, ORDER_ITEM_MODEL + orderSn + "_" + itemId + "_" + modelId + "_" + i, orderItemList, orderItem, OrderItem.class);
        }

        if (itemIdSet.size() > 0) {
            List<Long> itemIdList = itemIdSet.stream().toList();
            List<Map<String, Object>> createIn30DaysList = productDao.getCreateIn30Days(itemIdList);
            if (createIn30DaysList.size() > 0) {
                createIn30DaysList.stream().forEach(map -> {
                    Long tempItemId = Long.valueOf(map.get("id").toString());

                    String name = map.get("name").toString();

                    if ("".equals(name)) {
                        List<String> tagNameList = new ArrayList<>();
                        tagNameList.add("破零新品");
                        productOrImgTagService.saveProductOrImgTag(tagNameList, tempItemId,"AUTO");
                    } else if ("破零新品".equals(name)) {
                        List<String> tagNameList = new ArrayList<>();
                        tagNameList.add("二次破零");
                        productOrImgTagService.saveProductOrImgTag(tagNameList, tempItemId,"AUTO");
                    } else if ("二次破零".equals(name)) {
                        List<String> tagNameList = new ArrayList<>();
                        tagNameList.add("三次破零");
                        productOrImgTagService.saveProductOrImgTag(tagNameList, tempItemId,"AUTO");
                    } else if ("三次破零".equals(name)) {
                        List<String> tagNameList = new ArrayList<>();
                        tagNameList.add("潜力新品");
                        productOrImgTagService.saveProductOrImgTag(tagNameList, tempItemId,"AUTO");
                    }

                    orderDao.update(new UpdateWrapper<Order>().set("isNew", true).eq("order_sn", orderSn));
                });

            }
        }

    }

}
