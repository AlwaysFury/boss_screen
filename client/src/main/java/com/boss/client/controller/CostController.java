package com.boss.client.controller;


import com.boss.client.service.impl.CostServiceImpl;
import com.boss.client.vo.CostVO;
import com.boss.client.vo.PageResult;
import com.boss.client.vo.Result;
import com.boss.common.vo.SelectVO;
import com.boss.common.dto.ConditionDTO;
import com.boss.common.dto.CostDTO;
import com.boss.common.dto.UpdateStatusDTO;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @Description
 * @Author 罗宇航
 * @Date 2024/4/16
 */

@RestController
@RequestMapping("/cost")
@Slf4j
public class CostController {

    @Autowired
    private CostServiceImpl costService;

    /**
     * 获取成本列表
     * @param condition
     * @return
     */
    @GetMapping("/costList")
    public Result<PageResult<CostVO>> costList(ConditionDTO condition) {
        return Result.ok(costService.costListByCondition(condition));
    }

    /**
     * 物理删除
     *
     * @param ids 成本 id 列表
     * @return {@link Result<>}
     */
    @PostMapping("/delete")
    public Result<?> updateCostStatus(@Valid @RequestBody UpdateStatusDTO updateStatusDTO) {
        costService.deleteCost(updateStatusDTO.getIdList());
        return Result.ok();
    }

    @GetMapping("/getCostInfo")
    public Result<CostVO> getCostById(@RequestParam("cost_id") int id) {
        return Result.ok(costService.getCostById(id));
    }

    /**
     * 插入或更新
     */
    @PostMapping("/saveOrUpdate")
    public Result<?> saveOrUpdateCost(@Valid @RequestBody CostDTO costDTO) {
        costService.saveOrUpdateCost(costDTO);
        return Result.ok();
    }

    /**
     * 获取成本类型列表
     * @return
     */
    @GetMapping("/costTypeSelect")
    public Result<List<SelectVO>> costTypeSelect() {
        return Result.ok(costService.getCostType());
    }
}
