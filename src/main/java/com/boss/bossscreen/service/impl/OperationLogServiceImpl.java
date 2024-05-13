package com.boss.bossscreen.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.boss.bossscreen.dao.OperationLogDao;
import com.boss.bossscreen.dto.ConditionDTO;
import com.boss.bossscreen.dto.OperationLogDTO;
import com.boss.bossscreen.dto.UpdateStatusDTO;
import com.boss.bossscreen.enities.OperationLog;
import com.boss.bossscreen.service.OperationLogService;
import com.boss.bossscreen.util.BeanCopyUtils;
import com.boss.bossscreen.util.CommonUtil;
import com.boss.bossscreen.util.PageUtils;
import com.boss.bossscreen.vo.OperationLogVO;
import com.boss.bossscreen.vo.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.boss.bossscreen.constant.OptTypeConst.USER_LOG;

/**
 * 操作日志服务
 */
@Service
public class OperationLogServiceImpl extends ServiceImpl<OperationLogDao, OperationLog> implements OperationLogService {

    @Autowired
    private OperationLogDao operationLogDao;


    @Override
    public PageResult<OperationLogVO> optLogList(ConditionDTO condition) {
        // 查询分类数量
        Integer count = operationLogDao.logCount(condition);
        if (count == 0) {
            return new PageResult<>();
        }
        // 分页查询分类列表
        List<OperationLogVO> logList = operationLogDao.logList(PageUtils.getLimitCurrent(), PageUtils.getSize(), condition);
        return new PageResult<>(logList, count);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void saveOrUpdateLog(OperationLogDTO operationLogDTO) {
        OperationLog operationLog = BeanCopyUtils.copyObject(operationLogDTO, OperationLog.class);
        operationLog.setOptType(USER_LOG);
        this.saveOrUpdate(operationLog);
    }


    @Transactional(rollbackFor = Exception.class)
    @Override
    public void delete(UpdateStatusDTO updateStatusDTO) {
        operationLogDao.deleteBatchIds(updateStatusDTO.getIdList());
    }

    @Transactional
    @Override
    public OperationLogVO getOptLogById(int id) {
        OperationLog log = operationLogDao.selectOne(new QueryWrapper<OperationLog>().eq("id", id));
        OperationLogVO operationLogVO = BeanCopyUtils.copyObject(log, OperationLogVO.class);
        operationLogVO.setCreateTime(CommonUtil.localDateTime2String(log.getCreateTime()));
        return operationLogVO;
    }

}
