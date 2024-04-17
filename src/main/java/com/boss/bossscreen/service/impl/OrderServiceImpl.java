package com.boss.bossscreen.service.impl;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.boss.bossscreen.dao.OrderDao;
import com.boss.bossscreen.dao.ShopDao;
import com.boss.bossscreen.enities.Order;
import com.boss.bossscreen.enities.OrderItem;
import com.boss.bossscreen.enities.Shop;
import com.boss.bossscreen.service.OrderService;
import com.boss.bossscreen.util.ShopeeUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

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

            // todo 集合查询
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
                    ordertList = getOrderDetail(orderDetailObject, ordertList);
                    orderItemList = getOrderItem(orderDetailObject, orderItemList);
                }

            }
        }
        // todo 检测入库
        System.out.println(JSONArray.toJSONString(ordertList));
        this.saveBatch(ordertList);
        System.out.println(JSONArray.toJSONString(orderItemList));
        orderItemService.saveBatch(orderItemList);
    }

    private List<Order> getOrderDetail(JSONObject orderObject, List<Order> ordertList) {
        Order order = Order.builder()
                .createTime(orderObject.getLong("create_time"))
                .updateTime(orderObject.getLong("update_time"))
                .orderSn(orderObject.getString("order_sn"))
                .status(orderObject.getString("order_status"))
                // todo 订单金额，折扣价格，运费
                .totalAmount(orderObject.getBigDecimal("total_amount"))
                .payTime(orderObject.getLong("pay_time"))
                .buyerUerId(orderObject.getLong("buyer_user_id"))
                .buyerUserName(orderObject.getString("buyer_username"))
                .cancelReason(orderObject.getString("cancel_reason"))
                .cancelBy(orderObject.getString("cancel_by"))
                .buyerCancelReason(orderObject.getString("buyer_cancel_reason"))
                .estimatedShippingFee(orderObject.getBigDecimal("estimated_shipping_fee"))
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
                    .discountedPrice(itemObject.getBigDecimal("model_discounted_price"))
                    .originalPrice(itemObject.getBigDecimal("model_original_price"))
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
}
