package com.boss.bossscreen.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.boss.bossscreen.dao.OrderItemDao;
import com.boss.bossscreen.enities.OrderItem;
import com.boss.bossscreen.service.OrderItemService;
import com.boss.bossscreen.util.BeanCopyUtils;
import com.boss.bossscreen.vo.OrderEscrowItemVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @Description
 * @Author 罗宇航
 * @Date 2024/4/11
 */

@Service
@Slf4j
public class OrderItemServiceImpl extends ServiceImpl<OrderItemDao, OrderItem> implements OrderItemService {

    @Autowired
    private OrderItemDao orderItemDao;

    @Override
    public List<OrderEscrowItemVO> getOrderItemVOListByOrderSn(String orderSn) {
        List<OrderEscrowItemVO> orderEscrowItemVOList = orderItemDao.selectList(new QueryWrapper<OrderItem>().eq("order_sn", orderSn))
                .stream().map(orderItem ->

                        // todo 成本，利润，利润率计算
                        // 利润=平台钱 - 成本


                        BeanCopyUtils.copyObject(orderItem, OrderEscrowItemVO.class)
                ).collect(Collectors.toList());
        return orderEscrowItemVOList;
    }
}
