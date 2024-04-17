package com.boss.bossscreen.controller;

import com.boss.bossscreen.service.impl.ProductServiceImpl;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Description
 * @Author 罗宇航
 * @Date 2024/4/16
 */

@Api(tags = "店铺模块")
@RestController
@RequestMapping("/product")
@Slf4j
public class ProductController {
    @Autowired
    private ProductServiceImpl productService;

    // todo 产品列表
//    @ApiOperation(value = "获取所有产品")
//    @GetMapping("/shopsList")
//    public Result<PageResult<ProductVO>> productsList(ProductConditionDTO condition) {
//
//        return Result.ok(productService.productsByCondition(condition));
//    }


}
