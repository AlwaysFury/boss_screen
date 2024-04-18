package com.boss.bossscreen.controller;

import com.boss.bossscreen.dto.ConditionDTO;
import com.boss.bossscreen.service.impl.ProductServiceImpl;
import com.boss.bossscreen.vo.PageResult;
import com.boss.bossscreen.vo.ProductDetailVO;
import com.boss.bossscreen.vo.ProductVO;
import com.boss.bossscreen.vo.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Description
 * @Author 罗宇航
 * @Date 2024/4/16
 */

@Api(tags = "产品模块")
@RestController
@RequestMapping("/product")
@Slf4j
public class ProductController {
    @Autowired
    private ProductServiceImpl productService;

    @ApiOperation(value = "获取所有产品")
    @GetMapping("/productList")
    public Result<PageResult<ProductVO>> productList(ConditionDTO condition) {

        return Result.ok(productService.productListByCondition(condition));
    }

    @ApiOperation(value = "根据产品 id 获取产品详细")
    @GetMapping("/detail")
    public Result<ProductDetailVO> getProductDetail(@RequestParam("item_id") Long itemId) {

        return Result.ok(productService.getProductDetail(itemId));
    }


}
