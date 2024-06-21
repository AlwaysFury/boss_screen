package com.boss.task.task;


import com.boss.task.service.impl.ProductExtraInfoServiceImpl;
import com.boss.task.service.impl.ProductServiceImpl;
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
public class ProductTask {

    @Autowired
    private ProductServiceImpl productService;

    @Autowired
    private ProductExtraInfoServiceImpl productExtraInfoService;

    /**
     * corn六个位置参数分别表示：
     * 秒（0~59） 例如0/5表示每5秒
     * 分（0~59）
     * 时（0~23）
     * 日（0~31）的某天，需计算
     * 月（0~11）
     * 周几（ 可填1-7 或 SUN/MON/TUE/WED/THU/FRI/SAT）
     */

    @Scheduled(cron = "0 0 */1 * * ?")
    public void refreshProductExtraInfo() {
        log.info("======开始刷新产品额外信息");
        long startTime = System.currentTimeMillis();

        productExtraInfoService.saveOrUpdateProductExtraInfo();

        log.info("刷新产品额外信息耗时： {}秒", (System.currentTimeMillis() - startTime) / 1000);
    }

    @Scheduled(cron = "0 */10 * * * ?")
    public void refreshProduct() {
        log.info("======开始刷新产品信息");
        long startTime = System.currentTimeMillis();

        productService.refreshProductByStatus("&item_status=NORMAL&item_status=BANNED&item_status=UNLIST&item_status=REVIEWING");

        log.info("更新产品信息耗时： {}秒", (System.currentTimeMillis() - startTime) / 1000);
    }

        @Scheduled(cron = "0 */10 * * * ?")
    public void refreshDeletedProduct() {
        log.info("======开始刷新被删除产品信息");
        long startTime = System.currentTimeMillis();

        productService.refreshDeletedProduct();

        log.info("更新被删除产品信息耗时： {}秒", (System.currentTimeMillis() - startTime) / 1000);
    }
}
