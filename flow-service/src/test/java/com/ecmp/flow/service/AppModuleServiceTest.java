package com.ecmp.flow.service;

import com.ecmp.config.util.ApiJsonUtils;
import com.ecmp.flow.entity.AppModule;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.junit.Assert.*;

/**
 * <strong>实现功能:</strong>
 * <p></p>
 *
 * @author 王锦光 wangj
 * @version 1.0.1 2018-09-10 14:49
 */
public class AppModuleServiceTest extends BaseContextTestCase{
    @Autowired
    private AppModuleService service;

    @Test
    public void findAll() {
        List<AppModule> appModules = service.findAll();
        Assert.assertNotNull(appModules);
        System.out.println(ApiJsonUtils.toJson(appModules));
    }
}