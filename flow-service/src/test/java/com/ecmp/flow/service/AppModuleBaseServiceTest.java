package com.ecmp.flow.service;


import com.ecmp.flow.BasicContextTestCase;
import com.ecmp.flow.entity.AppModule;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.List;

public class AppModuleBaseServiceTest extends BasicContextTestCase {

    @Autowired
    private AppModuleService appModuleService;

    @Test
    public void save() {
        AppModule appModule = new AppModule();
        appModule.setCode("ecmp-flow-appModule" + System.currentTimeMillis());
        appModule.setName("应用模块测试1");
        appModule = appModuleService.save(appModule);
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

}
