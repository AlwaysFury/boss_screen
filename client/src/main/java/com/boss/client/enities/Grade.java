package com.boss.client.enities;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Description
 * @Author 罗宇航
 * @Date 2024/7/2
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName(value ="tb_grade")
public class Grade {


    private Long id;

    private String grade;

    private String type;
}
