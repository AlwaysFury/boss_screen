package com.boss.client.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.boss.client.vo.ShopVO;
import com.boss.client.dto.ConditionDTO;
import com.boss.common.enities.Shop;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


/**
 * 分类
 */
@Repository
public interface ShopDao extends BaseMapper<Shop> {


    Integer shopCount(@Param("condition") ConditionDTO condition);


    List<ShopVO> shopList(@Param("current") Long current, @Param("size") Long size, @Param("condition") ConditionDTO condition);

}
