package com.boss.client.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.boss.client.dao.*;
import com.boss.client.dto.GradeObject;
import com.boss.client.enities.Grade;
import com.boss.client.enities.Rule;
import com.boss.client.enities.Sku;
import com.boss.client.service.GradeService;
import com.boss.common.enities.Product;
import com.boss.common.enities.ProductOrImgTag;
import com.boss.common.util.CommonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

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
    private ProductDao productDao;

    @Autowired
    private SkuDao skuDao;

    @Autowired
    private OrderItemServiceImpl orderItemService;

    @Autowired
    private OrderItemDao orderItemDao;

    @Autowired
    private ProductOrImgTagDao productOrImgTagDao;

    @Autowired
    private RuleServiceImpl ruleService;

    @Autowired
    private RedisServiceImpl redisService;

    @Autowired
    @Qualifier("customThreadPool")
    private ThreadPoolExecutor customThreadPool;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void refreshGrade(String type) {
        // 先查出所有要同步等级的产品和款号，封装成GradeObject

        List<Rule> ruleList;
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


        Map<String, List<String>> productMap = new HashMap<>();
        Map<String, List<String>> skuMap = new HashMap<>();

        for (int i = 0; i < gradeObjects.size(); i++) {
            GradeObject gradeObject = gradeObjects.get(i);
            String grade = getGrade(gradeObject, finalRuleList, ITEM.getCode());
            if (grade != null) {
                long itemId = gradeObject.getItemId();

                if (ITEM.getCode().equals(type)) {
                    List<String> itemIds = new ArrayList<>();
                    if (productMap.containsKey(grade)) {
                        itemIds = productMap.get(grade);
                    }
                    itemIds.add(String.valueOf(itemId));
                    productMap.put(grade, itemIds);
                } else {
                    List<String> itemIds = new ArrayList<>();
                    if (skuMap.containsKey(grade)) {
                        itemIds = skuMap.get(grade);
                    }
                    itemIds.add(String.valueOf(itemId));
                    skuMap.put(grade, itemIds);
                }
            }
        }

        for (String grade : productMap.keySet()) {
            List<String> itemIds = productMap.get(grade);
            if (itemIds.size() > 100) {
                List<String> itemIdList = new ArrayList<>();
                for (int i = 0; i < itemIds.size(); i += 100) {
                    itemIdList.add(String.join(",", itemIds.subList(i, Math.min(i + 100, itemIds.size()))));
                }
                for (String itemIdStr : itemIdList) {
                    productDao.update(new UpdateWrapper<Product>().set("grade", grade).in("id", itemIdStr));
                }
            } else {
                productDao.update(new UpdateWrapper<Product>().set("grade", grade).in("id", itemIds));
            }
        }

        for (String grade : skuMap.keySet()) {
            List<String> itemIds = skuMap.get(grade);
            if (itemIds.size() > 100) {
                List<String> itemIdList = new ArrayList<>();
                for (int i = 0; i < itemIds.size(); i += 100) {
                    itemIdList.add(String.join(",", itemIds.subList(i, Math.min(i + 100, itemIds.size()))));
                }
                for (String itemIdStr : itemIdList) {
                    skuDao.update(new UpdateWrapper<Sku>().set("grade", grade).in("id", itemIdStr));
                }
            } else {
                skuDao.update(new UpdateWrapper<Sku>().set("grade", grade).in("id", itemIds));
            }
        }
    }

    public String getGrade(GradeObject gradeObject, List<Rule> ruleList, String type) {

        String grade = "";
        int weight = 0;
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
            if (ruleCount > 1 && rule.getWeight() >= weight) {
                weight = rule.getWeight();
                grade = rule.getGrade();
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
                BigDecimal minPrice = new BigDecimal(object.getString("minPrice"));
                // 最大价格
                BigDecimal maxPrice = new BigDecimal(object.getString("maxPrice"));

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

            List<ProductOrImgTag> productOrImgTags = productOrImgTagDao.selectList(new QueryWrapper<ProductOrImgTag>().select("tag_id").eq("type", type).eq("itemOrimg_id", gradeObject.getItemId()));

            List<Long> tagList = productOrImgTags.stream().map(ProductOrImgTag::getTagId).collect(Collectors.toList());
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

            if (object.containsKey("startTime") && object.containsKey("endTime")) {
                long startTime = CommonUtil.string2Timestamp(object.getString("startTime"));
                long endTime = CommonUtil.string2Timestamp(object.getString("endTime"));

                salesVolume = orderItemDao.countByCreateTimeRange(gradeObject.getItemId(), gradeObject.getItemSku(), startTime, endTime, type);
            } else {
                salesVolume = gradeObject.getSalesVolume();
            }

            if (object.containsKey("maxValue") && !object.containsKey("minValue") && salesVolume <= Integer.valueOf(object.getString("maxValue"))) {
                count++;
            } else if (!object.containsKey("maxValue") && object.containsKey("minValue") && salesVolume >= Integer.valueOf(object.getString("minValue"))) {
                count++;
            } else if (object.containsKey("maxValue") && object.containsKey("minValue")) {
                // 值
                int maxValue = Integer.valueOf(object.getString("maxValue"));
                int minValue = Integer.valueOf(object.getString("minValue"));
                if (salesVolume >= minValue && salesVolume <= maxValue) {
                    count++;
                }
            }


            if (returnOrNot(allOrNot, count)) {
                return count;
            }
        }

        if (ruleData.containsKey("salesVolume3days") && ruleData.getJSONObject("salesVolume3days") != null) {
            JSONObject object = ruleData.getJSONObject("salesVolume7days");

            // 当前时间 - 7 天的时间戳
            int salesVolume3daysCount = orderItemService.countByCreateTimeRange(nowDate,-3, gradeObject.getItemId(), gradeObject.getItemSku(), ITEM.getCode());

            if (object.containsKey("maxValue") && !object.containsKey("minValue") && salesVolume3daysCount <= Integer.valueOf(object.getString("maxValue"))) {
                count++;
            } else if (!object.containsKey("maxValue") && object.containsKey("minValue") && salesVolume3daysCount >= Integer.valueOf(object.getString("minValue"))) {
                count++;
            } else if (object.containsKey("maxValue") && object.containsKey("minValue")) {
                // 值
                int maxValue = Integer.valueOf(object.getString("maxValue"));
                int minValue = Integer.valueOf(object.getString("minValue"));
                if (salesVolume3daysCount >= minValue && salesVolume3daysCount <= maxValue) {
                    count++;
                }
            }

            if (returnOrNot(allOrNot, count)) {
                return count;
            }
        }

        if (ruleData.containsKey("salesVolume7days") && ruleData.getJSONObject("salesVolume7days") != null) {
            JSONObject object = ruleData.getJSONObject("salesVolume7days");

            // 当前时间 - 7 天的时间戳
            int salesVolume7daysCount = orderItemService.countByCreateTimeRange(nowDate,-7, gradeObject.getItemId(), gradeObject.getItemSku(), ITEM.getCode());

            if (object.containsKey("maxValue") && !object.containsKey("minValue") && salesVolume7daysCount <= Integer.valueOf(object.getString("maxValue"))) {
                count++;
            } else if (!object.containsKey("maxValue") && object.containsKey("minValue") && salesVolume7daysCount >= Integer.valueOf(object.getString("minValue"))) {
                count++;
            } else if (object.containsKey("maxValue") && object.containsKey("minValue")) {
                // 值
                int maxValue = Integer.valueOf(object.getString("maxValue"));
                int minValue = Integer.valueOf(object.getString("minValue"));
                if (salesVolume7daysCount >= minValue && salesVolume7daysCount <= maxValue) {
                    count++;
                }
            }

            if (returnOrNot(allOrNot, count)) {
                return count;
            }
        }

        if (ruleData.containsKey("salesVolume15days") && ruleData.getJSONObject("salesVolume15days") != null) {
            JSONObject object = ruleData.getJSONObject("salesVolume15days");

            // 当前时间 - 15 天的时间戳
            int salesVolume15daysCount = orderItemService.countByCreateTimeRange(nowDate,-15, gradeObject.getItemId(), gradeObject.getItemSku(), ITEM.getCode());

            if (object.containsKey("maxValue") && !object.containsKey("minValue") && salesVolume15daysCount <= Integer.valueOf(object.getString("maxValue"))) {
                count++;
            } else if (!object.containsKey("maxValue") && object.containsKey("minValue") && salesVolume15daysCount >= Integer.valueOf(object.getString("minValue"))) {
                count++;
            } else if (object.containsKey("maxValue") && object.containsKey("minValue")) {
                // 值
                int maxValue = Integer.valueOf(object.getString("maxValue"));
                int minValue = Integer.valueOf(object.getString("minValue"));
                if (salesVolume15daysCount >= minValue && salesVolume15daysCount <= maxValue) {
                    count++;
                }
            }

            if (returnOrNot(allOrNot, count)) {
                return count;
            }
        }

        if (ruleData.containsKey("salesVolume30days") && ruleData.getJSONObject("salesVolume30days") != null) {
            JSONObject object = ruleData.getJSONObject("salesVolume30days");

            // 当前时间 - 30 天的时间戳
            int salesVolume30daysCount = orderItemService.countByCreateTimeRange(nowDate,-30, gradeObject.getItemId(), gradeObject.getItemSku(), ITEM.getCode());

            if (object.containsKey("maxValue") && !object.containsKey("minValue") && salesVolume30daysCount <= Integer.valueOf(object.getString("maxValue"))) {
                count++;
            } else if (!object.containsKey("maxValue") && object.containsKey("minValue") && salesVolume30daysCount >= Integer.valueOf(object.getString("minValue"))) {
                count++;
            } else if (object.containsKey("maxValue") && object.containsKey("minValue")) {
                // 值
                int maxValue = Integer.valueOf(object.getString("maxValue"));
                int minValue = Integer.valueOf(object.getString("minValue"));
                if (salesVolume30daysCount >= minValue && salesVolume30daysCount <= maxValue) {
                    count++;
                }
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
