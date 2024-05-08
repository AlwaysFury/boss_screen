package com.boss.bossscreen.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Description
 * @Author 罗宇航
 * @Date 2024/5/8
 */

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SelectVO {

    private Object key;

    private String value;
}
