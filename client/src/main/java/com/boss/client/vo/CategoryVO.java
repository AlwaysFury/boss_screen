package com.boss.client.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Description
 * @Author 罗宇航
 * @Date 2024/4/24
 */

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CategoryVO {
    private Long id;

    private String name;

}
