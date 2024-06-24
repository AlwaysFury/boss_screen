package com.boss.common.enities;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @Description
 * @Author 罗宇航
 * @Date 2024/4/19
 */

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("tb_escrow_item")
public class EscrowItem {
//    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 修改时间
     */
    @TableField(fill = FieldFill.UPDATE)
    private LocalDateTime updateTime;

    /**
     * 订单号
     */
    private String orderSn;

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
    @TableField(updateStrategy = FieldStrategy.IGNORED, insertStrategy = FieldStrategy.IGNORED, whereStrategy = FieldStrategy.IGNORED)
    private Long activityId;

    /**
     * 活动类型
     */
    @TableField(updateStrategy = FieldStrategy.IGNORED, insertStrategy = FieldStrategy.IGNORED, whereStrategy = FieldStrategy.IGNORED)
    private String activityType;
}
