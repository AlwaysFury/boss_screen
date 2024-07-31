package com.boss.client.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.boss.client.dao.CategoryDao;
import com.boss.client.dto.CategoryDTO;
import com.boss.client.dto.ConditionDTO;
import com.boss.client.service.CategoryService;
import com.boss.client.vo.CategoryVO;
import com.boss.client.vo.PageResult;
import com.boss.common.enities.Category;
import com.boss.common.util.BeanCopyUtils;
import com.boss.common.util.PageUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


/**
 * 操作日志服务
 */
@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, Category> implements CategoryService {

    @Autowired
    private CategoryDao categoryDao;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void saveOrUpdateCategory(CategoryDTO categoryDTO) {
        Category category = BeanCopyUtils.copyObject(categoryDTO, Category.class);
        this.saveOrUpdate(category);
    }

    @Override
    public PageResult<CategoryVO> categoryListByCondition(ConditionDTO condition) {
        // 查询分类数量
        Integer count = categoryDao.categoryCount(condition);
        if (count == 0) {
            return new PageResult<>();
        }
        // 分页查询分类列表
        List<CategoryVO> categoryList = categoryDao.categoryList(PageUtils.getLimitCurrent(), PageUtils.getSize(), condition);

        return new PageResult<>(categoryList, count);
    }

}
