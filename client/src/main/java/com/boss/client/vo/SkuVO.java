package com.boss.client.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Description
 * @Author 罗宇航
 * @Date 2024/6/27
 */

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SkuVO {

//    @TableId(value = "id", type = IdType.AUTO)
private Long id;

    /**
     * 款号
     */
    private String name;

    /**
     * 关联款号个数
     */
    private int count;

    /**
     * 所属店铺id
     */
    private Long shopId;

    /**
     * 创建时间
     */
    private String createTime;


}
