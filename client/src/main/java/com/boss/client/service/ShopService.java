package com.boss.client.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.boss.client.vo.PageResult;
import com.boss.common.vo.SelectVO;
import com.boss.client.vo.ShopVO;
import com.boss.client.dto.ConditionDTO;
import com.boss.common.dto.ShopDTO;
import com.boss.common.dto.UpdateStatusDTO;
import com.boss.common.enities.Shop;

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

//    void refreshShopToken();
//
//    void refreshShopTokenByAccount();
//
//    String getAccessTokenByShopId(String shopId);

    List<SelectVO> getShopSelect();

    void saveName(long shopId, String name);
}
