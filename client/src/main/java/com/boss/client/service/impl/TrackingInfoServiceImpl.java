package com.boss.client.service.impl;

import cn.hutool.core.util.IdUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.boss.client.dao.TrackingInfoDao;
import com.boss.client.service.TrackingInfoService;
import com.boss.client.util.ShopeeUtil;
import com.boss.client.vo.PayoutInfoVO;
import com.boss.client.vo.TrackingInfoVO;
import com.boss.common.enities.PayoutInfo;
import com.boss.common.enities.TrackingInfo;
import com.boss.common.util.BeanCopyUtils;
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

    @Autowired
    private TrackingInfoDao trackingInfoDao;


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

    @Override
    public TrackingInfoVO getTrackInfoBySn(String orderSn) {
        TrackingInfo trackingInfo = trackingInfoDao.selectOne(new QueryWrapper<TrackingInfo>().eq("id", orderSn));
        TrackingInfoVO trackingInfoVO = BeanCopyUtils.copyObject(trackingInfo, TrackingInfoVO.class);
        trackingInfoVO.setLogisticsData(trackingInfo.getLogisticsData() == null || trackingInfo.getLogisticsData().isEmpty() ? new JSONArray() : JSONArray.parseArray(trackingInfo.getLogisticsData()));

        return trackingInfoVO;
    }
}
