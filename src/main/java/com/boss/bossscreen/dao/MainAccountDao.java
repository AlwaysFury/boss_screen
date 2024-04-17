package com.boss.bossscreen.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.boss.bossscreen.dto.ShopAndAccountConditionDTO;
import com.boss.bossscreen.enities.MainAccount;
import com.boss.bossscreen.vo.MainAccountVO;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


/**
 * 分类
 */
@Repository
public interface MainAccountDao extends BaseMapper<MainAccount> {


    Integer accountCount(@Param("condition") ShopAndAccountConditionDTO condition);


    List<MainAccountVO> accountList(@Param("current") Long current, @Param("size") Long size, @Param("condition") ShopAndAccountConditionDTO condition);
}
