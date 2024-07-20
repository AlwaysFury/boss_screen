package com.boss.client.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.boss.client.enities.excelEnities.Ads;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.Set;

/**
 * @Description
 * @Author 罗宇航
 * @Date 2024/4/10
 */
public interface AdsService extends IService<Ads>  {

    Map<String, Set<Long>> importCsv(MultipartFile file, int adsOrderCount, int searchOrderCount, int searchAutoOrderCount, int searchManualOrderCount, int relationOrderCount);
}
