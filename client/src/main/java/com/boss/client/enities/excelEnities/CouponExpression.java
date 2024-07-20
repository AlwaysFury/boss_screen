package com.boss.client.enities.excelEnities;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @Description 优惠券-优惠券表现
 * @Author 罗宇航
 * @Date 2024/6/15
 */

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("tb_coupon_expression")
public class CouponExpression {

    private Long id;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 修改时间
     */
    @TableField(fill = FieldFill.UPDATE)
    private LocalDateTime updateTime;

    /**
     * 店铺id
     */
    private Long shopId;

    /**
     * 优惠券名称
     */
    private String couponName;

    /**
     * 优惠券代码
     */
    private String couponCode;

    /**
     * 领取数量
     */
    private int receiveCount;

    /**
     * 订单
     */
    private int orderCount;

    /**
     * 销售额
     */
    private BigDecimal salesAmount;

    /**
     * 花费
     */
    private BigDecimal spend;

    /**
     * 销售商品件数
     */
    private int saleProductCount;

    /**
     * 客单价
     */
    private BigDecimal customerPrice;

    /**
     * 使用率
     */
    private double useRate;
}
