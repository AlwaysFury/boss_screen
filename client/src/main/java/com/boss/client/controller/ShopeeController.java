package com.boss.client.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.boss.client.service.WebSocket;
import com.boss.client.service.impl.MainAccountServiceImpl;
import com.boss.client.service.impl.ShopServiceImpl;
import com.boss.client.util.ShopeeUtil;
import com.boss.client.vo.Result;
import com.boss.common.dto.ShopDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static com.boss.common.constant.MQPrefixConst.SHOP_EXCHANGE;

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
    private RabbitTemplate rabbitTemplate;

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

            log.info("开始向mq推送授权消息");
            try {
                rabbitTemplate.convertAndSend(SHOP_EXCHANGE, "*", new Message(JSON.toJSONBytes(shopDTO), new MessageProperties()));
            } catch (Exception e) {
                log.error("向mq推送消息失败：" + e);
            }
            log.info("推送结束");

            WebSocket.pushResult(userId, Result.ok());

//            // 初始化产品信息
//            productService.initProduct(shopId);
//            // 初始化当天的订单
//            orderService.initOrder(shopId);

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
