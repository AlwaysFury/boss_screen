package com.boss.task.consumer;

import com.alibaba.fastjson.JSON;
import com.boss.common.dto.RefreshDTO;
import com.boss.task.service.impl.OrderServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.boss.common.constant.MQPrefixConst.ORDER_QUEUE;

/**
 * @Description
 * @Author 罗宇航
 * @Date 2024/8/3
 */

@Component
@RabbitListener(queues = ORDER_QUEUE)
@Slf4j
public class OrderConsumer {

    @Autowired
    private OrderServiceImpl orderService;

    @RabbitHandler
    public void process(byte[] data) {
        // 获取监听信息
        String dataStr = new String(data);
        log.info("消费者获取到 订单刷新 推送消息：" + dataStr);
        // 开始消费
        RefreshDTO refreshDTO = JSON.parseObject(new String(data), RefreshDTO.class);
        if ("id".equals(refreshDTO.getBy())) {
            orderService.refreshOrderBySn(refreshDTO.getOrderSns());
        } else {
            orderService.refreshOrderByTime(refreshDTO.getStartTime(), refreshDTO.getEndTime());
        }
        log.info("订单消费完成");
    }
}
