package com.boss.client.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * @Description
 * @Author 罗宇航
 * @Date 2024/4/24
 */

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CostVO {
    private Integer id;

    private BigDecimal price;

    private String type;

    private String startTime;

    private String endTime;

    private double exchangeRate;
}
