package com.boss.task.consumer;

import com.alibaba.fastjson.JSON;
import com.boss.common.dto.ShopDTO;
import com.boss.task.service.impl.ProductServiceImpl;
import com.boss.task.service.impl.ShopServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.boss.common.constant.MQPrefixConst.SHOP_QUEUE;

/**
 * @Description
 * @Author 罗宇航
 * @Date 2024/8/3
 */

@Component
@RabbitListener(queues = SHOP_QUEUE)
@Slf4j
public class ShopConsumer {

    @Autowired
    private ShopServiceImpl shopService;

    @Autowired
    private ProductServiceImpl productService;

    @RabbitHandler
    public void process(byte[] data) {
        // 获取监听信息
        String dataStr = new String(data);
        log.info("消费者获取到 授权 推送消息：" + dataStr);
        // 开始消费
        ShopDTO shopDTO = JSON.parseObject(new String(data), ShopDTO.class);
        shopService.saveOrUpdateToken(shopDTO);
        // 初始化产品
        productService.initProduct(shopDTO.getShopId());
    }
}
