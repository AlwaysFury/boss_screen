package com.boss.client.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * @Description
 * @Author 罗宇航
 * @Date 2024/7/2
 */

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GradeObject {

    private Long itemId;

    private Long categoryId;

    private String status;

    private String itemSku;

    private Long createTime;

    private BigDecimal price;

    private int salesVolume;
}
