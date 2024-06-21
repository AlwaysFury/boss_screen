package com.boss.client.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.boss.client.vo.OperationLogVO;
import com.boss.common.dto.ConditionDTO;
import com.boss.common.enities.OperationLog;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


/**
 * 操作日志
 */
@Repository
public interface OperationLogDao extends BaseMapper<OperationLog> {

    Integer logCount(@Param("condition") ConditionDTO condition);


    List<OperationLogVO> logList(@Param("current") Long current, @Param("size") Long size, @Param("condition") ConditionDTO condition);
}
