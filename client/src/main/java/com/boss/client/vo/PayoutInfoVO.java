package com.boss.client.vo;

import com.alibaba.fastjson.JSONArray;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Description
 * @Author 罗宇航
 * @Date 2024/6/17
 */

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PayoutInfoVO {

    private String id;

    private JSONArray data;

    /**
     * 订单号
     */
    private String orderSn;

    /**
     * 发布时间
     */
    private long payoutTime;
}
