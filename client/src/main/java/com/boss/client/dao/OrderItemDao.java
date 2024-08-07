package com.boss.client.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.boss.client.dto.GradeObject;
import com.boss.common.enities.OrderItem;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;


/**
 * 分类
 */
@Repository
public interface OrderItemDao extends BaseMapper<OrderItem> {


//    Integer shopCount(@Param("condition") ConditionDTO conditionDTO);
//
//
//    List<ShopVO> shopList(@Param("current") Long current, @Param("size") Long size, @Param("condition") ConditionDTO condition);


    Integer countByCreateTimeRange(@Param("item_id") long itemId, @Param("sku_name") String skuName, @Param("start_time") long startTime, @Param("end_time") long endTime, @Param("type") String type);

    List<Long> getProductBySalesCreateTimeRange(@Param("start_time") long startTime, @Param("end_time") long endTime, @Param("minSales") int minSales, @Param("maxSales") int maxSales);

    List<Long> getSkuBySalesCreateTimeRange(@Param("start_time") long startTime, @Param("end_time") long endTime, @Param("minSales") int minSales, @Param("maxSales") int maxSales);

    Integer itemCountByCreateTime(@Param("item_id") long itemId, @Param("time") long time);

    BigDecimal itemMinPrice(@Param("item_id") long itemId);

    Integer salesVolumeByItemId(@Param("item_id") long itemId);

    Integer salesVolumeByModelId(@Param("model_id") long modelId);

    GradeObject skuMinPriceAndCreateTime(@Param("sku_name") String skuName);

}
