package com.ecmp.flow.maindata;

import com.ecmp.config.util.ApiClient;
import com.ecmp.core.json.JsonUtil;
import com.ecmp.core.search.*;
import com.ecmp.core.vo.OperateStatus;
import com.ecmp.flow.api.IAppModuleService;
import com.ecmp.flow.entity.AppModule;
import com.ecmp.flow.entity.BusinessModel;
import com.ecmp.vo.OperateResult;
import com.ecmp.vo.OperateResultWithData;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.cxf.jaxrs.client.JAXRSClientFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.persistence.Column;
import javax.persistence.Transient;
import javax.servlet.ServletRequest;
import java.io.Serializable;
import java.util.*;

/**
 * *************************************************************************************************
 * <br>
 * 实现功能：
 * <br>
 * ------------------------------------------------------------------------------------------------
 * <br>
 * 版本          变更时间             变更人                     变更原因
 * <br>
 * ------------------------------------------------------------------------------------------------
 * <br>
 * 1.0.00      2017/4/26 9:32      詹耀(xxxlimit)                    新建
 * <br>
 * *************************************************************************************************<br>
 */
@Controller
@RequestMapping(value = "/maindata/appModule")
public class AppModuleController {

    @RequestMapping(value = "show", method = RequestMethod.GET)
    public String show() {
        return "maindata/AppModuleView";
    }

    /**
     * 查询所有业务实体
     *
     * @param request
     * @return 业务实体分页数据
     * @throws JsonProcessingException
     */
    @RequestMapping(value = "find")
    @ResponseBody
    public String find(ServletRequest request) throws JsonProcessingException {
        System.out.println("---------------------------------------------");
        Search search = SearchUtil.genSearch(request);
        IAppModuleService proxy = ApiClient.createProxy(IAppModuleService.class);
        PageResult<AppModule> appModulePageResult = proxy.findByPage(search);
        String appModule = JsonUtil.serialize(appModulePageResult);
        return appModule;
    }

    /**
     * 根据id删除应用模块
     * @param id
     * @return 操作结果
     * @throws JsonProcessingException
     */
    @RequestMapping(value = "delete")
    @ResponseBody
    public String delete(String id) throws JsonProcessingException {
        System.out.println("---------------------------------------------");
        System.out.println(id);
        //   System.out.print(status);
        IAppModuleService proxy = ApiClient.createProxy(IAppModuleService.class);
        OperateResult result = proxy.delete(id);
        OperateStatus operateStatus = new OperateStatus(result.successful(), result.getMessage());
        return JsonUtil.serialize(operateStatus);
    }

    /**
     * 保存应用模块
     * @param appModule
     * @return 保存后的岗位
     * @throws JsonProcessingException
     */
    @RequestMapping(value = "update")
    @ResponseBody
    public String update(AppModule appModule) throws JsonProcessingException {
        System.out.println("---------------------------------------------");
        System.out.println(appModule);
        IAppModuleService proxy = ApiClient.createProxy(IAppModuleService.class);
        OperateResultWithData<AppModule> result = proxy.save(appModule);
        OperateStatus operateStatus = new OperateStatus(result.successful(), result.getMessage(),result.getData());
        return JsonUtil.serialize(operateStatus);
    }
}
