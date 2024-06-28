package com.boss.client.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TagDTO {

    /**
     * id
     */
    private Long id;

    /**
     * 标签名
     */
    private String tagName;

    /**
     * 标签类型
     */
    private String tagType;

}
