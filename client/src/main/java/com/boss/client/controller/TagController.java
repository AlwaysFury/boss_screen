package com.boss.client.controller;


import com.boss.client.dto.TagDTO;
import com.boss.client.service.impl.TagServiceImpl;
import com.boss.client.vo.PageResult;
import com.boss.client.vo.Result;
import com.boss.client.vo.TagVO;
import com.boss.client.dto.ConditionDTO;
import com.boss.common.dto.UpdateStatusDTO;
import com.boss.common.vo.SelectVO;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;


/**
 * 标签控制器
 */
@RestController
@RequestMapping("/tag")
public class TagController {
    @Autowired
    private TagServiceImpl tagService;

    /**
     * 获取标签列表
     * @param condition
     * @return
     */
    @GetMapping("/tagList")
    public Result<PageResult<TagVO>> ruleList(ConditionDTO condition) {
        return Result.ok(tagService.tagsListByCondition(condition));
    }

    /**
     * 物理删除
     */
    @PostMapping("/delete")
    public Result<?> updateTagStatus(@Valid @RequestBody UpdateStatusDTO updateStatusDTO) {
        tagService.deleteTag(updateStatusDTO.getIdList());
        return Result.ok();
    }

    @GetMapping("/getTag")
    public Result<TagVO> getTagById(@RequestParam("tag_id") long id) {
        return Result.ok(tagService.getTagById(id));
    }

    /**
     * 插入或更新
     */
    @PostMapping("/saveOrUpdate")
    public Result<?> saveOrUpdateTag(@Valid @RequestBody TagDTO tagDTO) {
        tagService.saveOrUpdateTag(tagDTO);
        return Result.ok();
    }

    /**
     * 获取类型
     */
    @GetMapping("/typeSelect")
    public Result<List<SelectVO>> getStatusSelect() {
        return Result.ok(tagService.getTypeSelect());
    }

}

