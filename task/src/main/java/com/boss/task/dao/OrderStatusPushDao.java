
package com.boss.task.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.boss.common.enities.OrderStatusPush;
import org.springframework.stereotype.Repository;


/**
 * 分类
 */
@Repository
public interface OrderStatusPushDao extends BaseMapper<OrderStatusPush> {

    String maxTimeStatus(String orderSn);
}
