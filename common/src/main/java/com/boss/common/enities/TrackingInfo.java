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
 * @Date 2024/6/18
 */

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("tb_tracking_info")
public class TrackingInfo {

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
     * 订单号
     */
    private String orderSn;

    /**
     * 物流号
     */
    private String trackingNumber;

    /**
     * 物流状态
     */
    private String logisticsStatus;

    /**
     * 跟踪物流状态
     */
    private String logisticsData;

    /**
     * 所属店铺id
     */
    private Long shopId;
}
