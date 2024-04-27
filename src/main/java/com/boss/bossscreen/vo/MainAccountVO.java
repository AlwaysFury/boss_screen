package com.boss.bossscreen.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Description
 * @Author 罗宇航
 * @Date 2024/4/11
 */

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MainAccountVO {

    private Integer id;

    private long accountId;

    private Integer status;

    private String createTime;
}
