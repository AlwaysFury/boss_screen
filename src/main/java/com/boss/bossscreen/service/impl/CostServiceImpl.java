package com.boss.bossscreen.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.boss.bossscreen.dao.CostDao;
import com.boss.bossscreen.dto.ConditionDTO;
import com.boss.bossscreen.dto.CostDTO;
import com.boss.bossscreen.enities.Cost;
import com.boss.bossscreen.service.CostService;
import com.boss.bossscreen.util.BeanCopyUtils;
import com.boss.bossscreen.util.CommonUtil;
import com.boss.bossscreen.util.PageUtils;
import com.boss.bossscreen.vo.CostVO;
import com.boss.bossscreen.vo.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.boss.bossscreen.constant.RedisPrefixConst.CLOTHES_TYPE;

/**
 * 操作日志服务
 */
@Service
public class CostServiceImpl extends ServiceImpl<CostDao, Cost> implements CostService {

    @Autowired
    private CostService costService;

    @Autowired
    private CostDao costDao;

    @Autowired
    private RedisServiceImpl redisService;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void saveOrUpdateCost(CostDTO costDTO) {
        Cost cost = BeanCopyUtils.copyObject(costDTO, Cost.class);
        cost.setStartTime(CommonUtil.string2LocalDateTime(costDTO.getStartTime()));
        cost.setEndTime(CommonUtil.string2LocalDateTime(costDTO.getEndTime()));
        this.saveOrUpdate(cost);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void deleteCost(List<Integer> ids) {
        costDao.deleteBatchIds(ids);
    }

    @Override
    public PageResult<CostVO> costListByCondition(ConditionDTO condition) {
        // 查询分类数量
        Integer count = costDao.costCount(condition);
        if (count == 0) {
            return new PageResult<>();
        }
        // 分页查询分类列表
        List<CostVO> costList = costDao.costList(PageUtils.getLimitCurrent(), PageUtils.getSize(), condition);

        return new PageResult<>(costList, count);
    }

    @Override
    public CostVO getCostById(int id) {
        Cost cost = this.getOne(new QueryWrapper<Cost>().eq("id", id));
        CostVO costVO = BeanCopyUtils.copyObject(cost, CostVO.class);
        costVO.setStartTime(CommonUtil.localDateTime2String(cost.getStartTime()));
        costVO.setEndTime(CommonUtil.localDateTime2String(cost.getEndTime()));
        return costVO;
    }

    @Override
    public List<String> getCostType() {
        Set<String> keys = redisService.keys(CLOTHES_TYPE + "*");
        List<String> types = new ArrayList<>();
        for (String key : keys) {
            types.add(key.substring(key.indexOf(":") + 1, key.length()));
        }
        return types;
    }
}
