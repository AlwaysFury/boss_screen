package com.boss.client.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.boss.client.dao.ProductOrImgTagDao;
import com.boss.client.enities.ProductOrImgTag;
import com.boss.client.enities.Tag;
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
    public void saveProductOrImgTag(List<String> tagNameList, Long productOrImgId, String tagType) {
        // 编辑文章则删除文章所有标签
        if (Objects.nonNull(productOrImgId)) {
            productOrImgTagDao.delete(new LambdaQueryWrapper<ProductOrImgTag>()
                    .eq(ProductOrImgTag::getItemOrImgId, productOrImgId).eq(ProductOrImgTag::getTagType, tagType));
        }
        // 添加文章标签
//        List<String> tagNameList = photoInfoDTO.getTagNameList();
        if (CollectionUtils.isNotEmpty(tagNameList)) {
            // 查询已存在的标签
            List<Tag> existTagList = tagService.list(new LambdaQueryWrapper<Tag>()
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
                                .build())
                        .collect(Collectors.toList());
                tagService.saveBatch(tagList);
                List<Long> tagIdList = tagList.stream()
                        .map(Tag::getId)
                        .collect(Collectors.toList());
                existTagIdList.addAll(tagIdList);
            }
            // 提取标签id绑定文章
            List<ProductOrImgTag> productOrImgTagList = existTagIdList.stream().map(id -> ProductOrImgTag.builder()
                            .itemOrImgId(productOrImgId)
                            .tagId(id)
                            .tagType(tagType)
                            .build())
                    .collect(Collectors.toList());
            this.saveBatch(productOrImgTagList);
        }
    }
}
