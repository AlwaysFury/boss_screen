package com.boss.client.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.boss.common.enities.ReturnOrder;

/**
 * @Description
 * @Author 罗宇航
 * @Date 2024/4/10
 */
public interface ReturnOrderService extends IService<ReturnOrder> {

    void saveOrUpdateReturnOrder();
//
//    void deleteCost(List<Integer> ids);
//
//    PageResult<CostVO> costListByCondition(ConditionDTO condition);
}
