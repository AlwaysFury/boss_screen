package com.boss.client.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.boss.client.dto.GradeObject;
import com.boss.client.enities.Grade;
import org.springframework.stereotype.Repository;

import java.util.List;


/**
 * 分类
 */
@Repository
public interface GradeDao extends BaseMapper<Grade> {
    List<GradeObject> getProduct();

    List<GradeObject> getSku();
}
