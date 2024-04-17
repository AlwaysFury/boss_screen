package com.boss.bossscreen.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * 查询条件
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "查询条件")
public class ProductConditionDTO extends ConditionDTO {

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

}
