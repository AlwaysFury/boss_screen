package com.boss.client.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * @Description
 * @Author 罗宇航
 * @Date 2024/7/31
 */

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RuleConditionDTO {
    private boolean allOrNot;

    private Long itemId;

    private String skuName;

    private String status;

    private Long categoryId;

    private Long tagId;

    private BigDecimal minPrice;

    private BigDecimal maxPrice;

    private Long createStartTime;

    private Long createEndTime;
}
