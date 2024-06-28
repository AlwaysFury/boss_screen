
package com.boss.client.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.boss.client.dto.ConditionDTO;
import com.boss.client.dto.SkuDTO;
import com.boss.client.enities.Sku;
import com.boss.client.vo.PageResult;
import com.boss.client.vo.SkuInfoVO;
import com.boss.client.vo.SkuVO;

import java.util.List;

/**
 * @Description
 * @Author 罗宇航
 * @Date 2024/4/10
 */
public interface SkuService extends IService<Sku>  {

    void saveOrUpdateSku(SkuDTO skuDTO);

    void deleteSku(List<Long> ids);

    PageResult<SkuVO> skuListByCondition(ConditionDTO condition);

    SkuInfoVO getSkuById(long id);
//
//    List<SelectVO> getCostType();
}
