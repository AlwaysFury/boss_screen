package com.boss.bossscreen.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.boss.bossscreen.dao.ShopDao;
import com.boss.bossscreen.dto.ShopDTO;
import com.boss.bossscreen.enities.Shop;
import com.boss.bossscreen.service.ShopService;
import com.boss.bossscreen.vo.ConditionVO;
import com.boss.bossscreen.vo.PageResult;
import com.boss.bossscreen.vo.ShopVO;
import com.boss.bossscreen.vo.UpdateStatusVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @Description
 * @Author 罗宇航
 * @Date 2024/4/10
 */

@Service
public class ShopServiceImpl extends ServiceImpl<ShopDao, Shop> implements ShopService {

    @Autowired
    private ShopDao shopDao;


    @Override
    public void saveOrUpdateToken(ShopVO shopVO) {
        Shop existShop = shopDao.selectOne(new LambdaQueryWrapper<Shop>()
                .select(Shop::getId)
                .eq(Shop::getShopId, shopVO.getShopId()));
        if (Objects.nonNull(existShop) && !existShop.getId().equals(shopVO.getId())) {
            Shop shop = Shop.builder()
                    .id(existShop.getId())
                    .accessToken(shopVO.getAccessToken())
                    .refreshToken(shopVO.getRefreshToken())
                    .build();
            this.updateById(shop);
        } else {
            this.save(shopVO);
        }
    }

    @Override
    public PageResult<ShopDTO> shopsListByCondition(ConditionVO conditionVO) {
        return null;
    }

    @Override
    public void updateShopsStatus(UpdateStatusVO updateStatusVO) {
        List<Shop> shopList = updateStatusVO.getIdList().stream()
                .map(id -> Shop.builder()
                        .id(id)
                        .status(updateStatusVO.getStatus())
                        .build())
                .collect(Collectors.toList());
        this.updateBatchById(shopList);
    }
}
