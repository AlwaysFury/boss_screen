package com.boss.task.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.boss.common.dto.ShopDTO;
import com.boss.common.enities.Shop;
import com.boss.task.dao.ShopDao;
import com.boss.task.service.ShopService;
import com.boss.task.util.ShopeeUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

import static com.boss.common.constant.RedisPrefixConst.SHOP_TOKEN;

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

    @Autowired
    private RedisServiceImpl redisService;

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

        redisService.set(SHOP_TOKEN + shopDTO.getShopId(), shopDTO.getAccessToken());
    }


    @Transactional(rollbackFor = Exception.class)
    @Override
    public void refreshShopToken() {
        QueryWrapper<Shop> wrapper = new QueryWrapper<>();
        wrapper.select("id", "shop_id", "access_token", "refresh_token", "update_time");

        List<Shop> oldList = shopDao.selectList(wrapper);
        for (Shop shop : oldList) {
            long shopId = shop.getShopId();

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

            redisService.del(SHOP_TOKEN + shopId);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void refreshShopTokenByAccount() {
        QueryWrapper<Shop> wrapper = new QueryWrapper<>();
        wrapper.select("id", "shop_id", "access_token", "refresh_token","account_id","update_time").isNotNull("account_id");

        List<Shop> oldList = shopDao.selectList(wrapper);
        for (Shop shop : oldList) {
            long shopId = shop.getShopId();

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

    @Override
    public String getAccessTokenByShopId(String shopId) {

        String token = (String) redisService.get(SHOP_TOKEN + shopId);

        if (token == null) {
            QueryWrapper<Shop> queryWrapper = new QueryWrapper<>();
            queryWrapper.select("access_token" ).eq("shop_id", shopId);
            token = shopDao.selectOne(queryWrapper).getAccessToken();

            redisService.set(SHOP_TOKEN + shopId, token);
        }

        return token;
    }
}
