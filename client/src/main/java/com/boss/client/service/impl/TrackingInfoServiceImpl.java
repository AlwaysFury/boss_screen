package com.boss.client.service.impl;

import cn.hutool.core.util.IdUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.boss.client.dao.TrackingInfoDao;
import com.boss.client.service.TrackingInfoService;
import com.boss.client.util.ShopeeUtil;
import com.boss.common.enities.TrackingInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 操作日志服务
 */
@Service
public class TrackingInfoServiceImpl extends ServiceImpl<TrackingInfoDao, TrackingInfo> implements TrackingInfoService {

    @Autowired
    private ShopServiceImpl shopService;


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
}
