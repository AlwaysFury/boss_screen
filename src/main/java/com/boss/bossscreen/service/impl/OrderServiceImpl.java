package com.boss.bossscreen.service.impl;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.boss.bossscreen.dao.*;
import com.boss.bossscreen.dto.ConditionDTO;
import com.boss.bossscreen.enities.*;
import com.boss.bossscreen.service.OrderService;
import com.boss.bossscreen.util.BeanCopyUtils;
import com.boss.bossscreen.util.PageUtils;
import com.boss.bossscreen.util.ShopeeUtil;
import com.boss.bossscreen.vo.OrderEscrowInfoVO;
import com.boss.bossscreen.vo.OrderEscrowItemVO;
import com.boss.bossscreen.vo.OrderEscrowVO;
import com.boss.bossscreen.vo.PageResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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
    private EscrowInfoServiceImpl escrowInfoService;

    @Autowired
    private EscrowItemServiceImpl escrowItemService;

    @Autowired
    private OrderItemServiceImpl orderItemService;

    // todo 优化下拉
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void saveOrUpdateOrder() {
        // 遍历所有未冻结店铺获取 token 和 shopId
        QueryWrapper<Shop> shopQueryWrapper = new QueryWrapper<>();
        shopQueryWrapper.select("shop_id", "access_token").eq("status", "1");
        List<Shop> shopList = shopDao.selectList(shopQueryWrapper);

        // 根据每个店铺的 token 和 shopId 获取产品
        List<Order> ordertList = new ArrayList<>();
        List<OrderItem> orderItemList =  new ArrayList<>();
        long shopId;
        String accessToken;
        JSONObject result;
        for (Shop shop : shopList) {
            shopId = shop.getShopId();
            accessToken = shop.getAccessToken();

            // todo 优化为集合查询
            result = ShopeeUtil.getOrderList(accessToken, shopId);

            if (result.getString("error").contains("error")) {
                continue;
            }

            JSONArray itemArray = result.getJSONObject("response").getJSONArray("order_list");
            if (itemArray.size() == 0) {
                continue;
            }


            for (int i = 0; i < itemArray.size(); i++) {
                String orderSn = itemArray.getJSONObject(i).getString("order_sn");
                JSONObject orderObject = ShopeeUtil.getOrderDetail(accessToken, shopId, orderSn);
                if (orderObject.getString("error").contains("error")) {
                    continue;
                }
                JSONArray orderArray = orderObject.getJSONObject("response").getJSONArray("order_list");
                JSONObject orderDetailObject;
                for (int j = 0; j < orderArray.size(); j++) {
                    orderDetailObject = orderArray.getJSONObject(j);
                    ordertList = getOrderDetail(orderDetailObject, ordertList, shopId);
                    orderItemList = getOrderItem(orderDetailObject, orderItemList);
                }

                // 保存对应的支付信息
                saveEscrowInfoByOrderSn(accessToken, shopId, orderSn);
            }
        }

        // todo 检测入库
        // 将新旧数据全部数据缓存进入 redis
        // 新数据与旧数据进行比较：时间戳
        // key：order:订单id:时间戳
        // value：数据 json 格式化
        // 只检查为完成的订单！！！！！！
        // 新增：将数据存入新增集合，存入 redis 和 mysql
        // 更新：将更新数据存入更新集合，更新 reids 和 mysql 中的数据
        System.out.println(JSONArray.toJSONString(ordertList));
        this.saveBatch(ordertList);
        System.out.println(JSONArray.toJSONString(orderItemList));
        orderItemService.saveBatch(orderItemList);
    }

    private List<Order> getOrderDetail(JSONObject orderObject, List<Order> ordertList, Long shopId) {
        Order order = Order.builder()
                .shopId(shopId)
                .createTime(orderObject.getLong("create_time"))
                .updateTime(orderObject.getLong("update_time"))
                .orderSn(orderObject.getString("order_sn"))
                .status(orderObject.getString("order_status"))
                .payTime(orderObject.getLong("pay_time"))
                .buyerUerId(orderObject.getLong("buyer_user_id"))
                .buyerUserName(orderObject.getString("buyer_username"))
                .cancelReason(orderObject.getString("cancel_reason"))
                .cancelBy(orderObject.getString("cancel_by"))
                .buyerCancelReason(orderObject.getString("buyer_cancel_reason"))
                .build();

        JSONArray packageArray = orderObject.getJSONArray("package_list");
        if (packageArray != null && packageArray.size() > 0) {
            order.setPackageNumber(packageArray.getJSONObject(0).getString("package_number"));
        }

        ordertList.add(order);

        return ordertList;
    }

    private List<OrderItem> getOrderItem(JSONObject orderObject, List<OrderItem> orderItemList) {
        JSONArray itemList = orderObject.getJSONArray("item_list");
        JSONObject itemObject;
        for (int i = 0; i < itemList.size(); i++) {
            itemObject = itemList.getJSONObject(i);

            OrderItem orderItem = OrderItem.builder()
                    .orderSn(orderObject.getString("order_sn"))
                    .itemId(itemObject.getLong("item_id"))
                    .itemName(itemObject.getString("item_name"))
                    .itemSku(itemObject.getString("item_sku"))
                    .modelId(itemObject.getLong("model_id"))
                    .modelName(itemObject.getString("model_name"))
                    .modelSku(itemObject.getString("model_sku"))
                    .count(itemObject.getInteger("model_quantity_purchased"))
                    .promotionId(itemObject.getLong("promotion_id"))
                    .promotionType(itemObject.getString("promotion_type"))
                    .build();

            JSONArray packageArray = orderObject.getJSONArray("package_list");
            if (packageArray != null && packageArray.size() > 0) {
                orderItem.setPackageNumber(packageArray.getJSONObject(0).getString("package_number"));
            }

            JSONArray imageInfoArray = itemObject.getJSONArray("image_info");
            if (imageInfoArray != null && imageInfoArray.size() > 0) {
                orderItem.setImageUrl(imageInfoArray.getJSONObject(0).getString("image_url"));
            }

            orderItemList.add(orderItem);
        }

        return orderItemList;
    }

    private void saveEscrowInfoByOrderSn(String token, Long shopId, String orderSn) {
        JSONObject result = ShopeeUtil.getEscrowDetail(token, shopId, orderSn);
        // todo error怎么办
        JSONObject escrowInfoObject = result.getJSONObject("response");
        JSONObject escrowIncomeObject = escrowInfoObject.getJSONObject("order_income");

        EscrowInfo escrowInfo = EscrowInfo.builder()
                .orderSn(orderSn)
                .buyerUserName(escrowInfoObject.getString("buyer_user_name"))
                .buyerTotalAmount(escrowIncomeObject.getBigDecimal("buyer_total_amount"))
                .buyerPaidShippingFee(escrowIncomeObject.getBigDecimal("buyer_paid_shipping_fee"))
                .actualShippingFee(escrowIncomeObject.getBigDecimal("actual_shipping_fee"))
                .escrowAmount(escrowIncomeObject.getBigDecimal("escrow_amount"))
                .build();

        escrowInfoService.save(escrowInfo);

        // 保存支付单产品信息
        saveEscrowItem(escrowIncomeObject.getJSONArray("items"), orderSn);
    }

    private void saveEscrowItem(JSONArray items, String orderSn) {
        List<EscrowItem> escrowItemList = new ArrayList<>();
        JSONObject itemObject;
        for (int i = 0; i < items.size(); i++) {
            itemObject = items.getJSONObject(i);

            escrowItemList.add(EscrowItem.builder()
                            .orderSn(orderSn)
                            .itemId(itemObject.getLong("item_id"))
                            .itemName(itemObject.getString("item_name"))
                            .itemSku(itemObject.getString("item_sku"))
                            .modelId(itemObject.getLong("model_id"))
                            .modelName(itemObject.getString("model_name"))
                            .modelSku(itemObject.getString("model_sku"))
                            .count(itemObject.getInteger("quantity_purchased"))
                            .originalPrice(itemObject.getBigDecimal("original_price"))
                            .sellingPrice(itemObject.getBigDecimal("selling_price"))
                            .discountedPrice(itemObject.getBigDecimal("discounted_price"))
                            .sellerDiscount(itemObject.getBigDecimal("seller_discount"))
                            .activityId(itemObject.getLong("activity_id"))
                            .activityType(itemObject.getString("activity_type"))
                            .build());
        }
        escrowItemService.saveBatch(escrowItemList);
    }

    public PageResult<OrderEscrowVO> orderListByCondition(ConditionDTO condition) {
        // 查询分类数量
        Integer count = orderDao.orderCount(condition);
        if (count == 0) {
            return new PageResult<>();
        }
        // 分页查询分类列表
        List<OrderEscrowVO> orderEscrowVOList = orderDao.orderList(PageUtils.getLimitCurrent(), PageUtils.getSize(), condition).stream().map(object -> {
            List<OrderItem> orderItemList = orderItemDao.selectList(new QueryWrapper<OrderItem>().eq("order_sn", object.getOrderSn()));
            // T恤数量
            int tShirtCount = 0;
            // 双面数量
            int doubleCount = 0;
            // 短款T恤数量
            int shortCount = 0;
            // 卫衣数量
            int hoodieCount = 0;
            // 成品数量
            int finishCount = 0;
            // 聚酯纤维数量
            int fiberCount = 0;

            String modelSku;
            String modelName;
            int clothesCount = 0;
            OrderItem orderItem;
            for (int i = 0; i < orderItemList.size(); i++) {
                orderItem = orderItemList.get(i);
                modelSku = orderItem.getModelSku().toLowerCase();
                clothesCount = orderItem.getCount();
                if (modelSku.contains("100%cotton")) {
                    tShirtCount += clothesCount;
                } else if (modelSku.contains("short")) {
                    shortCount += clothesCount;
                } else if (modelSku.contains("hoodie")) {
                    hoodieCount += clothesCount;
                } else if (modelSku.contains("成品")) {
                    finishCount += clothesCount;
                } else if (modelSku.contains("t-shirt")) {
                    fiberCount += clothesCount;
                } else {
                    tShirtCount += clothesCount;
                }

                modelName = orderItem.getModelName();
                if (!"notsure".equals(modelName) && isEnglish(modelName.substring(0, 1)) && isEnglish(modelName.substring(1, 2))) {
                    doubleCount += 1;
                }
            }
            object.setTShirtCount(tShirtCount);
            object.setDoubleCount(doubleCount);
            object.setShortCount(shortCount);
            object.setHoodieCount(hoodieCount);
            object.setFinishCount(finishCount);
            object.setFiberCount(fiberCount);

            return object;
        }).collect(Collectors.toList());
        return new PageResult<>(orderEscrowVOList, count);
    }

    public OrderEscrowInfoVO getOrderInfo(String orderSn) {

        Order order = orderDao.selectOne(new QueryWrapper<Order>().eq("order_sn", orderSn));

        OrderEscrowInfoVO orderEscrowInfoVO = BeanCopyUtils.copyObject(order, OrderEscrowInfoVO.class);

        EscrowInfo escrowInfo = escrowInfoDao.selectOne(new QueryWrapper<EscrowInfo>().eq("order_sn", orderSn));
        if (escrowInfo != null) {
            orderEscrowInfoVO.setBuyerTotalAmount(escrowInfo.getBuyerTotalAmount());
            orderEscrowInfoVO.setBuyerPaidShippingFee(escrowInfo.getBuyerPaidShippingFee());
            orderEscrowInfoVO.setActualShippingFee(escrowInfo.getActualShippingFee());
            orderEscrowInfoVO.setEscrowAmount(escrowInfo.getEscrowAmount());
        }

        List<OrderEscrowItemVO> orderEscrowItemVOList = orderItemDao.selectList(new QueryWrapper<OrderItem>().eq("order_sn", orderSn)).stream().map(
                orderItem -> {
                    OrderEscrowItemVO orderEscrowItemVO = BeanCopyUtils.copyObject(orderItem, OrderEscrowItemVO.class);

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
                    }

                    return orderEscrowItemVO;
                }
        ).collect(Collectors.toList());

        orderEscrowInfoVO.setOrderEscrowItemVOList(orderEscrowItemVOList);

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
}
