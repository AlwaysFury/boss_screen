package com.boss.bossscreen.task;

import com.boss.bossscreen.service.impl.MainAccountServiceImpl;
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
public class RefreshTokenTask {

    @Autowired
    private ShopServiceImpl shopService;

    @Autowired
    private MainAccountServiceImpl mainAccountService;

    /**
     * corn六个位置参数分别表示：
     * 秒（0~59） 例如0/5表示每5秒
     * 分（0~59）
     * 时（0~23）
     * 日（0~31）的某天，需计算
     * 月（0~11）
     * 周几（ 可填1-7 或 SUN/MON/TUE/WED/THU/FRI/SAT）
     */
//    @Scheduled(cron = "*/10 * * * * *")
    public void refreshShopToken() {
        log.info("======开始刷新店铺 token");
        shopService.refreshShopToken();
    }

//    @Scheduled(cron = "*/10 * * * * *")
    public void refreshAccountToken() {
        log.info("======开始刷新账号 token");
        shopService.refreshShopTokenByAccount();
    }
}
