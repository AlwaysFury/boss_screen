package com.boss.client.vo;

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
public class RuleInfoVO {

    private Integer id;

    private String name;

    private String grade;

    private Boolean allOrNot;

    private JSONObject rule;

    private String type;

    private Integer weight;
}
