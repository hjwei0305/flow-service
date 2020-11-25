package com.ecmp.log.support;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.turbo.MarkerFilter;
import ch.qos.logback.core.spi.FilterReply;
import com.ecmp.log.util.LogUtil;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * 上下文工具
 */
public class LogContext implements ApplicationContextAware {
    /**
     * 上下文
     */
    private static ApplicationContext applicationContext;

    /**
     * 设置上下文
     *
     * @param applicationContext 上下文实例
     * @throws BeansException
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        LogContext.applicationContext = applicationContext;
    }

    /**
     * 获取上下文
     *
     * @return 返回上下文工具
     */
    public static ApplicationContext getContext() {
        return LogContext.applicationContext;
    }

    /**
     * 获取日志上下文
     */
    public static LoggerContext getLoggerContext() {
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        return context;
    }

    /**
     * 规则：
     * 1.记录marker为 @see LogUtil.BIZ_LOG 的日志
     * 2.记录level为 @see Level.ERROR 的日志
     * <p>
     * 如果值是neutral，就会有下一个filter进行判断,如果已经没有后续filter，那么会对这个日志事件进行处理;
     * 如果判断是accept，那么就会立即对该日志事件进行处理，不再进行后续判断
     */
    public static void addBizMarkerFilter() {
        MarkerFilter markerFilter = new MarkerFilter();
        markerFilter.setMarker(LogUtil.BIZ_LOG);
        markerFilter.setOnMatch(FilterReply.ACCEPT.name());
        markerFilter.setOnMismatch(FilterReply.NEUTRAL.name());
        markerFilter.start();
        getLoggerContext().addTurboFilter(markerFilter);
    }
}
