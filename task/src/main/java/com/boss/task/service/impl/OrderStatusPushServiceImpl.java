package com.boss.task.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.boss.common.enities.OrderStatusPush;
import com.boss.task.dao.OrderStatusPushDao;
import com.boss.task.service.OrderStatusPushService;
import org.springframework.stereotype.Service;

@Service
public class OrderStatusPushServiceImpl extends ServiceImpl<OrderStatusPushDao, OrderStatusPush> implements OrderStatusPushService {

}
