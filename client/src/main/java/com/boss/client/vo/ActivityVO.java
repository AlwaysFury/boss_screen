package com.boss.client.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * @Description
 * @Author 罗宇航
 * @Date 2024/8/5
 */

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ActivityVO {

    private Long id;

    /**
     * 商品id
     */
    private Long itemId;

    /**
     * 规格id
     */
    private Long modelId;

    /**
     * 活动价格
     */
    private BigDecimal price;

    /**
     * 母活动名称
     */
    private String mainName;

    /**
     * 子活动名称
     */
    private String subName;

    /**
     * 活动时间
     */
    private String date;
}
