package com.boss.client.controller;


import com.boss.client.dto.CategoryDTO;
import com.boss.client.dto.ConditionDTO;
import com.boss.client.service.impl.CategoryServiceImpl;
import com.boss.client.vo.CategoryVO;
import com.boss.client.vo.PageResult;
import com.boss.client.vo.Result;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @Description
 * @Author 罗宇航
 * @Date 2024/4/16
 */

@RestController
@RequestMapping("/category")
@Slf4j
public class CategoryController {

    @Autowired
    private CategoryServiceImpl categoryService;

    /**
     * 获取成本列表
     * @param condition
     * @return
     */
    @GetMapping("/categoryList")
    public Result<PageResult<CategoryVO>> categoryList(ConditionDTO condition) {
        return Result.ok(categoryService.categoryListByCondition(condition));
    }

    /**
     * 插入或更新
     */
    @PostMapping("/saveOrUpdate")
    public Result<?> saveOrUpdateCategory(@Valid @RequestBody CategoryDTO categoryDTO) {
        categoryService.saveOrUpdateCategory(categoryDTO);
        return Result.ok();
    }
}
