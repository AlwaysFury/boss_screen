package com.boss.client.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.boss.client.vo.CostVO;
import com.boss.client.vo.PageResult;
import com.boss.common.vo.SelectVO;
import com.boss.client.dto.ConditionDTO;
import com.boss.client.dto.CostDTO;
import com.boss.client.enities.Cost;

import java.util.List;

/**
 * @Description
 * @Author 罗宇航
 * @Date 2024/4/10
 */
public interface CostService extends IService<Cost>  {

    void saveOrUpdateCost(CostDTO costDTO);

    void deleteCost(List<Long> ids);

    PageResult<CostVO> costListByCondition(ConditionDTO condition);

    CostVO getCostById(int id);

    List<SelectVO> getCostType();
}
