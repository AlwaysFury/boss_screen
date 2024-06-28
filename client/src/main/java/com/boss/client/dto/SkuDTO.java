package com.boss.client.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SkuDTO {

    /**
     * id
     */
    private Long id;

    private String name;

    private List<Long> relevanceIds;

    private Long shopId;

}
