package com.boss.client.controller;


import com.boss.client.service.impl.MainAccountServiceImpl;
import com.boss.client.vo.MainAccountVO;
import com.boss.client.vo.PageResult;
import com.boss.client.vo.Result;
import com.boss.common.dto.ConditionDTO;
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
@RequestMapping("/mainAccount")
@Slf4j
public class MainAccountController {

    @Autowired
    private MainAccountServiceImpl mainAccountService;

    /**
     * 获取账号列表
     * @param condition
     * @return
     */
    @GetMapping("/accountList")
    public Result<PageResult<MainAccountVO>> accountList(ConditionDTO condition) {
        return Result.ok(mainAccountService.accountsListByCondition(condition));
    }

    /**
     * 更新账号状态（逻辑）0 / 冻结 2/ 激活 1
     *
     * @param updateStatusDTO 店铺id列表
     * @return {@link Result<>}
     */
    @PostMapping("/updateAccountStatus")
    public Result<?> updateAccountStatus(@Valid @RequestBody UpdateStatusDTO updateStatusDTO) {
        mainAccountService.updateAccountsStatus(updateStatusDTO);
        return Result.ok();
    }
}
