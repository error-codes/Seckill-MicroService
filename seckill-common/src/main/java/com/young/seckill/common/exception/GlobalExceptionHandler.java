package com.young.seckill.common.exception;

import com.young.seckill.common.response.RespCode;
import com.young.seckill.common.response.RespResult;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 拦截自定义异常
     */
    @ExceptionHandler(SeckillException.class)
    public RespResult<String> handleSeckillException(SeckillException e) {
        LOGGER.error("Seckill Exception：{}", e);
        return RespResult.error(e.getCode(), e.getMessage());
    }

    /**
     * 拦截断言异常
     */
    @ExceptionHandler(AssertException.class)
    public RespResult<String> handleSeckillAssertException(AssertException e) {
        LOGGER.error("Assert Exception：{}", e);
        return RespResult.error(e.getCode(), e.getMessage());
    }

    /**
     * 拦截其他异常
     */
    @ExceptionHandler(Exception.class)
    public RespResult<String> handleException(Exception e) {
        LOGGER.error("{} Exception：{}", e.getClass().getName(), e);
        return RespResult.error(RespCode.SERVER_EXCEPTION, e.getMessage());
    }

    /**
     * 拦截请求方式异常
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public RespResult<String> handlerHttpRequestMethodNotSupportedException(HttpServletRequest request,
                                                                            HttpRequestMethodNotSupportedException e) {
        LOGGER.error("Request Method Exception: URL: {}, 期望类型: {}, 实际类型: {}", request.getRequestURI(),
                     e.getSupportedHttpMethods(), request.getMethod());
        return RespResult.error(RespCode.REQUEST_METHOD_ERROR, e.getMessage());
    }

}