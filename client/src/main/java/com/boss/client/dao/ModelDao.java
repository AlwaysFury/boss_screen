package com.boss.client.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.boss.client.vo.ModelVO;
import com.boss.common.enities.Model;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


/**
 * 分类
 */
@Repository
public interface ModelDao extends BaseMapper<Model> {



    List<ModelVO> getModelVOListByItemId(@Param("item_id") Long itemId);

}
