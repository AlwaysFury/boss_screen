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
@TableName("tb_ads_detail")
public class AdsDetail {

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
     * 关键字
     */
    private String keyword;

    /**
     * 匹配类型
     */
    private String matchType;

    /**
     * 搜寻次数
     */
    private String searchCount;

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
     * 平均排名
     */
    private int averageRank;

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

    private String type;
}
