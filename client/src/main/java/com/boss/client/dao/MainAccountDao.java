package com.boss.client.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.boss.client.vo.MainAccountVO;
import com.boss.common.dto.ConditionDTO;
import com.boss.common.enities.MainAccount;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


/**
 * 分类
 */
@Repository
public interface MainAccountDao extends BaseMapper<MainAccount> {


    Integer accountCount(@Param("condition") ConditionDTO condition);


    List<MainAccountVO> accountList(@Param("current") Long current, @Param("size") Long size, @Param("condition") ConditionDTO condition);
}
