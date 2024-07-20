package com.boss.task.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.boss.common.enities.OrderStatusPush;
import com.boss.task.dao.OrderStatusPushDao;
import com.boss.task.service.OrderStatusPushService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
public class OrderStatusPushServiceImpl extends ServiceImpl<OrderStatusPushDao, OrderStatusPush> implements OrderStatusPushService {

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void saveOrUpdateOrderStatusPush(OrderStatusPush orderStatusPush) {
        OrderStatusPush existOrderStatusPush = this.getOne(new QueryWrapper<OrderStatusPush>().eq("order_sn", orderStatusPush.getOrderSn()));

        if (Objects.nonNull(existOrderStatusPush)) {
            this.update(orderStatusPush, new UpdateWrapper<OrderStatusPush>().eq("order_sn", orderStatusPush.getOrderSn()));
        } else {
            this.save(orderStatusPush);
        }
    }
}
