package com.boss.bossscreen.vo;

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
public class MainAccountVO {

    private Integer id;

    private long mainAccountId;

    private Integer status;
}
