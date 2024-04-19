package com.boss.bossscreen.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * @Description
 * @Author 罗宇航
 * @Date 2024/4/17
 */

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderEscrowItemVO {

    private Integer id;

    /**
     * 产品 id
     */
    private Long itemId;

    /**
     * 产品名称
     */
    private String itemName;

    /**
     * 产品 sku
     */
    private String itemSku;

    /**
     * model id
     */
    private Long modelId;

    /**
     * model 名称
     */
    private String modelName;

    /**
     * model sku
     */
    private String modelSku;

    /**
     * 下单数量
     */
    private int count;

    /**
     * 图片链接
     */
    private String imageUrl;

    /**
     * 原始价格
     */
    private BigDecimal originalPrice;

    /**
     * 销售价格
     */
    private BigDecimal sellingPrice;

    /**
     * 折扣价格
     */
    private BigDecimal discountedPrice;

    /**
     * 卖家折扣
     */
    private BigDecimal sellerDiscount;

    /**
     * 活动 id
     */
    private Long activityId;

    /**
     * 活动类型
     */
    private String activityType;

    /**
     * 成本
     */
    private BigDecimal cost;

    /**
     * 利润
     */
    private BigDecimal profit;

    /**
     * 利润率
     */
    private float profitRate;

}
