package com.boss.bossscreen.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

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
    private Integer id;

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
     * 订单中的产品
     */
    private List<OrderEscrowItemVO> orderEscrowItemVOList;

    /**
     * 调整交易金额
     */
    private BigDecimal adjustmentAmount;

    /**
     * 调整原因
     */
    private String adjustmentReason;
}
