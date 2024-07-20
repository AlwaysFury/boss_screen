package com.boss.client.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.boss.client.dao.ProductOrImgTagDao;
import com.boss.client.dao.TagDao;
import com.boss.client.dto.ConditionDTO;
import com.boss.client.dto.TagDTO;
import com.boss.common.enities.ProductOrImgTag;
import com.boss.common.enities.Tag;
import com.boss.client.exception.BizException;
import com.boss.client.service.TagService;
import com.boss.client.vo.PageResult;
import com.boss.client.vo.TagVO;
import com.boss.common.enums.TagTypeEnum;
import com.boss.common.util.BeanCopyUtils;
import com.boss.common.util.PageUtils;
import com.boss.common.vo.SelectVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 标签服务
 */
@Service
public class TagServiceImpl extends ServiceImpl<TagDao, Tag> implements TagService {
    @Autowired
    private TagDao tagDao;
    @Autowired
    private ProductOrImgTagDao productOrImgTagDao;

    @Override
    public PageResult<TagVO> tagsListByCondition(ConditionDTO condition) {
        // 查询标签数量
        int count = tagDao.tagCount(condition);
        if (count == 0) {
            return new PageResult<>();
        }
        // 分页查询标签列表
        List<TagVO> tagList = tagDao.tagList(PageUtils.getLimitCurrent(), PageUtils.getSize(), condition)
                .stream().map(tag -> {
                    tag.setTagType(TagTypeEnum.getDescByCode(tag.getTagType()));
                    return tag;
                }).collect(Collectors.toList());
        return new PageResult<>(tagList, count);
    }

    @Override
    public TagVO getTagById(long id) {
        Tag tag = this.getOne(new QueryWrapper<Tag>().eq("id", id));
        if (Objects.isNull(tag)) {
            throw new BizException("标签不存在");
        }
        TagVO tagVO = BeanCopyUtils.copyObject(tag, TagVO.class);
        tagVO.setTagType(TagTypeEnum.getDescByCode(tag.getTagType()));
        return tagVO;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void deleteTag(List<Long> tagIdList) {
        productOrImgTagDao.delete(new LambdaQueryWrapper<ProductOrImgTag>().in(ProductOrImgTag::getTagId, tagIdList));
        tagDao.deleteBatchIds(tagIdList);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void saveOrUpdateTag(TagDTO tagDTO) {
        // 查询标签名是否存在
        Tag existTag = tagDao.selectOne(new LambdaQueryWrapper<Tag>()
                .select(Tag::getId)
                .eq(Tag::getTagName, tagDTO.getTagName()));
        if (Objects.nonNull(existTag) && !existTag.getId().equals(tagDTO.getId())) {
            throw new BizException("该标签名已存在");
        }
        Tag tag = BeanCopyUtils.copyObject(tagDTO, Tag.class);
        this.saveOrUpdate(tag);
    }

    @Override
    public List<SelectVO> getTypeSelect() {
        return TagTypeEnum.getTagTypeEnum();
    }

}
