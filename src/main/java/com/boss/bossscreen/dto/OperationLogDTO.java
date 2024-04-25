package com.boss.bossscreen.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @Description
 * @Author 罗宇航
 * @Date 2024/4/21
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OperationLogDTO {

    private String type;

    private String desc;

    private LocalDateTime createTime;
}
