package com.boss.bossscreen.dto;

import com.alibaba.fastjson.JSONObject;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Description
 * @Author 罗宇航
 * @Date 2024/5/9
 */

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RuleDTO {

    private Integer id;

    private String name;

    private String grade;

    private boolean allOrNot;

    private JSONObject rule;
}
