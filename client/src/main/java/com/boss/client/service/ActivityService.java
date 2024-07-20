package com.boss.client.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.boss.client.enities.excelEnities.Activity;
import org.springframework.web.multipart.MultipartFile;

/**
 * @Description
 * @Author 罗宇航
 * @Date 2024/4/10
 */
public interface ActivityService extends IService<Activity>  {

    void importExcel(long shopId, String mainName, String subName, String date, MultipartFile file);
}
