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
@TableName("tb_ads")
public class Ads {

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
     * 广告名称
     */
    private String adsName;

    /**
     * 商品id
     */
    private Long itemId;

    /**
     * 状态
     */
    private String status;

    /**
     * 竞价方式
     */
    private String bidType;

    /**
     * 版位
     */
    private String position;

    /**
     * 展示次数
     */
    private int showCount;

    /**
     * 点击次数
     */
    private int clickCount;

    /**
     * 点击率
     */
    private double clickRate;

    /**
     * 转化
     */
    private int conversion;

    /**
     * 销售金额
     */
    private BigDecimal salesAmount;

    /**
     * 花费
     */
    private BigDecimal spend;

    /**
     * 广告支出回报率
     */
    private double adsCostRate;

    /**
     * 点击单价=花费/点击数
     */
    private BigDecimal clickPrice;

    /**
     * 投资产出比
     */
    private double investmentOutputRatio;

    /**
     * 类型
     */
    private String type;

    /**
     * 广告订单量
     */
    private int adsOrderCount;

    /**
     * 搜索订单量
     */
    private int searchOrderCount;

    /**
     * 搜索自动订单量
     */
    private int searchAutoOrderCount;

    /**
     * 搜索手动订单量
     */
    private int searchManualOrderCount;

    /**
     * 关联订单量
     */
    private int relationOrderCount;

}
