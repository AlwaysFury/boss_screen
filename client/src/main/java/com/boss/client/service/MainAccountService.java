package com.boss.client.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.boss.client.vo.MainAccountVO;
import com.boss.client.vo.PageResult;
import com.boss.client.dto.ConditionDTO;
import com.boss.common.dto.UpdateStatusDTO;
import com.boss.common.enities.MainAccount;

import java.io.IOException;
import java.text.ParseException;

/**
 * @Description
 * @Author 罗宇航
 * @Date 2024/4/11
 */
public interface MainAccountService extends IService<MainAccount> {

    void saveOrUpdateToken(String code, long mainAccountId) throws ParseException, IOException;

    PageResult<MainAccountVO> accountsListByCondition(ConditionDTO condition);

    void updateAccountsStatus(UpdateStatusDTO updateStatusDTO);

//    void refreshAccountToken();
}
