package com.ecmp.flow.util;

import com.ecmp.config.util.ApiClient;
import com.ecmp.context.ContextUtil;
import com.ecmp.flow.common.util.Constants;
import com.ecmp.flow.constant.FlowStatus;
import com.ecmp.flow.dao.util.PageUrlUtil;
import com.ecmp.flow.entity.AppModule;
import com.ecmp.flow.entity.BusinessModel;
import com.ecmp.log.util.LogUtil;
import com.ecmp.util.JsonUtils;
import com.ecmp.vo.ResponseData;
import org.apache.commons.lang.BooleanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.GenericType;
import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
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

    public static AppModule getAppModule(BusinessModel businessModel) {
        AppModule appModule = businessModel.getAppModule();
        return appModule;
    }

    public static String getErrorLogString(String name) {
        return "调用" + name + "接口异常,详情请查看日志!";
    }

    /**
     * 获取条件属性说明（键值对）
     *
     * @param businessModel 业务模型
     * @return
     */
    public static Map<String, String> getPropertiesDecMap(BusinessModel businessModel) {
        String businessModelCode = businessModel.getClassName();
        String apiBaseAddressConfig = getAppModule(businessModel).getApiBaseAddress();
        String clientApiBaseUrl = Constants.getConfigValueByApi(apiBaseAddressConfig);
        String clientApiUrl = PageUrlUtil.buildUrl(clientApiBaseUrl,businessModel.getConditonProperties());
        Map<String, Object> params = new HashMap();
        params.put(Constants.BUSINESS_MODEL_CODE, businessModelCode);
        params.put(Constants.ALL, false);
        String messageLog = "开始调用【条件属性说明服务地址】，接口url=" + clientApiUrl + ",参数值" + JsonUtils.toJson(params);
        ResponseData<Map<String,String>> result;
        Map<String, String> map;
        try {
            result = ApiClient.getEntityViaProxy(clientApiUrl, new GenericType<ResponseData<Map<String,String>>>() {
            }, params);
            if (result.successful()) {
                map = result.getData();
            } else {
                messageLog += "-接口返回信息：" + result.getMessage();
                LogUtil.error(messageLog);
                throw new FlowException(getErrorLogString("【条件属性说明服务地址】"));
            }
            messageLog += ",【result=" + JsonUtils.toJson(result) + "】";
            LogUtil.bizLog(messageLog);
        } catch (Exception e) {
            messageLog += "-调用异常：" + e.getMessage();
            LogUtil.error(messageLog);
            throw new FlowException(getErrorLogString("【条件属性说明服务地址】"), e);
        }
        return map;
    }


    /**
     * 获取条件属性的备注说明
     * @param businessModel 业务模型
     * @return
     */
    public  static Map<String,String>  getPropertiesRemark(BusinessModel businessModel){
        String businessModelCode = businessModel.getClassName();
        String apiBaseAddressConfig = getAppModule(businessModel).getApiBaseAddress();
        String clientApiBaseUrl =  Constants.getConfigValueByApi(apiBaseAddressConfig);
        String  clientApiUrl =  PageUrlUtil.buildUrl(clientApiBaseUrl,businessModel.getConditonProperties()+"Remark");
        Map<String,Object> params = new HashMap();
        params.put(Constants.BUSINESS_MODEL_CODE,businessModelCode);
        String messageLog = "开始调用【获取条件属性的备注说明】，接口url="+clientApiUrl+",参数值"+ JsonUtils.toJson(params);
        ResponseData<Map<String,String>> result;
        Map<String,String> map;
        try {
            result = ApiClient.getEntityViaProxy(clientApiUrl, new GenericType<ResponseData<Map<String,String>>>() {}, params);
            if (result.successful()) {
                map = result.getData();
            } else {
                messageLog += "-接口返回信息：" + result.getMessage();
                LogUtil.error(messageLog);
                return  null;
            }
            messageLog += ",【result=" + JsonUtils.toJson(result) + "】";
            LogUtil.bizLog(messageLog);
        }catch (Exception e){
            messageLog+="-调用异常："+e.getMessage();
            LogUtil.error(messageLog);
            return  null;
        }
        return map;
    }




    /**
     * 获取条件属性初始值（键值对）
     *
     * @param businessModel 业务实体模型
     * @return
     */
    public static Map<String, Object> getPropertiesInitialValuesMap(BusinessModel businessModel) {
        String apiBaseAddressConfig = getAppModule(businessModel).getApiBaseAddress();
        String clientApiBaseUrl = Constants.getConfigValueByApi(apiBaseAddressConfig);
        String clientApiUrl = PageUrlUtil.buildUrl(clientApiBaseUrl,businessModel.getConditonPSValue());
        Map<String, Object> params = new HashMap();
        params.put(Constants.BUSINESS_MODEL_CODE, businessModel.getClassName());
        String messageLog = "开始调用【条件属性初始值服务地址】，接口url=" + clientApiUrl + ",参数值" + JsonUtils.toJson(params);
        ResponseData<Map<String, Object>> result;
        Map<String, Object> map;
        try {
            result = ApiClient.getEntityViaProxy(clientApiUrl, new GenericType<ResponseData<Map<String, Object>>>() {
            }, params);
            if (result.successful()) {
                map = result.getData();
            } else {
                messageLog += "-接口返回信息：" + result.getMessage();
                LogUtil.error(messageLog);
                throw new FlowException(getErrorLogString("【条件属性初始值服务地址】"));
            }
            messageLog += ",【result=" + JsonUtils.toJson(result) + "】";
            LogUtil.bizLog(messageLog);
        } catch (Exception e) {
            messageLog += "-调用异常：" + e.getMessage();
            LogUtil.error(messageLog);
            throw new FlowException(getErrorLogString("【条件属性初始值服务地址】"), e);
        }
        return map;
    }


    /**
     * 获取条件属性值（键值对）
     *
     * @param businessModel 业务模型
     * @param businessId    业务ID
     * @return
     */
    public static Map<String, Object> getPropertiesValuesMap(BusinessModel businessModel, String businessId, Boolean all) {
        String businessModelCode = businessModel.getClassName();
        String apiBaseAddressConfig = getAppModule(businessModel).getApiBaseAddress();
        String clientApiBaseUrl = Constants.getConfigValueByApi(apiBaseAddressConfig);
        String clientApiUrl = PageUrlUtil.buildUrl(clientApiBaseUrl,businessModel.getConditonPValue());
        Map<String, Object> params = new HashMap();
        params.put(Constants.BUSINESS_MODEL_CODE, businessModelCode);
        params.put(Constants.ID, businessId);
        params.put(Constants.ALL, all);
        Date startDate = new Date();
        SimpleDateFormat sim = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
        String messageLog = sim.format(startDate) + "开始调用【条件属性值服务地址】，接口url=" + clientApiUrl + ",参数值" + JsonUtils.toJson(params);
        ResponseData<Map<String, Object>> result;
        Map<String, Object> map;
        try {
            result = ApiClient.getEntityViaProxy(clientApiUrl, new GenericType<ResponseData<Map<String, Object>>>() {
            }, params);
            Date endDate = new Date();
            if (result.successful()) {
                map = result.getData();
            } else {
                messageLog += "-接口返回信息：" + result.getMessage();
                LogUtil.error(messageLog);
                throw new FlowException(getErrorLogString("【条件属性值服务地址】"));
            }
            messageLog += "," + sim.format(endDate) + "【result=" + JsonUtils.toJson(result) + "】";
            LogUtil.bizLog(messageLog);
        } catch (Exception e) {
            messageLog += "-调用异常：" + e.getMessage();
            LogUtil.error(messageLog);
            throw new FlowException(getErrorLogString("【条件属性值服务地址】"), e);
        }
        return map;
    }


    /**
     * 重置单据状态
     *
     * @param businessModel 业务实体
     * @param status
     * @param businessId    业务ID
     * @return
     */
    public static boolean resetState(BusinessModel businessModel, String businessId, FlowStatus status) {
        String businessModelCode = businessModel.getClassName();
        String apiBaseAddressConfig = getAppModule(businessModel).getApiBaseAddress();
        String clientApiBaseUrl = Constants.getConfigValueByApi(apiBaseAddressConfig);
        String clientApiUrl = PageUrlUtil.buildUrl(clientApiBaseUrl,businessModel.getConditonStatusRest());
        Map<String, Object> params = new HashMap();
        params.put(Constants.BUSINESS_MODEL_CODE, businessModelCode);
        params.put(Constants.ID, businessId);
        params.put(Constants.STATUS, status);
        String messageLog = "开始调用【重置单据状态】接口，接口url=" + clientApiUrl + ",参数值" + JsonUtils.toJson(params);
        ResponseData<Boolean> result;
        Boolean boo;
        try {
            result = ApiClient.postViaProxyReturnResult(clientApiUrl, new GenericType<ResponseData<Boolean>>() {
            }, params);
            if (result.successful()) {
                boo = result.getData();
                if(boo == null){
                    messageLog += "-接口返回data信息为空：" + result.getMessage();
                    LogUtil.error(messageLog);
                    throw new FlowException(getErrorLogString("【重置单据状态】"));
                }
            } else {
                messageLog += "-接口返回信息：" + result.getMessage();
                LogUtil.error(messageLog);
                throw new FlowException(getErrorLogString("【重置单据状态】"));
            }
            messageLog += ",【result=" + JsonUtils.toJson(result) + "】";
            LogUtil.bizLog(messageLog);
        } catch (Exception e) {
            messageLog += "-调用异常：" + e.getMessage();
            LogUtil.error(messageLog);
            throw new FlowException(getErrorLogString("【重置单据状态】"), e);
        }
        return boo;
    }


    /**
     * 检证表达式语法是否合法
     *
     * @param businessModel 业务实体
     * @param expression    表达式
     * @return
     */
    public static Boolean validate(BusinessModel businessModel, String expression) throws ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
        //获取条件属性初始值
        Map<String, Object> pvs = getPropertiesInitialValuesMap(businessModel);
        Boolean result = ConditionUtil.groovyTest(expression, pvs);
        return result;
    }

    /**
     * 直接获取表达式验证结果
     *
     * @param businessModel 业务实体
     * @param expression
     * @param businessId    业务ID
     * @return
     */
    public static boolean result(BusinessModel businessModel, String businessId, String expression) {
        //获取条件属性值
        Map<String, Object> pvs = getPropertiesValuesMap(businessModel, businessId, true);
        boolean result = ConditionUtil.groovyTest(expression, pvs);
        return result;
    }


    /**
     * 模拟异步,上传调用日志
     *
     * @param message
     */
    static void asyncUploadLog(String message) {
        new Thread(new Runnable() {//模拟异步,上传调用日志
            @Override
            public void run() {
                LogUtil.bizLog(message);
            }
        }).start();
    }

}
