package com.boss.client.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.boss.client.dao.ProductOrImgTagDao;
import com.boss.common.enities.ProductOrImgTag;
import com.boss.common.enities.Tag;
import com.boss.client.service.ProductOrImgTagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 文章标签服务
 */
@Service
public class ProductOrImgTagServiceImpl extends ServiceImpl<ProductOrImgTagDao, ProductOrImgTag> implements ProductOrImgTagService {

    @Autowired
    private ProductOrImgTagDao productOrImgTagDao;

    @Autowired
    private TagServiceImpl tagService;

    @Override
    public void saveProductOrImgTag(List<String> tagNameList, Long productOrSkuId, String tagType) {
        // 编辑则删除所有标签
        if (Objects.nonNull(productOrSkuId)) {
            productOrImgTagDao.delete(new LambdaQueryWrapper<ProductOrImgTag>()
                    .eq(ProductOrImgTag::getItemOrImgId, productOrSkuId).eq(ProductOrImgTag::getTagType, tagType));
        }
        // 添加标签
//        List<String> tagNameList = photoInfoDTO.getTagNameList();
        if (CollectionUtils.isNotEmpty(tagNameList)) {
            // 查询已存在的标签
            List<Tag> existTagList = tagService.list(new LambdaQueryWrapper<Tag>()
                    .eq(Tag::getTagType, tagType)
                    .in(Tag::getTagName, tagNameList));
            List<String> existTagNameList = existTagList.stream()
                    .map(Tag::getTagName)
                    .collect(Collectors.toList());
            List<Long> existTagIdList = existTagList.stream()
                    .map(Tag::getId)
                    .collect(Collectors.toList());
            // 对比新增不存在的标签
            tagNameList.removeAll(existTagNameList);
            if (CollectionUtils.isNotEmpty(tagNameList)) {
                List<Tag> tagList = tagNameList.stream().map(item -> Tag.builder()
                                .tagName(item)
                                .tagType(tagType)
                                .build())
                        .collect(Collectors.toList());
                tagService.saveBatch(tagList);
                List<Long> tagIdList = tagList.stream()
                        .map(Tag::getId)
                        .collect(Collectors.toList());
                existTagIdList.addAll(tagIdList);
            }
            // 提取标签id绑定
            List<ProductOrImgTag> productOrImgTagList = existTagIdList.stream().map(id -> ProductOrImgTag.builder()
                            .itemOrImgId(productOrSkuId)
                            .tagId(id)
                            .tagType(tagType)
                            .build())
                    .collect(Collectors.toList());
            this.saveBatch(productOrImgTagList);
        }
    }

    @Override
    public void deleteBatch(List<Long> productOrImgIds) {
        productOrImgTagDao.delete(new QueryWrapper<ProductOrImgTag>().in("itemOrImg_id", productOrImgIds));
    }
}
