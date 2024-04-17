package com.boss.bossscreen.util;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * @Description
 * @Author 罗宇航
 * @Date 2024/4/16
 */
public class CommonUtil {

    public static LocalDateTime timestampToLocalDateTime(long timestamp) {
        // 使用Instant从时间戳创建时间点
        Instant instant = Instant.ofEpochSecond(timestamp);

        // 使用ZoneId定义时区（可以根据需要选择不同的时区）
        ZoneId zoneId = ZoneId.of("Asia/Shanghai");

        // 将Instant转换为LocalDateTime
        return instant.atZone(zoneId).toLocalDateTime();
    }
}
