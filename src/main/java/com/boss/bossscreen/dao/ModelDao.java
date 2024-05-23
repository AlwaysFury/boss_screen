package com.boss.bossscreen.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.boss.bossscreen.enities.Model;
import com.boss.bossscreen.vo.ModelVO;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


/**
 * 分类
 */
@Repository
public interface ModelDao extends BaseMapper<Model> {


//    Integer shopCount(@Param("condition") ConditionDTO conditionDTO);
//
//
//    List<ShopVO> shopList(@Param("current") Long current, @Param("size") Long size, @Param("condition") ConditionDTO condition);


    List<ModelVO> getModelVOListByItemId(@Param("item_id") Long itemId);

}
