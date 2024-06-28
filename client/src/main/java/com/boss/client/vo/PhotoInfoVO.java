package com.boss.client.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @Description
 * @Author 罗宇航
 * @Date 2024/6/28
 */

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PhotoInfoVO {

    /**
     * 照片id
     */
    private Long id;

    /**
     * 照片名
     */
    private String photoName;

    /**
     * 照片描述
     */
    private String photoDesc;

    /**
     * 照片地址
     */
    private String photoSrc;

    /**
     * skuId
     */
    private Long skuId;

    /**
     * 所属店铺id
     */
    private Long shopId;

    /**
     * 创建时间
     */
    private String createTime;

    /**
     * 标签
     */
    private List<TagVO> tagVOList;
}
