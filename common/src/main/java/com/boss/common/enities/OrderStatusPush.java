package com.boss.common.enities;

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
 * @Date 2024/6/20
 */

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("tb_order_status_push")
public class OrderStatusPush {

    private Long id;

    /**
     * 创建时间
     */
    private Long createTime;

    /**
     * 修改时间
     */
    @TableField(fill = FieldFill.UPDATE)
    private LocalDateTime updateTime;

    /**
     * 订单号
     */
    private String orderSn;

    /**
     * 状态
     */
    private String status;

    /**
     * 完成场景
     */
    private String completedScenario;

    /**
     * 所属店铺id
     */
    private Long shopId;
}
