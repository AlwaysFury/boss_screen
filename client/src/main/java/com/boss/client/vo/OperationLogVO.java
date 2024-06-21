package com.boss.client.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 操作日志
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OperationLogVO {
    /**
     * 日志id
     */
    private Integer id;

    /**
     * 操作类型
     */
    private String optType;

    /**
     * 操作描述
     */
    private String optDesc;

    /**
     * 创建时间
     */
    private String createTime;

}
