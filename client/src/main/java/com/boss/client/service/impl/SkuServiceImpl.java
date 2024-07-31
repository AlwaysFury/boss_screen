package com.boss.client.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.boss.client.dao.SkuDao;
import com.boss.client.dto.ConditionDTO;
import com.boss.client.dto.SkuDTO;
import com.boss.client.enities.Sku;
import com.boss.client.exception.BizException;
import com.boss.client.service.SkuService;
import com.boss.client.vo.PageResult;
import com.boss.client.vo.SkuInfoVO;
import com.boss.client.vo.SkuStatisticsVO;
import com.boss.client.vo.SkuVO;
import com.boss.common.util.BeanCopyUtils;
import com.boss.common.util.CommonUtil;
import com.boss.common.util.PageUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;


@Service
public class SkuServiceImpl extends ServiceImpl<SkuDao, Sku> implements SkuService {

    @Autowired
    private SkuDao skuDao;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void saveOrUpdateSku(SkuDTO skuDTO) {
        Sku sku = BeanCopyUtils.copyObject(skuDTO, Sku.class);
        List<Long> relevanceIds = skuDTO.getRelevanceIds();
        if (relevanceIds != null && !relevanceIds.isEmpty()) {
            sku.setRelevanceIds(relevanceIds.stream().map(String::valueOf).collect(Collectors.joining(",")));
        }

        this.saveOrUpdate(sku);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void deleteSku(List<Long> ids) {
        skuDao.deleteBatchIds(ids);
    }

    @Override
    public PageResult<SkuVO> skuListByCondition(ConditionDTO condition) {
        Integer count = skuDao.skuCount(condition);
        if (count == 0) {
            return new PageResult<>();
        }
        List<SkuVO> skuList = skuDao.skuList(PageUtils.getLimitCurrent(), PageUtils.getSize(), condition)
                .stream().map(sku -> {
                    SkuVO skuVO = BeanCopyUtils.copyObject(sku, SkuVO.class);
                    skuVO.setCount(0);
                    String relevanceIds = sku.getRelevanceIds();
                    if (relevanceIds != null && !relevanceIds.isEmpty()) {
                        skuVO.setCount(relevanceIds.split(",").length);
                    }
                    skuVO.setCreateTime(CommonUtil.localDateTime2String(sku.getCreateTime()));
                    return skuVO;
                }).collect(Collectors.toList());
        System.out.println(skuList);

        return new PageResult<>(skuList, count);
    }

    @Override
    public SkuInfoVO getSkuById(long id) {
        Sku sku = this.getOne(new QueryWrapper<Sku>().eq("id", id));
        if (sku == null) {
            throw new BizException("该款号不存在");
        }
        SkuInfoVO skuInfoVO = BeanCopyUtils.copyObject(sku, SkuInfoVO.class);
        skuInfoVO.setCreateTime(CommonUtil.localDateTime2String(sku.getCreateTime()));
        String relevanceIds = sku.getRelevanceIds();
        if (relevanceIds != null && !relevanceIds.isEmpty()) {
            List<Long> relevanceIdList = Arrays.stream(relevanceIds.split(",")).map(Long::valueOf).collect(Collectors.toList());
            skuInfoVO.setRelevanceIds(relevanceIdList);
        }

        return skuInfoVO;
    }

    @Override
    public List<SkuStatisticsVO> getSkuStatistics(String ids) {
        Sku sku = this.getOne(new QueryWrapper<Sku>().eq("id", ids));
        List<SkuStatisticsVO> skuList = handleStatisticsResult(ids, true);

        String relevanceIds = sku.getRelevanceIds();
        if (Objects.nonNull(relevanceIds)) {
            List<SkuStatisticsVO> relevanceSkuList = handleStatisticsResult(relevanceIds, false);
            skuList.addAll(relevanceSkuList);
        }

        return skuList;
    }

    public List<SkuStatisticsVO> handleStatisticsResult(String ids, boolean isTop) {
        List<SkuStatisticsVO> skuList = skuDao.skuSaleVolume(ids);
        if (isTop) {
            skuList.get(0).setTop(true);
        }

        List<Map<String, String>> skuMaps = skuDao.skuItemShop(ids);
        Map<String, Map<String, String>> result = new HashMap<>();
        for (Map<String, String> record : skuMaps) {
            String name = record.get("name");

            // 初始化内部Map，如果还没有为当前groupKey创建
            result.computeIfAbsent(name, k -> new HashMap<>());
            Map<String, String> resultMap = result.get(name);

            String itemId = String.valueOf(record.get("itemId"));
            String itemIdValue = resultMap.get("itemId");
            // 如果键不存在或当前值为空字符串，则直接添加或覆盖
            if (itemIdValue == null || itemIdValue.isEmpty()) {
                resultMap.put("itemId", itemId);
            } else {
                // 如果键存在，用逗号拼接新旧值
                resultMap.put("itemId", itemIdValue + "," + itemId);
            }

            String shopName = record.get("shopName");
            String shopNameValue = resultMap.get("shopName");
            // 如果键不存在或当前值为空字符串，则直接添加或覆盖
            if (shopNameValue == null || shopNameValue.isEmpty()) {
                resultMap.put("shopName", shopName);
            } else {
                // 如果键存在，用逗号拼接新旧值
                if (!shopNameValue.contains(shopName)) {
                    resultMap.put("shopName", shopNameValue + "," + shopName);
                }
            }
        }

        for (SkuStatisticsVO skuStatisticsVO : skuList) {
            String name = skuStatisticsVO.getName();
            if (result.containsKey(name)) {
                Map<String, String> map = result.get(name);
                skuStatisticsVO.setItemIds(map.get("itemId"));
                skuStatisticsVO.setShopNames(map.get("shopName"));
            }
        }

        return skuList;
    }


}
