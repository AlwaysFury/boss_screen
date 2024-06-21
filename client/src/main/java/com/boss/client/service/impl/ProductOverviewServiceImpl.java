package com.boss.client.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.boss.client.dao.ProductOverviewDao;
import com.boss.client.service.ProductOverviewService;
import com.boss.common.enities.excelEnities.ProductOverview;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 操作日志服务
 */
@Service
public class ProductOverviewServiceImpl extends ServiceImpl<ProductOverviewDao, ProductOverview> implements ProductOverviewService {

    @Autowired
    private ProductOverviewDao productOverviewDao;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void importExcel(long shopId, String dateType, MultipartFile file) {
        try {
            ExcelReader reader = ExcelUtil.getReader(file.getInputStream());

            List<Map<String, Object>> readAll = reader.readAll();

            List<ProductOverview> productOverviews = new ArrayList<>();
            readAll.forEach(map -> {
                LocalDateTime createTime;
                if ("day".equals(dateType)) {
                    createTime = str2LocalDateTime(map.get("日期").toString() + " 00:00");
                } else {
                    createTime = str2LocalDateTime(map.get("日期").toString());
                }

                ProductOverview productOverview = ProductOverview.builder()
                        .id(IdUtil.getSnowflakeNextId())
                        .shopId(shopId)
                        .dateType(dateType)
                        .createTime(createTime)
                        .addCartRate(string2Double(map.get("转化率 (加入购物车率)").toString()))
                        .orderRate(string2Double(map.get("转化率（已下订单）").toString()))
                        .confirmOrderRate(string2Double(map.get("转化率（已确定订单）").toString()))
                        .buyerCount(Integer.parseInt(map.get("买家数（已确定订单）").toString().replace(",", "")))
                        .confirmProductCount(Integer.parseInt(map.get("已确定的商品").toString().replace(",", "")))
                        .productCount(Integer.parseInt(map.get("件数（已确定订单）").toString().replace(",", "")))
                        .visitorCount(Integer.parseInt(map.get("商品访客数量").toString().replace(",", "")))
                        .salesAmount(new BigDecimal(0)).build();

                ProductOverview existProductOverview = productOverviewDao.selectOne(new LambdaQueryWrapper<ProductOverview>()
                        .select(ProductOverview::getId)
                        .eq(ProductOverview::getShopId, shopId)
                        .eq(ProductOverview::getDateType, dateType)
                        .eq(ProductOverview::getCreateTime, createTime));

                if (Objects.nonNull(existProductOverview)) {
                    productOverview.setId(existProductOverview.getId());
                }

                productOverviews.add(productOverview);
            });

            this.saveOrUpdateBatch(productOverviews);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    private Double string2Double(String str) {
        // 移除百分号
        String numberStr = str.replace("%", "");
        // 将处理后的字符串转换为double类型
        DecimalFormat df = new DecimalFormat("#.####");
        return Double.parseDouble(df.format(Double.parseDouble(numberStr) / 100));
    }

    private LocalDateTime str2LocalDateTime(String str) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
        return LocalDateTime.parse(str, formatter);
    }
}
