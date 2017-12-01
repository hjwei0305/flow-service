package com.ecmp.flow.util;

import com.ecmp.config.util.ApiClient;
import com.ecmp.context.ContextUtil;
import com.ecmp.flow.dao.FlowServiceUrlDao;
import com.ecmp.flow.entity.AppModule;
import com.ecmp.flow.entity.FlowServiceUrl;
import com.ecmp.flow.vo.FlowInvokeParams;
import com.ecmp.flow.vo.FlowOperateResult;
import com.ecmp.log.util.LogUtil;
import com.ecmp.util.JsonUtils;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
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
    public static FlowOperateResult callService(String serviceUrlId,String businessId,String... args){
        FlowOperateResult result = null;
        if(!StringUtils.isEmpty(serviceUrlId)){
            ApplicationContext applicationContext = ContextUtil.getApplicationContext();
            FlowServiceUrlDao flowServiceUrlDao = (FlowServiceUrlDao)applicationContext.getBean("flowServiceUrlDao");
            FlowServiceUrl flowServiceUrl = flowServiceUrlDao.findOne(serviceUrlId);
            if(flowServiceUrl != null){
              String  clientUrl = flowServiceUrl.getUrl();
              AppModule appModule = flowServiceUrl.getBusinessModel().getAppModule();

                Map<String,String> paramMap = new HashMap<String,String>();
                FlowInvokeParams params = new FlowInvokeParams();
                if(org.apache.commons.lang3.StringUtils.isNotEmpty(args[0])){
                    try {
                        JSONObject jsonObject = JSONObject.fromObject(args[0]);
                        if(jsonObject.has("approved")){
                            String approved = jsonObject.get("approved") + "";
                            if (StringUtils.isNotEmpty(approved) && !"null".equalsIgnoreCase(approved)) {
                                params.setAgree(Boolean.parseBoolean(approved));
                            }
                        }
                        if(jsonObject.has("approveResult")){
                            String approveResult = jsonObject.get("approveResult") + "";
                            if (StringUtils.isNotEmpty(approveResult)) {
                                params.setFinalAgree(Boolean.parseBoolean(approveResult));
                            }
                        }
                        if(jsonObject.has("receiveTaskActDefId")){
                            String receiveTaskActDefId = (String) jsonObject.get("receiveTaskActDefId") ;
                            if (StringUtils.isNotEmpty(receiveTaskActDefId)) {
                                params.setReceiveTaskActDefId(receiveTaskActDefId);
                            }
                        }
                        if(jsonObject.has("reject")){
                            int reject = jsonObject.getInt("reject");
                            if(reject==1){
                                params.setReject(true);//是否被驳回
                            }
                        }
                        if(jsonObject.has("callActivtiySonPaths")){
                            List<String> callActivtiySonPaths = jsonObject.getJSONArray("callActivtiySonPaths");
                            params.setCallActivtiySonPaths(callActivtiySonPaths);
                        }
                    }catch (Exception e){
                    }
                }
                params.setId(businessId);
                params.setParams(paramMap);
                String url = appModule.getApiBaseAddress()+"/"+clientUrl;
                result = ApiClient.postViaProxyReturnResult(url,new GenericType<FlowOperateResult>() {}, params);
                FlowOperateResult resultAy = result;
                new Thread(new Runnable() {//模拟异步
                    @Override
                    public void run() {
                        String paramsStr = JsonUtils.toJson(params);
                        String message = "Flow call Service url =" + url + ";params="+paramsStr+";result = "+resultAy.toString();
                        LogUtil.addExceptionLog(message);
                    }
                }).start();
            }else {
                throw new FlowException("服务对象找不到");
            }
        }
         return result;
    }
}
