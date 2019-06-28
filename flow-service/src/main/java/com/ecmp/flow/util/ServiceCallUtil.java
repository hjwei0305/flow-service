package com.ecmp.flow.util;

import com.ecmp.config.util.ApiClient;
import com.ecmp.context.ContextUtil;
import com.ecmp.flow.common.util.Constants;
import com.ecmp.flow.dao.FlowServiceUrlDao;
import com.ecmp.flow.entity.AppModule;
import com.ecmp.flow.entity.FlowServiceUrl;
import com.ecmp.flow.vo.FlowInvokeParams;
import com.ecmp.flow.vo.FlowOperateResult;
import com.ecmp.log.util.LogUtil;
import com.ecmp.util.JsonUtils;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import javax.ws.rs.core.GenericType;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * *************************************************************************************************
 * <p/>
 * 实现功能：服务调用
 * <p>
 * ------------------------------------------------------------------------------------------------
 * 版本          变更时间             变更人                     变更原因
 * ------------------------------------------------------------------------------------------------
 * 1.0.00      2017/5/27 13:06      谭军(tanjun)                    新建
 * <p/>
 * *************************************************************************************************
 */
public class ServiceCallUtil {
    private static final Logger logger = LoggerFactory.getLogger(ServiceCallUtil.class);
    public static FlowOperateResult callService(String serviceUrlId,String businessId,String... args){
        FlowOperateResult result = null;
        if(!StringUtils.isEmpty(serviceUrlId)){
            ApplicationContext applicationContext = ContextUtil.getApplicationContext();
            FlowServiceUrlDao flowServiceUrlDao = (FlowServiceUrlDao)applicationContext.getBean(Constants.FLOW_SERVICE_URL_DAO);
            FlowServiceUrl flowServiceUrl = flowServiceUrlDao.findOne(serviceUrlId);
            if(flowServiceUrl != null){
                String  clientUrl = flowServiceUrl.getUrl();
                AppModule appModule = flowServiceUrl.getBusinessModel().getAppModule();

                Map<String,String> paramMap = new HashMap<String,String>();
                FlowInvokeParams params = new FlowInvokeParams();
                if(org.apache.commons.lang3.StringUtils.isNotEmpty(args[0])){
                    try {
                        JSONObject jsonObject = JSONObject.fromObject(args[0]);
                        if(jsonObject.has(Constants.APPROVED)){
                            String approved = jsonObject.get(Constants.APPROVED)+"";
                            if (StringUtils.isNotEmpty(approved) && !Constants.NULL_S.equalsIgnoreCase(approved)) {
                                params.setAgree(Boolean.parseBoolean(approved));
                            }
                        }
                        if(jsonObject.has(Constants.APPROVE_RESULT)){
                            String approveResult = jsonObject.get(Constants.APPROVE_RESULT)+"";
                            if (StringUtils.isNotEmpty(approveResult) && !Constants.NULL_S.equalsIgnoreCase(approveResult)) {
                                params.setFinalAgree(Boolean.parseBoolean(approveResult));
                            }
                        }
                        if(jsonObject.has(Constants.RECEIVE_TASK_ACT_DEF_ID)){
                            String receiveTaskActDefId = jsonObject.get(Constants.RECEIVE_TASK_ACT_DEF_ID)+"";
                            if (StringUtils.isNotEmpty(receiveTaskActDefId) && !Constants.NULL_S.equalsIgnoreCase(receiveTaskActDefId)) {
                                params.setTaskActDefId(receiveTaskActDefId);
                            }
                        }
//                        tempV.put(Constants.POOL_TASK_ACT_DEF_ID,actTaskDefKey);
                        if(jsonObject.has(Constants.POOL_TASK_ACT_DEF_ID)){
                            String poolTaskActDefId = jsonObject.get(Constants.POOL_TASK_ACT_DEF_ID)+"";
                            if (StringUtils.isNotEmpty(poolTaskActDefId) && !Constants.NULL_S.equalsIgnoreCase(poolTaskActDefId)) {
                                params.setTaskActDefId(poolTaskActDefId);
                            }
                        }
                        if(jsonObject.has(Constants.POOL_TASK_CODE)){
                            String poolTaskCode = jsonObject.get(Constants.POOL_TASK_CODE)+"";
                            if (StringUtils.isNotEmpty(poolTaskCode) && !Constants.NULL_S.equalsIgnoreCase(poolTaskCode)) {
                                params.setPoolTaskCode(poolTaskCode);
                            }
                        }
                        if(jsonObject.has(Constants.REJECT)){
                            int reject = jsonObject.getInt(Constants.REJECT);
                            if(reject==1){
                                params.setReject(true);//是否被驳回
                            }
                        }
                        if(jsonObject.has(Constants.CALL_ACTIVITY_SON_PATHS)){
                            List<String> callActivitySonPaths = jsonObject.getJSONArray(Constants.CALL_ACTIVITY_SON_PATHS);
                            params.setCallActivitySonPaths(callActivitySonPaths);
                        }
                        if(jsonObject.has(Constants.OPINION)){
                            String  opinion = jsonObject.get(Constants.OPINION)+"";
                            paramMap.put(Constants.OPINION,opinion);
                        }
                        if(jsonObject.has("selectedNodesUserMap")){
                            JSONObject itemJSONObj = jsonObject.getJSONObject("selectedNodesUserMap");
                            Map<String, List<String>> itemMap = (Map<String, List<String>>) JSONObject.toBean(itemJSONObj, Map.class);
                            params.setNextNodeUserInfo(itemMap);
                        }
                        if(jsonObject.has("currentNodeCode")){
                            String  currentNodeCode = jsonObject.get("currentNodeCode")+"";
                            paramMap.put("nodeCode",currentNodeCode);
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                        throw e;
                    }
                }
                params.setId(businessId);
                params.setParams(paramMap);
                String apiBaseAddressConfig = appModule.getApiBaseAddress();
                String clientApiBaseUrl =  ContextUtil.getGlobalProperty(apiBaseAddressConfig);
                String url = clientApiBaseUrl+"/"+clientUrl;
                String exceptionMessage = null;
                try {
                    result = ApiClient.postViaProxyReturnResult(url, new GenericType<FlowOperateResult>() {
                    }, params);
                }catch (Exception e){
                    exceptionMessage="Flow call Service exception:"+e.getMessage();
                    result = new FlowOperateResult(false,"服务调用异常，请联系管理员！");
                }finally {
                    String exceptionMessageFinal = exceptionMessage;
                    FlowOperateResult resultAy = result;
                    new Thread(new Runnable() {//模拟异步,上传调用日志
                        @Override
                        public void run() {
                            String paramsStr = JsonUtils.toJson(params);
                            String message = "Flow call Service url =" + url + ";params="+paramsStr+";result = ";
                            if(StringUtils.isNotEmpty(exceptionMessageFinal)){
                                message+=exceptionMessageFinal;
                            }else {
                                message+=resultAy.toString();
                            }
                            LogUtil.bizLog(message);
                        }
                    }).start();
                }

            }else {
                throw new FlowException("serviceUrlId='"+serviceUrlId+"'s service object can't be found!");
            }
        }
        return result;
    }
}
