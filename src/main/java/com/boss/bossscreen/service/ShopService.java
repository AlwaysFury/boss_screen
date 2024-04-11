package com.boss.bossscreen.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.boss.bossscreen.dto.ShopDTO;
import com.boss.bossscreen.enities.Shop;
import com.boss.bossscreen.vo.ConditionVO;
import com.boss.bossscreen.vo.PageResult;
import com.boss.bossscreen.vo.ShopVO;
import com.boss.bossscreen.vo.UpdateStatusVO;

/**
 * @Description
 * @Author 罗宇航
 * @Date 2024/4/10
 */
public interface ShopService  extends IService<Shop>  {

    void saveOrUpdateToken(ShopVO shopVO);

    PageResult<ShopDTO> shopsListByCondition(ConditionVO conditionVO);

    void updateShopsStatus(UpdateStatusVO updateStatusVO);
}
