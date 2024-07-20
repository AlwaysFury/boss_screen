package com.boss.client.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * @Description
 * @Author 罗宇航
 * @Date 2024/4/18
 */

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderEscrowInfoVO {
    private Long id;

    /**
     * 创建时间
     */
    private String createTime;

    /**
     * 订单号
     */
    private String orderSn;

    /**
     * 运单号
     */
    private String trackingNumber;

    /**
     * 状态
     */
    private String status;

    /**
     * 所属店铺
     */
    private Long shopId;

    /**
     * 所属店铺名称
     */
    private String shopName;

    /**
     * 买家 id
     */
    private Long buyerUerId;

    /**
     * 买家名称
     */
    private String buyerUserName;

    /**
     * 取消原因
     */
    private String cancelReason;

    /**
     * 取消通过
     */
    private String cancelBy;

    /**
     * 买家取消原因
     */
    private String buyerCancelReason;

    /**
     * 付款时间
     */
    private String payTime;

    /**
     * 是否为新品订单
     */
    private boolean isNew;

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
