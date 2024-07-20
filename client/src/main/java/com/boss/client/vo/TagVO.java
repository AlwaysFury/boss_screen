package com.boss.client.vo;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * 标签
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TagVO {

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
