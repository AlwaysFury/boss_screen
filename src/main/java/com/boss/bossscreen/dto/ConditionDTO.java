package com.boss.bossscreen.dto;

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
    private String start_time;

    /**
     * 结束时间
     */
    private String end_time;

    /**
     * 店铺名称
     */
    private String shop_name;

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
     * 产品 sku
     */
    private String item_sku;

    /**
     * 产品状态
     */
    private String item_status;

    /**
     * 产品分类 id
     */
    private Long category_id;

    /**
     * 订单 id
     */
    private String order_sn;

    /**
     * 运单号
     */
    private String tracking_number;

    /**
     * 订单状态
     */
    private String order_status;

    /**
     * 日志类型
     */
    private String opt_type;

    /**
     * 成本类型
     */
    private String cost_type;
}
