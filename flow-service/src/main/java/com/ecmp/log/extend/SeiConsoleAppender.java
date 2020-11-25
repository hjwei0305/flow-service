package com.ecmp.log.extend;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.ConsoleAppender;
import com.ecmp.log.support.LogContext;

/**
 * 实现功能：
 *
 * @author 马超(Vision.Mac)
 * @version 1.0.00  2020-09-23 16:08
 */
public class SeiConsoleAppender extends ConsoleAppender<ILoggingEvent> {

    @Override
    public void start() {
        // 不论日志等级是什么都不影响bizLog的日志输出
        LogContext.addBizMarkerFilter();

        super.start();
    }
}
