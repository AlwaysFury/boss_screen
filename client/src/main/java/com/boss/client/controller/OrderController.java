package com.boss.client.controller;


import com.boss.client.service.impl.OrderServiceImpl;
import com.boss.client.vo.OrderEscrowInfoVO;
import com.boss.client.vo.OrderEscrowVO;
import com.boss.client.vo.PageResult;
import com.boss.client.vo.Result;
import com.boss.common.dto.ConditionDTO;
import com.boss.common.vo.SelectVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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

    /**
     * 获取状态
     */
    @GetMapping("/statusSelect")
    public Result<List<SelectVO>> getStatusSelect() {
        return Result.ok(orderService.getStatusSelect());
    }


}
