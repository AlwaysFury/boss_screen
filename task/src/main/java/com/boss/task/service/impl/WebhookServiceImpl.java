package com.boss.task.service.impl;

import cn.hutool.core.util.IdUtil;
import com.alibaba.fastjson.JSONObject;
import com.boss.common.enities.OrderStatusPush;
import com.boss.task.service.WebhookService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

import static com.boss.common.enums.OrderStatusEnum.CANCELLED;
import static com.boss.common.enums.OrderStatusEnum.UNPAID;

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
    private TrackingInfoServiceImpl trackingInfoService;

    @Autowired
    private OrderStatusPushServiceImpl orderStatusPushService;

    @Autowired
    @Qualifier("customThreadPool")
    private ThreadPoolExecutor customThreadPool;

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

                orderStatusPushService.save(orderStatusPush);

                if (UNPAID.getCode().equals(status) || CANCELLED.getCode().equals(status)) {
                    log.info("新增订单信息");

                    CompletableFuture.runAsync(() -> {
                        try {
                            orderService.refreshSingleOrderBySn(orderSn,shopId);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }, customThreadPool);
                }

                break;
            case 4:
                log.info("====获取物流信息");
                // {"data":{"ordersn":"2406179DN25C0Y","forder_id":"5456557173982721879","package_number":"OFG172323903251079","tracking_no":"TH240167042286R"},"shop_id":874244879,"code":4,"timestamp":1718877102}
                trackingInfoService.saveTrackingInfoBySn(dataObject.getString("ordersn"), shopId, dataObject.getString("tracking_no"));
                break;
        }
    }
}
