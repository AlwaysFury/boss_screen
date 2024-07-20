package com.boss.client.controller;


import com.boss.client.dto.ConditionDTO;
import com.boss.client.dto.SkuDTO;
import com.boss.client.service.impl.SkuServiceImpl;
import com.boss.client.vo.*;
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
@RequestMapping("/sku")
@Slf4j
public class SkuController {

    @Autowired
    private SkuServiceImpl skuService;

    /**
     * 获取成本列表
     * @param condition
     * @return
     */
    @GetMapping("/skuList")
    public Result<PageResult<SkuVO>> skuList(ConditionDTO condition) {
        return Result.ok(skuService.skuListByCondition(condition));
    }

    /**
     * 物理删除
     *
     * @param ids id 列表
     * @return {@link Result<>}
     */
    @PostMapping("/delete")
    public Result<?> updateCostStatus(@Valid @RequestBody UpdateStatusDTO updateStatusDTO) {
        skuService.deleteSku(updateStatusDTO.getIdList());
        return Result.ok();
    }

    @GetMapping("/getSkuInfo")
    public Result<SkuInfoVO> getSkuById(@RequestParam("sku_id") Long id) {
        return Result.ok(skuService.getSkuById(id));
    }

    /**
     * 插入或更新
     */
    @PostMapping("/save")
    public Result<?> saveOrUpdateCost(@Valid @RequestBody SkuDTO skuDTO) {
        skuService.saveOrUpdateSku(skuDTO);
        return Result.ok();
    }

    /**
     * 获取款号统计
     */
    @GetMapping("/skuStatistics")
    public Result<List<SkuStatisticsVO>> getSkuStatistics(@RequestParam("sku_id") Long id) {
        return Result.ok(skuService.getSkuStatistics(String.valueOf(id)));
    }
}
