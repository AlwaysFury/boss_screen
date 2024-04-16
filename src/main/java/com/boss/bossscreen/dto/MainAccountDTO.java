package com.boss.bossscreen.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Description
 * @Author 罗宇航
 * @Date 2024/4/11
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MainAccountDTO {

    private Integer id;

    private long accountId;

    private String authCode;

    private String accessToken;

    private String refreshToken;

    private Integer status;
}
