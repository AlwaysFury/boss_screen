package com.boss.client.enities.excelEnities;

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
 * @Date 2024/8/1
 */

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("tb_product_expression")
public class ProductExpression {

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
     * 商品编号
     */
    private Long itemId;

    /**
     * 赞
     */
    private int likes;

    /**
     * 访客数
     */
    private int visitorCount;

    /**
     * 转化率 (加入购物车率)
     */
    private double addCartRate;

    /**
     * 转化率（已确定订单）
     */
    private double confirmOrderRate;

    /**
     * 件数（已确定订单）
     */
    private int productCount;
}
