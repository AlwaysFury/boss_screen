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
public enum OrderStatusEnum {

    UNPAID("UNPAID", "订单已创建，买家尚未付款。"),
    READY_TO_SHIP("READY_TO_SHIP", "卖家可以安排发货。"),
    PROCESSED("PROCESSED", "卖家已在线安排发货并从 3PL 获得跟踪号。"),
    RETRY_SHIP("RETRY_SHIP", "3PL 取件包裹失败。需要重新安排发货。"),
    SHIPPED("SHIPPED", "包裹已投递至 3PL 或由 3PL 取件。"),
    TO_CONFIRM_RECEIVE("TO_CONFIRM_RECEIVE", "买家已收到订单。"),
    COMPLETED("COMPLETED", "已完成"),
    IN_CANCEL("IN_CANCEL", "订单的取消正在处理中。"),
    CANCELLED("CANCELLED", "已取消"),
    TO_RETURN("TO_RETURN", "买家要求退货，订单退货正在处理中。");

    private String code;
    private String desc;

    public static String getDescByCode(String code) {
        for (OrderStatusEnum statusEnum : OrderStatusEnum.values()) {
            if (statusEnum.getCode().equals(code)) {
                return statusEnum.getDesc();
            }
        }
        return null;
    }

    public static List<SelectVO> getOrderStatusEnum() {
        List<SelectVO> list = new ArrayList<>();
        for (OrderStatusEnum statusEnum : OrderStatusEnum.values()) {
            SelectVO vo = SelectVO.builder()
                    .key(statusEnum.getCode())
                    .value(statusEnum.getDesc()).build();
            list.add(vo);
        }
        return list;
    }
}
