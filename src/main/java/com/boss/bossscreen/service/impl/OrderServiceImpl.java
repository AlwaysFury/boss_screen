package com.boss.bossscreen.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.boss.bossscreen.dao.*;
import com.boss.bossscreen.dto.ConditionDTO;
import com.boss.bossscreen.enities.*;
import com.boss.bossscreen.service.OrderService;
import com.boss.bossscreen.util.BeanCopyUtils;
import com.boss.bossscreen.util.CommonUtil;
import com.boss.bossscreen.util.PageUtils;
import com.boss.bossscreen.util.ShopeeUtil;
import com.boss.bossscreen.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.boss.bossscreen.constant.RedisPrefixConst.*;

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
    private OrderItemServiceImpl orderItemService;

    @Autowired
    private EscrowInfoServiceImpl escrowInfoService;

    @Autowired
    private EscrowItemServiceImpl escrowItemService;

    @Autowired
    private ShopServiceImpl shopService;

    @Autowired
    private CostServiceImpl costService;

    @Autowired
    private CostDao costDao;

    @Autowired
    private RedisServiceImpl redisService;

    private static final HashMap<String, String> orderStatusMap = new HashMap<>();

    static {
        orderStatusMap.put("UNPAID", "未支付");
        orderStatusMap.put("READY_TO_SHIP", "待出货");
        orderStatusMap.put("PROCESSED", "已处理");
        orderStatusMap.put("SHIPPED", "运送中");
        orderStatusMap.put("COMPLETED", "已完成");
        orderStatusMap.put("IN_CANCEL", "取消中");
        orderStatusMap.put("CANCELLED", "已取消");
        orderStatusMap.put("INVOICE_PENDING", "等待退款");
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void saveOrUpdateOrder(String orderSnStartTime) {
        long logStart =  System.currentTimeMillis();

        // 定义日期格式
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd 00:00:00");

        // 将字符串转换为LocalDate对象
        LocalDate startTime = LocalDate.parse(orderSnStartTime, formatter);

        LocalDate endTime = LocalDate.from(LocalDateTime.now());

        // 遍历所有未冻结店铺获取 token 和 shopId
        QueryWrapper<Shop> shopQueryWrapper = new QueryWrapper<>();
        shopQueryWrapper.select("shop_id").eq("status", "1");
        List<Shop> shopList = shopDao.selectList(shopQueryWrapper);

        // 根据每个店铺的 token 和 shopId 获取产品
        List<Order> ordertList = new CopyOnWriteArrayList<>();
        List<Order> updateOrderList = new CopyOnWriteArrayList<>();
        List<OrderItem> orderItemList =  new CopyOnWriteArrayList<>();
        List<OrderItem> updateOrderItemList = new CopyOnWriteArrayList<>();
        List<EscrowInfo> escrowInfoList =  new CopyOnWriteArrayList<>();
        List<EscrowInfo> updateEscrowInfoList = new CopyOnWriteArrayList<>();
        List<EscrowItem> escrowItemList = new CopyOnWriteArrayList<>();
        List<EscrowItem> updateEscrowItemList = new CopyOnWriteArrayList<>();
        long shopId;
        String accessToken;
        JSONObject result;
        for (Shop shop : shopList) {
            shopId = shop.getShopId();
            accessToken = shopService.getAccessTokenByShopId(String.valueOf(shopId));

            List<LocalDate[]> timeList = splitIntoEvery15DaysTimestamp(startTime, endTime);
            List<String> orderSnList = new ArrayList<>();
            for (LocalDate[] time : timeList) {
                long start = time[0].atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli() / 1000L;
                long end = time[1].atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli() / 1000L;
                List<String> object = ShopeeUtil.getOrderList(accessToken, shopId, 0, new ArrayList<>(), start, end);
                orderSnList.addAll(object);
            }

            if (orderSnList.size() == 0) {
                continue;
            }

            List<String> newOrderSnList = new ArrayList<>();
            for (int i = 0; i < orderSnList.size(); i += 50) {
                newOrderSnList.add(String.join(",", orderSnList.subList(i, Math.min(i + 50, orderSnList.size()))));
            }

            CountDownLatch orderCountDownLatch = new CountDownLatch(newOrderSnList.size());
            // 开线程池，线程数量为要遍历的对象的长度
            ExecutorService orderExecutor = Executors.newFixedThreadPool(newOrderSnList.size());

            CountDownLatch escrowCountDownLatch = new CountDownLatch(newOrderSnList.size());
            // 开线程池，线程数量为要遍历的对象的长度
            ExecutorService escrowExecutor = Executors.newFixedThreadPool(newOrderSnList.size());
            for (String orderSn : newOrderSnList) {

                long finalShopId = shopId;

                CompletableFuture.runAsync(() -> {
                    try {
                        String finalAccessToken = shopService.getAccessTokenByShopId(String.valueOf(finalShopId));
                        JSONObject orderObject = ShopeeUtil.getOrderDetail(finalAccessToken, finalShopId, orderSn);
                        if (orderObject.getString("error").contains("error") && orderObject == null) {
                            return;
                        }
                        JSONArray orderArray = orderObject.getJSONObject("response").getJSONArray("order_list");
                        JSONObject orderDetailObject;
                        for (int j = 0; j < orderArray.size(); j++) {
                            orderDetailObject = orderArray.getJSONObject(j);
                            getOrderDetail(orderDetailObject, ordertList, finalShopId, updateOrderList);
                            getOrderItem(orderDetailObject, orderItemList, updateOrderItemList);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        orderCountDownLatch.countDown();
                        System.out.println("orderCountDownLatch===> " + orderCountDownLatch);
                    }
                }, orderExecutor);

                CompletableFuture.runAsync(() -> {
                    try {
                        String[] splitOrderSns = orderSn.split(",");
                        for (String sn : splitOrderSns) {
                            String finalAccessToken = shopService.getAccessTokenByShopId(String.valueOf(finalShopId));
                            JSONObject escrowResult = ShopeeUtil.getEscrowDetail(finalAccessToken, finalShopId, sn);
                            JSONObject escrowInfoObject = escrowResult.getJSONObject("response");
                            if (escrowResult.getString("error").contains("error") && escrowInfoObject == null) {
                                return;
                            }
                            JSONObject oderIncomeObject = escrowInfoObject.getJSONObject("order_income");
                            saveEscrowInfoByOrderSn(oderIncomeObject, sn, escrowInfoList, updateEscrowInfoList);
                            saveEscrowItem(oderIncomeObject, sn, escrowItemList, updateEscrowItemList);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        escrowCountDownLatch.countDown();
                        System.out.println("escrowCountDownLatch===> " + escrowCountDownLatch);
                    }
                }, escrowExecutor);
            }

            try {
                orderCountDownLatch.await();
                escrowCountDownLatch.await();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

//        System.out.println("orderList===>" + JSONArray.toJSONString(ordertList));
        this.saveBatch(ordertList);
//        System.out.println("updateOrderList===>" + JSONArray.toJSONString(updateOrderList));
        this.updateBatchById(updateOrderList);
//        System.out.println("orderItemList===>" + JSONArray.toJSONString(orderItemList));
        orderItemService.saveBatch(orderItemList);
//        System.out.println("updateOrderItemList===>" + JSONArray.toJSONString(updateOrderItemList));
        orderItemService.updateBatchById(updateOrderItemList);
//        System.out.println("escrowInfoList===>" + JSONArray.toJSONString(escrowInfoList));
        escrowInfoService.saveBatch(escrowInfoList);
//        System.out.println("updateEscrowInfoList===>" + JSONArray.toJSONString(updateEscrowInfoList));
        escrowInfoService.updateBatchById(updateEscrowInfoList);
//        System.out.println("escrowItemList===>" + JSONArray.toJSONString(escrowItemList));
        escrowItemService.saveBatch(escrowItemList);
//        System.out.println("updateEscrowItemList===>" + JSONArray.toJSONString(updateEscrowItemList));
        escrowItemService.updateBatchById(updateEscrowItemList);

        log.info("更新订单耗时： {}秒", (System.currentTimeMillis() - logStart) / 1000);
    }

    private List<LocalDate[]> splitIntoEvery15DaysTimestamp(LocalDate startDate, LocalDate endDate) {
        List<LocalDate[]> timestampPairs = new ArrayList<>();
        while (!startDate.isAfter(endDate)) {
            LocalDate endOfSplitDate = startDate.plusDays(14);
            if (endOfSplitDate.isAfter(endDate)) {
                endOfSplitDate = endDate;
            }
            LocalDate[] pair = new LocalDate[]{
                    startDate,
                    endOfSplitDate
            };
            timestampPairs.add(pair);
            startDate = endOfSplitDate.plusDays(1);
        }
        return timestampPairs;
    }

    private void getOrderDetail(JSONObject orderObject, List<Order> ordertList, Long shopId, List<Order> updateOrderList) {
        String orderSn = orderObject.getString("order_sn");

        Order order = Order.builder()
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

//        JSONArray packageArray = orderObject.getJSONArray("package_list");
//        if (packageArray != null && packageArray.size() > 0) {
//            order.setTrackingNumber(packageArray.getJSONObject(0).getString("package_number"));
//        }
        if (!"UNPAID".equals(order.getStatus()) && !"READY_TO_SHIP".equals(order.getStatus()) && !"PROCESSED".equals(order.getStatus()) && !"CANCELLED".equals(order.getStatus())) {
            String token = shopService.getAccessTokenByShopId(String.valueOf(shopId));
            JSONObject responseObject = ShopeeUtil.getTrackingNumber(token, shopId, orderSn).getJSONObject("response");
            if (responseObject != null) {
                order.setTrackingNumber(responseObject.getString("tracking_number"));
            } else {
                Object redisResult = redisService.get(ORDER + orderSn);
                if (!Objects.isNull(redisResult)) {
                    order.setTrackingNumber(JSONObject.parseObject(redisResult.toString()).getString("trackingNumber"));
                }
            }
        }

        CommonUtil.judgeRedis(redisService, ORDER + orderSn, ordertList, updateOrderList, order, Order.class);

    }

    private void getOrderItem(JSONObject orderObject, List<OrderItem> orderItemList, List<OrderItem> updateOrderItemList) {
        JSONArray itemList = orderObject.getJSONArray("item_list");
        JSONObject itemObject;
        for (int i = 0; i < itemList.size(); i++) {
            itemObject = itemList.getJSONObject(i);

            String orderSn = orderObject.getString("order_sn");
            long itemId = itemObject.getLong("item_id");
            long modelId = itemObject.getLong("model_id");
            OrderItem orderItem = OrderItem.builder()
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

//            JSONArray packageArray = orderObject.getJSONArray("package_list");
//            if (packageArray != null && packageArray.size() > 0) {
//                orderItem.setPackageNumber(packageArray.getJSONObject(0).getString("package_number"));
//            }

            JSONObject imageInfoArray = itemObject.getJSONObject("image_info");
            if (imageInfoArray != null && imageInfoArray.size() > 0) {
                orderItem.setImageUrl(imageInfoArray.getString("image_url"));
            }

            CommonUtil.judgeRedis(redisService, ORDER_ITEM_MODEL + orderSn + "_" + itemId + "_" + modelId + "_" + i, orderItemList, updateOrderItemList, orderItem, OrderItem.class);
        }

    }

    public void saveEscrowInfoByOrderSn(JSONObject orderIncomeObject, String orderSn, List<EscrowInfo> escrowInfoList, List<EscrowInfo> updateEscrowInfoList) {

        EscrowInfo escrowInfo = EscrowInfo.builder()
                .orderSn(orderSn)
                .buyerUserName(orderIncomeObject.getString("buyer_user_name"))
                .buyerTotalAmount(orderIncomeObject.getBigDecimal("buyer_total_amount"))
                .buyerPaidShippingFee(orderIncomeObject.getBigDecimal("buyer_paid_shipping_fee"))
                .actualShippingFee(orderIncomeObject.getBigDecimal("actual_shipping_fee"))
                .escrowAmount(orderIncomeObject.getBigDecimal("escrow_amount"))
                .build();

        if (orderIncomeObject.containsKey("order_adjustment")) {
            JSONObject adjustmentObject = orderIncomeObject.getJSONObject("order_adjustment");
            escrowInfo.setAdjustmentAmount(adjustmentObject.getBigDecimal("amount"));
            escrowInfo.setAdjustmentReason(adjustmentObject.getString("adjustment_reason"));
        }

        CommonUtil.judgeRedis(redisService, ESCROW + orderSn, escrowInfoList, updateEscrowInfoList, escrowInfo, EscrowInfo.class);
    }

    public void saveEscrowItem(JSONObject oderIncomeObject, String orderSn, List<EscrowItem> escrowItemList, List<EscrowItem> updateEscrowItemList) {
        JSONArray items = oderIncomeObject.getJSONArray("items");

        JSONObject itemObject;
        for (int i = 0; i < items.size(); i++) {
            itemObject = items.getJSONObject(i);
            long itemId = itemObject.getLong("item_id");
            long modelId = itemObject.getLong("model_id");
            EscrowItem escrowItem = EscrowItem.builder()
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

            CommonUtil.judgeRedis(redisService, ESCROW_ITEM_MODEL + orderSn + "_" + itemId + "_" + modelId + "_" + i, escrowItemList, updateEscrowItemList, escrowItem, EscrowItem.class);

        }
    }

    public PageResult<OrderEscrowVO> orderListByCondition(ConditionDTO condition) {
        // 查询分类数量
        Integer count = orderDao.orderCount(condition);
        if (count == 0) {
            return new PageResult<>();
        }
        // 分页查询分类列表
        List<OrderEscrowVO> orderEscrowVOList = orderDao.orderList(PageUtils.getLimitCurrent(), PageUtils.getSize(), condition).stream().map(order -> {
            OrderEscrowVO orderEscrowVO = BeanCopyUtils.copyObject(order, OrderEscrowVO.class);
            orderEscrowVO.setCreateTime(CommonUtil.timestamp2String(order.getCreateTime()));
            orderEscrowVO.setStatus(orderStatusMap.get(order.getStatus()));

            List<OrderItem> orderItemList = orderItemDao.selectList(new QueryWrapper<OrderItem>().eq("order_sn", order.getOrderSn()));
            int clothesCount = 0;
            for (int i = 0; i < orderItemList.size(); i++) {
                clothesCount += orderItemList.get(i).getCount();
            }
            orderEscrowVO.setTotalCount(clothesCount);

            orderEscrowVO.setShopName(shopDao.selectOne(new QueryWrapper<Shop>().eq("shop_id", order.getShopId())).getName());

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
        orderEscrowInfoVO.setStatus(orderStatusMap.get(order.getStatus()));

        orderEscrowInfoVO.setShopName(shopDao.selectOne(new QueryWrapper<Shop>().eq("shop_id", order.getShopId())).getName());

        EscrowInfo escrowInfo = escrowInfoDao.selectOne(new QueryWrapper<EscrowInfo>().eq("order_sn", orderSn));
        if (escrowInfo != null) {
            orderEscrowInfoVO.setBuyerTotalAmount(escrowInfo.getBuyerTotalAmount());
            orderEscrowInfoVO.setBuyerPaidShippingFee(escrowInfo.getBuyerPaidShippingFee());
            orderEscrowInfoVO.setActualShippingFee(escrowInfo.getActualShippingFee());
            orderEscrowInfoVO.setEscrowAmount(escrowInfo.getEscrowAmount());
            orderEscrowInfoVO.setAdjustmentAmount(escrowInfo.getAdjustmentAmount());
            orderEscrowInfoVO.setAdjustmentReason(escrowInfo.getAdjustmentReason());
        }

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
        List<SelectVO> list = new ArrayList<>();
        for(String key : orderStatusMap.keySet()){
            SelectVO vo = SelectVO.builder()
                    .key(key)
                    .value(orderStatusMap.get(key)).build();
            list.add(vo);
        }
        return list;
    }
}
