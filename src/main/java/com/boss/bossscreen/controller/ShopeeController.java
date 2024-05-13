package com.boss.bossscreen.controller;

import com.alibaba.fastjson.JSONObject;
import com.boss.bossscreen.dto.ShopDTO;
import com.boss.bossscreen.service.WebSocket;
import com.boss.bossscreen.service.impl.MainAccountServiceImpl;
import com.boss.bossscreen.service.impl.ProductServiceImpl;
import com.boss.bossscreen.service.impl.ShopServiceImpl;
import com.boss.bossscreen.util.ShopeeUtil;
import com.boss.bossscreen.vo.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Description
 * @Author 罗宇航
 * @Date 2024/4/9
 */

@RestController
@RequestMapping("/shopee")
@Slf4j
public class ShopeeController {

    @Autowired
    private ShopServiceImpl shopService;

    @Autowired
    private MainAccountServiceImpl mainAccountService;

    @Autowired
    private ProductServiceImpl productService;

    /**
     * 获取授权链接
     */
    @GetMapping("/getAuthUrl")
    public Result<String> getAuthUrl(String type, String userId) {
        log.info("type======>{}", type);
        log.info("userId======>{}", userId);
        return Result.ok(ShopeeUtil.getAuthUrl(type, userId));
    }

    /**
     * 获取店铺 token
     * @param code
     * @param shopId
     * @return
     */
    @GetMapping("/saveOrUpdateShop")
    public Result<JSONObject> saveOrUpdateShop(String code, @RequestParam("shop_id") long shopId, String userId) {

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

            WebSocket.pushResult(userId, Result.ok());

        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail("授权失败：" + e);
        }

        return Result.ok();
    }

    /**
     * 获取账号 token
     * @param code
     * @param mainAccountId
     * @return
     */
    @GetMapping("/saveOrUpdateAccount")
    public Result<JSONObject> saveOrUpdateAccount(String code, @RequestParam("main_account_id") long mainAccountId, String userId) {

        try {

            mainAccountService.saveOrUpdateToken(code, mainAccountId);
            WebSocket.pushResult(userId, Result.ok());
        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail("授权失败：" + e);
        }

        return Result.ok();
    }


}
