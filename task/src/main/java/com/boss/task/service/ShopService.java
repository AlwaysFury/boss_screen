package com.boss.task.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.boss.common.dto.ShopDTO;
import com.boss.common.enities.Shop;

/**
 * @Description
 * @Author 罗宇航
 * @Date 2024/4/10
 */
public interface ShopService extends IService<Shop>  {

    void saveOrUpdateToken(ShopDTO shopDTO);

    void refreshShopToken();

    void refreshShopTokenByAccount();

    String getAccessTokenByShopId(String shopId);
}
