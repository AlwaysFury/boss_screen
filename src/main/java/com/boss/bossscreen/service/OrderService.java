package com.boss.bossscreen.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.boss.bossscreen.enities.Order;

/**
 * @Description
 * @Author 罗宇航
 * @Date 2024/4/11
 */
public interface OrderService extends IService<Order> {

    void saveOrUpdateOrder();

//    PageResult<MainAccountVO> accountsListByCondition(ConditionDTO conditionDTO);
//
//    void updateAccountsStatus(UpdateStatusDTO updateStatusDTO);

//    void refreshAccountToken();
}
