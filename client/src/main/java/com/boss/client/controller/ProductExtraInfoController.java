package com.boss.client.controller;


import com.boss.client.service.impl.ProductExtraInfoServiceImpl;
import com.boss.client.vo.ProductExtraInfoVO;
import com.boss.client.vo.Result;
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
@RequestMapping("/productExtraInfo")
@Slf4j
public class ProductExtraInfoController {

    @Autowired
    private ProductExtraInfoServiceImpl productExtraInfoService;

    @GetMapping("/getProductExtraInfo")
    public Result<ProductExtraInfoVO> getProductExtraInfo(@RequestParam("item_id") Long itemId) {
        return Result.ok(productExtraInfoService.getProductExtraInfoByItemId(itemId));
    }
}
