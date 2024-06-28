package com.boss.client.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.boss.client.vo.CostVO;
import com.boss.client.dto.ConditionDTO;
import com.boss.client.enities.Cost;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


/**
 * 分类
 */
@Repository
public interface CostDao extends BaseMapper<Cost> {


    Integer costCount(@Param("condition") ConditionDTO condition);


    List<CostVO> costList(@Param("current") Long current, @Param("size") Long size, @Param("condition") ConditionDTO condition);

}
