package com.boss.shopee_receiver.service.impl;

import com.alibaba.fastjson.JSON;
import com.boss.shopee_receiver.service.ReceiverService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.boss.common.constant.MQPrefixConst.SHOPEE_EXCHANGE;

/**
 * @Description
 * @Author 罗宇航
 * @Date 2024/8/3
 */

@Service
@Slf4j
public class ReceiverServiceImpl implements ReceiverService {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Override
    public void getPush(String data) {
        log.info("开始向mq推送消息");
        try {
            rabbitTemplate.convertAndSend(SHOPEE_EXCHANGE, "*", new Message(JSON.toJSONBytes(data), new MessageProperties()));
        } catch (Exception e) {
            log.error("向mq推送消息失败：" + e);
        }
        log.info("推送结束");
    }
}
