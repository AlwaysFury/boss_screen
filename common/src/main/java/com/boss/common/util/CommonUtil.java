package com.boss.common.util;

import lombok.extern.slf4j.Slf4j;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

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

    public static String localDateTime2String(LocalDateTime timeStr, String format) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern(format);
        return timeStr.format(fmt);
    }

    public static long string2Timestamp(String time) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime dateTime = LocalDateTime.parse(time, formatter);
        return dateTime.toInstant(ZoneOffset.UTC).toEpochMilli() / 1000;
    }

    /**
     * 分割list
     * @param records
     * @param batchSize
     * @return
     * @param <T>
     */
    public static <T> List<List<T>> splitListBatches(List<T> records, int batchSize) {
        List<List<T>> batches = new ArrayList<>();
        for (int i = 0; i < records.size(); i += batchSize) {
            batches.add(records.subList(i, Math.min(i + batchSize, records.size())));
        }
        return batches;
    }

    /**
     * 获取时间字符串的开始时间和结束时间
     * @param timeStr
     * @param type
     * @return
     */
    public static Long getStartAndEndTimestamp(String timeStr, String type) {

        LocalDate specificDate = LocalDate.parse(timeStr); // 解析日期字符串

        long timestamp = 0;
        if ("start".equals(type)) {
            LocalTime startOfDay = LocalTime.of(0, 0, 0); // 00:00:00
            LocalDateTime startDateTime = LocalDateTime.of(specificDate, startOfDay);
            timestamp = Date.from(startDateTime.atZone(ZoneId.systemDefault()).toInstant()).getTime();
        }

        if ("end".equals(type)){
            LocalTime endOfDay = LocalTime.of(23, 59, 59); // 23:59:59
            LocalDateTime endDateTime = LocalDateTime.of(specificDate, endOfDay);
            timestamp = Date.from(endDateTime.atZone(ZoneId.systemDefault()).toInstant()).getTime();
        }

        return timestamp / 1000;
    }


    /**
     * 将给定的开始和结束时间戳，按每14天分割成多个时间段，
     * 每个时间段从某天的00:00:00开始，到第n天的23:59:59结束。
     *
     * @param startTime 开始时间戳，单位：毫秒
     * @param endTime 结束时间戳，单位：毫秒
     * @param days 每个时间段的天数
     * @return 分割后的时间段列表，每个时间段由两天的时间戳组成
     */
    public static List<Long[]> splitIntoEveryNDaysTimestamp(long startTime, long endTime, int days) {
        List<Long[]> timeRanges = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();

        // 设置日历时间为开始时间的00:00:00（需要将秒转为毫秒）
        calendar.setTimeInMillis(startTime * 1000L);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        while (calendar.getTimeInMillis() / 1000 <= endTime) {
            // 计算下一个周期的开始时间（即当前周期的结束时间的后一天的00:00:00）
            Calendar nextPeriodStart = (Calendar) calendar.clone();
            nextPeriodStart.add(Calendar.DAY_OF_MONTH, days);
            nextPeriodStart.set(Calendar.HOUR_OF_DAY, 0);
            nextPeriodStart.set(Calendar.MINUTE, 0);
            nextPeriodStart.set(Calendar.SECOND, 0);
            nextPeriodStart.set(Calendar.MILLISECOND, 0);

            // 确保结束时间不超过endTime且为23:59:59（转换回秒）
            long thisEnd = Math.min((nextPeriodStart.getTimeInMillis() / 1000) - 1, endTime);

            // 添加到结果列表，注意转换回秒
            timeRanges.add(new Long[]{calendar.getTimeInMillis() / 1000, thisEnd});

            // 移动到下一个周期的开始
            calendar = nextPeriodStart;
        }

        return timeRanges;
    }

    public static List<Long[]> splitIntoEveryNDaysTimestamp(String startDateStr, String endDateStr, int offset) {
        ZoneId zoneId = ZoneId.systemDefault();
        LocalDate startLocalDate = LocalDate.parse(startDateStr);
        LocalDate endLocalDate = LocalDate.parse(endDateStr);

        List<LocalDate[]> localDatePairs = splitIntoEvery15DaysTimestamp(startLocalDate, endLocalDate, offset);

        return localDatePairs.stream()
                .map(pair -> new Long[]{
                        pair[0].atStartOfDay(zoneId).toInstant().getEpochSecond(), // 开始时间为当天00:00:00，转换为秒
                        pair[1].atTime(LocalTime.of(23, 59, 59)).atZone(zoneId).toInstant().getEpochSecond()}) // 结束时间为当天23:59:59
                .collect(Collectors.toList());
    }

    private static List<LocalDate[]> splitIntoEvery15DaysTimestamp(LocalDate startDate, LocalDate endDate, int offset) {
        List<LocalDate[]> timestampPairs = new ArrayList<>();
        LocalDate currentDate = startDate;

        while (!currentDate.isAfter(endDate)) {
            LocalDate nextSplitStart = currentDate;
            LocalDate nextDate = currentDate.plusDays(offset - 1);

            if (nextDate.isAfter(endDate)) {
                nextDate = endDate;
            }

            timestampPairs.add(new LocalDate[]{nextSplitStart, nextDate});
            currentDate = nextDate.plusDays(1);
        }

        return timestampPairs;
    }

    // 用于将大列表分割成小批次
    public static <T> List<List<T>> splitList(List<T> list, int size) {
        List<List<T>> subLists = new ArrayList<>();
        for (int i = 0; i < list.size(); i += size) {
            subLists.add(new ArrayList<>(list.subList(i, Math.min(i + size, list.size()))));
        }
        return subLists;
    }

    /**
     * 并集
     * @param lists
     * @return
     * @param <T>
     */
    public static <T> Set<T> getUnion(List<List<T>> lists) {
        return lists.stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
    }

    /**
     * 交集
     * @param lists
     * @return
     */
    public static Set<Long> getIntersection(List<List<Long>> lists) {
        Set<Long> resultSet = new HashSet<>(lists.get(0));

        for (int i = 1; i < lists.size(); i++) {
            Set<Long> currentSet = new HashSet<>(lists.get(i));
            resultSet.retainAll(currentSet);

            if (resultSet.isEmpty()) {
                break; // Early exit if the intersection is already empty.
            }
        }

        return resultSet;
    }
}
