package com.boss.bossscreen.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Description
 * @Author 罗宇航
 * @Date 2024/4/17
 */

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductVO {

    private Long id;

    /**
     * 系统 id
     */
    private Long itemId;

    /**
     * 类目 id
     */
    private Long categoryId;

    /**
     * 类目名称
     */
    private String categoryName;

    /**
     * 名称
     */
    private String itemName;

    /**
     * sku
     */
    private String itemSku;

    /**
     * 主图 url
     */
    private String mainImgUrl;

    /**
     * 等级
     */
    private String grade;

    /**
     * 所属店铺 id
     */
    private long shopId;

    /**
     * 所属店铺名称
     */
    private String shopName;

    /**
     * 状态
     */
    private String status;

    /**
     * 创建时间
     */
    private Object createTime;

    /**
     * 总销量
     */
    private int salesVolume;
}
