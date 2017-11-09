package com.ecmp.flow.util;

import com.ecmp.config.util.ApiClient;
import com.ecmp.context.ContextUtil;
import com.ecmp.flow.dao.FlowServiceUrlDao;
import com.ecmp.flow.entity.AppModule;
import com.ecmp.flow.entity.FlowServiceUrl;
import com.ecmp.flow.vo.FlowInvokeParams;
import net.sf.json.JSONObject;
import org.apache.commons.collections.map.HashedMap;
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
    public static Object callService(String serviceUrlId,String businessId,String... args){
        Object result = false;
        if(!StringUtils.isEmpty(serviceUrlId)){
            ApplicationContext applicationContext = ContextUtil.getApplicationContext();
            FlowServiceUrlDao flowServiceUrlDao = (FlowServiceUrlDao)applicationContext.getBean("flowServiceUrlDao");
            FlowServiceUrl flowServiceUrl = flowServiceUrlDao.findOne(serviceUrlId);
            if(flowServiceUrl != null){
              String  clientUrl = flowServiceUrl.getUrl();
              AppModule appModule = flowServiceUrl.getBusinessModel().getAppModule();

              if(appModule.getCode().equalsIgnoreCase("FLOW")){
                 Map<String, Object> params = new HashMap<String,Object>();;
                 params.put("id",businessId);
                 params.put("paramJson",args[0]);
                 String url = appModule.getApiBaseAddress()+"/"+clientUrl;
                 result = ApiClient.postViaProxyReturnResult(url,new GenericType<String>() {}, params);
              }else{
                  JSONObject jsonObject = JSONObject.fromObject(args[0]);
                  //HashMap<String,Object> params =   JsonUtils.fromJson(changeText, new TypeReference<HashMap<String,Object>>() {});
                  String receiveTaskActDefId = jsonObject.get("receiveTaskActDefId")+"";
//                  List<String> callActivtiySonPaths = null;
//                  try{
//                      callActivtiySonPaths = jsonObject.getJSONArray("callActivtiySonPaths");
//                  }catch (Exception e){
//                  }
                  Map<String,String> paramMap = null;
                  if(StringUtils.isNotEmpty(receiveTaskActDefId)){
                      if(paramMap==null){
                          paramMap = new HashMap<>();
                      }
                      paramMap.put("receiveTaskActDefId",receiveTaskActDefId);
                  }
                  FlowInvokeParams params = new FlowInvokeParams();
                  params.setId(businessId);
                  params.setParams(paramMap);
                  String url = appModule.getApiBaseAddress()+"/"+clientUrl;
                  result = ApiClient.postViaProxyReturnResult(url,new GenericType<String>() {}, params);
              }
            }else {
                throw new RuntimeException("服务对象找不到");
            }
        }
         return result;
    }
}
