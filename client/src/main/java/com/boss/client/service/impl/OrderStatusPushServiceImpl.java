package com.boss.client.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.boss.client.dao.OrderStatusPushDao;
import com.boss.client.service.OrderStatusPushService;
import com.boss.common.enities.OrderStatusPush;
import org.springframework.stereotype.Service;

/**
 * 操作日志服务
 */
@Service
public class OrderStatusPushServiceImpl extends ServiceImpl<OrderStatusPushDao, OrderStatusPush> implements OrderStatusPushService {

}
