package com.boss.bossscreen.controller;

import com.alibaba.fastjson2.JSONObject;
import com.boss.bossscreen.annotation.OptLog;
import com.boss.bossscreen.dto.ConditionDTO;
import com.boss.bossscreen.dto.ShopDTO;
import com.boss.bossscreen.dto.UpdateStatusDTO;
import com.boss.bossscreen.service.impl.MainAccountServiceImpl;
import com.boss.bossscreen.service.impl.ShopServiceImpl;
import com.boss.bossscreen.util.ShopeeUtil;
import com.boss.bossscreen.vo.MainAccountVO;
import com.boss.bossscreen.vo.PageResult;
import com.boss.bossscreen.vo.Result;
import com.boss.bossscreen.vo.ShopVO;
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
@RequestMapping("/shopee")
@Log4j2
public class ShopeeController {

    @Autowired
    private ShopServiceImpl shopService;

    @Autowired
    private MainAccountServiceImpl mainAccountService;

    @ApiOperation("获取授权链接")
    @GetMapping("/getAuthUrl")
    public Result<String> getAuthUrl(String type) {
        return Result.ok(ShopeeUtil.getAuthUrl(type));
    }

    @ApiOperation("获取店铺 token")
    @GetMapping("/saveShopToken")
    public Result<JSONObject> saveShopToken(String code, @RequestParam("shop_id") long shopId) {

        try {
            // 获取access_token
            JSONObject object = ShopeeUtil.getShopAccessToken(code, shopId);
            System.out.println("首次获取 token 结果：" + object);

            // 入库
            ShopDTO shopDTO = new ShopDTO();
            shopDTO.setShopId(shopId);
            shopDTO.setStatus(1);
            shopDTO.setAuthCode(code);
            shopDTO.setAccessToken(object.getString("access_token"));
            shopDTO.setRefreshToken(object.getString("refresh_token"));

            shopService.saveOrUpdateToken(shopDTO);

        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail("授权失败：" + e);
        }

        return Result.ok();
    }

    @ApiOperation("获取账号 token")
    @GetMapping("/saveAccountToken")
    public Result<JSONObject> saveAccountToken(String code, @RequestParam("main_account_id") long mainAccountId) {

        try {

            mainAccountService.saveOrUpdateToken(code, mainAccountId);

        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail("授权失败：" + e);
        }

        return Result.ok();
    }

    @ApiOperation(value = "获取所有店铺")
    @GetMapping("/shopsList")
    public Result<PageResult<ShopVO>> shopsList(ConditionDTO conditionDTO) {
        return Result.ok(shopService.shopsListByCondition(conditionDTO));
    }

    @ApiOperation(value = "获取所有账号")
    @GetMapping("/accountsList")
    public Result<PageResult<MainAccountVO>> accountsList(ConditionDTO conditionDTO) {
        return Result.ok(mainAccountService.accountsListByCondition(conditionDTO));
    }

    /**
     * 删除店铺（逻辑）0 / 冻结 2/ 激活 1
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
