package com.boss.common.enums;


import com.boss.common.vo.SelectVO;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * @Description
 * @Author 罗宇航
 * @Date 2024/6/11
 */
@Getter
@AllArgsConstructor
public enum LogisticsStatusEnum {

    LOGISTICS_NOT_STARTED("LOGISTICS_NOT_STARTED", "初始状态，订单未准备好履行"),
    LOGISTICS_REQUEST_CREATED("LOGISTICS_REQUEST_CREATED", "订单安排发货"),
    LOGISTICS_PICKUP_DONE("LOGISTICS_PICKUP_DONE", "订单移交给第三方物流"),
    LOGISTICS_PICKUP_RETRY("LOGISTICS_PICKUP_RETRY", "订购待处理的 3PL 重试取件"),
    LOGISTICS_PICKUP_FAILED("LOGISTICS_PICKUP_FAILED", "由于取货失败或取货但无法继续送货而被 3PL 取消订单"),
    LOGISTICS_DELIVERY_DONE("LOGISTICS_DELIVERY_DONE", "订单成功交付"),
    LOGISTICS_DELIVERY_FAILED("LOGISTICS_DELIVERY_FAILED", "由于3PL交付失败，订单被取消"),
    LOGISTICS_REQUEST_CANCELED("LOGISTICS_REQUEST_CANCELED", "在订单安排发货点下单时取消订单"),
    LOGISTICS_COD_REJECTED("LOGISTICS_COD_REJECTED", "综合物流货到付款：货到付款订单被拒绝"),
    LOGISTICS_READY("LOGISTICS_READY", "订单准备履行 从付款角度看：非货到付款：订单已付货到付款：订单通过货到付款筛选"),
    LOGISTICS_INVALID("LOGISTICS_INVALID", "在LOGISTICS_READY点下单时取消订单"),
    LOGISTICS_LOST("LOGISTICS_LOST", "由于3PL丢失订单而取消订单"),
    LOGISTICS_PENDING_ARRANGE("LOGISTICS_PICKUP_DONE", "订单物流待定安排");

    private String code;
    private String desc;

    public static String getDescByCode(String code) {
        for (LogisticsStatusEnum statusEnum : LogisticsStatusEnum.values()) {
            if (statusEnum.getCode().equals(code)) {
                return statusEnum.getDesc();
            }
        }
        return null;
    }

    public static List<SelectVO> getOrderStatusEnum() {
        List<SelectVO> list = new ArrayList<>();
        for (LogisticsStatusEnum statusEnum : LogisticsStatusEnum.values()) {
            SelectVO vo = SelectVO.builder()
                    .key(statusEnum.getCode())
                    .value(statusEnum.getDesc()).build();
            list.add(vo);
        }
        return list;
    }
}
