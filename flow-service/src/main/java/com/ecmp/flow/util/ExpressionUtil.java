package com.ecmp.flow.util;

import com.ecmp.config.util.ApiRestJsonProvider;
import com.ecmp.flow.constant.FlowStatus;
import org.apache.cxf.jaxrs.client.WebClient;

import javax.ws.rs.core.MediaType;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * *************************************************************************************************
 * <p/>
 * 实现功能：条件表达式工具类
 * <p>
 * ------------------------------------------------------------------------------------------------
 * 版本          变更时间             变更人                     变更原因
 * ------------------------------------------------------------------------------------------------
 * 1.0.00      2017/4/27 13:40      谭军(tanjun)                    新建
 * <p/>
 * *************************************************************************************************
 */
public class ExpressionUtil {
    /**
     * 获取条件表达式的属性值对
     * @param clientApiBaseUrl
     * @param businessModelId 业务模型ID
     * @param  businessId 业务ID
     * @return
     */
    public  static Map<String,Object>  getConditonPojoValueMap(String clientApiBaseUrl,String businessModelId,String businessId){
        //获取API服务的应用模块
//        ContextAppModule appModule = getAppModule(apiClass);
        //获取服务基地址
//        String baseAddress = appModule.getApiBaseAddress();
        //平台API服务使用的JSON序列化提供类
        List<Object> providers = new ArrayList<>();
        providers.add(new ApiRestJsonProvider());
        //API会话检查的客户端过滤器
//        providers.add(new SessionClientRequestFilter());
//        return JAXRSClientFactory.create(baseAddress, apiClass,providers);

        LinkedHashMap<String,Object> pvs = WebClient.create(clientApiBaseUrl, providers)
                .path("condition/conditonPojoMapByBusinessModelId/{businessModelId}/{id}",businessModelId,businessId)
                .accept(MediaType.APPLICATION_JSON)
                .get(LinkedHashMap.class);
        return pvs;
    }

    /**
     * 检证表达式语法是否合法
     * @param clientApiBaseUrl 客户端baseUrl
     * @param clientClassName 客户端条件类全路径
     * @param expression 表达式
     * @return
     */
    public static Boolean validate(String clientApiBaseUrl,String clientClassName,String expression) throws ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
        Boolean result = true;
        //平台API服务使用的JSON序列化提供类
        List<Object> providers = new ArrayList<>();
        providers.add(new ApiRestJsonProvider());
        //API会话检查的客户端过滤器
//        providers.add(new SessionClientRequestFilter());
//
        Map<String,Object> pvs = WebClient.create(clientApiBaseUrl, providers)
                .path("condition/propertiesAndValues/{conditonPojoClassName}",clientClassName)
//                .query("conditonPojoClassName",clientClassName)
                .accept(MediaType.APPLICATION_JSON)
                .get(Map.class);

//        com.ecmp.flow.clientapi.ICommonConditionService proxy2 = ApiClient.createProxy(com.ecmp.flow.clientapi.ICommonConditionService.class);
//        Map<String,Object> pvs = proxy2.getPropertiesAndValues(clientClassName);

       result = ConditionUtil.groovyTest(expression,pvs);
        return result;
    }

    /**
     * 直接获取表达式验证结果
     * @param clientApiBaseUrl
     * @param businessModelId  业务模型ID
     * @param expression
     * @param businessId  业务ID
     * @return
     */
    public static boolean result(String clientApiBaseUrl,String businessModelId,String businessId,String expression){
        boolean result = true;
        //平台API服务使用的JSON序列化提供类
        List<Object> providers = new ArrayList<>();
        providers.add(new ApiRestJsonProvider());
        //API会话检查的客户端过滤器
//        providers.add(new SessionClientRequestFilter());

        LinkedHashMap<String,Object> pvs = WebClient.create(clientApiBaseUrl, providers)
                .path("condition/conditonPojoMapByBusinessModelId/{businessModelId}/{id}",businessModelId,businessId)
                .accept(MediaType.APPLICATION_JSON)
                .get(LinkedHashMap.class);
        result = ConditionUtil.groovyTest(expression,pvs);
        return result;
    }


    /**
     * 重置单据状态
     * @param clientApiBaseUrl
     * @param businessModelId  业务模型ID
     * @param status
     * @param businessId  业务ID
     * @return
     */
    public static boolean resetState(String clientApiBaseUrl, String businessModelId, String businessId, FlowStatus status){
        boolean result = true;
        //平台API服务使用的JSON序列化提供类
        List<Object> providers = new ArrayList<>();
        providers.add(new ApiRestJsonProvider());
        //API会话检查的客户端过滤器
//        providers.add(new SessionClientRequestFilter());

        result = WebClient.create(clientApiBaseUrl, providers)
                .path("condition/resetState/{businessModelId}/{id}/{status}",businessModelId,businessId,status)
                .accept(MediaType.APPLICATION_JSON)
                .get(Boolean.class);
        return result;
    }

}
