package com.boss.bossscreen.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.boss.bossscreen.dao.CostDao;
import com.boss.bossscreen.dto.ConditionDTO;
import com.boss.bossscreen.dto.CostDTO;
import com.boss.bossscreen.enities.Cost;
import com.boss.bossscreen.service.CostService;
import com.boss.bossscreen.util.BeanCopyUtils;
import com.boss.bossscreen.util.PageUtils;
import com.boss.bossscreen.vo.CostVO;
import com.boss.bossscreen.vo.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 操作日志服务
 */
@Service
public class CostServiceImpl extends ServiceImpl<CostDao, Cost> implements CostService {

    @Autowired
    private CostService costService;

    @Autowired
    private CostDao costDao;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void saveOrUpdateCost(CostDTO costDTO) {
        Cost cost = BeanCopyUtils.copyObject(costDTO, Cost.class);
        costService.saveOrUpdate(cost);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void deleteCost(List<Integer> ids) {
        costService.deleteCost(ids);
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
}
