package com.ecmp.flow.util;

import com.ecmp.basic.entity.AppModule;
import com.ecmp.config.util.ApiClient;
import com.ecmp.config.util.ApiRestJsonProvider;
import com.ecmp.config.util.SessionClientRequestFilter;
import com.ecmp.context.ContextUtil;
import com.ecmp.core.dao.jpa.BaseDao;
import com.ecmp.flow.dao.FlowServiceUrlDao;
import com.ecmp.flow.entity.FlowServiceUrl;
import org.apache.commons.lang.StringUtils;
import org.apache.cxf.jaxrs.client.WebClient;
import org.springframework.context.ApplicationContext;

import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import java.util.*;

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
    public static boolean callService(String serviceUrlId,String businessId,String... args){
        boolean result = false;
        if(!StringUtils.isEmpty(serviceUrlId)){
            ApplicationContext applicationContext = ContextUtil.getApplicationContext();
            FlowServiceUrlDao flowServiceUrlDao = (FlowServiceUrlDao)applicationContext.getBean("flowServiceUrlDao");
            FlowServiceUrl flowServiceUrl = flowServiceUrlDao.findOne(serviceUrlId);
            if(flowServiceUrl != null){
              String  clientUrl = flowServiceUrl.getUrl();
              String appModuleId = flowServiceUrl.getBusinessModel().getAppModuleId();
              com.ecmp.basic.api.IAppModuleService iAppModuleService = ApiClient.createProxy(com.ecmp.basic.api.IAppModuleService.class);
              AppModule appModule = iAppModuleService.findOne(appModuleId);
              Map<String, String> params = new HashMap<String,String>();;
              params.put("id",businessId);
              params.put("paramJson",args[0]);
              result = ApiClient.postViaProxyReturnResult(appModule.getCode(),  clientUrl,new GenericType<Boolean>() {}, params);
            }else {
                throw new RuntimeException("服务对象找不到");
            }
        }
         return result;
    }
}
