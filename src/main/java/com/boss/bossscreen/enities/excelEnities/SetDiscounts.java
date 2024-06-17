package com.boss.bossscreen.enities.excelEnities;

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
 * @Description 套装优惠
 * @Author 罗宇航
 * @Date 2024/6/15
 */

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("tb_set_discounts")
public class SetDiscounts {

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
     * 店铺id
     */
    private Long shopId;

    /**
     * 套装名称
     */
    private String setName;

    /**
     * 订单数
     */
    private int orderCount;

    /**
     * 销售商品件数
     */
    private int saleProductCount;

    /**
     * 客单价
     */
    private BigDecimal customerPrice;
}
