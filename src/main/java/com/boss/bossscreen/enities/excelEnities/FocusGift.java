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
 * @Description 关注礼
 * @Author 罗宇航
 * @Date 2024/6/15
 */

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("tb_focus_gift")
public class FocusGift {

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
     * 独立领取数
     */
    private Integer receiveCount;

    /**
     * 新关注者
     */
    private Integer newFollowerCount;

    /**
     * 订单
     */
    private Integer orderCount;

    /**
     * 花费
     */
    private BigDecimal spend;

    /**
     * 使用率
     */
    private Double useRate;

    /**
     * 销售额
     */
    private BigDecimal salesAmount;
}
