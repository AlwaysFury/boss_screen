package com.boss.common.enities;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @Description
 * @Author 罗宇航
 * @Date 2024/4/9
 */


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("tb_shop")
public class Shop {

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private long shopId;

    private String name;

    private String authCode;

    private String accessToken;

    private String refreshToken;

    private Integer status;

    private Long accountId;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 修改时间
     */
    @TableField(fill = FieldFill.UPDATE)
    private LocalDateTime updateTime;
}
