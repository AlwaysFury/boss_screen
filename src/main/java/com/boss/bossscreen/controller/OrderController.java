package com.boss.bossscreen.controller;

import com.boss.bossscreen.dto.ConditionDTO;
import com.boss.bossscreen.service.impl.OrderServiceImpl;
import com.boss.bossscreen.vo.OrderEscrowInfoVO;
import com.boss.bossscreen.vo.OrderEscrowVO;
import com.boss.bossscreen.vo.PageResult;
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
 * @Date 2024/4/16
 */

@RestController
@RequestMapping("/order")
@Slf4j
public class OrderController {
    @Autowired
    private OrderServiceImpl orderService;

    /**
     * 获取订单列表
     * @param condition
     * @return
     */
    @GetMapping("/orderList")
    public Result<PageResult<OrderEscrowVO>> orderList(ConditionDTO condition) {

        return Result.ok(orderService.orderListByCondition(condition));
    }

    /**
     * 根据订单 id 获取订单详细
     * @param orderSn
     * @return
     */
    @GetMapping("/info")
    public Result<OrderEscrowInfoVO> getOrderInfo(@RequestParam("order_sn") String orderSn) {

        return Result.ok(orderService.getOrderInfo(orderSn));
    }


}
