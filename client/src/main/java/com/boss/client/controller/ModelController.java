package com.boss.client.controller;


import com.boss.client.service.impl.ModelServiceImpl;
import com.boss.client.vo.ModelVO;
import com.boss.client.vo.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @Description
 * @Author 罗宇航
 * @Date 2024/4/16
 */

@RestController
@RequestMapping("/model")
@Slf4j
public class ModelController {

    @Autowired
    private ModelServiceImpl modelService;

    /**
     * 获取
     * @param itemId
     * @return
     */
    @GetMapping("/getModel")
    public Result<List<ModelVO>> getModel(@RequestParam("item_id") Long itemId) {
        return Result.ok(modelService.getModelVOListByItemId(itemId));
    }

}
