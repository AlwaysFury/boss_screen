package com.boss.bossscreen.controller;

import com.boss.bossscreen.annotation.OptLog;
import com.boss.bossscreen.dto.ConditionDTO;
import com.boss.bossscreen.dto.UpdateStatusDTO;
import com.boss.bossscreen.service.impl.ShopServiceImpl;
import com.boss.bossscreen.vo.PageResult;
import com.boss.bossscreen.vo.Result;
import com.boss.bossscreen.vo.ShopVO;
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
@RequestMapping("/shop")
@Slf4j
public class ShopController {
    @Autowired
    private ShopServiceImpl shopService;

    /**
     * 获取店铺列表
     * @param condition
     * @return
     */
    @GetMapping("/shopList")
    public Result<PageResult<ShopVO>> shopsList(ConditionDTO condition) {
        return Result.ok(shopService.shopsListByCondition(condition));
    }

    /**
     * 更新店铺状态 逻辑删除 0 / 冻结 2/ 激活 1
     * @param updateStatusDTO
     * @return
     */
    @OptLog(optType = REMOVE)
    @PostMapping("/updateShopStatus")
    public Result<?> updateShopsStatus(@Valid @RequestBody UpdateStatusDTO updateStatusDTO) {
        shopService.updateShopsStatus(updateStatusDTO);
        return Result.ok();
    }
}
