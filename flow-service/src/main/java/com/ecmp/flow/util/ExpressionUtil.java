package com.ecmp.flow.util;

import com.ecmp.config.util.ApiClient;
import com.ecmp.config.util.ApiRestJsonProvider;
import com.ecmp.flow.common.util.Constants;
import com.ecmp.flow.constant.FlowStatus;
import com.ecmp.flow.entity.AppModule;
import com.ecmp.flow.entity.BusinessModel;
import org.apache.commons.collections.map.HashedMap;
import org.apache.cxf.jaxrs.client.WebClient;

import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

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

     public static AppModule getAppModule(BusinessModel businessModel){
         AppModule appModule = businessModel.getAppModule();
        return appModule;
    }
    /**
     * 获取条件表达式的属性描述
     * @param businessModel 业务模型
     * @param  businessId 业务ID
     * @return
     */
    public  static Map<String,Object>  getPropertiesDecMap(BusinessModel businessModel, String businessId){
        String businessModelCode = businessModel.getClassName();
        String clientApiBaseUrl = getAppModule(businessModel).getApiBaseAddress();
        String clientApiUrl = clientApiBaseUrl + businessModel.getConditonProperties();
        Map<String,Object> params = new HashMap();
        params.put(Constants.BUSINESS_MODEL_CODE,businessModelCode);
        params.put(Constants.ID,businessId);
        Map<String,Object> pvs = ApiClient.getEntityViaProxy(clientApiUrl,new GenericType<Map<String,Object> >() {},params);
        return pvs;
    }

    /**
     * 获取条件表达式的属性值对
     * @param businessModel 业务模型
     * @param  businessId 业务ID
     * @return
     */
    public  static Map<String,Object>  getPropertiesValuesMap(BusinessModel businessModel, String businessId,Boolean all){
        String businessModelCode = businessModel.getClassName();
        String clientApiBaseUrl = getAppModule(businessModel).getApiBaseAddress();
        String clientApiUrl = clientApiBaseUrl + businessModel.getConditonPValue();
        Map<String,Object> params = new HashMap();
        params.put(Constants.BUSINESS_MODEL_CODE,businessModelCode);
        params.put(Constants.ID,businessId);
        params.put(Constants.ALL,all);
        Map<String,Object> pvs = ApiClient.getEntityViaProxy(clientApiUrl,new GenericType<Map<String,Object> >() {},params);
        return pvs;
    }

    /**
     * 检证表达式语法是否合法
     * @param  businessModel 业务实体
     * @param expression 表达式
     * @return
     */
    public static Boolean validate(BusinessModel businessModel,String expression) throws ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
        Boolean result = true;
        String clientApiBaseUrl = getAppModule(businessModel).getApiBaseAddress();
        String clientApiUrl = clientApiBaseUrl + businessModel.getConditonPSValue();
        Map<String,Object> params = new HashMap();
        params.put(Constants.BUSINESS_MODEL_CODE,businessModel.getClassName());
        Map<String,Object> pvs = ApiClient.getEntityViaProxy(clientApiUrl,new GenericType<Map<String,Object> >() {},params);
        result = ConditionUtil.groovyTest(expression,pvs);
        return result;
    }

    /**
     * 直接获取表达式验证结果
     * @param  businessModel 业务实体
     * @param expression
     * @param businessId  业务ID
     * @return
     */
    public static boolean result(BusinessModel businessModel,String businessId,String expression){
        boolean result = true;
        String businessModelCode = businessModel.getClassName();
        String clientApiBaseUrl = getAppModule(businessModel).getApiBaseAddress();
        String clientApiUrl = clientApiBaseUrl + businessModel.getConditonPValue();
        Map<String,Object> params = new HashMap();
        params.put(Constants.BUSINESS_MODEL_CODE,businessModelCode);
        params.put(Constants.ID,businessId);
        Map<String,Object> pvs = ApiClient.getEntityViaProxy(clientApiUrl,new GenericType<Map<String,Object> >() {},params);
        result = ConditionUtil.groovyTest(expression,pvs);
        return result;
    }

    /**
     * 重置单据状态
     * @param  businessModel 业务实体
     * @param status
     * @param businessId  业务ID
     * @return
     */
    public static boolean resetState(BusinessModel businessModel, String businessId, FlowStatus status){
        boolean result = true;
        String businessModelCode = businessModel.getClassName();
        String clientApiBaseUrl = getAppModule(businessModel).getApiBaseAddress();
        String clientApiUrl = clientApiBaseUrl + businessModel.getConditonStatusRest();
        Map<String,Object> params = new HashMap();
        params.put(Constants.BUSINESS_MODEL_CODE,businessModelCode);
        params.put(Constants.ID,businessId);
        params.put(Constants.STATUS,status);
        result = ApiClient.postViaProxyReturnResult(clientApiUrl,new GenericType<Boolean>() {},params);
        return result;
    }

}
