package com.boss.client.controller;


import com.boss.client.service.impl.ProductServiceImpl;
import com.boss.client.vo.PageResult;
import com.boss.client.vo.ProductInfoVO;
import com.boss.client.vo.ProductVO;
import com.boss.client.vo.Result;
import com.boss.common.dto.ConditionDTO;
import com.boss.common.dto.UpdateStatusDTO;
import com.boss.common.vo.SelectVO;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    /**
     * 获取分类
     */
    @GetMapping("/categorySelect")
    public Result<List<SelectVO>> getCategorySelect() {
        return Result.ok(productService.getCategorySelect());
    }

    /**
     * 获取状态
     */
    @GetMapping("/statusSelect")
    public Result<List<SelectVO>> getStatusSelect() {
        return Result.ok(productService.getStatusSelect());
    }

    @PostMapping("/refreshProducts")
    public Result<?> refreshProducts(@Valid @RequestBody UpdateStatusDTO updateStatusDTO) {
        productService.refreshProducts(updateStatusDTO.getIdList());
        return Result.ok();
    }


}
