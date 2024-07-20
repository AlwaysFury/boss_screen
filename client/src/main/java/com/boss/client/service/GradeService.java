package com.boss.client.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.boss.client.enities.Grade;

/**
 * @Description
 * @Author 罗宇航
 * @Date 2024/4/10
 */
public interface GradeService extends IService<Grade>  {
    void refreshGrade(String type);
}
