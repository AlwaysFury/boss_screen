package com.boss.client.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.boss.client.enities.excelEnities.Ads;
import org.springframework.stereotype.Repository;


/**
 * 分类
 */
@Repository
public interface AdsDao extends BaseMapper<Ads> {


//    Integer costCount(@Param("condition") ConditionDTO condition);
//
//
//    List<CostVO> costList(@Param("current") Long current, @Param("size") Long size, @Param("condition") ConditionDTO condition);

}
