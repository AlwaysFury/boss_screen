package com.boss.client.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SkuInfoVO {

    /**
     * id
     */
    private Long id;

    private String name;

    private List<Long> relevanceIds;

    private String createTime;
}
