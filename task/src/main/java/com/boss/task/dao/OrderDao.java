package com.boss.task.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.boss.common.enities.Order;
import org.springframework.stereotype.Repository;

import java.util.List;


/**
 * 分类
 */
@Repository
public interface OrderDao extends BaseMapper<Order> {


    List<Order> maxTimeList();

    List<String> getNonEscrowList(long shopId);
}
