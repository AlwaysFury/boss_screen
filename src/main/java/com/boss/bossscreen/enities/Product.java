package com.boss.bossscreen.enities;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Description
 * @Author 罗宇航
 * @Date 2024/4/16
 */

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("tb_product")
public class Product {

    @TableId(value = "id", type = IdType.AUTO)
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
     * 主图 id
     */
    @TableField("mainImg_id")
    private String mainImgId;

    /**
     * 主图 url
     */
    @TableField("mainImg_url")
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
     * 状态
     */
    private String status;
}
