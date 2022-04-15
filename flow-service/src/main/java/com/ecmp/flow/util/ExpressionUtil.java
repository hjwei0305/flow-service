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

import javax.ws.rs.core.GenericType;
import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

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

    public static AppModule getAppModule(BusinessModel businessModel) {
        AppModule appModule = businessModel.getAppModule();
        return appModule;
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
        String clientApiUrl = PageUrlUtil.buildUrl(clientApiBaseUrl, businessModel.getConditonProperties());
        Map<String, Object> params = new HashMap();
        params.put(Constants.BUSINESS_MODEL_CODE, businessModelCode);
        params.put(Constants.ALL, false);
        ResponseData<Map<String, String>> result;
        Map<String, String> map;
        try {
            result = ApiClient.getEntityViaProxy(clientApiUrl, new GenericType<ResponseData<Map<String, String>>>() {
            }, params);
            if (result.successful()) {
                map = result.getData();
            } else {
                LogUtil.error("开始调用【条件属性说明服务地址】,接口返回错误信息：{},url={},参数值{}", result.getMessage(), clientApiUrl, JsonUtils.toJson(params));
                throw new FlowException(ContextUtil.getMessage("10277", result.getMessage()));
            }
            LogUtil.bizLog("开始调用【条件属性说明服务地址】,接口url={},参数值{},返回【result={}】", clientApiUrl, JsonUtils.toJson(params), JsonUtils.toJson(result));
        } catch (Exception e) {
            LogUtil.error("开始调用【条件属性说明服务地址】,接口调用异常：{},接口url={},参数值{}", e.getMessage(), clientApiUrl, JsonUtils.toJson(params), e);
            throw new FlowException(ContextUtil.getMessage("10278", e.getMessage()));
        }
        return map;
    }


    /**
     * 获取条件属性的备注说明
     *
     * @param businessModel 业务模型
     * @return
     */
    public static Map<String, String> getPropertiesRemark(BusinessModel businessModel) {
        String businessModelCode = businessModel.getClassName();
        String apiBaseAddressConfig = getAppModule(businessModel).getApiBaseAddress();
        String clientApiBaseUrl = Constants.getConfigValueByApi(apiBaseAddressConfig);
        String clientApiUrl = PageUrlUtil.buildUrl(clientApiBaseUrl, businessModel.getConditonProperties() + "Remark");
        Map<String, Object> params = new HashMap();
        params.put(Constants.BUSINESS_MODEL_CODE, businessModelCode);
        ResponseData<Map<String, String>> result;
        Map<String, String> map;
        try {
            result = ApiClient.getEntityViaProxy(clientApiUrl, new GenericType<ResponseData<Map<String, String>>>() {
            }, params);
            if (result.successful()) {
                map = result.getData();
            } else {
                LogUtil.error("开始调用【获取条件属性的备注说明】,接口返回错误信息：{},接口url={},参数值{}", result.getMessage(), clientApiUrl, JsonUtils.toJson(params));
                return null;
            }
            LogUtil.bizLog("开始调用【获取条件属性的备注说明】，接口url={},参数值{},返回【result={}】", clientApiUrl, JsonUtils.toJson(params), JsonUtils.toJson(result));
        } catch (Exception e) {
            LogUtil.error("开始调用【获取条件属性的备注说明】,接口调用异常：{},接口url={},参数值{}", e.getMessage(), clientApiUrl, JsonUtils.toJson(params), e);
            return null;
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
        String clientApiUrl = PageUrlUtil.buildUrl(clientApiBaseUrl, businessModel.getConditonPSValue());
        Map<String, Object> params = new HashMap();
        params.put(Constants.BUSINESS_MODEL_CODE, businessModel.getClassName());
        ResponseData<Map<String, Object>> result;
        Map<String, Object> map;
        try {
            result = ApiClient.getEntityViaProxy(clientApiUrl, new GenericType<ResponseData<Map<String, Object>>>() {
            }, params);
            if (result.successful()) {
                map = result.getData();
            } else {
                LogUtil.error("开始调用【条件属性初始值服务地址】,接口返回错误信息：{},接口url={},参数值{}", result.getMessage(), clientApiUrl, JsonUtils.toJson(params));
                throw new FlowException(ContextUtil.getMessage("10279", result.getMessage()));
            }
            LogUtil.bizLog("开始调用【条件属性初始值服务地址】，接口url={},参数值{},返回【result={}】", clientApiUrl, JsonUtils.toJson(params), JsonUtils.toJson(result));
        } catch (Exception e) {
            LogUtil.error("开始调用【条件属性初始值服务地址】,接口调用异常：{},接口url={},参数值{}", e.getMessage(), clientApiUrl, JsonUtils.toJson(params), e);
            throw new FlowException(ContextUtil.getMessage("10280", e.getMessage()));
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
        String clientApiUrl = PageUrlUtil.buildUrl(clientApiBaseUrl, businessModel.getConditonPValue());
        Map<String, Object> params = new HashMap();
        params.put(Constants.BUSINESS_MODEL_CODE, businessModelCode);
        params.put(Constants.ID, businessId);
        params.put(Constants.ALL, all);
        Date startDate = new Date();
        SimpleDateFormat sim = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
        ResponseData<Map<String, Object>> result;
        Map<String, Object> map;
        try {
            result = ApiClient.getEntityViaProxy(clientApiUrl, new GenericType<ResponseData<Map<String, Object>>>() {
            }, params);
            Date endDate = new Date();
            if (result.successful()) {
                map = result.getData();
            } else {
                LogUtil.error("【{}】开始调用【条件属性值服务地址】,接口返回错误信息：{},接口url={},参数值{}", sim.format(startDate), result.getMessage(), clientApiUrl, JsonUtils.toJson(params));
                throw new FlowException(ContextUtil.getMessage("10281", result.getMessage()));
            }
            LogUtil.bizLog("【{}】开始调用【条件属性值服务地址】，接口url={},参数值{},【{}】返回【result={}】", sim.format(startDate), clientApiUrl, JsonUtils.toJson(params), sim.format(endDate), JsonUtils.toJson(result));
        } catch (Exception e) {
            LogUtil.error("【{}】开始调用【条件属性值服务地址】,接口调用异常：{},接口url={},参数值{}", sim.format(startDate), e.getMessage(), clientApiUrl, JsonUtils.toJson(params), e);
            throw new FlowException(ContextUtil.getMessage("10282", e.getMessage()));
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
    public static ResponseData resetState(BusinessModel businessModel, String businessId, FlowStatus status) {
        String businessModelCode = businessModel.getClassName();
        String apiBaseAddressConfig = getAppModule(businessModel).getApiBaseAddress();
        String clientApiBaseUrl = Constants.getConfigValueByApi(apiBaseAddressConfig);
        String clientApiUrl = PageUrlUtil.buildUrl(clientApiBaseUrl, businessModel.getConditonStatusRest());
        Map<String, Object> params = new HashMap();
        params.put(Constants.BUSINESS_MODEL_CODE, businessModelCode);
        params.put(Constants.ID, businessId);
        params.put(Constants.STATUS, status);
        try {
            ResponseData result = ApiClient.postViaProxyReturnResult(clientApiUrl, new GenericType<ResponseData>() {
            }, params);
            if (result.successful()) {
                Boolean boo = (Boolean) result.getData();
                if (BooleanUtils.isFalse(boo)) {
                    LogUtil.error("调用【重置单据状态】，接口返回失败信息：{}，地址=[{}]，参数=[{}]，返回=[{}]", result.getMessage(), clientApiUrl, JsonUtils.toJson(params), JsonUtils.toJson(result));
                    return ResponseData.operationFailure(ContextUtil.getMessage("10283", result.getMessage()));
                } else {
                    LogUtil.bizLog("调用【重置单据状态】，地址=[{}]，参数=[{}]，返回=[{}]", clientApiUrl, JsonUtils.toJson(params), JsonUtils.toJson(result));
                    return result;
                }
            } else {
                LogUtil.error("调用【重置单据状态】，接口返回错误信息：{}，地址=[{}]，参数=[{}]，返回=[{}]", result.getMessage(), clientApiUrl, JsonUtils.toJson(params), JsonUtils.toJson(result));
                return ResponseData.operationFailure("调用【重置单据状态】，接口返回错误信息：" + result.getMessage());
            }
        } catch (Exception e) {
            LogUtil.error("调用【重置单据状态】，接口调用异常：{}，地址=[{}]，参数=[{}]", e.getMessage(), clientApiUrl, JsonUtils.toJson(params), e);
            return ResponseData.operationFailure(ContextUtil.getMessage("10284", e.getMessage()));
        }
    }


    /**
     * 轮询设置单据状态
     *
     * @param businessModel
     * @param businessId
     * @param status
     */
    public static void pollingResetState(BusinessModel businessModel, String businessId, FlowStatus status) {
        //进行异步轮询
        Calendar startTreadTime = Calendar.getInstance();
        ScheduledExecutorService service = Executors
                .newSingleThreadScheduledExecutor();
        Runnable runnable = new Runnable() {
            public void run() {
                Calendar nowTime = Calendar.getInstance();
                nowTime.add(Calendar.MINUTE, -2);//不能超过2分钟
                if (nowTime.after(startTreadTime)) {
                    service.shutdown();
                }
                ResponseData resultData = resetState(businessModel, businessId, status);
                if (resultData.successful()) {
                    service.shutdown();
                }
            }
        };
        // 第二个参数为首次执行的延时时间，第三个参数为定时执行的间隔时间
        service.scheduleWithFixedDelay(runnable, 1, 20, TimeUnit.SECONDS);
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
    public static Boolean result(BusinessModel businessModel, String businessId, String expression) {
        //获取条件属性值
        Map<String, Object> pvs = getPropertiesValuesMap(businessModel, businessId, true);
        return ConditionUtil.groovyTest(expression, pvs);
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
