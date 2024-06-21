package com.boss.client.handler;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boss.common.util.PageUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.util.Strings;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Optional;

import static com.boss.common.constant.CommonConst.*;


/**
 * 分页拦截器
 **/
public class PageableHandlerInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String currentPage = request.getParameter(CURRENT);
        String pageSize = Optional.ofNullable(request.getParameter(SIZE)).orElse(DEFAULT_SIZE);
        if (currentPage != null && !Strings.isBlank(currentPage)) {
            PageUtils.setCurrentPage(new Page<>(Long.parseLong(currentPage), Long.parseLong(pageSize)));
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        PageUtils.remove();
    }

}