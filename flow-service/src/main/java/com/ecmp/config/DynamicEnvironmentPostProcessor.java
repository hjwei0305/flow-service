package com.ecmp.config;

import com.ecmp.context.BaseApplicationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.config.ConfigFileApplicationListener;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * <strong>实现功能:</strong>
 * <p>动态加载外部配置文件(Ordered)</p>
 *
 * @author 王锦光 wangj
 * @version 1.0.1 2018-06-19 16:54
 */
@Component
public class DynamicEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {
    private static final Logger log = LoggerFactory.getLogger(DynamicEnvironmentPostProcessor.class);

    /**
     * Post-process the given {@code environment}.
     *
     * @param environment the environment to post-process
     * @param application the application to which the environment belongs
     */
    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {

        String filePath  = System.getProperty("catalina.base")+"/webapps/flow-service.properties";
        File configFile = new File(filePath);
        if(!configFile.exists()){
            filePath = "/usr/local/tomcat/webapps/flow-service.properties";
//            filePath = "D:\\TempWork\\ecmp\\config-center-service\\application.properties";
        }
        try (InputStream inputStream = new FileInputStream(filePath)) {
            Properties source = new Properties();
            source.load(inputStream);

            PropertiesPropertySource propertySource = new PropertiesPropertySource("flow-service", source);
            environment.getPropertySources().addLast(propertySource);

            BaseApplicationContext baseContext = new BaseApplicationContext();
            baseContext.setEnvironment(environment);

            log.info("load properties from：{}", filePath);
            System.out.println("load properties from：" + filePath);
        } catch (IOException e) {
            log.error("load properties error！", e);
            System.out.println("load properties error!" + e.getMessage());
            log.info("load properties from：application.properties");
        }
    }

    /**
     * Get the order value of this object.
     * <p>Higher values are interpreted as lower priority. As a consequence,
     * the object with the lowest value has the highest priority (somewhat
     * analogous to Servlet {@code load-on-startup} values).
     * <p>Same order values will result in arbitrary sort positions for the
     * affected objects.
     *
     * @return the order value
     * @see #HIGHEST_PRECEDENCE
     * @see #LOWEST_PRECEDENCE
     */
    @Override
    public int getOrder() {
        return ConfigFileApplicationListener.DEFAULT_ORDER - 1;
    }
}
