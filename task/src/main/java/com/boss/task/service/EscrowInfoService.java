package com.boss.task.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.boss.common.enities.EscrowInfo;

import java.util.List;


/**
 * @Description
 * @Author 罗宇航
 * @Date 2024/4/11
 */
public interface EscrowInfoService extends IService<EscrowInfo> {

    void refreshEscrowBySn(List<List<String>> orderSnList, long shopId, int flag);

    void refreshEscrowInfoBySn(List<List<String>> orderSnLists, long shopId);

    void refreshSingleEscrowInfoBySn(List<String> orderSnList, long shopId);

//    void refreshEscrowByTime(long startTime, long endTime);

//    void refreshEscrowByStatus(String... status);

//    void refreshOrderNoOnEscrow();
}
