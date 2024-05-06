package com.boss.bossscreen.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.boss.bossscreen.dto.ConditionDTO;
import com.boss.bossscreen.dto.CostDTO;
import com.boss.bossscreen.enities.Cost;
import com.boss.bossscreen.vo.CostVO;
import com.boss.bossscreen.vo.PageResult;

import java.util.List;

/**
 * @Description
 * @Author 罗宇航
 * @Date 2024/4/10
 */
public interface CostService extends IService<Cost>  {

    void saveOrUpdateCost(CostDTO costDTO);

    void deleteCost(List<Integer> ids);

    PageResult<CostVO> costListByCondition(ConditionDTO condition);

    CostVO getCostById(int id);

    List<String> getCostType();
}
