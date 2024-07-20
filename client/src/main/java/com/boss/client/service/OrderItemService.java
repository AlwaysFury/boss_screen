package com.boss.client.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.boss.common.enities.OrderItem;

import java.util.Date;
import java.util.Map;


/**
 * @Description
 * @Author 罗宇航
 * @Date 2024/4/11
 */
public interface OrderItemService extends IService<OrderItem> {

    Map<String, Object> getOrderEscrowItemVOBySn(String orderSn);

    int countByCreateTimeRange(Date nowDate, int offsetDays, Long itemId, String skuName, String type);
}
