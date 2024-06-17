package com.boss.bossscreen.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.boss.bossscreen.dao.FlowOverviewDao;
import com.boss.bossscreen.enities.excelEnities.FlowOverview;
import com.boss.bossscreen.service.FlowOverviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

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
public class FlowOverviewServiceImpl extends ServiceImpl<FlowOverviewDao, FlowOverview> implements FlowOverviewService {

    @Autowired
    private FlowOverviewDao flowOverviewDao;

    @Override
    public void importExcel(long shopId, MultipartFile file) {
        try {
            ExcelReader reader = ExcelUtil.getReader(file.getInputStream());

            List<Map<String, Object>> readAll = reader.readAll();

            List<FlowOverview> flowOverviews = new ArrayList<>();

            for (Map<String, Object> map : readAll) {
                if ("日期".equals(map.get("日期"))) {
                    continue;
                }

                FlowOverview flowOverview = FlowOverview.builder()
                        .id(IdUtil.getSnowflakeNextId())
                        .shopId(shopId)
                        .visitorCount(Integer.parseInt(map.get("访客数").toString().replace(",", "")))
                        .newVisitorCount(Integer.parseInt(map.get("新访客").toString().replace(",", "")))
                        .currentVisitorCount(Integer.parseInt(map.get("现有访客").toString().replace(",", "")))
                        .newFollowerCount(Integer.parseInt(map.get("新关注者").toString().replace(",", "")))
                        .pageViewCount(Integer.parseInt(map.get("页面浏览数").toString().replace(",", "")))
                        .avgPageViewCount(Float.parseFloat(map.get("平均页面访问数").toString()))
                        .avgStayTime(map.get("平均停留时长").toString())
                        .bounceRate(string2Double(map.get("跳出率").toString())).build();

                String dateStr = map.get("日期").toString();
                String[] dateSplit = dateStr.split("-");
                LocalDateTime createTime;
                if (dateStr.contains("-") && dateSplit.length == 6) {
                    dateStr = dateSplit[0] + "-" + dateSplit[1] + "-" + dateSplit[2] + " 00:00";
                    createTime = str2LocalDateTime(dateStr);
                    flowOverview.setCreateTime(createTime);
                    flowOverview.setDateType("day");
                }

                if (dateStr.contains("-") && dateSplit.length == 3) {
                    createTime = str2LocalDateTime(dateStr);
                    flowOverview.setCreateTime(createTime);
                    flowOverview.setDateType("hour");
                }

                FlowOverview existFlowOverview = flowOverviewDao.selectOne(new LambdaQueryWrapper<FlowOverview>()
                        .select(FlowOverview::getId)
                        .eq(FlowOverview::getShopId, shopId)
                        .eq(FlowOverview::getDateType, flowOverview.getDateType())
                        .eq(FlowOverview::getCreateTime, flowOverview.getCreateTime()));

                if (Objects.nonNull(existFlowOverview)) {
                    flowOverview.setId(existFlowOverview.getId());
                }

                flowOverviews.add(flowOverview);
            }

            this.saveOrUpdateBatch(flowOverviews);

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
