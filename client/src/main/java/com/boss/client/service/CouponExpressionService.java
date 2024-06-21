package com.boss.client.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.boss.common.enities.excelEnities.CouponExpression;
import org.springframework.web.multipart.MultipartFile;

/**
 * @Description
 * @Author 罗宇航
 * @Date 2024/4/10
 */
public interface CouponExpressionService extends IService<CouponExpression>  {

    void importExcel(long shopId, String dateStr, MultipartFile file);
}
