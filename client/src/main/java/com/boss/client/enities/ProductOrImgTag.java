package com.boss.client.enities;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 文章标签
 * 标签
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("tb_productOrImg_tag")
public class ProductOrImgTag {

    /**
     * id
     */
    private Long id;

    /**
     * 产品id
     */
    @TableField("itemOrImg_id")
    private Long itemOrImgId;

    /**
     * 标签id
     */
    private Long tagId;

    /**
     * 标签类型
     */
    private String tagType;


}
