package com.boss.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @Description
 * @Author 罗宇航
 * @Date 2024/8/3
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RefreshDTO {

    private String by; // time：时间刷新, id：id刷新

    private List<Long> itemIds;// 产品id

    private List<String> orderSns;// 订单号

    private String startTime;// 开始时间

    private String endTime;// 结束时间
}
