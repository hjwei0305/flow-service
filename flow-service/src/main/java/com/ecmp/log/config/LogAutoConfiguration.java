package com.ecmp.log.config;

import com.ecmp.log.support.LogContext;
import com.ecmp.log.support.LogProcessor;
import org.slf4j.Logger;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * 实现功能：
 *
 * @author 马超(Vision.Mac)
 * @version 1.0.00  2020-03-10 13:34
 */
@Configuration
@ConditionalOnClass({Logger.class})
@Import({LogContext.class})
public class LogAutoConfiguration {

    @Bean
    public LogProcessor logProcessor() {
        return new LogProcessor();
    }
}
