package com.boss.common.enities;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

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
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 修改时间
     */
    @TableField(fill = FieldFill.UPDATE)
    private LocalDateTime updateTime;

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
