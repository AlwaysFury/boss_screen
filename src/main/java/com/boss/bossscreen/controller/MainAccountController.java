package com.boss.bossscreen.controller;

import com.boss.bossscreen.annotation.OptLog;
import com.boss.bossscreen.dto.ConditionDTO;
import com.boss.bossscreen.dto.UpdateStatusDTO;
import com.boss.bossscreen.service.impl.MainAccountServiceImpl;
import com.boss.bossscreen.vo.MainAccountVO;
import com.boss.bossscreen.vo.PageResult;
import com.boss.bossscreen.vo.Result;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import static com.boss.bossscreen.constant.OptTypeConst.REMOVE;

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
    @OptLog(optType = REMOVE)
    @PutMapping("/updateAccountStatus")
    public Result<?> updateAccountStatus(@Valid @RequestBody UpdateStatusDTO updateStatusDTO) {
        mainAccountService.updateAccountsStatus(updateStatusDTO);
        return Result.ok();
    }
}
