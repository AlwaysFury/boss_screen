package com.boss.client.controller;


import com.boss.client.service.impl.TrackingInfoServiceImpl;
import com.boss.client.vo.Result;
import com.boss.client.vo.TrackingInfoVO;
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
@RequestMapping("/trackingInfo")
@Slf4j
public class TrackingInfoController {

    @Autowired
    private TrackingInfoServiceImpl trackingInfoService;

    @GetMapping("/getTrackingInfo")
    public Result<TrackingInfoVO> getTrackingInfo(@RequestParam("order_sn") String orderSn) {
        return Result.ok(trackingInfoService.getTrackInfoBySn(orderSn));
    }
}
