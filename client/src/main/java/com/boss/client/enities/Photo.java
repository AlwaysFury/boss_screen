package com.boss.client.enities;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


/**
 * 照片
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName(value ="tb_photo")
public class Photo {

    /**
     * 照片id
     */
    @TableId(type = IdType.AUTO)
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
    private Long skuId;

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

}