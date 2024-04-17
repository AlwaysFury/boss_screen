package com.boss.bossscreen.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.boss.bossscreen.enities.Product;

/**
 * @Description
 * @Author 罗宇航
 * @Date 2024/4/11
 */
public interface ProductService extends IService<Product> {

    void saveOrUpdateProduct();

//    PageResult<MainAccountVO> accountsListByCondition(ConditionDTO conditionDTO);
//
//    void updateAccountsStatus(UpdateStatusDTO updateStatusDTO);

//    void refreshAccountToken();
}
