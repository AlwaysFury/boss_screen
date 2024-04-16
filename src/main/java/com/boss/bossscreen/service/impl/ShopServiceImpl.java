package com.boss.bossscreen.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.boss.bossscreen.dao.ShopDao;
import com.boss.bossscreen.dto.ConditionDTO;
import com.boss.bossscreen.dto.ShopDTO;
import com.boss.bossscreen.dto.UpdateStatusDTO;
import com.boss.bossscreen.enities.Shop;
import com.boss.bossscreen.service.ShopService;
import com.boss.bossscreen.util.PageUtils;
import com.boss.bossscreen.util.ShopeeUtil;
import com.boss.bossscreen.vo.PageResult;
import com.boss.bossscreen.vo.ShopVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @Description
 * @Author 罗宇航
 * @Date 2024/4/10
 */

@Service
@Slf4j
public class ShopServiceImpl extends ServiceImpl<ShopDao, Shop> implements ShopService {

    @Autowired
    private ShopDao shopDao;


    @Transactional(rollbackFor = Exception.class)
    @Override
    public void saveOrUpdateToken(ShopDTO shopDTO) {
        Shop existShop = shopDao.selectOne(new LambdaQueryWrapper<Shop>()
                .select(Shop::getId)
                .eq(Shop::getShopId, shopDTO.getShopId()));
        if (Objects.nonNull(existShop) && !existShop.getId().equals(shopDTO.getId())) {
            UpdateWrapper<Shop> wrapper = new UpdateWrapper<>();
            wrapper.set("access_token", shopDTO.getAccessToken());
            wrapper.set("refresh_token", shopDTO.getRefreshToken());
            wrapper.eq("id", existShop.getId());

            this.update(wrapper);
        } else {
            Shop shop = new Shop();
            BeanUtil.copyProperties(shopDTO, shop);
            this.save(shop);
        }
    }

    @Override
    public PageResult<ShopVO> shopsListByCondition(ConditionDTO condition) {
        // 查询分类数量
        Integer count = shopDao.shopCount(condition);
        if (count == 0) {
            return new PageResult<>();
        }
        // 分页查询分类列表
        List<ShopVO> shopList = shopDao.shopList(PageUtils.getLimitCurrent(), PageUtils.getSize(), condition);
        return new PageResult<>(shopList, count);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateShopsStatus(UpdateStatusDTO updateStatusDTO) {
        List<Shop> shopList = updateStatusDTO.getIdList().stream()
                .map(id -> Shop.builder()
                        .id(id)
                        .status(updateStatusDTO.getStatus())
                        .build())
                .collect(Collectors.toList());
        this.updateBatchById(shopList);
    }

    @Override
    public void refreshShopToken() {
        QueryWrapper<Shop> wrapper = new QueryWrapper<>();
        wrapper.select("id", "shop_id", "access_token", "refresh_token").isNull("account_id");

        List<Shop> oldList = shopDao.selectList(wrapper);
        for (Shop shop : oldList) {
            long shopId = shop.getShopId();

            JSONObject object = ShopeeUtil.refreshToken(shop.getRefreshToken(), shopId, "shop");
            log.info("====={} 的 token：{}", shopId, object);

            if ("error".equals(object.getString("error"))) {
                continue;
            }

            String newAccessToken = object.getString("access_token");
            String newRefreshToken = object.getString("refresh_token");

            UpdateWrapper<Shop> shopWrapper = new UpdateWrapper<>();
            shopWrapper.set("access_token", newAccessToken);
            shopWrapper.set("refresh_token", newRefreshToken);
            shopWrapper.eq("shop_id", shopId);
            shopDao.update(shopWrapper);
        }
    }

    @Override
    public void refreshShopTokenByAccount() {
        QueryWrapper<Shop> wrapper = new QueryWrapper<>();
        wrapper.select("id", "shop_id", "access_token", "refresh_token","account_id").isNotNull("account_id");

        List<Shop> oldList = shopDao.selectList(wrapper);
        for (Shop shop : oldList) {
            long shopId = shop.getShopId();
            long accountId = shop.getAccountId();

            JSONObject object = ShopeeUtil.refreshToken(shop.getRefreshToken(), shopId, "shop");
            log.info("====={} 的 token：{}", shopId, object);

            if (object.getString("error").contains("error")) {
                continue;
            }

            String newAccessToken = object.getString("access_token");
            String newRefreshToken = object.getString("refresh_token");

            UpdateWrapper<Shop> shopWrapper = new UpdateWrapper<>();
            shopWrapper.set("access_token", newAccessToken);
            shopWrapper.set("refresh_token", newRefreshToken);
            shopWrapper.eq("shop_id", shopId);
            shopDao.update(shopWrapper);
        }
    }
}
