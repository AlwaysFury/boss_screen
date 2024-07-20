package com.boss.client.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.boss.client.dto.ConditionDTO;
import com.boss.client.dto.RuleDTO;
import com.boss.client.enities.Rule;
import com.boss.client.vo.PageResult;
import com.boss.client.vo.RuleInfoVO;
import com.boss.client.vo.RuleVO;

import java.util.List;

/**
 * @Description
 * @Author 罗宇航
 * @Date 2024/4/10
 */
public interface RuleService extends IService<Rule>  {

    void saveOrUpdateRule(RuleDTO ruleDTO);

    void deleteRule(List<Long> ids);

    PageResult<RuleVO> ruleListByCondition(ConditionDTO condition);

    RuleInfoVO getRuleById(int id);

    List<Rule> getRuleList(String type);

//    String getGrade(Product product, int salesVolume);
}
