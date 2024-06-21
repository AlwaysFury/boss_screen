package com.boss.client.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.boss.client.dao.SetDiscountsDao;
import com.boss.client.service.SetDiscountsService;
import com.boss.client.util.CommonUtil;
import com.boss.common.enities.excelEnities.SetDiscounts;
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
public class SetDiscountsServiceImpl extends ServiceImpl<SetDiscountsDao, SetDiscounts> implements SetDiscountsService {

    @Autowired
    private SetDiscountsDao setDiscountsDao;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void importExcel(long shopId, String dateStr, MultipartFile file) {
        try {
            ExcelReader reader = ExcelUtil.getReader(file.getInputStream(), 2);

            List<Map<String, Object>> readAll = reader.readAll();

            List<SetDiscounts> setDiscounts = new ArrayList<>();

            for (Map<String, Object> map : readAll) {

                LocalDateTime dateTime = CommonUtil.string2LocalDateTime(dateStr);

                SetDiscounts couponExpression = SetDiscounts.builder()
                        .id(IdUtil.getSnowflakeNextId())
                        .shopId(shopId)
                        .createTime(dateTime)
                        .setName(map.get("套装名称").toString())
                        .orderCount(Integer.parseInt(map.get("订单（已确认订单）").toString().replace(",", "")))
                        .saleProductCount(Integer.parseInt(map.get("销售商品件数(已确认订单)").toString().replace(",", "")))
                        .customerPrice(new BigDecimal(map.get("客单价（已确认订单）(฿)").toString())).build();

                SetDiscounts existSetDiscounts = setDiscountsDao.selectOne(new LambdaQueryWrapper<SetDiscounts>()
                        .select(SetDiscounts::getId)
                        .eq(SetDiscounts::getShopId, shopId)
                        .eq(SetDiscounts::getCreateTime, dateTime));


                if (Objects.nonNull(existSetDiscounts)) {
                    couponExpression.setId(existSetDiscounts.getId());
                }

                setDiscounts.add(couponExpression);
            }

            this.saveOrUpdateBatch(setDiscounts);

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
