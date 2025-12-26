package com.hx.mqtt.config;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;

@Aspect
@Component
public class GlobalRequestAspect {
    private static final Logger logger = LoggerFactory.getLogger(GlobalRequestAspect.class);

    /**
     * 拦截所有Controller层的请求
     */
    @Around("execution(* com.hx.mqtt.controller..*.*(..))")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        // 获取请求信息
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();

        // 记录请求日志
        logger.info("Request Start ===> URL: {}, HTTP_METHOD: {}, IP: {}, CLASS_METHOD: {}, ARGS: {}",
                request.getRequestURL().toString(),
                request.getMethod(),
                request.getRemoteAddr(),
                joinPoint.getSignature().getDeclaringTypeName() + "." + joinPoint.getSignature().getName(),
                Arrays.toString(joinPoint.getArgs()));

        long startTime = System.currentTimeMillis();
        Object result;

        try {
            // 1. 参数校验 (可以在这里添加全局参数校验逻辑)

            // 2. 执行目标方法
            result = joinPoint.proceed();

            // 3. 记录响应日志
            logger.info("Request End <=== RESPONSE: {}, COST_TIME: {}ms",
                    result,
                    System.currentTimeMillis() - startTime);

        } catch (Exception e) {
            // 异常处理
            logger.error("Request Error ===> ", e);

            // 这里可以统一封装异常响应
            throw e; // 或者返回统一的错误响应
            // return Result.fail(e.getMessage());
        }

        return result;
    }
}