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
 * @Date 2024/4/25
 */

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("tb_return_order_item")
public class ReturnOrderItem {

    @TableId(value = "id", type = IdType.AUTO)
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
     * 关联回退订单号
     */
    private String returnSn;

    /**
     * 名称
     */
    private String name;

    /**
     * 产品 id
     */
    private Long itemId;

    /**
     * sku
     */
    private String itemSku;

    /**
     * 价格
     */
    private BigDecimal itemPrice;

    /**
     * 完整 sku
     */
    private String variationSku;

    /**
     * model id
     */
    private Long modelId;

    /**
     * 下单数量
     */
    private int amount;
}
