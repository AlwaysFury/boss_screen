package com.boss.bossscreen.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.boss.bossscreen.dao.SeckillIndexDao;
import com.boss.bossscreen.enities.excelEnities.SeckillIndex;
import com.boss.bossscreen.service.SeckillIndexService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
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
public class SeckillIndexServiceImpl extends ServiceImpl<SeckillIndexDao, SeckillIndex> implements SeckillIndexService {

    @Autowired
    private SeckillIndexDao seckillIndexDao;

    @Override
    public void importExcel(long shopId, MultipartFile file) {
        try {
            ExcelReader reader = ExcelUtil.getReader(file.getInputStream());

            List<Map<String, Object>> readAll = reader.readAll();

            List<SeckillIndex> seckillIndices = new ArrayList<>();

            for (Map<String, Object> map : readAll) {
                String dateStr = map.get("日期期间").toString();
                String[] dateSplit = dateStr.split("-");
                dateStr = dateSplit[0] + "-" + dateSplit[1] + "-" + dateSplit[2].substring(0,dateSplit[2].length() - 1);
                LocalDateTime createTime = str2LocalDateTime(dateStr);

                SeckillIndex seckillIndex = SeckillIndex.builder()
                        .id(IdUtil.getSnowflakeNextId())
                        .shopId(shopId)
                        .createTime(createTime)
                        .salesAmount(new BigDecimal(map.get("销售额(已确认订单)(฿)").toString().replace(",", "")))
                        .orderCount(Integer.parseInt(map.get("订单（已确认订单）").toString().replace(",", "")))
                        .displayVolume(Integer.parseInt(map.get("商品展示量").toString().replace(",", "")))
                        .clickVolume(Integer.parseInt(map.get("商品点击量").toString().replace(",", "")))
                        .customerPrice(new BigDecimal(map.get("客单价（已确认订单）(฿)").toString().replace(",", "")))
                        .clickRate(string2Double(map.get("点击率").toString())).build();

                SeckillIndex existSeckillIndex = seckillIndexDao.selectOne(new LambdaQueryWrapper<SeckillIndex>()
                        .select(SeckillIndex::getId)
                        .eq(SeckillIndex::getShopId, shopId)
                        .eq(SeckillIndex::getCreateTime, seckillIndex.getCreateTime()));

                if (Objects.nonNull(existSeckillIndex)) {
                    seckillIndex.setId(existSeckillIndex.getId());
                }

                seckillIndices.add(seckillIndex);
            }

            this.saveOrUpdateBatch(seckillIndices);

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
