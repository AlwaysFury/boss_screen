package com.boss.client.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.boss.client.dao.ProductExpressionDao;
import com.boss.client.enities.excelEnities.ProductExpression;
import com.boss.client.service.ProductExpressionService;
import com.boss.client.vo.ProductExpressionVO;
import com.boss.common.util.BeanCopyUtils;
import com.boss.common.util.CommonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 操作日志服务
 */
@Service
public class ProductExpressionServiceImpl extends ServiceImpl<ProductExpressionDao, ProductExpression> implements ProductExpressionService {

    @Autowired
    private ProductExpressionDao productExpressionDao;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void importExcel(MultipartFile file) {
        try {
            ExcelReader reader = ExcelUtil.getReader(file.getInputStream());

            List<Map<String, Object>> readAll = reader.readAll();

            List<ProductExpression> productExpressions = new ArrayList<>();
            readAll.forEach(map -> {

                Long itemId = Long.valueOf(map.get("商品编号").toString());
                Object createTimeObject = map.get("");
                LocalDateTime createTime = str2LocalDateTime(createTimeObject == null ? map.get("日期").toString() + " 00:00": createTimeObject.toString() + " 00:00");

                ProductExpression productExpression = ProductExpression.builder()
                        .id(IdUtil.getSnowflakeNextId())
                        .createTime(createTime)
                        .itemId(Long.parseLong(map.get("商品编号").toString()))
                        .likes(Integer.parseInt(map.get("赞").toString().replace(",", "")))
                        .addCartRate(string2Double(map.get("转化率（加入购物车）").toString()))
                        .confirmOrderRate(string2Double(map.get("转化率（已确定订单）").toString()))
                        .productCount(Integer.parseInt(map.get("件数（已确定订单）").toString().replace(",", "")))
                        .visitorCount(Integer.parseInt(map.get("商品访客数量").toString().replace(",", ""))).build();

                ProductExpression existProductExpression = productExpressionDao.selectOne(new LambdaQueryWrapper<ProductExpression>()
                        .select(ProductExpression::getId)
                        .eq(ProductExpression::getItemId, itemId)
                        .eq(ProductExpression::getCreateTime, createTime));

                if (Objects.nonNull(existProductExpression)) {
                    productExpression.setId(existProductExpression.getId());
                }

                productExpressions.add(productExpression);
            });

            this.saveOrUpdateBatch(productExpressions);
            reader.close();
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

    private String double2String(double number) {
        NumberFormat percentFormat = NumberFormat.getPercentInstance();
        // 设置小数点后保留的位数
        percentFormat.setMaximumFractionDigits(2);
        return percentFormat.format(number);
    }

    private LocalDateTime str2LocalDateTime(String str) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
        return LocalDateTime.parse(str, formatter);
    }

    @Override
    public List<ProductExpressionVO> getProductExpressionInfoById(long itemId) {
        List<ProductExpressionVO> productOverview = productExpressionDao.selectList(new LambdaQueryWrapper<ProductExpression>()
                .select(ProductExpression::getId,
                        ProductExpression::getItemId,
                        ProductExpression::getCreateTime,
                        ProductExpression::getLikes,
                        ProductExpression::getVisitorCount,
                        ProductExpression::getAddCartRate,
                        ProductExpression::getConfirmOrderRate,
                        ProductExpression::getProductCount)
                .eq(ProductExpression::getItemId, itemId)
                        .orderByDesc(ProductExpression::getCreateTime))
                .stream()
                .map(productExpression -> {
                    ProductExpressionVO productExpressionVO = BeanCopyUtils.copyObject(productExpression, ProductExpressionVO.class);
                    productExpressionVO.setCreateTime(CommonUtil.localDateTime2String(productExpression.getCreateTime(), "yyyy-MM-dd"));
                    productExpressionVO.setAddCartRate(productExpression.getAddCartRate() != 0 ? double2String(productExpression.getAddCartRate()) : "0");
                    productExpressionVO.setConfirmOrderRate(productExpression.getConfirmOrderRate() != 0 ? double2String(productExpression.getConfirmOrderRate()) : "0");
                    return productExpressionVO;
                }).collect(Collectors.toList());
        return productOverview;
    }


}
