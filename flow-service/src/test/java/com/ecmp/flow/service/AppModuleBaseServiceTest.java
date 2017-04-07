package com.ecmp.flow.service;


import com.ecmp.config.util.ApiClient;
import com.ecmp.flow.BasicContextTestCase;
import com.ecmp.flow.api.IAppModuleService;
import com.ecmp.flow.entity.AppModule;
import com.ecmp.vo.OperateResult;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import org.apache.cxf.jaxrs.client.JAXRSClientFactory;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AppModuleBaseServiceTest extends BasicContextTestCase {

    @Autowired
    private AppModuleService appModuleService;

    @Test
    public void save() {
        AppModule appModule = new AppModule();
        appModule.setCode("ecmp-flow-appModule22_" + System.currentTimeMillis());
        appModule.setName("应用模块测试22");
        OperateResult<AppModule> result  = appModuleService.save(appModule);
        appModule=result.getData();
        logger.debug("id = {}", appModule.getId());
        logger.debug("create结果：{}", appModule);
    }

    @Test
    public void update() {
        List<AppModule> appModuleList = appModuleService.findAll();
        if (appModuleList != null && appModuleList.size() > 0) {
            AppModule appModule = appModuleList.get(0);
            logger.debug("update前：{}", appModule);
            appModule.setCode("ecmp-flow-appModule2_" + System.currentTimeMillis());
            appModule.setName("应用模块测试2");
            appModuleService.save(appModule);
            logger.debug("update后：{}", appModule);
        } else {
            logger.warn("未能取到数据");
        }
    }
    @Test
    public void apiTest(){
        String baseAddress = "http://localhost:8080/flow";

        List<Object> providerList = new ArrayList<Object>();
        providerList.add(new JacksonJsonProvider());

        IAppModuleService appModuleService = JAXRSClientFactory.create(baseAddress, IAppModuleService.class, providerList);
        List<AppModule> result  = appModuleService.findAll();
        System.out.println(result.size());

        IAppModuleService proxy = ApiClient.createProxy(IAppModuleService.class);
        result  = proxy.findAll();
        System.out.println(result.size());
    }

}
