package com.boss.client.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.boss.client.dao.SalesMixDao;
import com.boss.client.service.SalesMixService;
import com.boss.client.util.CommonUtil;
import com.boss.common.enities.excelEnities.SalesMix;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * 操作日志服务
 */
@Service
public class SalesMixServiceImpl extends ServiceImpl<SalesMixDao, SalesMix> implements SalesMixService {

    @Autowired
    private SalesMixDao salesMixDao;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void importExcel(long shopId, String dateStr, MultipartFile file) {
        try {
            ExcelReader reader = ExcelUtil.getReader(file.getInputStream(), 1);
            List<List<Object>> readAll = reader.read();

            int newBuyerCount = 0;
            int currentBuyerCount = 0;
            for (List<Object> list : readAll) {
                if ("新买家".equals(list.get(0))) {
                    newBuyerCount = Integer.parseInt(list.get(1).toString());
                }
                if ("现有买家".equals(list.get(0))) {
                    currentBuyerCount = Integer.parseInt(list.get(1).toString());
                }
            }
            LocalDateTime dateTime = CommonUtil.string2LocalDateTime(dateStr);

            SalesMix salesMix = SalesMix.builder()
                    .id(IdUtil.getSnowflakeNextId())
                    .shopId(shopId)
                    .createTime(dateTime)
                    .newBuyerCount(newBuyerCount)
                    .currentBuyerCount(currentBuyerCount).build();

            SalesMix existSalesMix = salesMixDao.selectOne(new LambdaQueryWrapper<SalesMix>()
                    .select(SalesMix::getId)
                    .eq(SalesMix::getShopId, shopId)
                    .eq(SalesMix::getCreateTime, dateTime));
            if (Objects.nonNull(existSalesMix)) {
                salesMix.setId(existSalesMix.getId());
            }

            this.saveOrUpdate(salesMix);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
}
