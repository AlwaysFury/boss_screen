package com.boss.client.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.boss.client.vo.TrackingInfoVO;
import com.boss.common.enities.TrackingInfo;


/**
 * @Description
 * @Author 罗宇航
 * @Date 2024/6/18
 */
public interface TrackingInfoService extends IService<TrackingInfo> {

//    void saveTrackingInfoBySn(String orderSn, long shopId, String trackingNumber);

    TrackingInfoVO getTrackInfoBySn(String orderSn);
}
