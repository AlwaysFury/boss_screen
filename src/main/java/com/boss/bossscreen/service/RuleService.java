package com.boss.bossscreen.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.boss.bossscreen.dto.ConditionDTO;
import com.boss.bossscreen.dto.RuleDTO;
import com.boss.bossscreen.enities.Rule;
import com.boss.bossscreen.vo.PageResult;
import com.boss.bossscreen.vo.RuleInfoVO;
import com.boss.bossscreen.vo.RuleVO;

import java.util.List;

/**
 * @Description
 * @Author 罗宇航
 * @Date 2024/4/10
 */
public interface RuleService extends IService<Rule>  {

    void saveOrUpdateRule(RuleDTO ruleDTO);

    void deleteRule(List<Integer> ids);

    PageResult<RuleVO> ruleListByCondition(ConditionDTO condition);

    RuleInfoVO getRuleById(int id);
}
