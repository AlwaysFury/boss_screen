package com.boss.task.service.impl;

import cn.hutool.core.util.IdUtil;
import com.alibaba.fastjson.JSONObject;
import com.boss.common.enities.OrderStatusPush;
import com.boss.task.service.WebhookService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static com.boss.common.enums.OrderStatusEnum.*;
import static com.boss.common.enums.ProductStatusEnum.BANNED;
import static com.boss.common.enums.ProductStatusEnum.SHOPEE_DELETE;

/**
 * @Description
 * @Author 罗宇航
 * @Date 2024/6/20
 */
@Service
@Slf4j
public class WebhookServiceImpl implements WebhookService {

    @Autowired
    private OrderServiceImpl orderService;

    @Autowired
    private EscrowInfoServiceImpl escrowInfoService;

    @Autowired
    private ProductServiceImpl productService;

    @Autowired
    private TrackingInfoServiceImpl trackingInfoService;

    @Autowired
    private OrderStatusPushServiceImpl orderStatusPushService;


    @Override
    public void getPush(String data) {
        JSONObject object = JSONObject.parseObject(data);
        int code = object.getInteger("code");

        if (code == 0) {
            return;
        }

        long shopId = object.getLong("shop_id");
        JSONObject dataObject = object.getJSONObject("data");
        switch (code) {
            case 3:
                String orderSn = dataObject.getString("ordersn");
                String status = dataObject.getString("status");
                log.info("====更新订单状态：{}", orderSn);

                OrderStatusPush orderStatusPush = OrderStatusPush.builder()
                        .id(IdUtil.getSnowflakeNextId())
                        .createTime(dataObject.getLong("update_time"))
                        .orderSn(orderSn)
                        .status(status)
                        .completedScenario(dataObject.getString("completed_scenario"))
                        .shopId(shopId)
                        .build();

                try {
                    orderStatusPushService.saveOrUpdateOrderStatusPush(orderStatusPush);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (UNPAID.getCode().equals(status) || CANCELLED.getCode().equals(status)) {
                    log.info("====新增订单或更新订单取消原因");
                    orderService.refreshSingleOrderBySn(orderSn, shopId, status);
                }

                if (READY_TO_SHIP.getCode().equals(status)) {
                    log.info("====订单已支付，更新订单支付信息");
                    List<String> orderSnList = new ArrayList<>();
                    orderSnList.add(orderSn);
                    escrowInfoService.refreshSingleEscrowInfoBySn(orderSnList, shopId);
                }

                break;
            case 4:
                log.info("====更新物流信息");
                trackingInfoService.saveTrackingInfoBySn(dataObject.getString("ordersn"), shopId, dataObject.getString("tracking_no"));
                break;
            case 16:
                log.info("====更新平台删除或禁止产品状态");
                String itemStatus = dataObject.getString("item_status");

                if (BANNED.getCode().equals(itemStatus) || SHOPEE_DELETE.getCode().equals(itemStatus)) {
                    productService.updateStatusByItemId(dataObject.getLong("item_id"), itemStatus);
                }
        }
    }
}
