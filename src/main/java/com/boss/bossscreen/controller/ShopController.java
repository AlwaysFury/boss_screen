package com.boss.bossscreen.controller;

import com.boss.bossscreen.annotation.OptLog;
import com.boss.bossscreen.dto.ShopAndAccountConditionDTO;
import com.boss.bossscreen.dto.UpdateStatusDTO;
import com.boss.bossscreen.service.impl.ShopServiceImpl;
import com.boss.bossscreen.vo.PageResult;
import com.boss.bossscreen.vo.Result;
import com.boss.bossscreen.vo.ShopVO;
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
@RequestMapping("/shop")
@Slf4j
public class ShopController {
    @Autowired
    private ShopServiceImpl shopService;

    @ApiOperation(value = "获取所有店铺")
    @GetMapping("/shopsList")
    public Result<PageResult<ShopVO>> shopsList(ShopAndAccountConditionDTO condition) {
        return Result.ok(shopService.shopsListByCondition(condition));
    }

    /**
     * 删除店铺（逻辑）0 / 冻结 2/ 激活 1
     *
     * @param updateStatusDTO 店铺id列表
     * @return {@link Result<>}
     */
    @OptLog(optType = REMOVE)
    @ApiOperation(value = "删除店铺（逻辑）0 / 冻结 2/ 激活 1")
    @DeleteMapping("/updateShopsStatus")
    public Result<?> updateShopsStatus(@Valid @RequestBody UpdateStatusDTO updateStatusDTO) {
        shopService.updateShopsStatus(updateStatusDTO);
        return Result.ok();
    }
}
