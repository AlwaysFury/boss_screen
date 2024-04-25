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
 * @Date 2024/4/19
 */

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("tb_escrow_info")
public class EscrowInfo {

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
     * 买家姓名
     */
    private String buyerUserName;

    /**
     * 买家最终付款金额
     */
    private BigDecimal buyerTotalAmount;

    /**
     * 买家最终支付运费
     */
    private BigDecimal buyerPaidShippingFee;

    /**
     * 平台实际运费
     */
    private BigDecimal actualShippingFee;

    /**
     * 平台最终金额
     */
    private BigDecimal escrowAmount;

    /**
     * 调整交易金额
     */
    private BigDecimal adjustmentAmount;

    /**
     * 调整原因
     */
    private String adjustmentReason;
}
