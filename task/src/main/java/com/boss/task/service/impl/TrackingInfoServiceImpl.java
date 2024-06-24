package com.boss.task.service.impl;

import cn.hutool.core.util.IdUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.boss.common.enities.TrackingInfo;
import com.boss.common.util.CommonUtil;
import com.boss.task.dao.TrackingInfoDao;
import com.boss.task.service.TrackingInfoService;
import com.boss.task.util.ShopeeUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

/**
 * 操作日志服务
 */
@Service
@Slf4j
public class TrackingInfoServiceImpl extends ServiceImpl<TrackingInfoDao, TrackingInfo> implements TrackingInfoService {

    @Autowired
    private ShopServiceImpl shopService;

    @Autowired
    private TrackingInfoDao trackingInfoDao;

    @Autowired
    @Qualifier("customThreadPool")
    private ThreadPoolExecutor customThreadPool;

    private final TransactionTemplate transactionTemplate;

    @Autowired
    public TrackingInfoServiceImpl(DataSourceTransactionManager transactionManager) {
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }


    @Transactional(rollbackFor = Exception.class)
    @Override
    public void saveTrackingInfoBySn(String orderSn, long shopId, String trackingNumber) {

        String accessToken = shopService.getAccessTokenByShopId(String.valueOf(shopId));
        JSONObject trackingInfoObject = ShopeeUtil.getTrackingInfo(accessToken, shopId, orderSn);

        if (trackingInfoObject.getString("error").contains("error") && trackingInfoObject == null && trackingInfoObject.getJSONObject("response") == null) {
            return;
        }

        JSONObject response = trackingInfoObject.getJSONObject("response");

        TrackingInfo trackingInfo = TrackingInfo.builder()
                .id(IdUtil.getSnowflakeNextId())
                .shopId(shopId)
                .orderSn(orderSn)
                .trackingNumber(trackingNumber)
                .logisticsStatus(response.getString("logistics_status"))
                .build();

        JSONArray infoArray = response.getJSONArray("tracking_list");
        if (infoArray != null && !infoArray.isEmpty()) {
            trackingInfo.setLogisticsData(infoArray.toJSONString());
        }

        this.save(trackingInfo);

    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void refreshTrackInfoByStatus(String... status) {
        List<TrackingInfo> trackingInfos = trackingInfoDao.selectList(new QueryWrapper<TrackingInfo>().select("order_sn", "shop_id").in("logistics_status", status));

        if (trackingInfos.isEmpty()) {
            return;
        }

        Map<Long, List<String>> shopIdmap = new HashMap<>();
        Map<String, String> trackingInfoMap = new HashMap<>();
        for (TrackingInfo trackingInfo : trackingInfos) {
            String orderSn = trackingInfo.getOrderSn();
            long shopId = trackingInfo.getShopId();
            String trackingNumber = trackingInfo.getTrackingNumber();

            trackingInfoMap.put(orderSn, trackingNumber);

            List<String> tempMap;
            if (shopIdmap.get(shopId) != null) {
                tempMap = shopIdmap.get(orderSn);
            } else {
                tempMap = new ArrayList<>();
            }
            tempMap.add(orderSn);
            shopIdmap.put(shopId, tempMap);
        }

        for (Long shopId : shopIdmap.keySet()) {
            refreshTrackInfoBySnList(shopIdmap.get(shopId), shopId, trackingInfoMap);
        }
    }

    public void refreshTrackInfoBySnList(List<String> orderSnList, long shopId, Map<String, String> trackingInfoMap) {
        List<TrackingInfo> trackingInfoList = new CopyOnWriteArrayList<>();

        log.info("===运单发送请求及处理开始");
        long startTime =  System.currentTimeMillis();

        List<CompletableFuture<Void>> trackInfoFutures = orderSnList.stream()
                .map(orderSn -> {
                    long finalShopId = shopId;
                    return CompletableFuture.runAsync(() -> {
                        String finalAccessToken = shopService.getAccessTokenByShopId(String.valueOf(finalShopId));
                        JSONObject trackingInfoObject = ShopeeUtil.getTrackingInfo(finalAccessToken, shopId, orderSn);
                        if (trackingInfoObject.getString("error").contains("error") && trackingInfoObject == null && trackingInfoObject.getJSONObject("response") == null) {
                            return;
                        }
                        JSONObject response = trackingInfoObject.getJSONObject("response");

                        TrackingInfo trackingInfo = TrackingInfo.builder()
                                .id(IdUtil.getSnowflakeNextId())
                                .shopId(shopId)
                                .orderSn(orderSn)
                                .trackingNumber(trackingInfoMap.get(orderSn))
                                .logisticsStatus(response.getString("logistics_status"))
                                .build();

                        trackingInfoList.add(trackingInfo);
                    }, customThreadPool);
                }).collect(Collectors.toList());

        CompletableFuture.allOf(trackInfoFutures.toArray(new CompletableFuture[0])).join();

        log.info("===运单发送请求并处理结束，耗时：{}秒", (System.currentTimeMillis() - startTime) / 1000);

        log.info("===开始运单数据落库");
        startTime =  System.currentTimeMillis();

        List<List<TrackingInfo>> batchesTrackingInfoList = CommonUtil.splitListBatches(trackingInfoList, 100);
        List<CompletableFuture<Void>> insertTrackingInfoFutures = new ArrayList<>();
        for (List<TrackingInfo> batch : batchesTrackingInfoList) {
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                try {
                    transactionTemplate.executeWithoutResult(status -> {
                        this.saveOrUpdateBatch(batch);
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }, customThreadPool);

            insertTrackingInfoFutures.add(future);
        }
        CompletableFuture.allOf(insertTrackingInfoFutures.toArray(new CompletableFuture[0])).join();

        log.info("===运单数据落库结束，耗时：{}秒", (System.currentTimeMillis() - startTime) / 1000);
    }
}
