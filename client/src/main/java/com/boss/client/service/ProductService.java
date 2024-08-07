package com.boss.client.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.boss.client.vo.PageResult;
import com.boss.client.vo.ProductInfoVO;
import com.boss.client.vo.ProductVO;
import com.boss.common.dto.RefreshDTO;
import com.boss.common.vo.SelectVO;
import com.boss.client.dto.ConditionDTO;
import com.boss.common.enities.Product;

import java.util.List;

/**
 * @Description
 * @Author 罗宇航
 * @Date 2024/4/11
 */
public interface ProductService extends IService<Product> {

//    void refreshProductByStatus(String status);
//
//    void refreshDeletedProduct();

    PageResult<ProductVO> productListByCondition(ConditionDTO conditionDTO);

    ProductInfoVO getProductInfo(Long itemId);

    List<SelectVO> getCategorySelect();

    List<SelectVO> getStatusSelect();

//    void updateAccountsStatus(UpdateStatusDTO updateStatusDTO);
//
//    void refreshAccountToken();

    void refreshProducts(RefreshDTO refreshDTO);

//    void initProduct(long shopId);



    List<String> getNewerSaleProductNames();
}
