package com.boss.client.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.boss.client.dao.CouponExpressionDao;
import com.boss.client.service.CouponExpressionService;
import com.boss.common.enities.excelEnities.CouponExpression;
import com.boss.common.util.CommonUtil;
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
public class CouponExpressionServiceImpl extends ServiceImpl<CouponExpressionDao, CouponExpression> implements CouponExpressionService {

    @Autowired
    private CouponExpressionDao couponExpressionDao;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void importExcel(long shopId, String dateStr, MultipartFile file) {
        try {
            ExcelReader reader = ExcelUtil.getReader(file.getInputStream(), 2);

            List<Map<String, Object>> readAll = reader.readAll();

            List<CouponExpression> couponExpressions = new ArrayList<>();

            for (Map<String, Object> map : readAll) {
                if ("粉丝关注礼".equals(map.get("优惠券名称"))) {
                    continue;
                }

                LocalDateTime dateTime = CommonUtil.string2LocalDateTime(dateStr);

                CouponExpression couponExpression = CouponExpression.builder()
                        .id(IdUtil.getSnowflakeNextId())
                        .shopId(shopId)
                        .createTime(dateTime)
                        .couponName(map.get("优惠券名称").toString())
                        .salesAmount(new BigDecimal(map.get("销售额(已确认订单)(฿)").toString().replace(",", "")))
                        .spend(new BigDecimal(map.get("花费(已确认订单)(฿)").toString().replace(",", "")))
                        .customerPrice(new BigDecimal(map.get("客单价（已确认订单）(฿)").toString().replace(",", "")))
                        .receiveCount(Integer.parseInt(map.get("领取").toString().replace(",", "")))
                        .orderCount(Integer.parseInt(map.get("订单（已确认订单）").toString().replace(",", "")))
                        .saleProductCount(Integer.parseInt(map.get("销售商品件数(已确认订单)").toString().replace(",", "")))
                        .useRate(string2Double(map.get("使用率(已确认订单)").toString())).build();

                CouponExpression existCouponExpression = couponExpressionDao.selectOne(new LambdaQueryWrapper<CouponExpression>()
                        .select(CouponExpression::getId)
                        .eq(CouponExpression::getShopId, shopId)
                        .eq(CouponExpression::getCreateTime, dateTime));


                if (Objects.nonNull(existCouponExpression)) {
                    couponExpression.setId(existCouponExpression.getId());
                }

                couponExpressions.add(couponExpression);
            }

            this.saveOrUpdateBatch(couponExpressions);

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
