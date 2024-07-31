package com.boss.client.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.boss.client.dao.TrackingInfoDao;
import com.boss.client.service.TrackingInfoService;
import com.boss.client.vo.TrackingInfoVO;
import com.boss.common.enities.TrackingInfo;
import com.boss.common.enums.LogisticsStatusEnum;
import com.boss.common.util.BeanCopyUtils;
import com.boss.common.util.CommonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 操作日志服务
 */
@Service
public class TrackingInfoServiceImpl extends ServiceImpl<TrackingInfoDao, TrackingInfo> implements TrackingInfoService {

    @Autowired
    private TrackingInfoDao trackingInfoDao;

    @Override
    public TrackingInfoVO getTrackInfoBySn(String orderSn) {
        TrackingInfo trackingInfo = trackingInfoDao.selectOne(new QueryWrapper<TrackingInfo>().eq("order_sn", orderSn));
        if (trackingInfo == null) {
            return null;
        }
        TrackingInfoVO trackingInfoVO = BeanCopyUtils.copyObject(trackingInfo, TrackingInfoVO.class);
        if (trackingInfo.getLogisticsData() != null && !trackingInfo.getLogisticsData().isEmpty()) {
            JSONArray array = JSONArray.parseArray(trackingInfo.getLogisticsData());
            for (int i = 0; i < array.size(); i++) {
                JSONObject object = array.getJSONObject(i);
                object.put("update_time", CommonUtil.timestamp2String(object.getLong("update_time")));
            }
            trackingInfoVO.setLogisticsData(array);
        }
        trackingInfoVO.setLogisticsStatus(LogisticsStatusEnum.getDescByCode(trackingInfo.getLogisticsStatus()));
        return trackingInfoVO;
    }
}
