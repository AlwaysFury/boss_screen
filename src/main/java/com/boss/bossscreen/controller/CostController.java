package com.boss.bossscreen.controller;

import com.boss.bossscreen.dto.ConditionDTO;
import com.boss.bossscreen.dto.CostDTO;
import com.boss.bossscreen.dto.UpdateStatusDTO;
import com.boss.bossscreen.service.impl.CostServiceImpl;
import com.boss.bossscreen.vo.CostVO;
import com.boss.bossscreen.vo.PageResult;
import com.boss.bossscreen.vo.Result;
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
     * 获取账号列表
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
    public Result<?> updateAccountStatus(@Valid @RequestBody UpdateStatusDTO updateStatusDTO) {
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
    public Result<?> saveOrUpdateOrder(@Valid @RequestBody CostDTO costDTO) {
        costService.saveOrUpdateCost(costDTO);
        return Result.ok();
    }

    /**
     * 获取成本类型列表
     * @return
     */
    @GetMapping("/costTypeSelect")
    public Result<List<String>> costTypeSelect() {
        return Result.ok(costService.getCostType());
    }
}
