package com.boss.bossscreen.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.boss.bossscreen.enities.OrderItem;
import org.springframework.stereotype.Repository;


/**
 * 分类
 */
@Repository
public interface OrderItemDao extends BaseMapper<OrderItem> {


//    Integer shopCount(@Param("condition") ConditionDTO conditionDTO);
//
//
//    List<ShopVO> shopList(@Param("current") Long current, @Param("size") Long size, @Param("condition") ConditionDTO condition);

}
