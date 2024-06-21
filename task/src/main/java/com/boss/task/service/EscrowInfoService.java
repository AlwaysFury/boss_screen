package com.boss.task.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.boss.common.enities.EscrowInfo;


/**
 * @Description
 * @Author 罗宇航
 * @Date 2024/4/11
 */
public interface EscrowInfoService extends IService<EscrowInfo> {

    void refreshEscrowByTime(String startTime, String endTime);

    void refreshEscrowByStatus(String... status);

    void refreshOrderNoOnEscrow();
}
