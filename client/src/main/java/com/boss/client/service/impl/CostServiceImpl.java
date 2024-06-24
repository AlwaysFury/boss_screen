package com.boss.client.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.boss.client.dao.CostDao;
import com.boss.client.service.CostService;
import com.boss.client.vo.CostVO;
import com.boss.client.vo.PageResult;
import com.boss.common.dto.ConditionDTO;
import com.boss.common.dto.CostDTO;
import com.boss.common.enities.Cost;
import com.boss.common.util.BeanCopyUtils;
import com.boss.common.util.CommonUtil;
import com.boss.common.util.PageUtils;
import com.boss.common.vo.SelectVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.boss.common.constant.RedisPrefixConst.CLOTHES_TYPE;


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
    public void deleteCost(List<Long> ids) {
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
    public List<SelectVO> getCostType() {
        List<SelectVO> list = new ArrayList<>();
        Set<String> keys = redisService.keys(CLOTHES_TYPE + "*");
        for (String key : keys) {
            String value = key.substring(key.indexOf(":") + 1, key.length());
            SelectVO vo = SelectVO.builder()
                    .key(value)
                    .value(value).build();
            list.add(vo);
        }
        return list;
    }
}
