package com.boss.bossscreen.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.boss.bossscreen.dto.ConditionDTO;
import com.boss.bossscreen.enities.Rule;
import com.boss.bossscreen.vo.RuleVO;
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
