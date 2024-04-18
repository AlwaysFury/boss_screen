package com.boss.bossscreen.dto;

import io.swagger.annotations.ApiModelProperty;
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
    @ApiModelProperty(name = "current", value = "页码", dataType = "Long")
    private Long current;

    /**
     * 条数
     */
    @ApiModelProperty(name = "size", value = "条数", dataType = "Long")
    private Long size;

    /**
     * 状态
     */
    @ApiModelProperty(name = "status", value = "状态", dataType = "Integer")
    private Integer status;

    /**
     * 开始时间
     */
    @ApiModelProperty(name = "startTime", value = "开始时间", dataType = "LocalDateTime")
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    @ApiModelProperty(name = "endTime", value = "结束时间", dataType = "LocalDateTime")
    private LocalDateTime endTime;

    /**
     * 商店 id
     */
    @ApiModelProperty(name = "shop_id", value = "商店 id", dataType = "Long")
    private Long shop_id;

    /**
     * 账号 id
     */
    @ApiModelProperty(name = "account_id", value = "账号 id", dataType = "Long")
    private Long account_id;

    /**
     * 产品 id
     */
    @ApiModelProperty(name = "item_id", value = "产品 id", dataType = "Long")
    private Long item_id;

    /**
     * 产品 id
     */
    @ApiModelProperty(name = "item_sku", value = "产品 sku", dataType = "Long")
    private String item_sku;
}
