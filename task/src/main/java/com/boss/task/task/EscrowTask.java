package com.boss.task.task;


import com.boss.task.service.impl.EscrowInfoServiceImpl;
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
public class EscrowTask {

    @Autowired
    private EscrowInfoServiceImpl escrowInfoService;

    /**
     * corn六个位置参数分别表示：
     * 秒（0~59） 例如0/5表示每5秒
     * 分（0~59）
     * 时（0~23）
     * 日（0~31）的某天，需计算
     * 月（0~11）
     * 周几（ 可填1-7 或 SUN/MON/TUE/WED/THU/FRI/SAT）
     */
//    @Scheduled(cron = "0 */20 * * * ?")
    public void refreshEscrow() {
        log.info("======开始刷新支付信息");
        long startTime = System.currentTimeMillis();

//        escrowInfoService.refreshEscrowByTime("2024-05-01", "2024-05-31");

        log.info("更新支付信息耗时： {}秒", (System.currentTimeMillis() - startTime) / 1000);
    }

//    @Scheduled(cron = "0 */20 * * * ?")
    public void refreshUnPaidEscrow() {
        log.info("======开始刷新未支付支付信息");
        long startTime = System.currentTimeMillis();

        escrowInfoService.refreshEscrowByStatus("UNPAID");

        log.info("更新未支付支付信息耗时： {}秒", (System.currentTimeMillis() - startTime) / 1000);
    }

    @Scheduled(cron = "0 */20 * * * ?")
    public void refreshOrderNoOnEscrow() {
        log.info("======开始刷新还没有的支付信息");
        long startTime = System.currentTimeMillis();

        escrowInfoService.refreshOrderNoOnEscrow();

        log.info("更新还没有的支付信息耗时： {}秒", (System.currentTimeMillis() - startTime) / 1000);
    }
}
