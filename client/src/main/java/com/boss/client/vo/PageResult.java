package com.boss.client.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 分页对象
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PageResult<T> {

    /**
     * 分页列表
     */
    private List<T> recordList;

    /**
     * 总数
     */
    private Integer count;

}
