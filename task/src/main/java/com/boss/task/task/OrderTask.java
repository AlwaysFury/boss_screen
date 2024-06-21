package com.boss.task.task;


import com.boss.task.service.impl.OrderServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @Description
 * @Author 罗宇航
 * @Date 2024/4/12
 */

@Component
@Slf4j
public class OrderTask {

    @Autowired
    private OrderServiceImpl orderService;

    /**
     * corn六个位置参数分别表示：
     * 秒（0~59） 例如0/5表示每5秒
     * 分（0~59）
     * 时（0~23）
     * 日（0~31）的某天，需计算
     * 月（0~11）
     * 周几（ 可填1-7 或 SUN/MON/TUE/WED/THU/FRI/SAT）
     */

//    @Scheduled(cron = "0 */10 * * * ?")
    public void refreshNewOrder() {
        // 获取数据库最新的订单创建时间和当前时间之间的订单
        log.info("======开始刷新新的订单信息");
        long startTime = System.currentTimeMillis();

        orderService.refreshNewOrder();

        log.info("更新新的订单耗时： {}秒", (System.currentTimeMillis() - startTime) / 1000);
    }

//    @Scheduled(cron = "0 */10 * * * ?")
    public void refreshOrderByStatus() {
        log.info("======开始刷新未完成订单信息");
        long startTime = System.currentTimeMillis();

        orderService.refreshOrderByStatus("COMPLETED", "CANCELLED");

        log.info("更新未完成订单耗时： {}秒", (System.currentTimeMillis() - startTime) / 1000);
    }
}
