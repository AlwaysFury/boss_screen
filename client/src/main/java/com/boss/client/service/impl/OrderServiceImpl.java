package com.boss.client.service.impl;

import cn.hutool.core.util.IdUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.boss.client.dao.*;
import com.boss.client.dto.ConditionDTO;
import com.boss.client.service.OrderService;
import com.boss.client.util.RedisUtil;
import com.boss.client.util.ShopeeUtil;
import com.boss.client.vo.OrderEscrowInfoVO;
import com.boss.client.vo.OrderEscrowVO;
import com.boss.client.vo.PageResult;
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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadPoolExecutor;
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
        for (int i = 0; i < orderSnList.size(); i += 50) {
            newOrderSnList.add(String.join(",", orderSnList.subList(i, Math.min(i + 50, orderSnList.size()))));
        }

        refreshBatchOrderBySn(newOrderSnList, shopId);
    }

    private void refreshBatchOrderBySn(List<String> orderSnList, long shopId) {
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
                            getOrderItem(orderDetailObject, orderItemList, finalShopId);
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
            CompletableFuture.runAsync(() -> {
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
            CompletableFuture.runAsync(() -> {
                try {
                    transactionTemplate.executeWithoutResult(status -> {
                        orderItemService.saveOrUpdateBatch(batch);
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
                    .shopId(shopId)
                    .skuName(itemObject.getString("model_sku").split("-")[0])
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
        OrderStatusPush statusPush = orderStatusPushDao.selectOne(new QueryWrapper<OrderStatusPush>().eq("order_sn", orderSn));
        order.setStatus(statusPush.getStatus());
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
            orderEscrowInfoVO.setSellerReturnRefund(escrowInfo.getSellerReturnRefund());
            orderEscrowInfoVO.setShopeeDiscount(escrowInfo.getShopeeDiscount());
            orderEscrowInfoVO.setVoucherFromSeller(escrowInfo.getVoucherFromSeller());
            orderEscrowInfoVO.setReverseShippingFee(escrowInfo.getReverseShippingFee());
            orderEscrowInfoVO.setServiceFee(escrowInfo.getServiceFee());
            orderEscrowInfoVO.setOrderAmsCommissionFee(escrowInfo.getOrderAmsCommissionFee());
            orderEscrowInfoVO.setSellerTransactionFee(escrowInfo.getSellerTransactionFee());
            orderEscrowInfoVO.setCommissionFee(escrowInfo.getCommissionFee());
        }

        return orderEscrowInfoVO;
    }


    @Override
    public List<SelectVO> getStatusSelect() {
        return OrderStatusEnum.getOrderStatusEnum();
    }
}
