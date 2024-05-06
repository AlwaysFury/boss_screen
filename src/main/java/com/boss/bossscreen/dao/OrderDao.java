package com.boss.bossscreen.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.boss.bossscreen.dto.ConditionDTO;
import com.boss.bossscreen.enities.Order;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


/**
 * 分类
 */
@Repository
public interface OrderDao extends BaseMapper<Order> {


    Integer orderCount(@Param("condition") ConditionDTO condition);


    List<Order> orderList(@Param("current") Long current, @Param("size") Long size, @Param("condition") ConditionDTO condition);

}
