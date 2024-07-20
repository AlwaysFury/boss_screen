package com.boss.client.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @Description
 * @Author 罗宇航
 * @Date 2024/6/27
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PhotoInfoDTO {

    /**
     * 照片id
     */
    private Long id;

    /**
     * 照片名
     */
    private String photoName;

    /**
     * 标签名称
     */
    private List<String> tagNameList;

    /**
     * 照片地址
     */
    private String photoSrc;
}
