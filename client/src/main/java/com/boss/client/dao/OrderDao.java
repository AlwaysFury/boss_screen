package com.boss.client.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.boss.client.vo.OrderEscrowVO;
import com.boss.client.dto.ConditionDTO;
import com.boss.common.enities.Order;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


/**
 * 分类
 */
@Repository
public interface OrderDao extends BaseMapper<Order> {


    Integer orderCount(@Param("condition") ConditionDTO condition);

    List<OrderEscrowVO> orderList(@Param("current") Long current, @Param("size") Long size, @Param("condition") ConditionDTO condition);

    List<Order> maxTimeList();

    List<String> getNonEscrowList(long shopId);
}
