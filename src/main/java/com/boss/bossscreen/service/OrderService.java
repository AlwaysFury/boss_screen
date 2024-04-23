package com.boss.bossscreen.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.boss.bossscreen.dto.ConditionDTO;
import com.boss.bossscreen.enities.Order;
import com.boss.bossscreen.vo.OrderEscrowInfoVO;
import com.boss.bossscreen.vo.OrderEscrowVO;
import com.boss.bossscreen.vo.PageResult;

/**
 * @Description
 * @Author 罗宇航
 * @Date 2024/4/11
 */
public interface OrderService extends IService<Order> {

    void saveOrUpdateOrder(String orderSnStartTime);

    PageResult<OrderEscrowVO> orderListByCondition(ConditionDTO condition);

    OrderEscrowInfoVO getOrderInfo(String orderSn);
}
