package com.boss.task.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.boss.common.enities.PayoutInfo;

/**
 * @Description
 * @Author 罗宇航
 * @Date 2024/4/10
 */
public interface PayoutInfoService extends IService<PayoutInfo>  {

    void refreshPayoutInfoByTime(long startTime, long endTime);

    void refreshNewerPayoutInfo();
}
