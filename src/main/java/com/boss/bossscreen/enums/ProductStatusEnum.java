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
public enum ProductStatusEnum {

    NORMAL("NORMAL", "已上架"),
    BANNED("BANNED", "禁止"),
    UNLIST("UNLIST", "未上架"),
    SELLER_DELETE("SELLER_DELETE", "卖家删除"),
    SHOPEE_DELETE("SHOPEE_DELETE", "平台删除"),
    REVIEWING("REVIEWING", "审查中");

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

    public static List<SelectVO> getProductStatusEnum() {
        List<SelectVO> list = new ArrayList<>();
        for (ProductStatusEnum statusEnum : ProductStatusEnum.values()) {
            SelectVO vo = SelectVO.builder()
                    .key(statusEnum.getCode())
                    .value(statusEnum.getDesc()).build();
            list.add(vo);
        }
        return list;
    }
}
