package com.boss.bossscreen.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.boss.bossscreen.enities.excelEnities.SetDiscounts;
import org.springframework.web.multipart.MultipartFile;

/**
 * @Description
 * @Author 罗宇航
 * @Date 2024/6/17
 */
public interface SetDiscountsService extends IService<SetDiscounts> {

    void importExcel(long shopId, String dateStr, MultipartFile file);
}
