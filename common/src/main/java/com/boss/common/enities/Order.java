package com.boss.common.enities;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
     * 订单号
     */
    private String orderSn;

    /**
     * 运单号
     */
//    private String trackingNumber;

    /**
     * 所属店铺
     */
    private Long shopId;

    /**
     * 状态
     */
    private String status;

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
    @TableField(updateStrategy = FieldStrategy.IGNORED, insertStrategy = FieldStrategy.IGNORED, whereStrategy = FieldStrategy.IGNORED)
    private String cancelBy;

    /**
     * 买家取消原因
     */
    @TableField(updateStrategy = FieldStrategy.IGNORED, insertStrategy = FieldStrategy.IGNORED, whereStrategy = FieldStrategy.IGNORED)
    private String buyerCancelReason;

    /**
     * 运送方式
     */
    @TableField(updateStrategy = FieldStrategy.IGNORED, insertStrategy = FieldStrategy.IGNORED, whereStrategy = FieldStrategy.IGNORED)
    private String shippingCarrier;

}
