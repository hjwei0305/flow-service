package com.ecmp.flow.util;

import com.ecmp.config.util.ApiClient;
import com.ecmp.context.ContextUtil;
import com.ecmp.flow.common.util.Constants;
import com.ecmp.flow.constant.FlowStatus;
import com.ecmp.flow.entity.AppModule;
import com.ecmp.flow.entity.BusinessModel;
import com.ecmp.log.util.LogUtil;
import com.ecmp.util.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.GenericType;
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

    private static final Logger logger = LoggerFactory.getLogger(ExpressionUtil.class);

    public static AppModule getAppModule(BusinessModel businessModel){
        AppModule appModule = businessModel.getAppModule();
        return appModule;
    }
    /**
     * 获取条件属性说明
     * @param businessModel 业务模型
     * @return
     */
    public  static Map<String,String>  getPropertiesDecMap(BusinessModel businessModel){
        String businessModelCode = businessModel.getClassName();
        String apiBaseAddressConfig = getAppModule(businessModel).getApiBaseAddress();
        String clientApiBaseUrl =  ContextUtil.getGlobalProperty(apiBaseAddressConfig);
        String clientApiUrl = clientApiBaseUrl + businessModel.getConditonProperties();
        Map<String,Object> params = new HashMap();
        params.put(Constants.BUSINESS_MODEL_CODE,businessModelCode);
        params.put(Constants.ALL,false);
        String messageLog = "开始调用‘条件属性说明服务地址’，接口url="+clientApiUrl+",参数值"+ JsonUtils.toJson(params);
        Map<String,String> result = null;
        try {
            result = ApiClient.getEntityViaProxy(clientApiUrl, new GenericType<Map<String, String>>() {}, params);
        }catch (Exception e){
            messageLog+="-调用异常："+e.getMessage();
        }finally {
            LogUtil.error(messageLog);
        }
        return result;
    }

    /**
     * 获取条件表达式的属性值对
     * @param businessModel 业务模型
     * @param  businessId 业务ID
     * @return
     */
    public  static Map<String,Object>  getPropertiesValuesMap(BusinessModel businessModel, String businessId,Boolean all){
        String businessModelCode = businessModel.getClassName();
        String apiBaseAddressConfig = getAppModule(businessModel).getApiBaseAddress();
        String clientApiBaseUrl =  ContextUtil.getGlobalProperty(apiBaseAddressConfig);
        String clientApiUrl = clientApiBaseUrl + businessModel.getConditonPValue();
        Map<String,Object> params = new HashMap();
        params.put(Constants.BUSINESS_MODEL_CODE,businessModelCode);
        params.put(Constants.ID,businessId);
        params.put(Constants.ALL,all);
        String messageLog = "开始调用‘获取条件表达式的属性值对’接口，接口url="+clientApiUrl+",参数值"+ JsonUtils.toJson(params);
        Map<String,Object> pvs = null;
        try {
            pvs =  ApiClient.getEntityViaProxy(clientApiUrl,new GenericType<Map<String,Object> >() {},params);
            messageLog+=",【result=" + pvs==null?null:JsonUtils.toJson(pvs)+"】";
        }catch (Exception e){
            messageLog+="调用异常："+e.getMessage();
            throw e;
        }finally {
            logger.info(messageLog);
            asyncUploadLog(messageLog);
        }
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
        String apiBaseAddressConfig = getAppModule(businessModel).getApiBaseAddress();
        String clientApiBaseUrl =  ContextUtil.getGlobalProperty(apiBaseAddressConfig);
        String clientApiUrl = clientApiBaseUrl + businessModel.getConditonPSValue();
        Map<String,Object> params = new HashMap();
        params.put(Constants.BUSINESS_MODEL_CODE,businessModel.getClassName());
        String messageLog = "开始调用‘检证表达式语法是否合法’接口，接口url="+clientApiUrl+",参数值"+ JsonUtils.toJson(params);
        Map<String,Object> pvs = null;
        try {
            pvs =  ApiClient.getEntityViaProxy(clientApiUrl,new GenericType<Map<String,Object> >() {},params);
            messageLog+=",【result=" + pvs==null?null:JsonUtils.toJson(pvs)+"】";
        }catch (Exception e){
            messageLog+="调用异常："+e.getMessage();
            throw e;
        }finally {
            logger.info(messageLog);
            asyncUploadLog(messageLog);
        }
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
        String apiBaseAddressConfig = getAppModule(businessModel).getApiBaseAddress();
        String clientApiBaseUrl =  ContextUtil.getGlobalProperty(apiBaseAddressConfig);
        String clientApiUrl = clientApiBaseUrl + businessModel.getConditonPValue();
        Map<String,Object> params = new HashMap();
        params.put(Constants.BUSINESS_MODEL_CODE,businessModelCode);
        params.put(Constants.ID,businessId);
        String messageLog = "开始调用‘直接获取表达式验证结果’接口，接口url="+clientApiUrl+",参数值"+ JsonUtils.toJson(params);
        Map<String,Object> pvs = null;
        try {
            pvs = ApiClient.getEntityViaProxy(clientApiUrl,new GenericType<Map<String,Object> >() {},params);
            messageLog+=",【result=" + pvs==null?null:JsonUtils.toJson(pvs)+"】";
        }catch (Exception e){
            messageLog+="调用异常："+e.getMessage();
            throw e;
        }finally {
            logger.info(messageLog);
            asyncUploadLog(messageLog);
        }
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
        String apiBaseAddressConfig = getAppModule(businessModel).getApiBaseAddress();
        String clientApiBaseUrl =  ContextUtil.getGlobalProperty(apiBaseAddressConfig);
        String clientApiUrl = clientApiBaseUrl + businessModel.getConditonStatusRest();
        Map<String,Object> params = new HashMap();
        params.put(Constants.BUSINESS_MODEL_CODE,businessModelCode);
        params.put(Constants.ID,businessId);
        params.put(Constants.STATUS,status);
        String messageLog = "开始调用‘重置单据状态’接口，接口url="+clientApiUrl+",参数值"+ JsonUtils.toJson(params);

        try {
            result = ApiClient.postViaProxyReturnResult(clientApiUrl,new GenericType<Boolean>() {},params);
            messageLog+=",【result=" + result+"】";
        }catch (Exception e){
            messageLog+="调用异常："+e.getMessage();
            throw e;
        }finally {
            logger.info(messageLog);
            asyncUploadLog(messageLog);
        }
        return result;
    }


    /**
     * 模拟异步,上传调用日志
     * @param message
     */
    static void asyncUploadLog(String message){
        new Thread(new Runnable() {//模拟异步,上传调用日志
            @Override
            public void run() {
                LogUtil.bizLog(message);
            }
        }).start();
    }

}
