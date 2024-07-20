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
 * @Description 商品概述
 * @Author 罗宇航
 * @Date 2024/6/15
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("tb_product_overview")
public class ProductOverview {

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
     * 时间类型
     */
    private String dateType;

    /**
     * 转化率 (加入购物车率)
     */
    private double addCartRate;

    /**
     * 转化率（已下订单）
     */
    private double orderRate;

    /**
     * 转化率（已确定订单）
     */
    private double confirmOrderRate;

    /**
     * 买家数（已确定订单）
     */
    private int buyerCount;

    /**
     * 已确定的商品
     */
    private int confirmProductCount;

    /**
     * 件数（已确定订单）
     */
    private int productCount;

    /**
     * 访客数
     */
    private int visitorCount;

    /**
     * 销售额
     */
    private BigDecimal salesAmount;

    /**
     * 赞
     */
    private int likes;

    /**
     * 客单价
     */
    private BigDecimal perCustomerTransaction;

    /**
     * 每单件数
     */
    private double perOrderProductCount;

    /**
     * roi
     */
    private BigDecimal roi;

    /**
     * uv
     */
    private BigDecimal uv;
}
