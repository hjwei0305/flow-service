package com.ecmp.context;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.*;
import org.springframework.core.env.Environment;

import java.util.Locale;

/**
 * <strong>实现功能:</strong>
 * <p>ecmp上下文基类</p>
 *
 * @author <a href="mailto:chao2.ma@changhong.com">马超(Vision.Mac)</a>
 * @version 1.0.1 2017/3/30 17:07
 * @see ApplicationContextAware
 */
public class BaseApplicationContext implements ApplicationContextAware, EnvironmentAware {

    /**
     * spring应用上下文对象
     *
     * @see ApplicationContext
     */
    private static ApplicationContext applicationContext;
    private static Environment environment_;

    @Override
    public void setEnvironment(Environment environment) {
        environment_ = environment;
    }

    public static boolean containsProperty(String key) {
        return environment_.containsProperty(key);
    }

    public static String getProperty(String key) {
        return environment_.getProperty(key);
    }

    public static String getProperty(String key, String defaultValue) {
        return environment_.getProperty(key, defaultValue);
    }

    public static <T> T getProperty(String key, Class<T> targetType) {
        return environment_.getProperty(key, targetType);
    }

    public static <T> T getProperty(String key, Class<T> targetType, T defaultValue) {
        return environment_.getProperty(key, targetType, defaultValue);
    }

    public static String getRequiredProperty(String key) throws IllegalStateException {
        return environment_.getRequiredProperty(key);
    }

    public static <T> T getRequiredProperty(String key, Class<T> targetType) throws IllegalStateException {
        return environment_.getRequiredProperty(key, targetType);
    }

    public static String resolvePlaceholders(String key) {
        return environment_.resolvePlaceholders(key);
    }

    public static String resolveRequiredPlaceholders(String key) throws IllegalArgumentException {
        return environment_.resolveRequiredPlaceholders(key);
    }

    @Override
    public void setApplicationContext(ApplicationContext context) {
        applicationContext = context;
    }

    /**
     * 获取spring会话上下文
     *
     * @return 返回spring上下文
     */
    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    public static void registerBean(String beanId, Class<?> clazz) {
        //将applicationContext转换为ConfigurableApplicationContext
        ConfigurableApplicationContext context = (ConfigurableApplicationContext) applicationContext;

        // 获取bean工厂并转换为DefaultListableBeanFactory
        BeanDefinitionRegistry beanDefinitionRegistry = (DefaultListableBeanFactory) context.getBeanFactory();

        // get the BeanDefinitionBuilder
        BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(clazz);
        // get the BeanDefinition
        BeanDefinition beanDefinition = beanDefinitionBuilder.getBeanDefinition();
        // register the bean
        beanDefinitionRegistry.registerBeanDefinition(beanId, beanDefinition);
    }

    public static void removeBean(String beanName) {
        //将applicationContext转换为ConfigurableApplicationContext
        ConfigurableApplicationContext context = (ConfigurableApplicationContext) applicationContext;

        // 获取bean工厂并转换为DefaultListableBeanFactory
        BeanDefinitionRegistry beanDefinitionRegistry = (DefaultListableBeanFactory) context.getBeanFactory();

        beanDefinitionRegistry.removeBeanDefinition(beanName);
    }

    public static boolean containsBean(String beanId) {
        return applicationContext.containsBean(beanId);
    }

    @SuppressWarnings("unchecked")
    public static <T> T getBean(String beanId) {
        return (T) applicationContext.getBean(beanId);
    }

    public static <T> T getBean(Class<T> clazz) {
        return applicationContext.getBean(clazz);
    }

    /**
     * Return the unique id of this application context.
     *
     * @return the unique id of the context, or {@code null} if none
     * @see ApplicationContext#getId()
     */
    public static String getId() {
        return applicationContext.getId();
    }

    /**
     * Return a name for the deployed application that this context belongs to.
     *
     * @return a name for the deployed application, or the empty String by default
     * @see ApplicationContext#getApplicationName()
     */
    public static String getApplicationName() {
        return applicationContext.getApplicationName();
    }

    /**
     * Return a friendly name for this context.
     *
     * @return a display name for this context (never {@code null})
     * @see ApplicationContext#getDisplayName()
     */
    public static String getDisplayName() {
        return applicationContext.getDisplayName();
    }

    /**
     * Return the timestamp when this context was first loaded.
     *
     * @return the timestamp (ms) when this context was first loaded
     * @see ApplicationContext#getStartupDate()
     */
    public static long getStartupDate() {
        return applicationContext.getStartupDate();
    }

    /**
     * @param key    多语言key
     * @param args   填充参数 如：key=参数A{0},参数B{1}  此时的args={"A", "B"}
     * @param locale 语言环境
     * @return 返回语意
     */
    public static String getMessage(String key, Object[] args, Locale locale) {
        if (null != key && key.trim().length() > 0) {
            return applicationContext.getMessage(key, args, locale);
        } else {
            return "";
        }
    }

    public static String getMessage(String key, Object[] args, String defaultMessage, Locale locale) {
        if (null != key && key.trim().length() > 0) {
            return applicationContext.getMessage(key, args, defaultMessage, locale);
        } else {
            return "";
        }
    }

    /**
     * 发布事件
     *
     * @param event 事件
     */
    public static void publishEvent(ApplicationEvent event) {
        applicationContext.publishEvent(event);
    }

}
