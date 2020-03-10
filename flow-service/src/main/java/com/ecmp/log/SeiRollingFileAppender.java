package com.ecmp.log;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.filter.LevelFilter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.spi.FilterReply;
import com.ecmp.log.util.LogUtil;

/**
 * 实现功能：
 *
 * @author 马超(Vision.Mac)
 * @version 1.0.00  2020-01-08 11:58
 */
public class SeiRollingFileAppender extends RollingFileAppender<ILoggingEvent> {

    public SeiRollingFileAppender() {
    }

    @Override
    public void start() {
        /*
        规则：
            1.记录marker为 @see LogUtil.BIZ_LOG 的日志
            2.记录level为 @see Level.ERROR 的日志
        */
        LogMarkerFilter markerFilter = new LogMarkerFilter();
        markerFilter.setMarker(LogUtil.BIZ_LOG);
        markerFilter.setOnMatch(FilterReply.ACCEPT);
        markerFilter.setOnMismatch(FilterReply.NEUTRAL);
        markerFilter.start();
        super.addFilter(markerFilter);

        LevelFilter levelFilter = new LevelFilter();
        //非ERROR级别的日志，被过滤掉
        levelFilter.setLevel(Level.ERROR);
        levelFilter.setOnMatch(FilterReply.ACCEPT);
        levelFilter.setOnMismatch(FilterReply.DENY);
        levelFilter.start();
        super.addFilter(levelFilter);

        super.start();
    }

}
