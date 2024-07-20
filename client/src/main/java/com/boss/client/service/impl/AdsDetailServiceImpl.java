package com.boss.client.service.impl;

import cn.hutool.core.text.csv.CsvData;
import cn.hutool.core.text.csv.CsvReader;
import cn.hutool.core.text.csv.CsvRow;
import cn.hutool.core.text.csv.CsvUtil;
import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.boss.client.dao.AdsDetailDao;
import com.boss.client.enities.excelEnities.AdsDetail;
import com.boss.client.service.AdsDetailService;
import com.boss.common.util.CommonUtil;
import com.boss.common.util.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.Charset;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 操作日志服务
 */
@Service
public class AdsDetailServiceImpl extends ServiceImpl<AdsDetailDao, AdsDetail> implements AdsDetailService {

    @Autowired
    private AdsDetailDao adsDetailDao;

    @Autowired
    @Qualifier("customThreadPool")
    private ThreadPoolExecutor customThreadPool;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void importCsv(MultipartFile file, String type) {
        try {
            CsvReader csvReader = CsvUtil.getReader();
            CsvData csvData = csvReader.read(FileUtils.multipartFileToFile(file), Charset.forName("utf8"));

            List<CsvRow> rows = csvData.getRows();

            Long shopId = Long.parseLong(rows.get(3).get(1).toString());
            String adsName = rows.get(4).get(1).toString();
            Long itemId = Long.parseLong(rows.get(5).get(1).toString());

            String tempTime = rows.get(8).get(1).toString().split("-")[0].replace(" ", "").replace("/", "-") + " 00:00:00";
            LocalDateTime createTime = CommonUtil.string2LocalDateTime(tempTime);

            List<CsvRow> infoRows = new ArrayList<>();

            if ("hm".equals(type)) {
                infoRows = rows.subList(12, rows.size() - 3);
            } else if ("auto".equals(type)) {
                infoRows = rows.subList(11, rows.size());
            }

            List<AdsDetail> adsDetailList = new ArrayList<>();
            for (CsvRow row : infoRows) {

                int clickCount = "hm".equals(type) ? Integer.valueOf(row.get(6).toString()) : Integer.valueOf(row.get(7).toString());

                if (clickCount > 0) {
                    BigDecimal spend = "hm".equals(type) ? new BigDecimal(row.get(18).toString()) : new BigDecimal(row.get(19).toString());

                    BigDecimal salesAmount = "hm".equals(type) ? new BigDecimal(row.get(16).toString()) : new BigDecimal(row.get(17).toString());
                    double investmentOutputRatio = 0.00;
                    if (spend.compareTo(new BigDecimal(0)) != 0) {
                        investmentOutputRatio = Double.parseDouble(new DecimalFormat("#.##").format(salesAmount.divide(spend, 2, RoundingMode.HALF_UP)));
                    }

                    BigDecimal clickPrice = spend.divide(new BigDecimal(clickCount), 2, BigDecimal.ROUND_HALF_UP);

                    AdsDetail adsDetail = AdsDetail.builder()
                            .id(IdUtil.getSnowflakeNextId())
                            .shopId(shopId)
                            .createTime(createTime)
                            .adsName(adsName)
                            .itemId(itemId)
                            .keyword(row.get(1).toString())
                            .matchType(row.get(2).toString())
                            .searchCount(row.get(3).toString())
                            .showCount("hm".equals(type) ? Integer.valueOf(row.get(5).toString()) : Integer.valueOf(row.get(6).toString()))
                            .clickCount(clickCount)
                            .clickRate("hm".equals(type) ? string2Double(row.get(7).toString()) : string2Double(row.get(8).toString()))
                            .conversion("hm".equals(type) ? Integer.valueOf(row.get(8).toString()) : Integer.valueOf(row.get(9).toString()))
                            .salesAmount(salesAmount)
                            .spend(spend)
                            .averageRank("hm".equals(type) ? Integer.valueOf(row.get(19).toString()) : Integer.valueOf(row.get(20).toString()))
                            .adsCostRate("hm".equals(type) ? Double.valueOf(row.get(20).toString()) : Double.valueOf(row.get(21).toString()))
                            .clickPrice(clickPrice)
                            .investmentOutputRatio(investmentOutputRatio)
                            .type(type)
                            .build();

                    AdsDetail existAdsDetail = adsDetailDao.selectOne(new LambdaQueryWrapper<AdsDetail>()
                            .select(AdsDetail::getId)
                            .eq(AdsDetail::getShopId, adsDetail.getShopId())
                            .eq(AdsDetail::getAdsName, adsDetail.getAdsName())
                            .eq(AdsDetail::getItemId, adsDetail.getItemId())
                            .eq(AdsDetail::getMatchType, adsDetail.getMatchType())
                            .eq(AdsDetail::getKeyword, adsDetail.getKeyword())
                            .eq(AdsDetail::getSearchCount, adsDetail.getSearchCount())
                            .eq(AdsDetail::getShowCount, adsDetail.getShowCount())
                            .eq(AdsDetail::getClickCount, adsDetail.getClickCount())
                            .eq(AdsDetail::getClickRate, adsDetail.getClickRate())
                            .eq(AdsDetail::getConversion, adsDetail.getConversion())
                            .eq(AdsDetail::getSalesAmount, adsDetail.getSalesAmount())
                            .eq(AdsDetail::getSpend, adsDetail.getSpend())
                            .eq(AdsDetail::getAverageRank, adsDetail.getAverageRank())
                            .eq(AdsDetail::getAdsCostRate, adsDetail.getAdsCostRate())
                            .eq(AdsDetail::getClickPrice, adsDetail.getClickPrice())
                            .eq(AdsDetail::getInvestmentOutputRatio, adsDetail.getInvestmentOutputRatio())
                            .eq(AdsDetail::getCreateTime, adsDetail.getCreateTime())
                            .eq(AdsDetail::getType, adsDetail.getType())
                            .eq(AdsDetail::getShopId, shopId));

                    if (Objects.nonNull(existAdsDetail)) {
                        adsDetail.setId(existAdsDetail.getId());
                    }

                    adsDetailList.add(adsDetail);
                }


            }

            CompletableFuture.runAsync(() -> {
                try {
                    this.saveOrUpdateBatch(adsDetailList);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }, customThreadPool);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Double string2Double(String str) {
        // 移除百分号
        String numberStr = str.replace("%", "");
        // 将处理后的字符串转换为double类型
        DecimalFormat df = new DecimalFormat("#.##");
        return Double.parseDouble(df.format(Double.parseDouble(numberStr) / 100));
    }
}
