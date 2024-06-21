package com.boss.common.enities.excelEnities;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @Description 流量概述
 * @Author 罗宇航
 * @Date 2024/6/15
 */

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("tb_flow_overview")
public class FlowOverview {

    private Long id;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 修改时间
     */
    @TableField(fill = FieldFill.UPDATE)
    private LocalDateTime updateTime;

    /**
     * 店铺id
     */
    private Long shopId;

    /**
     * 时间类型
     */
    private String dateType;

    /**
     * 访客数
     */
    private int visitorCount;

    /**
     * 新访客数
     */
    private int newVisitorCount;

    /**
     * 现有访客数
     */
    private int currentVisitorCount;

    /**
     * 新关注者
     */
    private int newFollowerCount;

    /**
     * 页面浏览数
     */
    private int pageViewCount;

    /**
     * 平均页面访问数
     */
    private double avgPageViewCount;

    /**
     * 平均停留时长
     */
    private String avgStayTime;

    /**
     * 跳出率
     */
    private double bounceRate;
}
