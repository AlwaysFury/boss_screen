package com.boss.bossscreen.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.boss.bossscreen.enities.OrderItem;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;


/**
 * 分类
 */
@Repository
public interface OrderItemDao extends BaseMapper<OrderItem> {


//    Integer shopCount(@Param("condition") ConditionDTO conditionDTO);
//
//
//    List<ShopVO> shopList(@Param("current") Long current, @Param("size") Long size, @Param("condition") ConditionDTO condition);


    Integer itemCountByCreateTimeRange(@Param("item_id") long itemId, @Param("start_time") long startTime, @Param("end_time") long endTime);

    Integer itemCountByCreateTime(@Param("item_id") long itemId, @Param("time") long time);

    BigDecimal itemMinPrice(@Param("item_id") long itemId);

    Integer salesVolumeByItemId(@Param("item_id") long itemId);

    Integer salesVolumeByModelId(@Param("model_id") long modelId);

}
