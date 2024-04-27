package com.boss.bossscreen.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @Description
 * @Author 罗宇航
 * @Date 2024/4/18
 */

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductInfoVO {

    private Integer id;

    /**
     * 创建时间
     */
    private Long createTime;

    /**
     * 修改时间
     */
    private Long updateTime;

    /**
     * 系统 id
     */
    private Long itemId;

    /**
     * 类目 id
     */
    private Long categoryId;

    /**
     * 名称
     */
    private String itemName;

    /**
     * sku
     */
    private String itemSku;

    /**
     * 等级
     */
//    private String grade;

    /**
     * 所属店铺 id
     */
    private long shopId;

    /**
     * 状态
     */
    private String status;


    List<ModelVO> modelVOList;
}
