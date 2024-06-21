package com.boss.client.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.boss.client.dao.OrderItemDao;
import com.boss.client.service.OrderItemService;
import com.boss.common.enities.OrderItem;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @Description
 * @Author 罗宇航
 * @Date 2024/4/11
 */

@Service
@Slf4j
public class OrderItemServiceImpl extends ServiceImpl<OrderItemDao, OrderItem> implements OrderItemService {

}
