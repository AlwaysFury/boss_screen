package com.boss.task.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.boss.common.enities.PayoutInfo;
import org.springframework.stereotype.Repository;


/**
 * 分类
 */
@Repository
public interface PayoutInfoDao extends BaseMapper<PayoutInfo> {

    long selectMaxTime(long shopId);
}
