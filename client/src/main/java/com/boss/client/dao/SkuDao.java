package com.boss.client.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.boss.client.dto.ConditionDTO;
import com.boss.client.enities.Sku;
import com.boss.client.vo.SkuStatisticsVO;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;


/**
 * 分类
 */
@Repository
public interface SkuDao extends BaseMapper<Sku> {


    Integer skuCount(@Param("condition") ConditionDTO condition);


    List<Sku> skuList(@Param("current") Long current, @Param("size") Long size, @Param("condition") ConditionDTO condition);

    List<SkuStatisticsVO> skuSaleVolume(@Param("ids") String ids);

    List<Map<String, String>> skuItemShop(@Param("ids") String ids);

}
