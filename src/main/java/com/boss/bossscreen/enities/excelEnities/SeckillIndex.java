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
 * @Description 店内限时秒杀
 * @Author 罗宇航
 * @Date 2024/6/15
 */

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("tb_seckill_index")
public class SeckillIndex {

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
     * 商品展示量
     */
    private int displayVolume;

    /**
     * 商品点击量
     */
    private int clickVolume;

    /**
     * 客单价
     */
    private BigDecimal customerPrice;

    /**
     * 点击率
     */
    private double clickRate;
}
