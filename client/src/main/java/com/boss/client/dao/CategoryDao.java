package com.boss.client.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.boss.client.dto.ConditionDTO;
import com.boss.client.vo.CategoryVO;
import com.boss.common.enities.Category;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


/**
 * 分类
 */
@Repository
public interface CategoryDao extends BaseMapper<Category> {


    Integer categoryCount(@Param("condition") ConditionDTO condition);


    List<CategoryVO> categoryList(@Param("current") Long current, @Param("size") Long size, @Param("condition") ConditionDTO condition);

}
