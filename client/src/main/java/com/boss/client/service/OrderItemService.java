package com.boss.client.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.boss.client.vo.OrderEscrowItemVO;
import com.boss.common.enities.OrderItem;

import java.util.List;


/**
 * @Description
 * @Author 罗宇航
 * @Date 2024/4/11
 */
public interface OrderItemService extends IService<OrderItem> {

    List<OrderEscrowItemVO> getOrderEscrowItemVOBySn(String orderSn);
}
