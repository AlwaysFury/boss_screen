package com.boss.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Description
 * @Author 罗宇航
 * @Date 2024/4/10
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ShopDTO {
    private Integer id;

    private long shopId;

    private String name;

    private String authCode;

    private String accessToken;

    private String refreshToken;

    private Integer status;

    private Long accountId;
}
