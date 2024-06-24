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
}
