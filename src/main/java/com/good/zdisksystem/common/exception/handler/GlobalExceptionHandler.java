package com.good.zdisksystem.common.exception.handler;

import com.good.zdisksystem.common.exception.CustomException;
import com.good.zdisksystem.common.exception.enums.GlobalErrorCodeConstants;
import com.good.zdisksystem.common.result.CommonResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;


/**
 * 全局异常处理器 - 将 Exception 翻译成 CommonResult + 对应的异常编号
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    //TODO 预留 API 错误日志

    /**
     * 处理所有异常，主要是提供给 Filter 使用
     */
    // TODO 处理所有异常，主要是提供给 Filter 使用

    /**
     * 处理 自定义异常
     */
    @ExceptionHandler(value = CustomException.class)
    public CommonResult<?> customExceptionHandler(CustomException ex) {
        log.error("[customExceptionHandler]", ex);
        return CommonResult.error(ex.getCode(), ex.getMsg());
    }

    /**
     * 处理系统异常 - 兜底处理所有的一切
     */
    @ExceptionHandler(value = Exception.class)
    public CommonResult<?> defaultExceptionHandler(HttpServletRequest request, Exception ex) {
        // 处理异常
        log.error("[defaultExceptionHandler]", ex);
        return CommonResult.error(GlobalErrorCodeConstants.FAILURE);
    }

}
