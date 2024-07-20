package com.boss.client.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.boss.client.enities.excelEnities.Activity;
import org.springframework.stereotype.Repository;


/**
 * 分类
 */
@Repository
public interface ActivityDao extends BaseMapper<Activity> {


//    Integer costCount(@Param("condition") ConditionDTO condition);
//
//
//    List<CostVO> costList(@Param("current") Long current, @Param("size") Long size, @Param("condition") ConditionDTO condition);

}
