package com.boss.client.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Description
 * @Author 罗宇航
 * @Date 2024/6/28
 */

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PhotoVO {

    /**
     * 照片id
     */
    private Long id;

    /**
     * 照片名
     */
    private String photoName;

    /**
     * 照片地址
     */
    private String photoSrc;

    /**
     * skuId
     */
    private Long skuName;

    /**
     * 所属店铺id
     */
    private Long shopName;

    /**
     * 关联数量
     */
    private int count;

    /**
     * 创建时间
     */
    private String createTime;
}
