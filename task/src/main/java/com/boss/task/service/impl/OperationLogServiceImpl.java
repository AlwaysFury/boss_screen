package com.boss.task.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.boss.common.enities.OperationLog;
import com.boss.task.dao.OperationLogDao;
import com.boss.task.service.OperationLogService;
import org.springframework.stereotype.Service;


/**
 * 操作日志服务
 */
@Service
public class OperationLogServiceImpl extends ServiceImpl<OperationLogDao, OperationLog> implements OperationLogService {

}
