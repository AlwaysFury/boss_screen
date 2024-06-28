package com.boss.client.enities;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @Description
 * @Author 罗宇航
 * @Date 2024/6/27
 */

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("tb_sku")
public class Sku {

//    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 款号
     */
    private String name;

    /**
     * 关联款号
     */
    private String relevanceIds;

    /**
     * 所属店铺id
     */
    private Long shopId;

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
