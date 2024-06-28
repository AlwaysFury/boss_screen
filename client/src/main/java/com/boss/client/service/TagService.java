package com.boss.client.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.boss.client.dto.TagDTO;
import com.boss.client.vo.PageResult;
import com.boss.client.vo.TagVO;
import com.boss.client.enities.Tag;
import com.boss.client.dto.ConditionDTO;
import com.boss.common.vo.SelectVO;

import java.util.List;

/**
 * 标签服务
 */
public interface TagService extends IService<Tag> {

    /**
     * 查询标签列表
     *
     * @return 标签列表
     */
    PageResult<TagVO> tagsListByCondition(ConditionDTO condition);

    /**
     * 删除标签
     *
     * @param tagIdList 标签id集合
     */
    void deleteTag(List<Long> tagIdList);

    /**
     * 保存或更新标签
     *
     * @param tagDTO 标签
     */
    void saveOrUpdateTag(TagDTO tagDTO);

    TagVO getTagById(long id);

    List<SelectVO> getTypeSelect();

}
