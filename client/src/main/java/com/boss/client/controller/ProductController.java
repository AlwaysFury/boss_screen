package com.boss.client.controller;


import com.boss.client.dto.ConditionDTO;
import com.boss.client.dto.ProductTagDTO;
import com.boss.client.service.impl.*;
import com.boss.client.vo.*;
import com.boss.common.dto.RefreshDTO;
import com.boss.common.vo.SelectVO;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.boss.common.enums.TagTypeEnum.ITEM;

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

    @Autowired
    private GradeServiceImpl gradeService;

    @Autowired
    private RuleServiceImpl ruleService;

    @Autowired
    private TagServiceImpl tagService;

    @Autowired
    private ProductOrImgTagServiceImpl productOrImgTagService;

    @Autowired
    private ProductExpressionServiceImpl productExpressionService;

    @Autowired
    private ActivityServiceImpl activityService;

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

    /**
     * 刷新产品
     * @param refreshDTO
     * @return
     */
    @PostMapping("/refreshProducts")
    public Result<?> refreshProducts(@Valid @RequestBody RefreshDTO refreshDTO) {
        productService.refreshProducts(refreshDTO);
        return Result.ok("", "已提交后台任务，请稍后查看");
    }

    /**
     * 保存标签
     * @param productTagDTO
     * @return
     */
    @PostMapping("/saveTag")
    public Result<?> saveProductTag(@Valid @RequestBody ProductTagDTO productTagDTO) {
        productOrImgTagService.saveProductOrImgTag(productTagDTO.getTagNameList(), productTagDTO.getId(), ITEM.getCode());
        return Result.ok();
    }

    /**
     * 刷新图片等级
     * @return
     */
    @GetMapping("/refreshGrade")
    public Result<?> refreshGrade() {
        gradeService.refreshGrade(ITEM.getCode());
        return Result.ok("", "已提交后台任务，请稍后查看");
    }

    @GetMapping("/getNewerSaleProduct")
    public Result<List<String>> getNewerSaleProduct() {
        return Result.ok(productService.getNewerSaleProductNames());
    }

    @GetMapping("/gradeSelect")
    public Result<List<SelectVO>> gradeSelect() {
        return Result.ok(ruleService.gradeSelect(ITEM.getCode()));
    }

    @GetMapping("/tagSelect")
    public Result<List<SelectVO>> tagSelect() {
        return Result.ok(tagService.tagSelect(ITEM.getCode()));
    }

    @GetMapping("/getProductExpressionInfo")
    public Result<List<ProductExpressionVO>> getProductExpressionInfo(@RequestParam("item_id") Long itemId) {
        return Result.ok(productExpressionService.getProductExpressionInfoById(itemId));
    }

    @GetMapping("/getActivityInfo")
    public Result<List<ActivityVO>> getActivityInfoByItemId(@RequestParam("item_id") Long itemId) {
        return Result.ok(activityService.getActivityInfoByItemId(itemId));
    }

}
