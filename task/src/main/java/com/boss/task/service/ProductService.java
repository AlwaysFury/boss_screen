package com.boss.task.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.boss.common.enities.Product;

/**
 * @Description
 * @Author 罗宇航
 * @Date 2024/4/11
 */
public interface ProductService extends IService<Product> {

    void refreshProductByStatus(String status);

    void refreshProductByTime(String startTimeStr, String endTimeStr);

    void refreshDeletedProduct();

    void initProduct(long shopId);

    void updateStatusByItemId(Long itemId, String status);
}
