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

import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

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
                String clientApiBaseUrl = ContextUtil.getAppModule(appModule.getCode()).getApiBaseAddress();
                //平台API服务使用的JSON序列化提供类

                List<Object> providers = new ArrayList<>();
                providers.add(new ApiRestJsonProvider());
                //API会话检查的客户端过滤器
                providers.add(new SessionClientRequestFilter());

                result = WebClient.create(clientApiBaseUrl, providers)
                        .path(clientUrl+"/{id}/{changeText}",businessId,args[0])
                        .accept(MediaType.APPLICATION_JSON)
                        .get(boolean.class);
            }else {
                throw new RuntimeException("服务对象找不到");
            }
        }
         return result;
    }
}
