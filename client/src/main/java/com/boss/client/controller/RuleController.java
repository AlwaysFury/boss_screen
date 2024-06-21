package com.boss.client.controller;


import com.boss.client.service.impl.RuleServiceImpl;
import com.boss.client.vo.PageResult;
import com.boss.client.vo.Result;
import com.boss.client.vo.RuleInfoVO;
import com.boss.client.vo.RuleVO;
import com.boss.common.dto.ConditionDTO;
import com.boss.common.dto.RuleDTO;
import com.boss.common.dto.UpdateStatusDTO;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @Description
 * @Author 罗宇航
 * @Date 2024/4/16
 */

@RestController
@RequestMapping("/rule")
@Slf4j
public class RuleController {

    @Autowired
    private RuleServiceImpl ruleService;

    /**
     * 获取成本列表
     * @param condition
     * @return
     */
    @GetMapping("/ruleList")
    public Result<PageResult<RuleVO>> ruleList(ConditionDTO condition) {
        return Result.ok(ruleService.ruleListByCondition(condition));
    }

    /**
     * 物理删除
     */
    @PostMapping("/delete")
    public Result<?> updateRuleStatus(@Valid @RequestBody UpdateStatusDTO updateStatusDTO) {
        ruleService.deleteRule(updateStatusDTO.getIdList());
        return Result.ok();
    }

    @GetMapping("/getRuleInfo")
    public Result<RuleInfoVO> getCostById(@RequestParam("rule_id") int id) {
        return Result.ok(ruleService.getRuleById(id));
    }

    /**
     * 插入或更新
     */
    @PostMapping("/saveOrUpdate")
    public Result<?> saveOrUpdateRule(@Valid @RequestBody RuleDTO ruleDTO) {
        ruleService.saveOrUpdateRule(ruleDTO);
        return Result.ok();
    }
}
