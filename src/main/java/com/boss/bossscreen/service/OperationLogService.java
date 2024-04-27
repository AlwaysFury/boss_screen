package com.boss.bossscreen.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.boss.bossscreen.dto.ConditionDTO;
import com.boss.bossscreen.dto.OperationLogDTO;
import com.boss.bossscreen.dto.UpdateStatusDTO;
import com.boss.bossscreen.enities.OperationLog;
import com.boss.bossscreen.vo.OperationLogVO;
import com.boss.bossscreen.vo.PageResult;


/**
 * 操作日志服务
 */
public interface OperationLogService extends IService<OperationLog> {

    /**
     * 查询日志列表
     *
     * @param condition 条件
     * @return 日志列表
     */
    PageResult<OperationLogVO> optLogList(ConditionDTO condition);

    /**
     * 保存或更新
     * @param operationLogDTO
     */
    void saveOrUpdateLog(OperationLogDTO operationLogDTO);

    /**
     * 批量逻辑删除
     * @param updateStatusDTO
     */
    void delete(UpdateStatusDTO updateStatusDTO);

    OperationLogVO getOptLogById(int id);

}
