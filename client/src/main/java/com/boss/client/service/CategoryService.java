package com.boss.client.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.boss.client.dto.CategoryDTO;
import com.boss.client.dto.ConditionDTO;
import com.boss.client.vo.CategoryVO;
import com.boss.client.vo.PageResult;
import com.boss.common.enities.Category;

/**
 * @Description
 * @Author 罗宇航
 * @Date 2024/4/10
 */
public interface CategoryService extends IService<Category>  {

    void saveOrUpdateCategory(CategoryDTO categoryDTO);


    PageResult<CategoryVO> categoryListByCondition(ConditionDTO condition);

}
