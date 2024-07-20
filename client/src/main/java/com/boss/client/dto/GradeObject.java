package com.boss.client.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * @Description
 * @Author 罗宇航
 * @Date 2024/7/2
 */

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GradeObject {

    private Long itemId;

    private Long categoryId;

    private String status;

    private String itemSku;

    private List<Long> tagIds;

    private Long createTime;

    private BigDecimal price;

    private int salesVolume;

    // 1.其他条件，2.指定等级，3.等级排序
    // 1.正常返回
    // 2.redis找到指定等级然后查找sql数据返回
    // 3.redis排序然后找到sql数据返回
    // 12.在sql找出符合条件的，在redis找出符合等级的
    // 13.
    // 23.
    // 123.
}
