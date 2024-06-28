package com.boss.client.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.boss.client.dao.SkuDao;
import com.boss.client.dto.ConditionDTO;
import com.boss.client.dto.SkuDTO;
import com.boss.client.enities.Sku;
import com.boss.client.service.SkuService;
import com.boss.client.vo.PageResult;
import com.boss.client.vo.SkuInfoVO;
import com.boss.client.vo.SkuVO;
import com.boss.common.util.BeanCopyUtils;
import com.boss.common.util.PageUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

                    return skuVO;
                }).collect(Collectors.toList());

        return new PageResult<>(skuList, count);
    }

    @Override
    public SkuInfoVO getSkuById(long id) {
        Sku sku = this.getOne(new QueryWrapper<Sku>().eq("id", id));
        SkuInfoVO skuInfoVO = BeanCopyUtils.copyObject(sku, SkuInfoVO.class);
        String relevanceIds = sku.getRelevanceIds();
        if (relevanceIds != null && !relevanceIds.isEmpty()) {
            List<Map<String, Object>> relevanceNames = skuDao.selectList(new QueryWrapper<Sku>().select("id", "name").in("id", relevanceIds))
                            .stream().map(s -> {
                                Map<String, Object> map = new HashMap<>();
                                map.put("id", s.getId());
                                map.put("name", s.getName());
                                return map;
                            }).collect(Collectors.toList());
            skuInfoVO.setRelevanceList(relevanceNames);
        }

        return skuInfoVO;
    }
}
