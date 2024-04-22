package com.boss.bossscreen.controller;

import com.boss.bossscreen.dto.ConditionDTO;
import com.boss.bossscreen.dto.OperationLogDTO;
import com.boss.bossscreen.dto.UpdateStatusDTO;
import com.boss.bossscreen.service.impl.OperationLogServiceImpl;
import com.boss.bossscreen.vo.OperationLogVO;
import com.boss.bossscreen.vo.PageResult;
import com.boss.bossscreen.vo.Result;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @Description
 * @Author 罗宇航
 * @Date 2024/4/21
 */

@RestController
@RequestMapping("/product")
@Slf4j
public class OperationLogController {

    @Autowired
    private OperationLogServiceImpl operationLogService;


    /**
     * 查询日志列表
     * @param condition
     * @return
     */
    @GetMapping("/optLogList")
    public Result<PageResult<OperationLogVO>> optLogList(ConditionDTO condition) {

        return Result.ok(operationLogService.optLogList(condition));
    }

    /**
     * 保存或更新日志
     * @return
     */
    @PutMapping("/saveOrUpdateLog")
    public Result<?> saveOrUpdateLog(@Valid @RequestBody OperationLogDTO operationLogDTO) {
        operationLogService.saveOrUpdateLog(operationLogDTO);
        return Result.ok();
    }

    /**
     * 批量删除日志
     * @param updateStatusDTO
     * @return
     */
    @DeleteMapping("/deleteLog")
    public Result<?> deleteLog(@Valid @RequestBody UpdateStatusDTO updateStatusDTO) {
        operationLogService.updateLogStatus(updateStatusDTO);
        return Result.ok();
    }
}
