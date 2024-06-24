package com.boss.task.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.boss.common.enities.Order;

import java.util.List;

/**
 * @Description
 * @Author 罗宇航
 * @Date 2024/4/11
 */
public interface OrderService extends IService<Order> {

    void refreshOrderByTimeStr(long startTime, long endTime);

    void refreshOrder(List<String> sns);

    void refreshOrderByStatus(String... status);

    void refreshNewOrder();

    void initOrder(long shopId);
}
