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
 * @Date 2024/6/17
 */

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("tb_payout_info")
public class PayoutInfo {

    private String id;

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

    private String data;

    /**
     * 订单号
     */
    private String orderSn;

    /**
     * 发布时间
     */
    private long payoutTime;
}
