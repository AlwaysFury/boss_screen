package com.boss.client.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.boss.client.enities.excelEnities.ProductExpression;
import com.boss.client.vo.ProductExpressionVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * @Description
 * @Author 罗宇航
 * @Date 2024/4/10
 */
public interface ProductExpressionService extends IService<ProductExpression>  {

    void importExcel(MultipartFile file);

    List<ProductExpressionVO> getProductExpressionInfoById(long itemId);
}
