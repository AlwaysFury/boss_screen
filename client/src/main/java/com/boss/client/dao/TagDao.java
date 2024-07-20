package com.boss.client.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.boss.client.vo.TagVO;
import com.boss.client.dto.ConditionDTO;
import com.boss.common.enities.Tag;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 标签
 */
@Repository
public interface TagDao extends BaseMapper<Tag> {



    Integer tagCount(@Param("condition") ConditionDTO condition);


    List<TagVO> tagList(@Param("current") Long current, @Param("size") Long size, @Param("condition") ConditionDTO condition);

}
