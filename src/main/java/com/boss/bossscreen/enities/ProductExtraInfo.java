package com.boss.bossscreen.enities;

import com.baomidou.mybatisplus.annotation.TableName;
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
@TableName("tb_product_extra_info")
public class ProductExtraInfo {

    private Long id;

    /**
     * 创建时间
     */
    private Long createTime;

    /**
     * 修改时间
     */
    private Long updateTime;

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
