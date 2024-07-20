package com.boss.client.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.boss.client.vo.ProductExtraInfoVO;
import com.boss.common.enities.ProductExtraInfo;


/**
 * @Description
 * @Author 罗宇航
 * @Date 2024/4/11
 */
public interface ProductExtraInfoService extends IService<ProductExtraInfo> {

//    void saveOrUpdateProductExtraInfo();

    ProductExtraInfoVO getProductExtraInfoByItemId(Long itemId);

}
