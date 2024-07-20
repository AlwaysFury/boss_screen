package com.boss.client.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.boss.common.enities.ProductOrImgTag;

import java.util.List;

/**
 * 文章标签服务
 */
public interface ProductOrImgTagService extends IService<ProductOrImgTag> {

    void saveProductOrImgTag(List<String> tagNameList, Long productOrImgId, String tagType);

    void deleteBatch(List<Long> productOrImgIds);

}
