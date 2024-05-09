package com.boss.bossscreen.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.boss.bossscreen.dao.RuleDao;
import com.boss.bossscreen.dto.ConditionDTO;
import com.boss.bossscreen.dto.RuleDTO;
import com.boss.bossscreen.enities.Rule;
import com.boss.bossscreen.service.RuleService;
import com.boss.bossscreen.util.BeanCopyUtils;
import com.boss.bossscreen.util.PageUtils;
import com.boss.bossscreen.vo.PageResult;
import com.boss.bossscreen.vo.RuleInfoVO;
import com.boss.bossscreen.vo.RuleVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.boss.bossscreen.constant.RedisPrefixConst.RULE;


/**
 * 操作日志服务
 */
@Service
public class RuleServiceImpl extends ServiceImpl<RuleDao, Rule> implements RuleService {

    @Autowired
    private RuleDao ruleDao;

    @Autowired
    private RedisServiceImpl redisService;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void saveOrUpdateRule(RuleDTO ruleDTO) {
        Rule rule = BeanCopyUtils.copyObject(ruleDTO, Rule.class);
        rule.setRuleData(ruleDTO.getRule().toJSONString());
        this.saveOrUpdate(rule);
        redisService.set(RULE + String.valueOf(rule.getId()), JSON.toJSONString(rule, SerializerFeature.WriteMapNullValue));
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void deleteRule(List<Integer> ids) {
        for (Integer id : ids) {
            redisService.del(RULE + String.valueOf(id));
        }
        ruleDao.deleteBatchIds(ids);
    }

    @Override
    public PageResult<RuleVO> ruleListByCondition(ConditionDTO condition) {
        // 查询分类数量
        Integer count = ruleDao.ruleCount(condition);
        if (count == 0) {
            return new PageResult<>();
        }
        // 分页查询分类列表
        List<RuleVO> ruleList = ruleDao.ruleList(PageUtils.getLimitCurrent(), PageUtils.getSize(), condition);

        return new PageResult<>(ruleList, count);
    }

    @Override
    public RuleInfoVO getRuleById(int id) {
        Rule rule = this.getOne(new QueryWrapper<Rule>().eq("id", id));
        RuleInfoVO ruleInfoVO = BeanCopyUtils.copyObject(rule, RuleInfoVO.class);
        ruleInfoVO.setRule(JSON.parseObject(rule.getRuleData()));

        return ruleInfoVO;
    }
}
