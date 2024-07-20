package com.boss.client.enities.excelEnities;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @Description
 * @Author 罗宇航
 * @Date 2024/7/12
 */

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("tb_activity")
public class Activity {


    private Long id;

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

    /**
     * 商品id
     */
    private Long itemId;

    /**
     * 规格id
     */
    private Long modelId;

    /**
     * 状态
     */
    private String status;

    /**
     * 活动价格
     */
    private BigDecimal price;

    /**
     * 所属店铺id
     */
    private Long shopId;

    /**
     * 时间段
     */
    private String date;

    /**
     * 母活动名称
     */
    private String mainName;

    /**
     * 子活动名称
     */
    private String subName;
}
