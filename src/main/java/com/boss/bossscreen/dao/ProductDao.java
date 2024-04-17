package com.boss.bossscreen.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.boss.bossscreen.enities.Product;
import org.springframework.stereotype.Repository;


/**
 * 分类
 */
@Repository
public interface ProductDao extends BaseMapper<Product> {


//    Integer shopCount(@Param("condition") ConditionDTO conditionDTO);
//
//
//    List<ShopVO> shopList(@Param("current") Long current, @Param("size") Long size, @Param("condition") ConditionDTO condition);

}
