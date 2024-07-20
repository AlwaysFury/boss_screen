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
 * @Date 2024/4/19
 */

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("tb_escrow_info")
public class EscrowInfo {

//    @TableId(value = "id", type = IdType.AUTO)
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
     * 订单号
     */
    private String orderSn;

    /**
     * 买家姓名
     */
//    @TableField(updateStrategy = FieldStrategy.IGNORED, insertStrategy = FieldStrategy.IGNORED, whereStrategy = FieldStrategy.IGNORED)
//    private String buyerUserName;

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
     * 所属店铺id
     */
    private Long shopId;

    /**
     * 退款金額
     */
    private BigDecimal sellerReturnRefund;

    /**
     * Shopee回扣金额
     */
    private BigDecimal shopeeDiscount;

    /**
     * 卖家优惠券金额
     */
    private BigDecimal voucherFromSeller;

    /**
     * 退货运费
     */
    private BigDecimal reverseShippingFee;

    /**
     * 联盟营销方案佣金
     */
    private BigDecimal orderAmsCommissionFee;

    /**
     * 佣金
     */
    private BigDecimal commissionFee;

    /**
     * 服务费
     */
    private BigDecimal serviceFee;

    /**
     * 交易手续费
     */
    private BigDecimal sellerTransactionFee;

    /**
     * 汇率
     */
    private double exchangeRate;
}
