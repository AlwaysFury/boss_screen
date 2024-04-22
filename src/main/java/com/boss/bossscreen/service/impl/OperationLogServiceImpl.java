package com.boss.bossscreen.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.boss.bossscreen.dao.OperationLogDao;
import com.boss.bossscreen.dto.ConditionDTO;
import com.boss.bossscreen.dto.OperationLogDTO;
import com.boss.bossscreen.dto.UpdateStatusDTO;
import com.boss.bossscreen.enities.OperationLog;
import com.boss.bossscreen.service.OperationLogService;
import com.boss.bossscreen.util.BeanCopyUtils;
import com.boss.bossscreen.util.PageUtils;
import com.boss.bossscreen.vo.OperationLogVO;
import com.boss.bossscreen.vo.PageResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import static com.boss.bossscreen.constant.OptTypeConst.USER_LOG;

/**
 * 操作日志服务
 */
@Service
public class OperationLogServiceImpl extends ServiceImpl<OperationLogDao, OperationLog> implements OperationLogService {

    @Override
    public PageResult<OperationLogVO> optLogList(ConditionDTO condition) {
        Page<OperationLog> page = new Page<>(PageUtils.getCurrent(), PageUtils.getSize());
        // 查询日志列表
        Page<OperationLog> operationLogPage = this.page(page, new LambdaQueryWrapper<OperationLog>().orderByDesc(OperationLog::getCreateTime));
        List<OperationLogVO> operationLogDTOList = BeanCopyUtils.copyList(operationLogPage.getRecords(), OperationLogVO.class);
        return new PageResult<>(operationLogDTOList, (int) operationLogPage.getTotal());
    }

    @Override
    public void saveOrUpdateLog(OperationLogDTO operationLogDTO) {
        OperationLog operationLog = BeanCopyUtils.copyObject(operationLogDTO, OperationLog.class);
        operationLog.setStatus(1);
        operationLog.setOptType(USER_LOG);
        this.saveOrUpdate(operationLog);
    }


    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateLogStatus(UpdateStatusDTO updateStatusDTO) {
        // 更新账号状态
        List<OperationLog> logList = updateStatusDTO.getIdList().stream()
                .map(id -> OperationLog.builder()
                        .id(id)
                        .status(updateStatusDTO.getStatus())
                        .build())
                .collect(Collectors.toList());
        this.updateBatchById(logList);
    }

}
