package com.boss.client.service.impl;

import cn.hutool.core.text.csv.CsvData;
import cn.hutool.core.text.csv.CsvReader;
import cn.hutool.core.text.csv.CsvRow;
import cn.hutool.core.text.csv.CsvUtil;
import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.boss.client.dao.AdsDao;
import com.boss.client.enities.excelEnities.Ads;
import com.boss.client.service.AdsService;
import com.boss.common.util.CommonUtil;
import com.boss.common.util.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.nio.charset.Charset;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

/**
 * 操作日志服务
 */
@Service
public class AdsServiceImpl extends ServiceImpl<AdsDao, Ads> implements AdsService {

    @Autowired
    private AdsDao adsDao;

    @Autowired
    @Qualifier("customThreadPool")
    private ThreadPoolExecutor customThreadPool;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Map<String, Set<Long>> importCsv(MultipartFile file, int adsOrderCount, int searchOrderCount, int searchAutoOrderCount, int searchManualOrderCount, int relationOrderCount) {

        Map<String, Set<Long>> resultMap = new HashMap<>();
        try {
            CsvReader csvReader = CsvUtil.getReader();
            CsvData csvData = csvReader.read(FileUtils.multipartFileToFile(file), Charset.forName("utf8"));


            List<CsvRow> rows = csvData.getRows();

            Long shopId = Long.parseLong(rows.get(3).get(1).toString());
            String tempTime = rows.get(5).get(1).toString().split("-")[0].replace(" ", "").replace("/", "-") + " 00:00:00";
            LocalDateTime createTime = CommonUtil.string2LocalDateTime(tempTime);

            List<CsvRow> infoRows = rows.subList(10, rows.size());

            List<Ads> autoList = new ArrayList<>();
            List<Ads> hmList = new ArrayList<>();
            List<Ads> adsList = new ArrayList<>();

            int totalShowCount = 0;
            int totalClickCount = 0;
            double totalClickRate = 0;
            int totalConversion = 0;
            BigDecimal totalSalesAmount = new BigDecimal(0);
            BigDecimal totalSpend = new BigDecimal(0);
            double totalAdsCostRate = 0;

            for (CsvRow row : infoRows) {
                List<String> rawList = row.getRawList();
                String status = rawList.get(2).toString();
                String bindType = rawList.get(4).toString();
                String position = rawList.get(5).toString();
                BigDecimal spend = new BigDecimal(rawList.get(21).toString());
                int clickCount = Integer.valueOf(rawList.get(9).toString());
                BigDecimal salesAmount = new BigDecimal(rawList.get(19).toString());

                double investmentOutputRatio = 0;
                if (spend.compareTo(new BigDecimal(0)) != 0) {
                    investmentOutputRatio = Double.parseDouble(new DecimalFormat("#.##").format(salesAmount.divide(spend, 2, RoundingMode.HALF_UP)));
                }

                BigDecimal clickPrice = new BigDecimal(0);
                if (clickCount != 0) {
                    clickPrice = spend.divide(new BigDecimal(clickCount), 2, BigDecimal.ROUND_HALF_UP);
                }

                int showCount = Integer.valueOf(rawList.get(8).toString());
                totalShowCount = totalShowCount + showCount;
                totalClickCount = totalClickCount + clickCount;
                double clickRate = string2Double(rawList.get(10).toString());
                totalClickRate = totalClickRate + clickRate;
                int conversion = Integer.valueOf(rawList.get(11).toString());
                totalConversion = totalConversion + conversion;
                totalSalesAmount = totalSalesAmount.add(salesAmount);
                totalSpend = totalSpend.add(spend);
                double adsCostRate = Double.valueOf(rawList.get(22).toString());
                totalAdsCostRate = totalAdsCostRate + adsCostRate;

                Ads ads = Ads.builder()
                        .id(IdUtil.getSnowflakeNextId())
                        .shopId(shopId)
                        .createTime(createTime)
                        .adsName(rawList.get(1).toString())
                        .status(status)
                        .itemId(Long.parseLong(rawList.get(3).toString()))
                        .bidType(bindType)
                        .position(position)
                        .showCount(showCount)
                        .clickCount(clickCount)
                        .clickRate(clickRate)
                        .conversion(conversion)
                        .salesAmount(salesAmount)
                        .spend(spend)
                        .adsCostRate(adsCostRate)
                        .clickPrice(clickPrice)
                        .investmentOutputRatio(investmentOutputRatio)
                        .type("single")
                        .build();

                Ads existAds = adsDao.selectOne(new LambdaQueryWrapper<Ads>()
                        .select(Ads::getId)
                        .eq(Ads::getAdsName, ads.getAdsName())
                        .eq(Ads::getItemId, ads.getItemId())
                        .eq(Ads::getCreateTime, ads.getCreateTime())
                        .eq(Ads::getType, ads.getType())
                        .eq(Ads::getShopId, shopId));

                if (Objects.nonNull(existAds)) {
                    ads.setId(existAds.getId());
                }

                adsList.add(ads);

                if ("正在进行".equals(status) && "手动竞价".equals(bindType) && "搜索".equals(position)) {
                    hmList.add(ads);
                }

                if ("正在进行".equals(status) && "自动竞价".equals(bindType) && "搜索".equals(position)) {
                    autoList.add(ads);
                }

            }

            double totalInvestmentOutputRatio = 0;
            if (totalSpend.compareTo(new BigDecimal(0)) != 0) {
                totalInvestmentOutputRatio = totalSalesAmount.divide(totalSpend, new MathContext(2, RoundingMode.HALF_UP)).doubleValue();
            }

            BigDecimal totalClickPrice = new BigDecimal(0);
            if (totalClickCount != 0) {
                totalClickPrice = totalSpend.divide(new BigDecimal(totalClickCount), 2, BigDecimal.ROUND_HALF_UP);
            }

            Ads totalAds = Ads.builder()
                    .id(IdUtil.getSnowflakeNextId())
                    .shopId(shopId)
                    .createTime(createTime)
                    .showCount(totalShowCount)
                    .clickCount(totalClickCount)
                    .clickRate(Double.parseDouble(new DecimalFormat("#.####").format(totalClickRate / infoRows.size())))
                    .conversion(totalConversion)
                    .salesAmount(totalSalesAmount)
                    .spend(totalSpend)
                    .adsCostRate(Double.parseDouble(new DecimalFormat("#.####").format(totalAdsCostRate / infoRows.size())))
                    .clickPrice(totalClickPrice)
                    .investmentOutputRatio(totalInvestmentOutputRatio)
                    .adsOrderCount(adsOrderCount)
                    .searchOrderCount(searchOrderCount)
                    .searchAutoOrderCount(searchAutoOrderCount)
                    .searchManualOrderCount(searchManualOrderCount)
                    .relationOrderCount(relationOrderCount)
                    .type("total")
                    .build();

            Ads existTotalAds = adsDao.selectOne(new LambdaQueryWrapper<Ads>()
                    .select(Ads::getId)
                    .eq(Ads::getCreateTime, totalAds.getCreateTime())
                    .eq(Ads::getType, totalAds.getType())
                    .eq(Ads::getShopId, shopId));

            if (Objects.nonNull(existTotalAds)) {
                totalAds.setId(existTotalAds.getId());
            }

            adsList.add(totalAds);

            resultMap.put("auto", getAdsId(autoList));
            resultMap.put("hm", getAdsId(hmList));


            CompletableFuture.runAsync(() -> {
                try {
                    this.saveOrUpdateBatch(adsList);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }, customThreadPool);

            csvReader.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return resultMap;
    }

    private Double string2Double(String str) {
        // 移除百分号
        String numberStr = str.replace("%", "");
        // 将处理后的字符串转换为double类型
        DecimalFormat df = new DecimalFormat("#.####");
        return Double.parseDouble(df.format(Double.parseDouble(numberStr) / 100));
    }

    private Set<Long> getAdsId(List<Ads> autoList) {

        Set<Long> clickCountSet = autoList.stream()
                .sorted(Comparator.comparingInt(Ads::getClickCount).reversed())
                .limit(10)
                .map(Ads::getItemId)
                .collect(Collectors.toSet());

        Set<Long> conversionSet = autoList.stream()
                .sorted(Comparator.comparingInt(Ads::getConversion).reversed())
                .limit(10)
                .map(Ads::getItemId)
                .collect(Collectors.toSet());
        clickCountSet.addAll(conversionSet);

        Set<Long> spendSet = autoList.stream()
                .sorted(Comparator.comparing(Ads::getSpend).reversed())
                .limit(10)
                .map(Ads::getItemId)
                .collect(Collectors.toSet());
        clickCountSet.addAll(spendSet);

        Set<Long> clickPriceSet = autoList.stream()
                .sorted(Comparator.comparing(Ads::getClickPrice).reversed())
                .limit(10)
                .map(Ads::getItemId)
                .collect(Collectors.toSet());
        clickCountSet.addAll(clickPriceSet);

        Set<Long> investmentOutputRatioSet = autoList.stream()
                .sorted(Comparator.comparingDouble(Ads::getInvestmentOutputRatio).reversed())
                .limit(10)
                .map(Ads::getItemId)
                .collect(Collectors.toSet());
        clickCountSet.addAll(investmentOutputRatioSet);

        return clickCountSet;
    }
}
