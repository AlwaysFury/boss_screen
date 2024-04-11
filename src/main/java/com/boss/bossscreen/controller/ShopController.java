package com.boss.bossscreen.controller;

import com.alibaba.fastjson2.JSONObject;
import com.boss.bossscreen.annotation.OptLog;
import com.boss.bossscreen.dto.ShopDTO;
import com.boss.bossscreen.service.impl.ShopServiceImpl;
import com.boss.bossscreen.util.ShopeeUtil;
import com.boss.bossscreen.vo.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import jakarta.validation.Valid;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import static com.boss.bossscreen.constant.OptTypeConst.REMOVE;

/**
 * @Description
 * @Author 罗宇航
 * @Date 2024/4/9
 */

@Api(tags = "店铺模块")
@RestController
@RequestMapping("/shops")
@Log4j2
public class ShopController {

    @Autowired
    private ShopServiceImpl shopService;

    @ApiOperation("获取授权链接")
    @GetMapping("/getAuthUrl")
    public Result<String> getAuthUrl() {
        return Result.ok(ShopeeUtil.getAuthUrl());
    }

    @ApiOperation("获取 token")
    @GetMapping("/save")
    public Result<JSONObject> save(String code, @RequestParam("shop_id") long shopId) {

        try {
            System.out.println(code);
            System.out.println(shopId);

            // 获取access_token
            JSONObject object = ShopeeUtil.getShopAccessToken(code, ShopAuthVO.getPartnerId(), "62484d6c546c7a474c53456b646154464d4b736f6c79437266564c4d6346756b", shopId);
            System.out.println("首次获取 token 结果：" + object);

            // 入库
            ShopVO shopVO = new ShopVO();
            shopVO.setShopId(shopId);
            shopVO.setStatus(1);
            shopVO.setAuthCode(code);
            shopVO.setAccessToken(object.getString("access_token"));
            shopVO.setRefreshToken(object.getString("refresh_token"));

            shopService.saveOrUpdateToken(shopVO);

        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail("授权失败：" + e);
        }

        return Result.ok();
    }

    @ApiOperation(value = "获取所有店铺")
    @GetMapping("/shopsList")
    public Result<PageResult<ShopDTO>> shopsList(ConditionVO conditionVO) {
        return Result.ok(shopService.shopsListByCondition(conditionVO));
    }

    /**
     * 删除店铺（逻辑）0 / 冻结 2/ 激活 1
     *
     * @param updateStatusVO 店铺id列表
     * @return {@link Result<>}
     */
    @OptLog(optType = REMOVE)
    @ApiOperation(value = "删除店铺（逻辑）0 / 冻结 2/ 激活 1")
    @DeleteMapping("/updateShopsStatus")
    public Result<?> updateShopsStatus(@Valid @RequestBody UpdateStatusVO updateStatusVO) {
        shopService.updateShopsStatus(updateStatusVO);
        return Result.ok();
    }

}
