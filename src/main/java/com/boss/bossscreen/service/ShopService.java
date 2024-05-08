package com.boss.bossscreen.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.boss.bossscreen.dto.ConditionDTO;
import com.boss.bossscreen.dto.ShopDTO;
import com.boss.bossscreen.dto.UpdateStatusDTO;
import com.boss.bossscreen.enities.Shop;
import com.boss.bossscreen.vo.PageResult;
import com.boss.bossscreen.vo.SelectVO;
import com.boss.bossscreen.vo.ShopVO;

import java.util.List;

/**
 * @Description
 * @Author 罗宇航
 * @Date 2024/4/10
 */
public interface ShopService extends IService<Shop>  {

    void saveOrUpdateToken(ShopDTO shopDTO);

    PageResult<ShopVO> shopsListByCondition(ConditionDTO condition);

    void updateShopsStatus(UpdateStatusDTO updateStatusDTO);

    void refreshShopToken();

    void refreshShopTokenByAccount();

    String getAccessTokenByShopId(String shopId);

    List<SelectVO> getShopSelect();

    void saveName(long shopId, String name);
}
