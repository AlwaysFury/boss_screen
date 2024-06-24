package com.boss.client.vo;

import com.alibaba.fastjson.JSONArray;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Description
 * @Author 罗宇航
 * @Date 2024/6/18
 */

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TrackingInfoVO {

    private Long id;

    /**
     * 订单号
     */
    private String orderSn;

    /**
     * 物流号
     */
    private String trackingNumber;

    /**
     * 物流状态
     */
    private String logisticsStatus;

    /**
     * 跟踪物流状态
     */
    private JSONArray logisticsData;
}
