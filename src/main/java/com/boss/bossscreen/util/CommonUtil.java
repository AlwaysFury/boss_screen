package com.boss.bossscreen.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.boss.bossscreen.service.impl.RedisServiceImpl;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Objects;

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

    public static <T> void judgeRedis(RedisServiceImpl redisService, String redisKey, List<T> insertList, List<T> updateList, T t, Class<T> clazz) {
        // 检测入库
        // 将新旧数据全部数据缓存进入 redis
        // 新数据与旧数据进行比较：时间戳
        // key：product:产品id:时间戳
        // value：数据 json 格式化
        // 全量检查！！！！！！
        // 新增：将数据存入新增集合，存入 redis 和 mysql
        // 更新：将更新数据存入更新集合，更新 reids 和 mysql 中的数据
        // 删除：指示标记该条数据被删除！！！不是物理删除，存入删除集合，并在更新 redis 和 mysql 中的数据
        Object redisResult = redisService.get(redisKey);
        String newJsonStr = JSON.toJSONString(t, SerializerFeature.WriteMapNullValue);
        if (Objects.isNull(redisResult)) {
            // 为空入库
            redisService.set(redisKey, newJsonStr);
            insertList.add(t);
        } else {
            // 不为空判断更新
            String oldJsonStr = redisResult.toString();
            if (!newJsonStr.equals(oldJsonStr)) {
                redisService.set(redisKey, newJsonStr);
                updateList.add(JSON.parseObject(redisResult.toString(), clazz));
            }
        }
    }
}
