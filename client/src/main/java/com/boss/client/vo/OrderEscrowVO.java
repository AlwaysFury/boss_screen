package com.boss.client.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Description
 * @Author 罗宇航
 * @Date 2024/4/18
 */

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderEscrowVO {
    private Long id;

    /**
     * 创建时间
     */
    private Object createTime;

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
     * 是否为新品订单
     */
    private boolean isNew;

    /**
     * 总件数
     */
    private int totalCount;
}
