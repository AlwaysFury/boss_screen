package com.boss.bossscreen.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.boss.bossscreen.dto.ConditionDTO;
import com.boss.bossscreen.enities.OperationLog;
import com.boss.bossscreen.vo.OperationLogVO;
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
