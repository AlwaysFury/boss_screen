package com.boss.task.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.boss.common.enities.Product;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;


/**
 * 分类
 */
@Repository
public interface ProductDao extends BaseMapper<Product> {

    List<Map<String, Object>> getCreateIn30Days(@Param("itemIds") List<Long> itemIds);
}
