package com.ecmp.log.extend;

import ch.qos.logback.classic.spi.ILoggingEvent;
import com.ecmp.log.support.LogContext;
import com.github.danielwegener.logback.kafka.KafkaAppender;

/**
 * 实现功能：
 *
 * @author 马超(Vision.Mac)
 * @version 1.0.00  2020-09-23 16:15
 */
public class SeiKafkaAppender extends KafkaAppender<ILoggingEvent> {

    @Override
    public void start() {
        // 不论日志等级是什么都不影响bizLog的日志输出
        LogContext.addBizMarkerFilter();

        super.start();
    }
}
