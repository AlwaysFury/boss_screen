package com.boss.client.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.boss.client.dao.FavorableDao;
import com.boss.client.service.FavorableService;
import com.boss.client.util.CommonUtil;
import com.boss.common.enities.excelEnities.Favorable;
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
public class FavorableServiceImpl extends ServiceImpl<FavorableDao, Favorable> implements FavorableService {

    @Autowired
    private FavorableDao favorableDao;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void importExcel(long shopId, String dateStr, MultipartFile file) {
        try {
            ExcelReader reader = ExcelUtil.getReader(file.getInputStream(), 2);

            List<Map<String, Object>> readAll = reader.readAll();

            List<Favorable> favorables = new ArrayList<>();

            readAll.forEach(map -> {
                if ("整体".equals(map.get("类型").toString())) {
                    LocalDateTime dateTime = CommonUtil.string2LocalDateTime(dateStr);

                    Favorable favorable = Favorable.builder()
                            .id(IdUtil.getSnowflakeNextId())
                            .shopId(shopId)
                            .createTime(dateTime)
                            .couponName(map.get("加购优惠名称").toString())
                            .saleProductCount(Integer.parseInt(map.get("销售商品件数(已确认订单)").toString().replace(",", "")))
                            .orderCount(Integer.parseInt(map.get("订单（已确认订单）").toString().replace(",", "")))
                            .customerPrice(new BigDecimal(map.get("客单价（已确认订单） (THB)").toString().replace(",", ""))).build();

                    Favorable existFavorable = favorableDao.selectOne(new LambdaQueryWrapper<Favorable>()
                            .select(Favorable::getId)
                            .eq(Favorable::getShopId, shopId)
                            .eq(Favorable::getCreateTime, dateTime));

                    if (Objects.nonNull(existFavorable)) {
                        favorable.setId(existFavorable.getId());
                    }

                    favorables.add(favorable);
                }
            });

            this.saveOrUpdateBatch(favorables);

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
