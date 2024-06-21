package com.boss.task.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.boss.common.enities.OperationLog;
import org.springframework.stereotype.Repository;


/**
 * 操作日志
 */
@Repository
public interface OperationLogDao extends BaseMapper<OperationLog> {
}
