package com.boss.client.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.boss.client.enities.excelEnities.ProductOverview;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * @Description
 * @Author 罗宇航
 * @Date 2024/4/10
 */
public interface ProductOverviewService extends IService<ProductOverview>  {

    void importExcel(long shopId, String dateType, MultipartFile file);

    List<ProductOverview> getProductOverviewInfoById(long itemId);
}
