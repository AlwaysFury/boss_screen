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
public enum TagTypeEnum {

    PHOTO("PHOTO", "图案"),
    ITEM("ITEM", "链接");

    private String code;
    private String desc;

    public static String getDescByCode(String code) {
        for (TagTypeEnum statusEnum : TagTypeEnum.values()) {
            if (statusEnum.getCode().equals(code)) {
                return statusEnum.getDesc();
            }
        }
        return null;
    }

    public static List<SelectVO> getTagTypeEnum() {
        List<SelectVO> list = new ArrayList<>();
        for (TagTypeEnum statusEnum : TagTypeEnum.values()) {
            SelectVO vo = SelectVO.builder()
                    .key(statusEnum.getCode())
                    .value(statusEnum.getDesc()).build();
            list.add(vo);
        }
        return list;
    }
}
