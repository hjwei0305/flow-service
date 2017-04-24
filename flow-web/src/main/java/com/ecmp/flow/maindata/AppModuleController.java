package com.ecmp.flow.maindata;

import com.ecmp.config.util.ApiClient;
import com.ecmp.core.json.JsonUtil;
import com.ecmp.core.search.*;
import com.ecmp.core.vo.OperateStatus;
import com.ecmp.flow.api.IAppModuleService;
import com.ecmp.flow.entity.AppModule;
import com.ecmp.flow.entity.BusinessModel;
import com.ecmp.vo.OperateResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.cxf.jaxrs.client.JAXRSClientFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.persistence.Column;
import javax.persistence.Transient;
import javax.servlet.ServletRequest;
import java.io.Serializable;
import java.util.*;

/**
 * *************************************************************************************************
 * <p/>
 * 实现功能：
 * 应用模块控制器
 * <p>
 * ------------------------------------------------------------------------------------------------
 * 版本          变更时间             变更人                     变更原因
 * ------------------------------------------------------------------------------------------------
 * 1.0.00      2017/3/30 16:10      陈飞(Vision.Mac)            新建
 * <p/>
 * *************************************************************************************************
 */
@Controller
@RequestMapping(value = "/maindata/appModule")
public class AppModuleController {


    @RequestMapping()
    public String showAppModule() {
        return "maindata/AppModuleView";
    }

    @RequestMapping(value = "find")
    @ResponseBody
    public String find(ServletRequest request) throws JsonProcessingException {
         System.out.println("---------------------------------------------");
        Search search = SearchUtil.genSearch(request);
        IAppModuleService proxy = ApiClient.createProxy(IAppModuleService.class);
        PageResult<AppModule> appModulePageResult  = proxy.findByPage(search);
        String appModule = JsonUtil.serialize(appModulePageResult);
        return appModule;
    }

    @RequestMapping(value = "delete")
    @ResponseBody
    public String delete(String id) throws JsonProcessingException {
        System.out.println("---------------------------------------------");
        System.out.println(id);
        //   System.out.print(status);
        IAppModuleService proxy = ApiClient.createProxy(IAppModuleService.class);
        OperateResult result = proxy.delete(id);
        OperateStatus operateStatus = new OperateStatus(result.successful(), result.getMessage());
        String delSuccess = JsonUtil.serialize(operateStatus);
        return delSuccess;
    }

    @RequestMapping(value = "update")
    @ResponseBody
    public String update(AppModule appModule) throws JsonProcessingException {
        System.out.println("---------------------------------------------");
        System.out.println(appModule);
        IAppModuleService proxy = ApiClient.createProxy(IAppModuleService.class);
        OperateResult result = proxy.save(appModule);
        OperateStatus operateStatus = new OperateStatus(result.successful(), result.getMessage());
        String updateSuccess = JsonUtil.serialize(operateStatus);
        return updateSuccess;
    }
}
