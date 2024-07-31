package com.young.seckill.common.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.young.seckill.common.annotation.RequestDescDoc;
import com.young.seckill.common.utils.JACKSON;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.CodeSignature;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;


@Aspect
@Component
public class RequestAspect {

    private static final Logger LOGGER = LoggerFactory.getLogger(RequestAspect.class);

    @Pointcut("execution(* com.young.seckill..*.interfaces.controller.*.*(..))*")
    public void requestLog() {

    }

    @Around("requestLog()")
    public Object doAround(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        assert attributes != null;
        HttpServletRequest request = attributes.getRequest();

        Object result = joinPoint.proceed();

        long endTime = System.currentTimeMillis();
        Signature signature = joinPoint.getSignature();
        MethodSignature methodSignature = (MethodSignature) signature;
        Method method = methodSignature.getMethod();
        String uri = request.getRequestURI();

        LOGGER.info("-----------------------------------------------------------------------");
        LOGGER.info("请求路径: {} {}", request.getMethod(), uri);
        LOGGER.info("用户:IP: {}:{}", request.getRemoteUser(), request.getRemoteAddr());
        if (method.isAnnotationPresent(RequestDescDoc.class)) {
            RequestDescDoc requestDescDoc = method.getAnnotation(RequestDescDoc.class);
            LOGGER.info("请求描述: {}", requestDescDoc.value());
        }
        LOGGER.info("请求授权: {}", request.getHeader("Authorization"));
        LOGGER.info("请求参数: {}", getParameters(joinPoint));
        LOGGER.info("响应结果: {}", result);
        LOGGER.info("请求时间: {} ms", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                                                        .format(LocalDateTime.ofInstant(Instant.ofEpochMilli(startTime),
                                                                                        ZoneId.systemDefault())));
        LOGGER.info("执行耗时: {} ms", endTime - startTime);
        LOGGER.info("-----------------------------------------------------------------------");

        return result;
    }

    private String getParameters(JoinPoint joinPoint) throws JsonProcessingException {
        Map<String, Object> parameters = new HashMap<>();
        Object[] parameterValue = joinPoint.getArgs();
        String[] parameterNames = ((CodeSignature) joinPoint.getSignature()).getParameterNames();
        for (int i = 0; i < parameterNames.length; i++) {
            parameters.put(parameterNames[i], parameterValue[i]);
        }
        return JACKSON.toJson(parameters);
    }
}
