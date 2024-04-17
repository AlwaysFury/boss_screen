package com.boss.bossscreen.controller;

import com.boss.bossscreen.annotation.OptLog;
import com.boss.bossscreen.dto.ShopAndAccountConditionDTO;
import com.boss.bossscreen.dto.UpdateStatusDTO;
import com.boss.bossscreen.service.impl.MainAccountServiceImpl;
import com.boss.bossscreen.vo.MainAccountVO;
import com.boss.bossscreen.vo.PageResult;
import com.boss.bossscreen.vo.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
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

@Api(tags = "店铺模块")
@RestController
@RequestMapping("/mainAccount")
@Slf4j
public class MainAccountController {

    @Autowired
    private MainAccountServiceImpl mainAccountService;


    @ApiOperation(value = "获取所有账号")
    @GetMapping("/accountsList")
    public Result<PageResult<MainAccountVO>> accountsList(ShopAndAccountConditionDTO condition) {
        return Result.ok(mainAccountService.accountsListByCondition(condition));
    }

    /**
     * 删除账号（逻辑）0 / 冻结 2/ 激活 1
     *
     * @param updateStatusDTO 店铺id列表
     * @return {@link Result<>}
     */
    @OptLog(optType = REMOVE)
    @ApiOperation(value = "删除店铺（逻辑）0 / 冻结 2/ 激活 1")
    @DeleteMapping("/updateAccountStatus")
    public Result<?> updateAccountStatus(@Valid @RequestBody UpdateStatusDTO updateStatusDTO) {
        mainAccountService.updateAccountsStatus(updateStatusDTO);
        return Result.ok();
    }
}
