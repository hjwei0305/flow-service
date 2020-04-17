package com.ecmp.context.common;

/**
 * <strong>实现功能:</strong>
 * <p>平台配置常量接口</p>
 *
 * @author <a href="mailto:chao2.ma@changhong.com">马超(Vision.Mac)</a>
 * @version 1.0.1 2017/5/15 13:10
 */
public abstract class ConfigConstants {

    /**
     * 应用名称
     */
    public static final String SYS_NAME = "SYS_NAME";
    /**
     * 应用服务AppId
     */
    public static final String ENV_ECMP_APP_ID = "ECMP_APP_ID";
    /**
     * 云平台配置中心zookeeper服务地址
     */
    public static final String ENV_ECMP_CONFIG_CENTER = "ECMP_CONFIG_CENTER";
    /**
     * 配置中心zookeeper的节点命名空间
     */
    public static final String ZK_NAME_SPACE = "com.center.config";

    // /////////////////////// applicationConfig.properties配置 ////////////////////////////////


    // /////////////////////// 全局配置参数 ////////////////////////////////

    /**
     * <i style="color:red;">可选</i> 全局配置参数key，BASIC_API服务基路径
     */
    public static final String BASIC_API = "BASIC_API";
    /**
     * <i style="color:red;">可选</i> 全局配置参数key，CONFIG_CENTER_API服务基路径
     */
    public static final String CONFIG_CENTER_API = "CONFIG_CENTER_API";
    /**
     * <i style="color:red;">可选</i> 全局配置参数key，认证中心相关配置
     */
    public static final String PARAM_AUTH_CENTER = "AUTH_CENTER";

    /**
     * <i style="color:red;">可选</i> 全局配置参数key，API_PACKAGE相关配置
     */
    public static final String PARAM_API_PACKAGE = "API_PACKAGE";
    /**
     * <i style="color:red;">可选</i> 全局配置参数key，data source相关配置
     */
    public static final String PARAM_DATASOURCE = "DATASOURCE";
    public static final String PARAM_SECOND_LEVEL_CACHE_HOST = "second_level_cache_host";
    public static final String PARAM_SECOND_LEVEL_CACHE_PORT = "second_level_cache_port";
    public static final String PARAM_SECOND_LEVEL_CACHE_PW = "second_level_cache_password";
    public static final String PARAM_SECOND_LEVEL_CACHE_DB = "second_level_cache_db";
    /**
     * <i style="color:red;">可选</i> 全局配置参数key，业务缓存相关配置
     */
    public static final String PARAM_BIZ_CACHE = "BIZ_CACHE";
    /**
     * <i style="color:red;">可选</i> 全局配置参数key，会话缓存相关配置
     */
    public static final String PARAM_SESSION_CACHE = "ECMP_SESSION_CACHE";
    /**
     * <i style="color:red;">可选</i> 全局配置参数key，EDM_MONGODB相关配置
     */
    public static final String PARAM_EDM_MONGODB = "EDM_MONGODB";
    public static final String SWITCH_EDM_KEY = "com.ecmp.edm.enable";
    /**
     * <i style="color:red;">可选</i> 全局配置参数key，ELASTIC_SEARCH相关配置
     */
    public static final String PARAM_ELASTIC_SEARCH = "ELASTIC_SEARCH";
    /**
     * <i style="color:red;">可选</i> 全局配置参数key，web相关配置
     */
    public static final String PARAM_WEB_CONFIG = "WEB_CONFIG";
    /**
     * <i style="color:red;">可选</i> 全局配置参数key，kafka参数配置键
     */
    public static final String MQ_CONFIG_KEY = "MQ_PRODUCER";
    /**
     * <i style="color:red;">可选</i> 全局配置参数key，是否启用验证码
     * boolean类型，默认:true
     */
    public static final String PARAM_CAPTCHA_ENABLED = "captcha.enabled";
    /**
     * <i style="color:red;">可选</i> 全局配置参数key，验证码参数key
     * String，默认:captchaEnabledKey
     */
    public static final String PARAM_CAPTCHA_ENABLED_KEY = "captcha.enabled.key";
    /**
     * 登录账号密码加密传输
     */
    public static final String PARAM_LOGIN_ENCRYPT = "login.encrypt";

    /**
     * 静态资源服务器的全局配置参数key
     * <i style="color:red;">必须</i> 平台统一配置
     */
    public static final String STATIC_RESOURCE_CDN = "static.resource.url";

    public static final String ZIPKIN_SERVER_KEY = "ZIPKIN_SERVER";
    /**
     * 全局会话超时时间（秒）
     */
    public static final String SEI_GLOBAL_SESSION_TIMEOUT = "server.servlet.session.timeout";
}
