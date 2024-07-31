package com.boss.task.task;


import com.boss.task.service.impl.PayoutInfoServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * @Description
 * @Author 罗宇航
 * @Date 2024/4/12
 */

@Component
@Slf4j
public class PayoutTask {

    @Autowired
    private PayoutInfoServiceImpl payoutInfoService;

    /**
     * corn六个位置参数分别表示：
     * 秒（0~59） 例如0/5表示每5秒
     * 分（0~59）
     * 时（0~23）
     * 日（0~31）的某天，需计算
     * 月（0~11）
     * 周几（ 可填1-7 或 SUN/MON/TUE/WED/THU/FRI/SAT）
     */

    @Scheduled(cron = "0 */20 * * * ?")
    public void refreshPayoutInfo() {
        log.info("======开始刷新调整信息");
        long startTime = System.currentTimeMillis();

        payoutInfoService.refreshNewerPayoutInfo();

        log.info("更新调整信息耗时： {}秒", (System.currentTimeMillis() - startTime) / 1000);
    }
}
