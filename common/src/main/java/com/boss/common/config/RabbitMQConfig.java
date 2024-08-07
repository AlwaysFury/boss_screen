package com.boss.common.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.boss.common.constant.MQPrefixConst.*;


/**
 * Rabbitmq配置类
 */
@Configuration
public class RabbitMQConfig {

    @Bean
    public Queue productQueue() {
        return new Queue(PRODUCT_QUEUE, true);
    }

    @Bean
    public FanoutExchange productExchange() {
        return new FanoutExchange(PRODUCT_EXCHANGE, true, false);
    }

    @Bean
    public Binding bindingRefreshProductDirect() {
        return BindingBuilder.bind(productQueue()).to(productExchange());
    }

    @Bean
    public Queue orderQueue() {
        return new Queue(ORDER_QUEUE, true);
    }

    @Bean
    public FanoutExchange orderExchange() {
        return new FanoutExchange(ORDER_EXCHANGE, true, false);
    }

    @Bean
    public Binding bindingRefreshOrderDirect() {
        return BindingBuilder.bind(orderQueue()).to(orderExchange());
    }

    @Bean
    public Queue shopeeQueue() {
        return new Queue(SHOPEE_QUEUE, true);
    }

    @Bean
    public FanoutExchange shopeeExchange() {
        return new FanoutExchange(SHOPEE_EXCHANGE, true, false);
    }

    @Bean
    public Binding bindingShopeeDirect() {
        return BindingBuilder.bind(shopeeQueue()).to(shopeeExchange());
    }

}
