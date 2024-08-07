package com.boss.client.service.impl;

import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.boss.client.dao.*;
import com.boss.client.dto.RuleConditionDTO;
import com.boss.client.enities.Grade;
import com.boss.client.enities.Rule;
import com.boss.client.enities.Sku;
import com.boss.client.service.GradeService;
import com.boss.common.enities.OperationLog;
import com.boss.common.enities.Product;
import com.boss.common.enities.ProductOrImgTag;
import com.boss.common.util.CommonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import static com.boss.common.constant.OptTypeConst.SYSTEM_LOG;
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
    private ProductDao productDao;

    @Autowired
    private SkuDao skuDao;

    @Autowired
    private OrderItemServiceImpl orderItemService;

    @Autowired
    private OperationLogServiceImpl operationLogService;

    @Autowired
    private OrderItemDao orderItemDao;

    @Autowired
    private ProductOrImgTagDao productOrImgTagDao;

    @Autowired
    private RuleServiceImpl ruleService;

    @Autowired
    private RedisServiceImpl redisService;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void refreshGrade(String type) {
        // 先查出所有要同步等级的产品和款号，封装成GradeObject

        List<Rule> ruleList;
        if (ITEM.getCode().equals(type)) {
            ruleList = ruleService.getRuleList(ITEM.getCode());
            productDao.update(new UpdateWrapper<Product>().set("grade", ""));
        } else {
            ruleList = ruleService.getRuleList(PHOTO.getCode());
            skuDao.update(new UpdateWrapper<Sku>().set("grade", ""));
        }

//        List<GradeObject> gradeObjects = new ArrayList<>();
//        if (ITEM.getCode().equals(type)) {
//            gradeObjects = gradeDao.getProduct();
//        } else {
//            gradeObjects = gradeDao.getSku();
//        }
//
//        List<Rule> finalRuleList = ruleList;
//
//

//
//        for (int i = 0; i < gradeObjects.size(); i++) {
//            GradeObject gradeObject = gradeObjects.get(i);
//            String grade = getGrade(gradeObject, finalRuleList, ITEM.getCode());
//            if (grade != null) {
//                long itemId = gradeObject.getItemId();
//
//                if (ITEM.getCode().equals(type)) {
//                    List<String> itemIds = new ArrayList<>();
//                    if (productMap.containsKey(grade)) {
//                        itemIds = productMap.get(grade);
//                    }
//                    itemIds.add(String.valueOf(itemId));
//                    productMap.put(grade, itemIds);
//                } else {
//                    List<String> itemIds = new ArrayList<>();
//                    if (skuMap.containsKey(grade)) {
//                        itemIds = skuMap.get(grade);
//                    }
//                    itemIds.add(String.valueOf(itemId));
//                    skuMap.put(grade, itemIds);
//                }
//            }
//        }
//
//        for (String grade : productMap.keySet()) {
//            List<String> itemIds = productMap.get(grade);
//            if (itemIds.size() > 100) {
//                List<String> itemIdList = new ArrayList<>();
//                for (int i = 0; i < itemIds.size(); i += 100) {
//                    itemIdList.add(String.join(",", itemIds.subList(i, Math.min(i + 100, itemIds.size()))));
//                }
//                for (String itemIdStr : itemIdList) {
//                    productDao.update(new UpdateWrapper<Product>().set("grade", grade).in("id", itemIdStr));
//                }
//            } else {
//                productDao.update(new UpdateWrapper<Product>().set("grade", grade).in("id", itemIds));
//            }
//        }
//
//        for (String grade : skuMap.keySet()) {
//            List<String> itemIds = skuMap.get(grade);
//            if (itemIds.size() > 100) {
//                List<String> itemIdList = new ArrayList<>();
//                for (int i = 0; i < itemIds.size(); i += 100) {
//                    itemIdList.add(String.join(",", itemIds.subList(i, Math.min(i + 100, itemIds.size()))));
//                }
//                for (String itemIdStr : itemIdList) {
//                    skuDao.update(new UpdateWrapper<Sku>().set("grade", grade).in("id", itemIdStr));
//                }
//            } else {
//                skuDao.update(new UpdateWrapper<Sku>().set("grade", grade).in("id", itemIds));
//            }
//        }

        List<Integer> weightList = new ArrayList<>();
        List<List<Long>> idList = new ArrayList<>();
        List<String> gradeList = new ArrayList<>();

        for (Rule rule : ruleList) {
            JSONObject ruleData = JSONObject.parseObject(rule.getRuleData());

            if (ruleData == null || ruleData.keySet().size() == 0) {
                continue;
            }
            String grade = rule.getGrade();
            boolean allOrNot = rule.getAllOrNot();
            int weight = rule.getWeight();

            weightList.add(weight);
            gradeList.add(grade);

            Set<Long> idSet = getRuleList(ruleData, allOrNot, type);

            idList.add(new ArrayList<>(idSet));
        }

        optimizeAndEliminateIntersections(idList, weightList);

        Map<String, List<Long>> map = new HashMap<>();
        for (int i = 0; i < idList.size(); i++) {
            String grade = gradeList.get(i);
            List<Long> itemIds = new ArrayList<>();
            if (map.containsKey(grade)) {
                itemIds = map.get(grade);
            }
            itemIds.addAll(idList.get(i));
            map.put(grade, itemIds);
        }

        updateGrade(type, map);
    }

    private void updateGrade(String type, Map<String, List<Long>> map) {

        if (ITEM.getCode().equals(type)) {
            List<List<Long>> allIds = new ArrayList<>();
            List<Long> productIds = productDao.getAllIds();
            allIds.add(productIds);
            List<OperationLog> operationLogs = new ArrayList<>();
            for (String grade : map.keySet()) {
                List<Long> itemIds = map.get(grade);

                allIds.add(itemIds);

                if (itemIds.size() > 50) {
                    for (int i = 0; i < itemIds.size(); i += 50) {
                        List<Long> ids = itemIds.subList(i, Math.min(i + 50, itemIds.size()));
                        productDao.update(new UpdateWrapper<Product>().set("grade", grade).in("id", ids));
                    }
                } else if (itemIds.size() > 0 && itemIds.size() <= 50) {
                    productDao.update(new UpdateWrapper<Product>().set("grade", grade).in("id", itemIds));
                }

                for (Long itemId : itemIds) {
                    String redisGrade = redisService.getStr(GRADE_PRODUCT + itemId);
                    if (redisGrade == null || "".equals(redisGrade)) {
                        redisGrade = "\"\"";
                    }
                    if (redisGrade.equals(grade)) {
                        continue;
                    }
                    OperationLog operationLog = OperationLog.builder()
                            .optDesc("产品 " + itemId + " 等级由 " + redisGrade + " 变为 " + grade)
                            .optType(SYSTEM_LOG)
                            .build();
                    operationLogs.add(operationLog);

                    redisService.set(GRADE_PRODUCT + itemId, grade);
                }
            }

            List<Long> intersectionIds = new ArrayList<>(CommonUtil.getIntersection(allIds));
            productIds.removeAll(intersectionIds);
            if (allIds.size() > 0) {
                for (List<Long> itemId : allIds) {
                    String redisGrade = redisService.getStr(GRADE_PRODUCT + itemId);
                    if (redisGrade != null || !"".equals(redisGrade)) {
                        OperationLog operationLog = OperationLog.builder()
                                .optDesc("产品 " + itemId + " 等级由 " + redisGrade + " 变为 \"\"")
                                .optType(SYSTEM_LOG)
                                .build();
                        operationLogs.add(operationLog);
                    }
                    redisService.set(GRADE_PRODUCT + itemId, "");
                }
            }

//            operationLogService.saveBatch(operationLogs);

        } else {
            for (String grade : map.keySet()) {
                List<Long> itemIds = map.get(grade);
                if (itemIds.size() > 50) {
                    for (int i = 0; i < itemIds.size(); i += 50) {
                        List<Long> ids = itemIds.subList(i, Math.min(i + 50, itemIds.size()));
                        skuDao.update(new UpdateWrapper<Sku>().set("grade", grade).in("id", ids));
                    }
                } else if (itemIds.size() > 0 && itemIds.size() <= 50) {
                    skuDao.update(new UpdateWrapper<Sku>().set("grade", grade).in("id", itemIds));
                }
            }
        }
    }

    private Set<Long> getRuleList(JSONObject ruleData, boolean allOrNot, String type) {
        Set<Long> idSet = new HashSet<>();

        Date nowDate = new Date();
        List<List<Long>> allIds = new ArrayList<>();
        List<Long> ruleProductByPrice = new ArrayList<>();
        List<Long> ruleSkuByTime = new ArrayList<>();
        List<Long> ruleByTag = new ArrayList<>();
        List<Long> ids = new ArrayList<>();
        RuleConditionDTO ruleConditionDTO = new RuleConditionDTO();
        ruleConditionDTO.setAllOrNot(allOrNot);
        if (ITEM.getCode().equals(type)) {
            if (ruleData.containsKey("itemId") && ruleData.getJSONObject("itemId") != null) {
                ruleConditionDTO.setItemId(ruleData.getJSONObject("itemId").getLong("value"));
            }

            if (ruleData.containsKey("categoryId") && ruleData.getJSONObject("categoryId") != null) {
                ruleConditionDTO.setCategoryId(ruleData.getJSONObject("categoryId").getLong("value"));
            }

            if (ruleData.containsKey("status") && ruleData.getJSONObject("status") != null) {
                ruleConditionDTO.setStatus(ruleData.getJSONObject("status").getString("value"));
            }

            if (ruleData.containsKey("price") && ruleData.getJSONObject("price") != null) {
                JSONObject object = ruleData.getJSONObject("price");
                // 最小价格
                BigDecimal minPrice = new BigDecimal(object.getString("minPrice"));
                // 最大价格
                BigDecimal maxPrice = new BigDecimal(object.getString("maxPrice"));

                ruleConditionDTO.setMinPrice(minPrice);
                ruleConditionDTO.setMaxPrice(maxPrice);

                ruleProductByPrice = productDao.getRuleProductByPrice(ruleConditionDTO);
                allIds.add(ruleProductByPrice);
            }

            if (ruleData.containsKey("createTime") && ruleData.getJSONObject("createTime") != null) {
                JSONObject object = ruleData.getJSONObject("createTime");
                long startTime = CommonUtil.string2Timestamp(object.getString("startTime"));
                long endTime = CommonUtil.string2Timestamp(object.getString("endTime"));

                ruleConditionDTO.setCreateStartTime(startTime);
                ruleConditionDTO.setCreateEndTime(endTime);
            }

            if (ruleData.containsKey("tag") && ruleData.getJSONObject("tag") != null) {
                JSONArray tagArray = ruleData.getJSONObject("tag").getJSONArray("value");
                if (tagArray != null && tagArray.size() > 0) {
                    List<Long> tagIds = tagArray.toJavaList(Long.class);
                    List<String> typeList = new ArrayList<>();
                    typeList.add(type);
                    typeList.add("AUTO");
                    ruleByTag = productOrImgTagDao.selectList(new QueryWrapper<ProductOrImgTag>().in("tag_type", typeList).in("tag_id", tagIds).groupBy("itemOrImg_id")).stream()
                            .map(ProductOrImgTag::getItemOrImgId).collect(Collectors.toList());
                    allIds.add(ruleByTag);
                }
            }

            if (ruleConditionDTO.getItemId() != null || ruleConditionDTO.getCategoryId() != null || ruleConditionDTO.getStatus() != null
                    || ruleConditionDTO.getCreateStartTime() != null || ruleConditionDTO.getCreateEndTime() != null) {
                ids = productDao.getRuleProduct(ruleConditionDTO);
                allIds.add(ids);
            }
        }

        if (PHOTO.getCode().equals(type)) {
            if (ruleData.containsKey("itemSku") && ruleData.getJSONObject("itemSku") != null) {
                ruleConditionDTO.setSkuName(ruleData.getJSONObject("itemSku").getString("value"));
            }

            if (ruleConditionDTO.getSkuName() != null) {
                ids = skuDao.getRuleSku(ruleConditionDTO);
                allIds.add(ids);
            }

            if (ruleData.containsKey("createTime") && ruleData.getJSONObject("createTime") != null) {
                JSONObject object = ruleData.getJSONObject("createTime");
                long startTime = CommonUtil.string2Timestamp(object.getString("startTime"));
                long endTime = CommonUtil.string2Timestamp(object.getString("endTime"));

                ruleConditionDTO.setCreateStartTime(startTime);
                ruleConditionDTO.setCreateEndTime(endTime);

                ruleSkuByTime = skuDao.getRuleSkuByTime(ruleConditionDTO);
                allIds.add(ruleSkuByTime);
            }

            if (ruleData.containsKey("tag") && ruleData.getJSONObject("tag") != null) {
                JSONArray tagArray = ruleData.getJSONObject("tag").getJSONArray("value");
                if (tagArray != null && tagArray.size() > 0) {
                    List<Long> tagIds = tagArray.toJavaList(Long.class);
                    ruleByTag = productOrImgTagDao.selectList(new QueryWrapper<ProductOrImgTag>().eq("tag_type", PHOTO.getCode()).in("tag_id", tagIds).groupBy("itemOrImg_id")).stream()
                            .map(ProductOrImgTag::getItemOrImgId).collect(Collectors.toList());
                    allIds.add(ruleByTag);
                }
            }
        }

        List<Long> idsBySalesVolume = new ArrayList<>();
        if (ruleData.containsKey("salesVolume") && ruleData.getJSONObject("salesVolume") != null) {
            JSONObject object = ruleData.getJSONObject("salesVolume");

            int maxValue = 0;
            if (object.containsKey("maxValue")) {
                maxValue = object.getInteger("maxValue");
            }
            int minValue = 0;
            if (object.containsKey("minValue")) {
                minValue = object.getInteger("minValue");
            }

            long startTime = 0;
            if (object.containsKey("startTime")) {
                startTime = CommonUtil.string2Timestamp(object.getString("startTime"));
            }
            long endTime = 0;
            if (object.containsKey("endTime")) {
                endTime = CommonUtil.string2Timestamp(object.getString("endTime"));
            }

            if (ITEM.getCode().equals(type)) {
                if (object.containsKey("startTime") || object.containsKey("endTime")) {
                    idsBySalesVolume = orderItemDao.getProductBySalesCreateTimeRange(startTime, endTime, minValue, maxValue);
                } else {
                    idsBySalesVolume = productDao.getRuleProductBySales(object.getInteger("minValue"), maxValue);
                }
            } else {
                if (object.containsKey("startTime") || object.containsKey("endTime")) {
                    idsBySalesVolume = orderItemDao.getSkuBySalesCreateTimeRange(startTime, endTime, minValue, maxValue);
                } else {
                    idsBySalesVolume = orderItemDao.getSkuBySalesCreateTimeRange(0, 0, minValue, maxValue);
                }
            }
            allIds.add(idsBySalesVolume);
        }

        List<Long> idsBySalesVolume3days = new ArrayList<>();
        if (ruleData.containsKey("salesVolume3days") && ruleData.getJSONObject("salesVolume3days") != null) {
            JSONObject object = ruleData.getJSONObject("salesVolume3days");

            // 当前时间 - 3 天的时间戳
            idsBySalesVolume3days = getIdsBySalesCreateTimeRange(-3, object, nowDate, type);
            allIds.add(idsBySalesVolume3days);
        }

        List<Long> idsBySalesVolume7days = new ArrayList<>();
        if (ruleData.containsKey("salesVolume7days") && ruleData.getJSONObject("salesVolume7days") != null) {
            JSONObject object = ruleData.getJSONObject("salesVolume7days");

            // 当前时间 - 7 天的时间戳
            idsBySalesVolume7days = getIdsBySalesCreateTimeRange(-7, object, nowDate, type);
            allIds.add(idsBySalesVolume7days);
        }

        List<Long> idsBySalesVolume15days = new ArrayList<>();
        if (ruleData.containsKey("salesVolume15days") && ruleData.getJSONObject("salesVolume15days") != null) {
            JSONObject object = ruleData.getJSONObject("salesVolume15days");

            // 当前时间 - 15 天的时间戳
            idsBySalesVolume15days = getIdsBySalesCreateTimeRange(-15, object, nowDate, type);
            allIds.add(idsBySalesVolume15days);
        }

        List<Long> idsBySalesVolume30days = new ArrayList<>();
        if (ruleData.containsKey("salesVolume30days") && ruleData.getJSONObject("salesVolume30days") != null) {
            JSONObject object = ruleData.getJSONObject("salesVolume30days");

            // 当前时间 - 30 天的时间戳
            idsBySalesVolume30days = getIdsBySalesCreateTimeRange(-30, object, nowDate, type);
            allIds.add(idsBySalesVolume30days);
        }


        if (allOrNot) {
            // 全部满足：交集
            idSet = CommonUtil.getIntersection(allIds);
        } else {
            // 任意条件：并集
            idSet = CommonUtil.getUnion(allIds);
        }

        return idSet;
    }

    private List<Long> getIdsBySalesCreateTimeRange(int offset, JSONObject object, Date nowDate, String type) {
        List<Long> ids = new ArrayList<>();
        long startTime = DateUtil.offsetDay(nowDate, -offset).getTime() / 1000;
        long endTime = nowDate.getTime() / 1000;

        int maxValue = 0;
        if (object.containsKey("maxValue")) {
            maxValue = object.getInteger("maxValue");
        }
        int minValue = 0;
        if (object.containsKey("minValue")) {
            minValue = object.getInteger("minValue");
        }
        if (ITEM.getCode().equals(type)) {
            ids = orderItemDao.getProductBySalesCreateTimeRange(startTime, endTime, minValue, maxValue);
        } else {
            ids = orderItemDao.getSkuBySalesCreateTimeRange(startTime, endTime, minValue, maxValue);
        }
        return ids;
    };

//    private int getSatisfactionCount(GradeObject gradeObject,  JSONObject ruleData, boolean allOrNot, String type) {
//        Date nowDate = new Date();
//
//        int count = 0;
//
//        if (ITEM.getCode().equals(type)) {
//            if (ruleData.containsKey("itemId") && ruleData.getJSONObject("itemId") != null) {
//                if (ruleData.getJSONObject("itemId").getString("value").equals(String.valueOf(gradeObject.getItemId()))) {
//                    count ++;
//                }
//
//                if (returnOrNot(allOrNot, count)) {
//                    return count;
//                }
//            }
//
//            if (ruleData.containsKey("categoryId") && ruleData.getJSONObject("categoryId") != null) {
//                if (ruleData.getJSONObject("categoryId").getString("value").equals(String.valueOf(gradeObject.getCategoryId()))) {
//                    count ++;
//                }
//
//                if (returnOrNot(allOrNot, count)) {
//                    return count;
//                }
//            }
//
//            if (ruleData.containsKey("status") && ruleData.getJSONObject("status") != null) {
//                if (ruleData.getJSONObject("status").getString("value").equals(String.valueOf(gradeObject.getStatus()))) {
//                    count ++;
//                }
//
//                if (returnOrNot(allOrNot, count)) {
//                    return count;
//                }
//            }
//
//            if (ruleData.containsKey("price") && ruleData.getJSONObject("price") != null) {
//                JSONObject object = ruleData.getJSONObject("price");
//                // 最小价格
//                BigDecimal minPrice = new BigDecimal(object.getString("minPrice"));
//                // 最大价格
//                BigDecimal maxPrice = new BigDecimal(object.getString("maxPrice"));
//
//                BigDecimal tempItemMinPrice = gradeObject.getPrice();
//                if (ITEM.getCode().equals(type)) {
//                    tempItemMinPrice = orderItemDao.itemMinPrice(gradeObject.getItemId());
//                }
//
//                BigDecimal itemMinPrice = tempItemMinPrice == null ? new BigDecimal(0) : tempItemMinPrice;
//
//                if (itemMinPrice.compareTo(minPrice) >= 0 && itemMinPrice.compareTo(maxPrice) <= 0) {
//                    count++;
//                }
//
//                if (returnOrNot(allOrNot, count)) {
//                    return count;
//                }
//            }
//        }
//
//        if (PHOTO.getCode().equals(type)) {
//            if (ruleData.containsKey("itemSku") && ruleData.getJSONObject("itemSku") != null) {
//                String itemSku = gradeObject.getItemSku();
//
//                if (ruleData.getJSONObject("itemSku").getString("value").equals(itemSku)) {
//                    count ++;
//                }
//
//                if (returnOrNot(allOrNot, count)) {
//                    return count;
//                }
//            }
//        }
//
//        if (ruleData.containsKey("tag") && ruleData.getJSONArray("tag") != null) {
//            List<Long> list = ruleData.getJSONArray("tag").toJavaList(Long.class);
//
//            List<ProductOrImgTag> productOrImgTags = productOrImgTagDao.selectList(new QueryWrapper<ProductOrImgTag>().select("tag_id").eq("type", type).eq("itemOrimg_id", gradeObject.getItemId()));
//
//            List<Long> tagList = productOrImgTags.stream().map(ProductOrImgTag::getTagId).collect(Collectors.toList());
//            if (tagList != null && tagList.size() != 0 && tagList.stream().anyMatch(list::contains)) {
//                count ++;
//            }
//
//            if (returnOrNot(allOrNot, count)) {
//                return count;
//            }
//        }
//
//        if (ruleData.containsKey("createTime") && ruleData.getJSONObject("createTime") != null) {
//            JSONObject object = ruleData.getJSONObject("createTime");
//            Long createTime = gradeObject.getCreateTime();
//            LocalDateTime tempCreateTime = CommonUtil.timestamp2LocalDateTime(createTime);
//            LocalDateTime startTime = CommonUtil.string2LocalDateTime(object.getString("startTime"));
//            LocalDateTime endTime = CommonUtil.string2LocalDateTime(object.getString("endTime"));
//
//            if ((tempCreateTime.isAfter(startTime) && tempCreateTime.isBefore(endTime)) || tempCreateTime.equals(startTime) || tempCreateTime.equals(endTime)) {
//                count ++;
//            }
//
//            if (returnOrNot(allOrNot, count)) {
//                return count;
//            }
//        }
//
//        if (ruleData.containsKey("salesVolume") && ruleData.getJSONObject("salesVolume") != null) {
//            JSONObject object = ruleData.getJSONObject("salesVolume");
//            int salesVolume = 0;
//
//            if (object.containsKey("startTime") && object.containsKey("endTime")) {
//                long startTime = CommonUtil.string2Timestamp(object.getString("startTime"));
//                long endTime = CommonUtil.string2Timestamp(object.getString("endTime"));
//
//                salesVolume = orderItemDao.countByCreateTimeRange(gradeObject.getItemId(), gradeObject.getItemSku(), startTime, endTime, type);
//            } else {
//                salesVolume = gradeObject.getSalesVolume();
//            }
//
//            if (object.containsKey("maxValue") && !object.containsKey("minValue") && salesVolume <= Integer.valueOf(object.getString("maxValue"))) {
//                count++;
//            } else if (!object.containsKey("maxValue") && object.containsKey("minValue") && salesVolume >= Integer.valueOf(object.getString("minValue"))) {
//                count++;
//            } else if (object.containsKey("maxValue") && object.containsKey("minValue")) {
//                // 值
//                int maxValue = Integer.valueOf(object.getString("maxValue"));
//                int minValue = Integer.valueOf(object.getString("minValue"));
//                if (salesVolume >= minValue && salesVolume <= maxValue) {
//                    count++;
//                }
//            }
//
//
//            if (returnOrNot(allOrNot, count)) {
//                return count;
//            }
//        }
//
//        if (ruleData.containsKey("salesVolume3days") && ruleData.getJSONObject("salesVolume3days") != null) {
//            JSONObject object = ruleData.getJSONObject("salesVolume3days");
//
//            // 当前时间 - 7 天的时间戳
//            int salesVolume3daysCount = orderItemService.countByCreateTimeRange(nowDate,-3, gradeObject.getItemId(), gradeObject.getItemSku(), ITEM.getCode());
//
//            if (object.containsKey("maxValue") && !object.containsKey("minValue") && salesVolume3daysCount <= Integer.valueOf(object.getString("maxValue"))) {
//                count++;
//            } else if (!object.containsKey("maxValue") && object.containsKey("minValue") && salesVolume3daysCount >= Integer.valueOf(object.getString("minValue"))) {
//                count++;
//            } else if (object.containsKey("maxValue") && object.containsKey("minValue")) {
//                // 值
//                int maxValue = Integer.valueOf(object.getString("maxValue"));
//                int minValue = Integer.valueOf(object.getString("minValue"));
//                if (salesVolume3daysCount >= minValue && salesVolume3daysCount <= maxValue) {
//                    count++;
//                }
//            }
//
//            if (returnOrNot(allOrNot, count)) {
//                return count;
//            }
//        }
//
//        if (ruleData.containsKey("salesVolume7days") && ruleData.getJSONObject("salesVolume7days") != null) {
//            JSONObject object = ruleData.getJSONObject("salesVolume7days");
//
//            // 当前时间 - 7 天的时间戳
//            int salesVolume7daysCount = orderItemService.countByCreateTimeRange(nowDate,-7, gradeObject.getItemId(), gradeObject.getItemSku(), ITEM.getCode());
//
//            if (object.containsKey("maxValue") && !object.containsKey("minValue") && salesVolume7daysCount <= Integer.valueOf(object.getString("maxValue"))) {
//                count++;
//            } else if (!object.containsKey("maxValue") && object.containsKey("minValue") && salesVolume7daysCount >= Integer.valueOf(object.getString("minValue"))) {
//                count++;
//            } else if (object.containsKey("maxValue") && object.containsKey("minValue")) {
//                // 值
//                int maxValue = Integer.valueOf(object.getString("maxValue"));
//                int minValue = Integer.valueOf(object.getString("minValue"));
//                if (salesVolume7daysCount >= minValue && salesVolume7daysCount <= maxValue) {
//                    count++;
//                }
//            }
//
//            if (returnOrNot(allOrNot, count)) {
//                return count;
//            }
//        }
//
//        if (ruleData.containsKey("salesVolume15days") && ruleData.getJSONObject("salesVolume15days") != null) {
//            JSONObject object = ruleData.getJSONObject("salesVolume15days");
//
//            // 当前时间 - 15 天的时间戳
//            int salesVolume15daysCount = orderItemService.countByCreateTimeRange(nowDate,-15, gradeObject.getItemId(), gradeObject.getItemSku(), ITEM.getCode());
//
//            if (object.containsKey("maxValue") && !object.containsKey("minValue") && salesVolume15daysCount <= Integer.valueOf(object.getString("maxValue"))) {
//                count++;
//            } else if (!object.containsKey("maxValue") && object.containsKey("minValue") && salesVolume15daysCount >= Integer.valueOf(object.getString("minValue"))) {
//                count++;
//            } else if (object.containsKey("maxValue") && object.containsKey("minValue")) {
//                // 值
//                int maxValue = Integer.valueOf(object.getString("maxValue"));
//                int minValue = Integer.valueOf(object.getString("minValue"));
//                if (salesVolume15daysCount >= minValue && salesVolume15daysCount <= maxValue) {
//                    count++;
//                }
//            }
//
//            if (returnOrNot(allOrNot, count)) {
//                return count;
//            }
//        }
//
//        if (ruleData.containsKey("salesVolume30days") && ruleData.getJSONObject("salesVolume30days") != null) {
//            JSONObject object = ruleData.getJSONObject("salesVolume30days");
//
//            // 当前时间 - 30 天的时间戳
//            int salesVolume30daysCount = orderItemService.countByCreateTimeRange(nowDate,-30, gradeObject.getItemId(), gradeObject.getItemSku(), ITEM.getCode());
//
//            if (object.containsKey("maxValue") && !object.containsKey("minValue") && salesVolume30daysCount <= Integer.valueOf(object.getString("maxValue"))) {
//                count++;
//            } else if (!object.containsKey("maxValue") && object.containsKey("minValue") && salesVolume30daysCount >= Integer.valueOf(object.getString("minValue"))) {
//                count++;
//            } else if (object.containsKey("maxValue") && object.containsKey("minValue")) {
//                // 值
//                int maxValue = Integer.valueOf(object.getString("maxValue"));
//                int minValue = Integer.valueOf(object.getString("minValue"));
//                if (salesVolume30daysCount >= minValue && salesVolume30daysCount <= maxValue) {
//                    count++;
//                }
//            }
//
//            if (returnOrNot(allOrNot, count)) {
//                return count;
//            }
//        }
//
//        return count;
//    }
//
//    private boolean returnOrNot(boolean allOrNot, int count) {
//        if (!allOrNot && count > 0) {
//            return true;
//        }
//        return false;
//    }

    public static void optimizeAndEliminateIntersections(List<List<Long>> lists, List<Integer> weights) {
        int maxWeightIndex = findMaxWeightIndex(weights);
        List<Long> maxWeightList = lists.get(maxWeightIndex);

        for (int i = 0; i < lists.size(); i++) {
            if (i == maxWeightIndex) continue;

            List<Long> currentList = lists.get(i);
            Set<Long> intersection = new HashSet<>(currentList);
            intersection.retainAll(maxWeightList);

            if (!intersection.isEmpty() && weights.get(i) < weights.get(maxWeightIndex)) {
                currentList.removeAll(intersection);
            }
        }
    }

    private static int findMaxWeightIndex(List<Integer> weights) {
        int maxIndex = 0;
        int maxWeight = weights.get(0);
        for (int i = 1; i < weights.size(); i++) {
            if (weights.get(i) > maxWeight) {
                maxWeight = weights.get(i);
                maxIndex = i;
            }
        }
        return maxIndex;
    }

//    public static void main(String[] args) {
//        Set<Integer> set = new HashSet<>();
//        List<Integer> list1 = new ArrayList<>(set);
//        List<Integer> list2 = new ArrayList<>(Arrays.asList(3, 4, 5, 6, 7));
//        List<Integer> list3 = new ArrayList<>(Arrays.asList(4, 5, 6, 7, 2));
//
//        List<List<Integer>> lists = new ArrayList<>();
//        lists.add(list1);
//        lists.add(list2);
//        lists.add(list3);
//
//        List<Integer> weights = new ArrayList<>(Arrays.asList(10, 20, 15));
//
//        optimizeAndEliminateIntersections(lists, weights);
//
//        // Print the final state of each list
//        for (List<Integer> list : lists) {
//            System.out.println(list);
//        }
//    }
}
