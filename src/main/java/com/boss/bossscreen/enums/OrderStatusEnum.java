package com.boss.bossscreen.enums;

import com.boss.bossscreen.vo.SelectVO;
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


    UNPAID("UNPAID", "未支付"),
    READY_TO_SHIP("READY_TO_SHIP", "待出货"),
    PROCESSED("PROCESSED", "已处理"),
    SHIPPED("SHIPPED", "运送中"),
    COMPLETED("COMPLETED", "已完成"),
    IN_CANCEL("IN_CANCEL", "取消中"),
    CANCELLED("CANCELLED", "已取消"),
    INVOICE_PENDING("INVOICE_PENDING", "等待退款");

    private String code;
    private String desc;

    public static String getDescByCode(String code) {
        for (ProductStatusEnum statusEnum : ProductStatusEnum.values()) {
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
