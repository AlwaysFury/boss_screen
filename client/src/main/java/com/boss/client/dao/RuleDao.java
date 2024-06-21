package com.boss.client.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.boss.client.vo.RuleVO;
import com.boss.common.dto.ConditionDTO;
import com.boss.common.enities.Rule;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


/**
 * 分类
 */
@Repository
public interface RuleDao extends BaseMapper<Rule> {


    Integer ruleCount(@Param("condition") ConditionDTO condition);


    List<RuleVO> ruleList(@Param("current") Long current, @Param("size") Long size, @Param("condition") ConditionDTO condition);

}
