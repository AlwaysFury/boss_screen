package com.boss.bossscreen.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.boss.bossscreen.dto.ConditionDTO;
import com.boss.bossscreen.enities.Shop;
import com.boss.bossscreen.vo.ShopVO;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


/**
 * 分类
 */
@Repository
public interface ShopDao extends BaseMapper<Shop> {


    Integer shopCount(@Param("condition") ConditionDTO conditionDTO);


    List<ShopVO> shopList(@Param("current") Long current, @Param("size") Long size, @Param("condition") ConditionDTO condition);

}
