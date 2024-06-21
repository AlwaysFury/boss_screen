package com.boss.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Description
 * @Author 罗宇航
 * @Date 2024/4/21
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OperationLogDTO {

    private Integer id;

    private String optDesc;

    private String createTime;
}
