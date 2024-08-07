package com.boss.task.consumer;

import com.boss.task.service.impl.WebhookServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.boss.common.constant.MQPrefixConst.SHOPEE_QUEUE;

/**
 * maxwell监听数据
 */
@Component
@RabbitListener(queues = SHOPEE_QUEUE)
@Slf4j
public class ShopeeConsumer {
    @Autowired
    private WebhookServiceImpl webhookService;

    @RabbitHandler
    public void process(byte[] data) {
        // 获取监听信息
        String dataStr = new String(data);
        log.info("消费者获取到 shopee 推送消息：" + dataStr);
        // 开始消费
        webhookService.getPush(dataStr);
        log.info("shopee消费完成");
    }

}