package com.boss.common.enities;

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
 * @Description
 * @Author 罗宇航
 * @Date 2024/4/16
 */


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("tb_model")
public class Model {

//    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 系统 id
     */
    private Long modelId;

    /**
     * 名称
     */
    private String modelName;

    /**
     * sku
     */
    private String modelSku;

    /**
     * 款号
     */
    private String skuName;

    /**
     * 时价
     */
    private BigDecimal currentPrice;

    /**
     * 原价
     */
    private BigDecimal originalPrice;

    /**
     * 库存
     */
    private int stock;

    /**
     * 活动 id
     */
    private long promotionId;

    /**
     * 所属产品系统 id
     */
    private Long itemId;

    /**
     * 状态
     */
    private String status;

    /**
     * 图片 id
     */
    private String imageId;

    /**
     * 图片 url
     */
    private String imageUrl;

    /**
     * 所属店铺id
     */
    private Long shopId;

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
}
