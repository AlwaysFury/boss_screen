package com.boss.client.controller;

import com.boss.client.service.impl.*;
import com.boss.client.vo.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.Set;

/**
 * @Description
 * @Author 罗宇航
 * @Date 2024/6/15
 */

@RestController
@RequestMapping("/importExcel")
@Slf4j
public class ImportExcelController {

    @Autowired
    private ProductOverviewServiceImpl productOverviewService;

    @Autowired
    private SalesMixServiceImpl salesMixService;

    @Autowired
    private FlowOverviewServiceImpl flowOverviewService;

    @Autowired
    private FavorableServiceImpl favorableService;

    @Autowired
    private SeckillIndexServiceImpl seckillIndexService;

    @Autowired
    private CouponIndexServiceImpl couponIndexService;

    @Autowired
    private CouponExpressionServiceImpl couponExpressionService;

    @Autowired
    private SetDiscountsServiceImpl setDiscountsService;

    @Autowired
    private FocusGiftServiceImpl focusGiftService;

    @Autowired
    private ActivityServiceImpl activityService;

    @Autowired
    private AdsServiceImpl adsService;

    @Autowired
    private AdsDetailServiceImpl adsDetailService;

    @Autowired
    private ProductExpressionServiceImpl productExpressionService;

    // 商品概览
    @PostMapping("/productOverview")
    public Result<?> saveProductOverview(@RequestParam("shop_id") long shopId, String dateType, @RequestParam("file") MultipartFile file) {
        // dateType 天：day 小时：hour
        productOverviewService.importExcel(shopId, dateType, file);
        return Result.ok();
    }

    // 销售组合
    @PostMapping("/salesMix")
    public Result<String> saveSalesMix(@RequestParam("shop_id") long shopId, @RequestParam("date") String dateStr, @RequestParam("file") MultipartFile file) {
        salesMixService.importExcel(shopId, dateStr, file);
        return Result.ok();
    }

    // 流量概述
    @PostMapping("/flowOverview")
    public Result<String> saveFlowOverview(@RequestParam("shop_id") long shopId, @RequestParam("file") MultipartFile file) {
        flowOverviewService.importExcel(shopId, file);
        return Result.ok();
    }

    //加购优惠概述
    @PostMapping("/favorable")
    public Result<String> saveFlowOverview(@RequestParam("shop_id") long shopId, @RequestParam("date") String dateStr, @RequestParam("file") MultipartFile file) {
        favorableService.importExcel(shopId, dateStr, file);
        return Result.ok();
    }

    // 店内限时秒杀
    @PostMapping("/seckillIndex")
    public Result<String> saveSeckillIndex(@RequestParam("shop_id") long shopId, @RequestParam("file") MultipartFile file) {
        seckillIndexService.importExcel(shopId, file);
        return Result.ok();
    }

    // 优惠券
    @PostMapping("/coupon")
    public Result<String> saveCoupon(@RequestParam("shop_id") long shopId, @RequestParam("date") String dateStr, @RequestParam("file") MultipartFile file) {
        couponIndexService.importExcel(shopId, file);
        couponExpressionService.importExcel(shopId, dateStr, file);
        return Result.ok();
    }

    // 套装
    @PostMapping("/setDiscounts")
    public Result<String> saveSetDiscounts(@RequestParam("shop_id") long shopId, @RequestParam("date") String dateStr, @RequestParam("file") MultipartFile file) {
        setDiscountsService.importExcel(shopId, dateStr, file);
        return Result.ok();
    }

    // 关注礼
    @PostMapping("/focusGifts")
    public Result<String> saveFocusGifts(@RequestParam("shop_id") long shopId, @RequestParam("file") MultipartFile file) {
        focusGiftService.importExcel(shopId, file);
        return Result.ok();
    }

    // 活动
    @PostMapping("/activity")
    public Result<String> saveActivity(@RequestParam("shop_id") long shopId,
                                       @RequestParam("main_name") String mainName,
                                       @RequestParam("sub_name") String subName,
                                       @RequestParam("date") String date,
                                       @RequestParam("file") MultipartFile file) {
        activityService.importExcel(shopId, mainName, subName, date, file);
        return Result.ok();
    }

    // 广告
    @PostMapping("/ads")
    public Result<Map<String, Set<Long>>> saveAds(@RequestParam("file") MultipartFile file,
                                                  @RequestParam("ads_order_count") int adsOrderCount,
                                                  @RequestParam("search_order_count") int searchOrderCount,
                                                  @RequestParam("search_auto_order_count") int searchAutoOrderCount,
                                                  @RequestParam("search_manual_order_count") int searchManualOrderCount,
                                                  @RequestParam("relation_order_count") int relationOrderCount) {
        return Result.ok(adsService.importCsv(file, adsOrderCount, searchOrderCount, searchAutoOrderCount, searchManualOrderCount, relationOrderCount));
    }

    // 广告详情
    @PostMapping("/adsDetail")
    public Result<?> saveAdsDetail(@RequestParam("file") MultipartFile file, @RequestParam("type") String type) {
        adsDetailService.importCsv(file, type);
        return Result.ok();
    }

    // 广告详情
    @PostMapping("/productExpression")
    public Result<?> saveProductExpression(@RequestParam("file") MultipartFile file) {
        productExpressionService.importExcel(file);
        return Result.ok();
    }
}
