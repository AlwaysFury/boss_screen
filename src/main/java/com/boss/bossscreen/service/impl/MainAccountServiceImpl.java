package com.boss.bossscreen.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.boss.bossscreen.dao.MainAccountDao;
import com.boss.bossscreen.dao.ShopDao;
import com.boss.bossscreen.dto.ShopAndAccountConditionDTO;
import com.boss.bossscreen.dto.MainAccountDTO;
import com.boss.bossscreen.dto.ShopDTO;
import com.boss.bossscreen.dto.UpdateStatusDTO;
import com.boss.bossscreen.enities.MainAccount;
import com.boss.bossscreen.enities.Shop;
import com.boss.bossscreen.service.MainAccountService;
import com.boss.bossscreen.util.PageUtils;
import com.boss.bossscreen.util.ShopeeUtil;
import com.boss.bossscreen.vo.MainAccountVO;
import com.boss.bossscreen.vo.PageResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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
            MainAccount mainAccount = new MainAccount();
            BeanUtil.copyProperties(mainAccountDTO, mainAccount);
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
    public PageResult<MainAccountVO> accountsListByCondition(ShopAndAccountConditionDTO condition) {
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
        List<MainAccount> accountList = updateStatusDTO.getIdList().stream()
                .map(id -> MainAccount.builder()
                        .id(id)
                        .status(updateStatusDTO.getStatus())
                        .build())
                .collect(Collectors.toList());
        this.updateBatchById(accountList);

        // 更新账号下的店铺状态
        UpdateWrapper<Shop> updateWrapper = new UpdateWrapper<>();
        updateWrapper.set("status", updateStatusDTO.getStatus());
        updateWrapper.in("account_id", updateStatusDTO.getIdList());
        shopDao.update(updateWrapper);
    }

//    @Override
//    public void refreshAccountToken() {
//        QueryWrapper<MainAccount> wrapper = new QueryWrapper<>();
//        wrapper.select("id", "account_id", "access_token", "refresh_token");
//
//        List<MainAccount> oldList = mainAccountDao.selectList(wrapper);
//        for (MainAccount account : oldList) {
//            long accountId = account.getAccountId();
//
//            JSONObject object = ShopeeUtil.refreshToken(account.getRefreshToken(), accountId, "account");
//            log.info("====={} 的 token：{}", accountId, object);
//
//            if ("error".equals(object.getString("error"))) {
//                continue;
//            }
//
//            String newAccessToken = object.getString("access_token");
//            String newRefreshToken = object.getString("refresh_token");
//
//            UpdateWrapper<Shop> shopWrapper = new UpdateWrapper<>();
//            shopWrapper.set("access_token", newAccessToken);
//            shopWrapper.set("refresh_token", newRefreshToken);
//            shopWrapper.eq("account_id", accountId);
//            shopDao.update(shopWrapper);
//
//            UpdateWrapper<MainAccount> accountWrapper = new UpdateWrapper<>();
//            accountWrapper.set("access_token", newAccessToken);
//            accountWrapper.set("refresh_token", newRefreshToken);
//            accountWrapper.eq("account_id", accountId);
//            this.update(accountWrapper);
//        }
//    }

}
