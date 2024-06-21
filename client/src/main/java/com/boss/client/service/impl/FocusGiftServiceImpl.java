package com.boss.client.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.boss.client.dao.FocusGiftDao;
import com.boss.client.service.FocusGiftService;
import com.boss.common.enities.excelEnities.FocusGift;
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
public class FocusGiftServiceImpl extends ServiceImpl<FocusGiftDao, FocusGift> implements FocusGiftService {

    @Autowired
    private FocusGiftDao focusGiftDao;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void importExcel(long shopId, MultipartFile file) {
        try {
            ExcelReader reader = ExcelUtil.getReader(file.getInputStream());

            List<Map<String, Object>> readAll = reader.readAll();

            List<FocusGift> focusGifts = new ArrayList<>();

            for (Map<String, Object> map : readAll) {
                String dateStr = map.get("日期期间").toString();
                String[] dateSplit = dateStr.split("-");
                dateStr = dateSplit[0] + "-" + dateSplit[1] + "-" + dateSplit[2].substring(0,dateSplit[2].length() - 1);
                LocalDateTime createTime = str2LocalDateTime(dateStr);

                FocusGift focusGift = FocusGift.builder()
                        .id(IdUtil.getSnowflakeNextId())
                        .shopId(shopId)
                        .createTime(createTime)
                        .salesAmount(new BigDecimal(map.get("销售额(已确认订单)(฿)").toString().replace(",", "")))
                        .orderCount(Integer.parseInt(map.get("订单（已确认订单）").toString().replace(",", "")))
                        .spend(new BigDecimal(map.get("花费(已确认订单)(฿)").toString().replace(",", "")))
                        .useRate(string2Double(map.get("使用率(已确认订单)").toString()))
                        .newFollowerCount(Integer.parseInt(map.get("新关注者").toString().replace(",", "")))
                        .receiveCount(Integer.parseInt(map.get("独立领取数量").toString().replace(",", ""))).build();

                FocusGift existFocusGift = focusGiftDao.selectOne(new LambdaQueryWrapper<FocusGift>()
                        .select(FocusGift::getId)
                        .eq(FocusGift::getShopId, shopId)
                        .eq(FocusGift::getCreateTime, focusGift.getCreateTime()));

                if (Objects.nonNull(existFocusGift)) {
                    focusGift.setId(existFocusGift.getId());
                }

                focusGifts.add(focusGift);
            }

            this.saveOrUpdateBatch(focusGifts);

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
