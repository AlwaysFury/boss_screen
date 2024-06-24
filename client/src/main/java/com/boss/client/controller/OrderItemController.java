package com.boss.client.controller;


import com.boss.client.service.impl.OrderItemServiceImpl;
import com.boss.client.vo.OrderEscrowItemVO;
import com.boss.client.vo.Result;
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
@RequestMapping("/orderItem")
@Slf4j
public class OrderItemController {

    @Autowired
    private OrderItemServiceImpl orderItemService;

    @GetMapping("/costList")
    public Result<List<OrderEscrowItemVO>> costList(@RequestParam("order_sn") String orderSn) {
        return Result.ok(orderItemService.getOrderEscrowItemVOBySn(orderSn));
    }

}
