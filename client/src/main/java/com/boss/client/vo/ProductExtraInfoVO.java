package com.boss.client.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Description
 * @Author 罗宇航
 * @Date 2024/6/15
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductExtraInfoVO {

    private Long id;

    /**
     * 系统id
     */
    private Long itemId;

    /**
     * 销售量
     */
    private int sale;

    /**
     * 浏览量
     */
    private int views;

    /**
     * 点赞数
     */
    private int likes;

    /**
     * 评分
     */
    private float ratingStar;

    /**
     * 评论数
     */
    private int commentCount;

    /**
     * 等级
     */
    private String grade;

    /**
     * 近3天销量
     */
    private int salesVolume3daysCount;

    /**
     * 近7天销量
     */
    private int salesVolume7daysCount;

    /**
     * 近15天销量
     */
    private int salesVolume15daysCount;

    /**
     * 近30天销量
     */
    private int salesVolume30daysCount;

    /**
     * 转化率 (加入购物车率)
     */
    private double addCartRate;

    /**
     * 转化率（已下订单）
     */
    private double orderRate;

    /**
     * 件数（已确定订单）
     */
    private int productCount;
}
