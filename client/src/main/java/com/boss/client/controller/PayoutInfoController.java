package com.boss.client.controller;


import com.boss.client.service.impl.PayoutInfoServiceImpl;
import com.boss.client.vo.PayoutInfoVO;
import com.boss.client.vo.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Description
 * @Author 罗宇航
 * @Date 2024/4/16
 */

@RestController
@RequestMapping("/payoutInfo")
@Slf4j
public class  PayoutInfoController {

    @Autowired
    private PayoutInfoServiceImpl payoutInfoService;

    @GetMapping("/getPayoutInfo")
    public Result<PayoutInfoVO> getPayoutInfo(@RequestParam("order_sn") String orderSn) {
        return Result.ok(payoutInfoService.getPayoutInfoBySn(orderSn));
    }
}
