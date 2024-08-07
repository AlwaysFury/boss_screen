package com.boss.client.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Description
 * @Author 罗宇航
 * @Date 2024/8/1
 */

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductExpressionVO {

    private Long id;

    /**
     * 创建时间
     */
    private String createTime;

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
    private String addCartRate;

    /**
     * 转化率（已确定订单）
     */
    private String confirmOrderRate;

    /**
     * 件数（已确定订单）
     */
    private int productCount;
}
