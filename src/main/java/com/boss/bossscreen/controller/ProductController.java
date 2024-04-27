package com.boss.bossscreen.controller;

import com.boss.bossscreen.dto.ConditionDTO;
import com.boss.bossscreen.service.impl.ProductServiceImpl;
import com.boss.bossscreen.vo.PageResult;
import com.boss.bossscreen.vo.ProductInfoVO;
import com.boss.bossscreen.vo.ProductVO;
import com.boss.bossscreen.vo.Result;
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

@RestController
@RequestMapping("/product")
@Slf4j
public class ProductController {
    @Autowired
    private ProductServiceImpl productService;

    /**
     * 获取产品列表
     * @param condition
     * @return
     */
    @GetMapping("/productList")
    public Result<PageResult<ProductVO>> productList(ConditionDTO condition) {

        return Result.ok(productService.productListByCondition(condition));
    }

    /**
     * 根据产品 id 获取产品详细
     * @param itemId
     * @return
     */
    @GetMapping("/info")
    public Result<ProductInfoVO> getProductInfo(@RequestParam("item_id") Long itemId) {

        return Result.ok(productService.getProductInfo(itemId));
    }


}
