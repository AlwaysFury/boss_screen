package com.boss.client.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Description
 * @Author 罗宇航
 * @Date 2024/8/1
 */

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RelevanceSkuVO {

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long id;

    private String name;
}
