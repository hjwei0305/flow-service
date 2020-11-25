package com.ecmp.log.support;

import com.ecmp.util.JsonUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;

import java.lang.reflect.Method;
import java.util.Objects;

/**
 * 日志处理器
 */
@Aspect
@Order(1)
public class LogProcessor {
    private static final Logger log = LoggerFactory.getLogger(LogProcessor.class);
    /**
     * 当前链路信息获取
     */
    private static final String TRACE_ID = "traceId";
    private static final String TRACE_PATH = "tracePath";
    private static final String TRACE_FROM_SERVER = "from_server";
    private static final String TRACE_CURRENT_SERVER = "current_server";

    private static final String MDC_CLASS_NAME = "className";
    private static final String MDC_METHOD_NAME = "methodName";
    private static final String MDC_ARGS = "args";

    /**
     * 打印异常日志
     *
     * @param joinPoint 切入点
     * @param throwable 异常
     */
    @AfterThrowing(value = "@within(org.springframework.stereotype.Service)||@within(org.springframework.stereotype.Component)" +
            "||@within(org.springframework.web.bind.annotation.RestController)||@within(org.springframework.stereotype.Controller)" +
            "||execution(public * com.ecmp.core.service..*.*(..)))", throwing = "throwable")
    public void throwingPrint(JoinPoint joinPoint, Throwable throwable) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String className = signature.getDeclaringTypeName();
        Method method = signature.getMethod();
        String methodName = method.getName();
        Object[] args = joinPoint.getArgs();
        MDC.put(MDC_CLASS_NAME, className);
        MDC.put(MDC_METHOD_NAME, methodName);
        if (Objects.nonNull(args)) {
            try {
                if (args.length == 1) {
                    MDC.put(MDC_ARGS, JsonUtils.toJson(args[0]));
                } else {
                    MDC.put(MDC_ARGS, JsonUtils.toJson(args));
                }
            } catch (Exception ignored) {
            }
        }
        try {
            String message = ExceptionUtils.getRootCauseMessage(throwable);

            LoggerFactory.getLogger(className).error(message, throwable);
        } catch (Exception e) {
            log.error("{}.{}方法错误: {}", className, methodName, e.getMessage());
        } finally {
            MDC.remove(MDC_CLASS_NAME);
            MDC.remove(MDC_METHOD_NAME);
            MDC.remove(MDC_ARGS);
        }
    }
}
