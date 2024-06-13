package com.boss.bossscreen.task;

import com.boss.bossscreen.service.impl.OrderServiceImpl;
import com.boss.bossscreen.service.impl.ProductServiceImpl;
import com.boss.bossscreen.service.impl.ReturnOrderServiceImpl;
import com.boss.bossscreen.service.impl.ShopServiceImpl;
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
public class RefreshTask {

    @Autowired
    private ProductServiceImpl productService;

    @Autowired
    private OrderServiceImpl orderService;

    @Autowired
    private ShopServiceImpl shopService;

    @Autowired
    private ReturnOrderServiceImpl returnOrderService;

    /**
     * corn六个位置参数分别表示：
     * 秒（0~59） 例如0/5表示每5秒
     * 分（0~59）
     * 时（0~23）
     * 日（0~31）的某天，需计算
     * 月（0~11）
     * 周几（ 可填1-7 或 SUN/MON/TUE/WED/THU/FRI/SAT）
     */
//    @Scheduled(cron = "0 0 */2 * * ?")
    public void refreshToken() {
        log.info("======开始刷新 token");
        shopService.refreshShopToken();
    }

//    @Scheduled(cron = "0 */10 * * * ?")
    public void refreshProduct() {
        log.info("======开始刷新产品信息");
        long startTime =  System.currentTimeMillis();

        productService.saveOrUpdateProduct();

        log.info("更新订单耗时： {}秒", (System.currentTimeMillis() - startTime) / 1000);
    }

//    @Scheduled(cron = "0 */10 * * * ?", fixedDelay = 3000)
    public void refreshOrder() {
        log.info("======开始刷新订单信息");
        long startTime =  System.currentTimeMillis();

        orderService.saveOrUpdateOrder("2024-05-01", "2024-05-31");

        log.info("更新订单耗时： {}秒", (System.currentTimeMillis() - startTime) / 1000);
    }

//    @Scheduled(cron = "0 */30 * * * ?")
    public void refreshReturnOrder() {
        log.info("======开始刷新退单信息");
        long startTime =  System.currentTimeMillis();

        returnOrderService.saveOrUpdateReturnOrder();

        log.info("更新退单耗时： {}秒", (System.currentTimeMillis() - startTime) / 1000);
    }
}
