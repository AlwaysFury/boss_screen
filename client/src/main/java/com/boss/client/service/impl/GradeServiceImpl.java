package com.boss.client.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.boss.client.dao.GradeDao;
import com.boss.client.dao.OrderItemDao;
import com.boss.client.dto.GradeObject;
import com.boss.client.enities.Grade;
import com.boss.client.enities.Rule;
import com.boss.client.service.GradeService;
import com.boss.common.util.CommonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

import static com.boss.common.constant.RedisPrefixConst.GRADE_PRODUCT;
import static com.boss.common.enums.TagTypeEnum.ITEM;
import static com.boss.common.enums.TagTypeEnum.PHOTO;


/**
 * 操作日志服务
 */
@Service
public class GradeServiceImpl extends ServiceImpl<GradeDao, Grade> implements GradeService {


    @Autowired
    private GradeDao gradeDao;

    @Autowired
    private OrderItemServiceImpl orderItemService;

    @Autowired
    private OrderItemDao orderItemDao;

    @Autowired
    private RuleServiceImpl ruleService;

    @Autowired
    private RedisServiceImpl redisService;

    @Autowired
    @Qualifier("customThreadPool")
    private ThreadPoolExecutor customThreadPool;

    @Override
    public void refreshGrade(String type) {
        // 先查出所有要同步等级的产品和款号，封装成GradeObject

        List<Rule> ruleList = new ArrayList<>();
        if (ITEM.getCode().equals(type)) {
            ruleList = ruleService.getRuleList(ITEM.getCode());
        } else {
            ruleList = ruleService.getRuleList(PHOTO.getCode());
        }

        List<GradeObject> gradeObjects = new ArrayList<>();
        if (ITEM.getCode().equals(type)) {
            gradeObjects = gradeDao.getProduct();
        } else {
            gradeObjects = gradeDao.getSku();
        }

        List<Rule> finalRuleList = ruleList;
        gradeObjects.stream()
            .map(gradeObject -> CompletableFuture.supplyAsync(() ->
                    getGrade(gradeObject, finalRuleList, ITEM.getCode()), customThreadPool)
                .thenAccept(grade -> {
                    if (grade != null) {
                        long itemId = gradeObject.getItemId();

                        if (ITEM.getCode().equals(type)) {
                            redisService.set(GRADE_PRODUCT + itemId, grade);
                        } else {
                            redisService.set(GRADE_PRODUCT + itemId, grade);
                        }
                    }
                }));

//        List<CompletableFuture<Void>> skuFutures = skuGradeObjectList.stream()
//                .map(gradeObject -> CompletableFuture.supplyAsync(() ->
//                                getGrade(gradeObject, photoRuleList, PHOTO.getCode()), customThreadPool)
//                        .thenAccept(grade -> {
//                            if (grade != null) {
//                                long itemId = gradeObject.getItemId();
//                                redisService.set(GRADE_PRODUCT + itemId, grade);
//                            }
//                        })).collect(Collectors.toList());
//
//        CompletableFuture.allOf(productFutures.toArray(new CompletableFuture[0])).join();
//        CompletableFuture.allOf(skuFutures.toArray(new CompletableFuture[0])).join();

    }

    public String getGrade(GradeObject gradeObject, List<Rule> ruleList, String type) {

        String grade = "";
        boolean allOrNot;
        JSONObject ruleData;
        // 满足规则次数
        int ruleCount = 0;


        if (ruleList == null || ruleList.size() == 0) {
            return "";
        }

        for (Rule rule : ruleList) {
            ruleData = JSONObject.parseObject(rule.getRuleData());

            if (ruleData == null || ruleData.keySet().size() == 0) {
                continue;
            }
            grade = rule.getGrade();
            allOrNot = rule.getAllOrNot();

            // true：全部满足
            // false：满足任一条件
            // 满足条件次数
            int count = getSatisfactionCount(gradeObject, ruleData, allOrNot, type);
            // 全部满足：满足条件次数 = 全部条件个数
            if (allOrNot && count == ruleData.keySet().size()) {
                ruleCount ++;
            } else if (!allOrNot && count > 0) {
                // 满足任一条件：满足条件次数 > 0
                ruleCount ++;
            }

            // 满足超过一个规则直接 break
            if (ruleCount > 1) {
                grade = "!";
                break;
            }
        }

        if (ruleCount == 0) {
            grade = "";
        }

        return grade;
    }

    private int getSatisfactionCount(GradeObject gradeObject,  JSONObject ruleData, boolean allOrNot, String type) {
        Date nowDate = new Date();

        int count = 0;

        if (ITEM.getCode().equals(type)) {
            if (ruleData.containsKey("itemId") && ruleData.getJSONObject("itemId") != null) {
                if (ruleData.getJSONObject("itemId").getString("value").equals(String.valueOf(gradeObject.getItemId()))) {
                    count ++;
                }

                if (returnOrNot(allOrNot, count)) {
                    return count;
                }
            }

            if (ruleData.containsKey("categoryId") && ruleData.getJSONObject("categoryId") != null) {
                if (ruleData.getJSONObject("categoryId").getString("value").equals(String.valueOf(gradeObject.getCategoryId()))) {
                    count ++;
                }

                if (returnOrNot(allOrNot, count)) {
                    return count;
                }
            }

            if (ruleData.containsKey("status") && ruleData.getJSONObject("status") != null) {
                if (ruleData.getJSONObject("status").getString("value").equals(String.valueOf(gradeObject.getStatus()))) {
                    count ++;
                }

                if (returnOrNot(allOrNot, count)) {
                    return count;
                }
            }

            if (ruleData.containsKey("price") && ruleData.getJSONObject("price") != null) {
                JSONObject object = ruleData.getJSONObject("price");
                // 最小价格
                BigDecimal minPrice = new BigDecimal(object.getString("minValue"));
                // 最大价格
                BigDecimal maxPrice = new BigDecimal(object.getString("maxValue"));

                BigDecimal tempItemMinPrice = gradeObject.getPrice();
                if (ITEM.getCode().equals(type)) {
                    tempItemMinPrice = orderItemDao.itemMinPrice(gradeObject.getItemId());
                }

                BigDecimal itemMinPrice = tempItemMinPrice == null ? new BigDecimal(0) : tempItemMinPrice;

                if (itemMinPrice.compareTo(minPrice) >= 0 && itemMinPrice.compareTo(maxPrice) <= 0) {
                    count++;
                }

                if (returnOrNot(allOrNot, count)) {
                    return count;
                }
            }
        }

        if (PHOTO.getCode().equals(type)) {
            if (ruleData.containsKey("itemSku") && ruleData.getJSONObject("itemSku") != null) {
                String itemSku = gradeObject.getItemSku();

                if (ruleData.getJSONObject("itemSku").getString("value").equals(itemSku)) {
                    count ++;
                }

                if (returnOrNot(allOrNot, count)) {
                    return count;
                }
            }
        }

        if (ruleData.containsKey("tag") && ruleData.getJSONArray("tag") != null) {
            List<Long> list = ruleData.getJSONArray("tag").toJavaList(Long.class);
            List<Long> tagList = gradeObject.getTagIds();
            if (tagList != null && tagList.size() != 0 && tagList.stream().anyMatch(list::contains)) {
                count ++;
            }

            if (returnOrNot(allOrNot, count)) {
                return count;
            }
        }

        if (ruleData.containsKey("createTime") && ruleData.getJSONObject("createTime") != null) {
            JSONObject object = ruleData.getJSONObject("createTime");
            Long createTime = gradeObject.getCreateTime();
            LocalDateTime tempCreateTime = CommonUtil.timestamp2LocalDateTime(createTime);
            LocalDateTime startTime = CommonUtil.string2LocalDateTime(object.getString("startTime"));
            LocalDateTime endTime = CommonUtil.string2LocalDateTime(object.getString("endTime"));

            if ((tempCreateTime.isAfter(startTime) && tempCreateTime.isBefore(endTime)) || tempCreateTime.equals(startTime) || tempCreateTime.equals(endTime)) {
                count ++;
            }

            if (returnOrNot(allOrNot, count)) {
                return count;
            }
        }

        if (ruleData.containsKey("salesVolume") && ruleData.getJSONObject("salesVolume") != null) {
            JSONObject object = ruleData.getJSONObject("salesVolume");
            int salesVolume = 0;
            // 值
            int maxValue = Integer.valueOf(object.getString("maxValue"));
            int minValue = Integer.valueOf(object.getString("minValue"));

            if (object.containsKey("startTime") && object.containsKey("endTime")) {
                long startTime = CommonUtil.getStartAndEndTimestamp(object.getString("startTime"), "start");
                long endTime = CommonUtil.getStartAndEndTimestamp(object.getString("endTime"), "end");

                salesVolume = orderItemDao.countByCreateTimeRange(gradeObject.getItemId(), gradeObject.getItemSku(), startTime, endTime, type);
            } else {
                salesVolume = gradeObject.getSalesVolume();
            }

            if (salesVolume >= minValue && salesVolume <= maxValue) {
                count++;
            }

            if (returnOrNot(allOrNot, count)) {
                return count;
            }
        }

        if (ruleData.containsKey("salesVolume3days") && ruleData.getJSONObject("salesVolume3days") != null) {
            JSONObject object = ruleData.getJSONObject("salesVolume7days");
            // 值
            int maxValue = Integer.valueOf(object.getString("maxValue"));
            int minValue = Integer.valueOf(object.getString("minValue"));
            // 当前时间 - 7 天的时间戳
            int salesVolume3daysCount = orderItemService.countByCreateTimeRange(nowDate,-3, gradeObject.getItemId(), gradeObject.getItemSku(), ITEM.getCode());

            if (salesVolume3daysCount >= minValue && salesVolume3daysCount <= maxValue) {
                count++;
            }

            if (returnOrNot(allOrNot, count)) {
                return count;
            }
        }

        if (ruleData.containsKey("salesVolume7days") && ruleData.getJSONObject("salesVolume7days") != null) {
            JSONObject object = ruleData.getJSONObject("salesVolume7days");
            // 值
            int maxValue = Integer.valueOf(object.getString("maxValue"));
            int minValue = Integer.valueOf(object.getString("minValue"));
            // 当前时间 - 7 天的时间戳
            int salesVolume7daysCount = orderItemService.countByCreateTimeRange(nowDate,-7, gradeObject.getItemId(), gradeObject.getItemSku(), ITEM.getCode());

            if (salesVolume7daysCount >= minValue && salesVolume7daysCount <= maxValue) {
                count++;
            }

            if (returnOrNot(allOrNot, count)) {
                return count;
            }
        }

        if (ruleData.containsKey("salesVolume15days") && ruleData.getJSONObject("salesVolume15days") != null) {
            JSONObject object = ruleData.getJSONObject("salesVolume15days");
            // 值
            int maxValue = Integer.valueOf(object.getString("maxValue"));
            int minValue = Integer.valueOf(object.getString("minValue"));
            // 当前时间 - 15 天的时间戳
            int salesVolume7daysCount = orderItemService.countByCreateTimeRange(nowDate,-15, gradeObject.getItemId(), gradeObject.getItemSku(), ITEM.getCode());

            if (salesVolume7daysCount >= minValue && salesVolume7daysCount <= maxValue) {
                count++;
            }

            if (returnOrNot(allOrNot, count)) {
                return count;
            }
        }

        if (ruleData.containsKey("salesVolume30days") && ruleData.getJSONObject("salesVolume30days") != null) {
            JSONObject object = ruleData.getJSONObject("salesVolume30days");
            // 值
            int maxValue = Integer.valueOf(object.getString("maxValue"));
            int minValue = Integer.valueOf(object.getString("minValue"));
            // 当前时间 - 30 天的时间戳
            int salesVolume7daysCount = orderItemService.countByCreateTimeRange(nowDate,-30, gradeObject.getItemId(), gradeObject.getItemSku(), ITEM.getCode());

            if (salesVolume7daysCount >= minValue && salesVolume7daysCount <= maxValue) {
                count++;
            }

            if (returnOrNot(allOrNot, count)) {
                return count;
            }
        }

        return count;
    }

//    private int judgeIntegerRange(int salesVolume, int ruleValue, String type, int count) {
//        if ("=".equals(type) && salesVolume == ruleValue) {
//            count++;
//        } else if ("<=".equals(type) && salesVolume <= ruleValue) {
//            count++;
//        } else if (">=".equals(type) && salesVolume >= ruleValue) {
//            count++;
//        } else if (">".equals(type) && salesVolume > ruleValue) {
//            count++;
//        } else if ("<".equals(type) && salesVolume < ruleValue) {
//            count++;
//        }
//
//        return count;
//    }

    private boolean returnOrNot(boolean allOrNot, int count) {
        if (!allOrNot && count > 0) {
            return true;
        }
        return false;
    }




}
