package com.boss.client.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.boss.client.dao.ActivityDao;
import com.boss.client.enities.excelEnities.Activity;
import com.boss.client.service.ActivityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 操作日志服务
 */
@Service
public class ActivityServiceImpl extends ServiceImpl<ActivityDao, Activity> implements ActivityService {

    @Autowired
    private ActivityDao activityDao;

    @Autowired
    @Qualifier("customThreadPool")
    private ThreadPoolExecutor customThreadPool;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void importExcel(long shopId, String mainName, String subName, String date, MultipartFile file) {
        try {
            ExcelReader reader = ExcelUtil.getReader(file.getInputStream());

            List<Map<String, Object>> readAll = reader.read(0, 3, Integer.MAX_VALUE);

            List<Activity> activities = new ArrayList<>();

            for (Map<String, Object> map : readAll) {

                Activity activity = Activity.builder()
                        .id(IdUtil.getSnowflakeNextId())
                        .shopId(shopId)
                        .itemId(Long.parseLong(map.get("商品ID").toString()))
                        .modelId(Long.parseLong(map.get("规格ID").toString()))
                        .price(new BigDecimal(map.get("活动价格").toString()))
                        .status(map.get("状态").toString())
                        .date(date)
                        .mainName(mainName)
                        .subName(subName).build();

                Activity existActivity = activityDao.selectOne(new LambdaQueryWrapper<Activity>()
                        .select(Activity::getId)
                        .eq(Activity::getModelId, activity.getModelId())
                        .eq(Activity::getMainName, activity.getMainName())
                        .eq(Activity::getSubName, activity.getSubName())
                        .eq(Activity::getDate, activity.getDate())
                        .eq(Activity::getShopId, shopId));


                if (Objects.nonNull(existActivity)) {
                    activity.setId(existActivity.getId());
                }

                activities.add(activity);
            }

            CompletableFuture.runAsync(() -> {
                try {
                    this.saveOrUpdateBatch(activities);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }, customThreadPool);

            reader.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
}
