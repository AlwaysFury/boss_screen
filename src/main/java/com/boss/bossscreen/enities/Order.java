package com.boss.bossscreen.enities;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * @Description
 * @Author 罗宇航
 * @Date 2024/4/17
 */

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("tb_order")
public class Order {

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 创建时间
     */
    private Long createTime;

    /**
     * 修改时间
     */
    private Long updateTime;

    /**
     * 订单号
     */
    private String orderSn;

    /**
     * 运单号
     */
    private String packageNumber;

    /**
     * 状态
     */
    private String status;

    /**
     * 付款金额
     */
    private BigDecimal totalAmount;

    /**
     * 付款时间
     */
    private Long payTime;

    /**
     * 买家id
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
     * 实际运费 todo 哪一个
     */
    private BigDecimal estimatedShippingFee;
}
