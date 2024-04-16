package com.boss.bossscreen.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.boss.bossscreen.enities.MainAccount;
import com.boss.bossscreen.dto.ConditionDTO;
import com.boss.bossscreen.vo.MainAccountVO;
import com.boss.bossscreen.vo.PageResult;
import com.boss.bossscreen.dto.UpdateStatusDTO;

import java.io.IOException;
import java.text.ParseException;

/**
 * @Description
 * @Author 罗宇航
 * @Date 2024/4/11
 */
public interface MainAccountService extends IService<MainAccount> {

    void saveOrUpdateToken(String code, long mainAccountId) throws ParseException, IOException;

    PageResult<MainAccountVO> accountsListByCondition(ConditionDTO conditionDTO);

    void updateAccountsStatus(UpdateStatusDTO updateStatusDTO);

//    void refreshAccountToken();
}
