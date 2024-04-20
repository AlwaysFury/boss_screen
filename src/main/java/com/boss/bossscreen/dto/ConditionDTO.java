package com.boss.bossscreen.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @Description
 * @Author 罗宇航
 * @Date 2024/4/17
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ConditionDTO {

    /**
     * 页码
     */
    private Long current;

    /**
     * 条数
     */
    private Long size;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;

    /**
     * 商店 id
     */
    private Long shop_id;

    /**
     * 账号 id
     */
    private Long account_id;

    /**
     * 产品 id
     */
    private Long item_id;

    /**
     * 产品 id
     */
    private String item_sku;

    /**
     * 订单 id
     */
    private String order_sn;
}
