package com.boss.client.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.boss.client.enities.excelEnities.SalesMix;
import org.springframework.web.multipart.MultipartFile;

/**
 * @Description
 * @Author 罗宇航
 * @Date 2024/4/10
 */
public interface SalesMixService extends IService<SalesMix>  {

    void importExcel(long shopId, String dateStr, MultipartFile file);
}
