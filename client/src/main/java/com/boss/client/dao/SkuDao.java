package com.boss.client.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.boss.client.dto.ConditionDTO;
import com.boss.client.enities.Sku;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


/**
 * 分类
 */
@Repository
public interface SkuDao extends BaseMapper<Sku> {


    Integer skuCount(@Param("condition") ConditionDTO condition);


    List<Sku> skuList(@Param("current") Long current, @Param("size") Long size, @Param("condition") ConditionDTO condition);

}
