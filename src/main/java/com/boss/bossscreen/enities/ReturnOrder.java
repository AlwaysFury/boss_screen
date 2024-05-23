package com.boss.bossscreen.enities;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * @Description
 * @Author 罗宇航
 * @Date 2024/4/25
 */

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("tb_return_order")
public class ReturnOrder {
//    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 创建时间
     */
    private Long createTime;

    /**
     * 修改时间
     */
    private Long updateTime;

    /**
     * 原因
     */
    private String reason;

    /**
     * 买家提供理由
     */
    private String textReason;

    /**
     * 返回单号
     */
    private String returnSn;

    /**
     * 退款金额
     */
    private BigDecimal refundAmount;

    /**
     * 状态
     */
    private String status;

    /**
     * 折扣前金额
     */
    private BigDecimal amountBeforeDiscount;

    /**
     * 原订单
     */
    private String orderSn;
}
