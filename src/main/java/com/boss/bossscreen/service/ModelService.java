package com.boss.bossscreen.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.boss.bossscreen.enities.Model;

import java.util.List;

/**
 * @Description
 * @Author 罗宇航
 * @Date 2024/4/11
 */
public interface ModelService extends IService<Model> {

    void getModel(long itemId, String token, long shopId, List<Model> modelList, List<Model> updateModeList);
}
