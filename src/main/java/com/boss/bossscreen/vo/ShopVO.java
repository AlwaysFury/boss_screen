package com.boss.bossscreen.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Description
 * @Author 罗宇航
 * @Date 2024/4/9
 */


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ShopVO {

    private Integer id;

    private long shopId;

    private String name;

    private Integer status;

    private String createTime;

    private long accountId;

}
