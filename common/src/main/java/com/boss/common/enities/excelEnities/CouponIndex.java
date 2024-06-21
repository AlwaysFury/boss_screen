package com.boss.common.enities.excelEnities;

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
 * @Description 优惠券-关键指标
 * @Author 罗宇航
 * @Date 2024/6/15
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("tb_coupon_index")
public class CouponIndex {

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
     * 销售额
     */
    private BigDecimal salesAmount;

    /**
     * 订单
     */
    private int orderCount;

    /**
     * 使用率
     */
    private double useRate;

    /**
     * 花费
     */
    private BigDecimal spend;
}
