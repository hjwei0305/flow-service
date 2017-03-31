package com.ecmp.config.util;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import org.apache.cxf.jaxrs.client.JAXRSClientFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * *************************************************************************************************
 * <p/>
 * 实现功能：平台调用API服务的客户端工具
 * <p>
 * ------------------------------------------------------------------------------------------------
 * 版本          变更时间             变更人                     变更原因
 * ------------------------------------------------------------------------------------------------
 * 1.0.00      2017-03-27 10:07      王锦光(wangj)                新建
 * <p/>
 * *************************************************************************************************
 */
public class ApiClient {
    private final static String ECMP_SYSTEM_CONFIG = "ECMP_SYSTEM_CONFIG";
    private final static String ECMP_JDBC_DRIVER = "ECMP_JDBC_DRIVER";
    //运行服务器的环境变量配置
    private static Map<String,String> ecmpEnvironment;
    //API服务配置清单
    private static Map<String, ApiServiceConfigEntity> apiServiceConfigs;

    /**
     * 静态初始化代码
     */
    static {
        getSystemConfig();
        loadApiServiceConfigs();
    }

    /**
     * 获取服务器的环境变量
     * @return 环境变量
     */
    private static void getSystemConfig() {
        if (ecmpEnvironment!=null){
            return;
        }
        ecmpEnvironment = new HashMap<>();
        String config = System.getenv(ECMP_SYSTEM_CONFIG);
        if (config == null) {
            throw new ExceptionInInitializerError("系统没有配置环境变量：" + ECMP_SYSTEM_CONFIG + "！");
        }
        ecmpEnvironment.put(ECMP_SYSTEM_CONFIG,config);
        String jdbcDriver = System.getenv(ECMP_JDBC_DRIVER);
        if (jdbcDriver == null) {
            throw new ExceptionInInitializerError("系统没有配置环境变量：" + ECMP_JDBC_DRIVER + "！");
        }
        ecmpEnvironment.put(ECMP_JDBC_DRIVER,jdbcDriver);
    }

    /**
     * 加载API服务配置
     */
    private static void loadApiServiceConfigs(){
        if (apiServiceConfigs != null) {
            return;
        }
        apiServiceConfigs = new HashMap<>();
        String url = ecmpEnvironment.get(ECMP_SYSTEM_CONFIG);
        String jdbcDriver = ecmpEnvironment.get(ECMP_JDBC_DRIVER);
        Connection conn = null;
        try {
            Class.forName(jdbcDriver);
            conn = DriverManager.getConnection(url);
            Statement stmt = conn.createStatement();
            //加载运行环境
            String configSql = "select * from ApiServiceConfig";
            ResultSet configSet = stmt.executeQuery(configSql);
            while (configSet.next()) {
                String interfaceName = configSet.getString("interfaceName");
                String baseAddress = configSet.getString("baseAddress");
                apiServiceConfigs.put(interfaceName,
                        new ApiServiceConfigEntity(interfaceName,GlobalParam.environmentFormat(baseAddress)));
            }

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 创建API服务代理
     * @param apiClass API服务接口
     * @param <T> API服务接口泛型
     * @return 服务代理
     */
    public static <T> T createProxy(Class<T> apiClass){
        String apiName = apiClass.getName();
        //获取服务基地址
        ApiServiceConfigEntity configEntity = apiServiceConfigs.get(apiName);
        if (configEntity==null){
            throw new ExceptionInInitializerError(String.format("系统没有配置API服务接口：%s！",apiName));
        }
        String  baseAddress = configEntity.getBaseAddress();
        List<Object> providerList = new ArrayList<Object>();
        providerList.add(new JacksonJsonProvider());
        return JAXRSClientFactory.create(baseAddress,apiClass,providerList);
    }
}
