package com.boss.task.task;

import com.boss.task.service.impl.FeishuServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * @Description
 * @Author 罗宇航
 * @Date 2024/7/11
 */

@Component
@Slf4j
public class NoticeTask {

    @Autowired
    private FeishuServiceImpl feishuService;

    /**
     * corn六个位置参数分别表示：
     * 秒（0~59） 例如0/5表示每5秒
     * 分（0~59）
     * 时（0~23）
     * 日（0~31）的某天，需计算
     * 月（0~11）
     * 周几（ 可填1-7 或 SUN/MON/TUE/WED/THU/FRI/SAT）
     */
    @Scheduled(cron = "0 */30 * * * ?")
    public void sendAdsMessage() {
        log.info("======开始发送广告余额告警");
        long startTime = System.currentTimeMillis();

        feishuService.sendAdsMessage();

        log.info("发送广告余额告警耗时： {}秒", (System.currentTimeMillis() - startTime) / 1000);
    }
}
