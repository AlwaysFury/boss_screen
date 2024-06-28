package com.boss.client.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.boss.client.dao.MainAccountDao;
import com.boss.client.dao.ShopDao;
import com.boss.client.service.MainAccountService;
import com.boss.client.util.ShopeeUtil;
import com.boss.client.vo.MainAccountVO;
import com.boss.client.vo.PageResult;
import com.boss.client.dto.ConditionDTO;
import com.boss.common.dto.MainAccountDTO;
import com.boss.common.dto.ShopDTO;
import com.boss.common.dto.UpdateStatusDTO;
import com.boss.common.enities.MainAccount;
import com.boss.common.enities.Shop;
import com.boss.common.util.PageUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;
import java.util.Objects;

/**
 * @Description
 * @Author 罗宇航
 * @Date 2024/4/11
 */

@Service
@Slf4j
public class MainAccountServiceImpl extends ServiceImpl<MainAccountDao, MainAccount> implements MainAccountService {

    @Autowired
    private MainAccountDao mainAccountDao;

    @Autowired
    private ShopDao shopDao;

    @Autowired
    private ShopServiceImpl shopService;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void saveOrUpdateToken(String code, long mainAccountId) throws ParseException, IOException {

        // 获取access_token
        JSONObject object = ShopeeUtil.getAccountAccessToken(code, mainAccountId);
        log.info("首次获取 token 结果：" + object);
        String accessToken = object.getString("access_token");
        String refreshToken = object.getString("refresh_token");
        MainAccountDTO mainAccountDTO = new MainAccountDTO();
        mainAccountDTO.setAccountId(mainAccountId);
        mainAccountDTO.setAuthCode(code);
        mainAccountDTO.setAccessToken(accessToken);
        mainAccountDTO.setRefreshToken(refreshToken);
        mainAccountDTO.setStatus(1);

        // 保存账号 token
        MainAccount existAccount = mainAccountDao.selectOne(new LambdaQueryWrapper<MainAccount>()
                .select(MainAccount::getId)
                .eq(MainAccount::getAccountId, mainAccountId));
        if (Objects.nonNull(existAccount) && !existAccount.getId().equals(mainAccountDTO.getId())) {
            UpdateWrapper<MainAccount> wrapper = new UpdateWrapper<>();
            wrapper.set("access_token", accessToken);
            wrapper.set("refresh_token", refreshToken);
            wrapper.eq("id", existAccount.getId());
            this.update(wrapper);
        } else {
            MainAccount mainAccount = BeanUtil.copyProperties(mainAccountDTO, MainAccount.class);
            this.save(mainAccount);
        }

        // 保存账号下店铺 token
        JSONArray shopIdsArray = object.getJSONArray("shop_id_list");

        for (int i = 0; i < shopIdsArray.size(); i++) {
            ShopDTO shopDTO = new ShopDTO();
            shopDTO.setShopId(shopIdsArray.getLong(i));
            shopDTO.setStatus(1);
            shopDTO.setAuthCode(code);
            shopDTO.setAccessToken(accessToken);
            shopDTO.setRefreshToken(refreshToken);
            shopDTO.setAccountId(mainAccountId);

            shopService.saveOrUpdateToken(shopDTO);
        }
    }

    @Override
    public PageResult<MainAccountVO> accountsListByCondition(ConditionDTO condition) {
        // 查询分类数量
        Integer count = mainAccountDao.accountCount(condition);
        if (count == 0) {
            return new PageResult<>();
        }
        // 分页查询分类列表
        List<MainAccountVO> accountList = mainAccountDao.accountList(PageUtils.getLimitCurrent(), PageUtils.getSize(), condition);
        return new PageResult<>(accountList, count);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateAccountsStatus(UpdateStatusDTO updateStatusDTO) {
        // 更新账号状态
        UpdateWrapper<MainAccount> accountUpdateWrapper = new UpdateWrapper<>();
        accountUpdateWrapper.set("status", updateStatusDTO.getStatus());
        accountUpdateWrapper.in("account_id", updateStatusDTO.getIdList());
        mainAccountDao.update(accountUpdateWrapper);

        // 更新账号下的店铺状态
        UpdateWrapper<Shop> updateWrapper = new UpdateWrapper<>();
        updateWrapper.set("status", updateStatusDTO.getStatus());
        updateWrapper.in("account_id", updateStatusDTO.getIdList());
        shopDao.update(updateWrapper);
    }

}
