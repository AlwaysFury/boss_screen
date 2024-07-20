package com.boss.client.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SkuStatisticsVO {

    /**
     * id
     */
    private Long id;

    private String name;

    private int saleVolume;

    private String itemIds;

    private String shopNames;

    private boolean isTop;
}
