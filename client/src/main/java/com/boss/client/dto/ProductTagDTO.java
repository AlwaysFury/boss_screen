package com.boss.client.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @Description
 * @Author 罗宇航
 * @Date 2024/6/28
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductTagDTO {

    private Long id;

    /**
     * 标签名称
     */
    private List<String> tagNameList;
}
