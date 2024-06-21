package com.boss.client.handler;


import com.boss.client.exception.BizException;
import com.boss.client.vo.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Objects;

import static com.boss.common.enums.StatusCodeEnum.*;


/**
 * 全局异常处理
 **/
@Slf4j
@RestControllerAdvice
public class ControllerAdviceHandler {

    /**
     * 处理服务异常
     *
     * @param e 异常
     * @return 接口异常信息
     */
    @ExceptionHandler(value = BizException.class)
    public Result<?> errorHandler(BizException e) {
        return Result.fail(e.getCode(), e.getMessage());
    }

    /**
     * 处理参数校验异常
     *
     * @param e 异常
     * @return 接口异常信息
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<?> errorHandler(MethodArgumentNotValidException e) {
        return Result.fail(VALID_ERROR.getCode(), Objects.requireNonNull(e.getBindingResult().getFieldError()).getDefaultMessage());
    }

    /**
     * 处理系统异常
     *
     * @param e 异常
     * @return 接口异常信息
     */
    @ExceptionHandler(value = Exception.class)
    public Result<?> errorHandler(Exception e) {
        e.printStackTrace();
        return Result.fail(SYSTEM_ERROR.getCode(), SYSTEM_ERROR.getDesc());
    }

}
