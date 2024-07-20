package com.boss.client.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.boss.client.enities.excelEnities.AdsDetail;
import org.springframework.web.multipart.MultipartFile;

/**
 * @Description
 * @Author 罗宇航
 * @Date 2024/4/10
 */
public interface AdsDetailService extends IService<AdsDetail>  {

    void importCsv(MultipartFile file, String type);
}
