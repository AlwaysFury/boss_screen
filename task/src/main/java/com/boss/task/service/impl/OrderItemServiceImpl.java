package com.boss.task.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.boss.common.enities.OrderItem;
import com.boss.task.dao.OrderItemDao;
import com.boss.task.service.OrderItemService;
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
