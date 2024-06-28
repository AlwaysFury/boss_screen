package com.boss.client.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

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

    private List<Map<String, Object>> relevanceList;

}
