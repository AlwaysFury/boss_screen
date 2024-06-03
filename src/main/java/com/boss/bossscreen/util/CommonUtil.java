package com.boss.bossscreen.util;

import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.boss.bossscreen.service.impl.RedisServiceImpl;
import lombok.extern.slf4j.Slf4j;
import me.codeleep.jsondiff.DefaultJsonDifference;
import me.codeleep.jsondiff.common.model.JsonCompareResult;
import me.codeleep.jsondiff.core.config.JsonComparedOption;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @Description
 * @Author 罗宇航
 * @Date 2024/4/16
 */
@Slf4j
public class CommonUtil {

    public static String timestamp2String(long timestamp) {
        // 使用Instant从时间戳创建时间点
        Instant instant = Instant.ofEpochSecond(timestamp);

        // 使用ZoneId定义时区（可以根据需要选择不同的时区）
        ZoneId zoneId = ZoneId.of("Asia/Shanghai");

        LocalDateTime dateTime = instant.atZone(zoneId).toLocalDateTime();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        // 将Instant转换为LocalDateTime
        return dateTime.format(formatter);
    }

    public static LocalDateTime timestamp2LocalDateTime(long timestamp) {
        return string2LocalDateTime(timestamp2String(timestamp));
    }

    public static LocalDateTime string2LocalDateTime(String timeStr) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return LocalDateTime.parse(timeStr, formatter);
    }

    public static String localDateTime2String(LocalDateTime timeStr) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return timeStr.format(fmt);
    }

    public static long string2Timestamp(String time) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime dateTime = LocalDateTime.parse(time, formatter);
        return dateTime.toInstant(ZoneOffset.UTC).toEpochMilli() / 1000;
    }

    public static <T> String judgeRedis(RedisServiceImpl redisService, String redisKey, List<T> list, T t, Class<T> clazz) {
        // 检测入库
        // 将新旧数据全部数据缓存进入 redis
        // 新数据与旧数据进行比较：时间戳
        // key：product:产品id:时间戳
        // value：数据 json 格式化
        // 全量检查！！！！！！
        // 新增：将数据存入新增集合，存入 redis 和 mysql
        // 更新：将更新数据存入更新集合，更新 reids 和 mysql 中的数据
        // 删除：指示标记该条数据被删除！！！不是物理删除，存入删除集合，并在更新 redis 和 mysql 中的数据
        String result = "";
        Object redisResult = redisService.get(redisKey);
        String newJsonStr = JSON.toJSONString(t, SerializerFeature.WriteMapNullValue);
        if (Objects.isNull(redisResult)) {
            // 为空入库
            redisService.set(redisKey, newJsonStr);
            list.add(t);
        } else {
            // 不为空判断更新
            JSONObject oldObject = JSON.parseObject(redisResult.toString());
            long id = oldObject.getLong("id");
            oldObject.remove("id");
            String oldJsonStr = oldObject.toJSONString();

            JSONObject newObject = JSON.parseObject(newJsonStr);
            newObject.remove("id");
            newJsonStr = newObject.toJSONString();


            if (!newJsonStr.equals(oldJsonStr)) {
                JsonComparedOption jsonComparedOption = new JsonComparedOption().setIgnoreOrder(true);
                JsonCompareResult jsonCompareResult = new DefaultJsonDifference()
                        .option(jsonComparedOption)
                        .detectDiff(newJsonStr, oldJsonStr);
                result = JSON.toJSONString(jsonCompareResult);

                log.info("newJsonStr====>"+newJsonStr);
                log.info("oldJsonStr====>"+oldJsonStr);

                newObject.put("id", id);
                newJsonStr = newObject.toJSONString();
                redisService.set(redisKey, newJsonStr);
                list.add(JSON.parseObject(newJsonStr, clazz));
            }
        }
        return result;
    }

    /**
     * 生成Snowflake id
     */
    public static long createNo() {
        Snowflake snowflake = IdUtil.getSnowflake(1, 1);//单例方式获取实例，否则高并发会重复！！！
        return snowflake.nextId() / 1000;
    }

    public static List<LocalDate[]> splitIntoEvery15DaysTimestamp(LocalDate startDate, LocalDate endDate, int offset) {
        List<LocalDate[]> timestampPairs = new ArrayList<>();
        while (!startDate.isAfter(endDate)) {
            LocalDate endOfSplitDate = startDate.plusDays(offset);
            if (endOfSplitDate.isAfter(endDate)) {
                endOfSplitDate = endDate;
            }
            LocalDate[] pair = new LocalDate[]{
                    startDate,
                    endOfSplitDate
            };
            timestampPairs.add(pair);
            startDate = endOfSplitDate.plusDays(1);
        }
        return timestampPairs;
    }
}
