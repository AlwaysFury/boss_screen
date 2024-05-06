package com.boss.bossscreen.enities;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @Description
 * @Author 罗宇航
 * @Date 2024/4/17
 */

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("tb_order_item")
public class OrderItem {

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

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
     * 订单号
     */
    private String orderSn;

    /**
     * 运单号
     */
    private String trackingNumber;

    /**
     * 产品 id
     */
    private Long itemId;

    /**
     * 产品名称
     */
    private String itemName;

    /**
     * 产品 sku
     */
    private String itemSku;

    /**
     * model id
     */
    private Long modelId;

    /**
     * model 名称
     */
    private String modelName;

    /**
     * model sku
     */
    private String modelSku;

    /**
     * 下单数量
     */
    private int count;

    /**
     * 图片链接
     */
    private String imageUrl;

    /**
     * 活动 id
     */
    private Long promotionId;

    /**
     * 活动类型
     */
    private String promotionType;

    /**
     * 成本
     */
    private BigDecimal cost;
}
