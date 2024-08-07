package com.boss.task.consumer;

import com.alibaba.fastjson.JSON;
import com.boss.common.dto.RefreshDTO;
import com.boss.task.service.impl.ProductServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.boss.common.constant.MQPrefixConst.SHOPEE_QUEUE;

/**
 * @Description
 * @Author 罗宇航
 * @Date 2024/8/3
 */

@Component
@RabbitListener(queues = SHOPEE_QUEUE)
@Slf4j
public class ProductConsumer {

    @Autowired
    private ProductServiceImpl productService;

    @RabbitHandler
    public void process(byte[] data) {
        // 获取监听信息
        String dataStr = new String(data);
        log.info("消费者获取到 商品刷新 推送消息：" + dataStr);
        // 开始消费
        RefreshDTO refreshDTO = JSON.parseObject(new String(data), RefreshDTO.class);
        if ("id".equals(refreshDTO.getBy())) {
            productService.refreshProductsById(refreshDTO.getItemIds());
        } else {
            productService.refreshProductByTime(refreshDTO.getStartTime(), refreshDTO.getEndTime());
        }
        log.info("商品消费完成");
    }
}
