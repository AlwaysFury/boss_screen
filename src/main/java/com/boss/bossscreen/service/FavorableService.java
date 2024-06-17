package com.boss.bossscreen.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.boss.bossscreen.enities.excelEnities.Favorable;
import org.springframework.web.multipart.MultipartFile;

/**
 * @Description
 * @Author 罗宇航
 * @Date 2024/4/10
 */
public interface FavorableService extends IService<Favorable>  {

    void importExcel(long shopId, String dateStr, MultipartFile file);
}
