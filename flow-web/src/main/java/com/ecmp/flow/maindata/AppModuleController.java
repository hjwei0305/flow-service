package com.ecmp.flow.maindata;

import com.ecmp.config.util.ApiClient;
import com.ecmp.core.json.JsonUtil;
import com.ecmp.core.vo.OperateStatus;
import com.ecmp.flow.api.IAppModuleService;
import com.ecmp.flow.entity.AppModule;
import com.ecmp.vo.OperateResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

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
    public String showAppModule(Model model) {
        return "maindata/AppModuleView";
    }

    @RequestMapping(value = "find")
    @ResponseBody
    public Object find() throws JsonProcessingException {
        //  System.out.println("---------------------------------------------");
        IAppModuleService proxy = ApiClient.createProxy(IAppModuleService.class);
        List<AppModule> appModuleList = proxy.findAll();
        ApiClient.createProxy(IAppModuleService.class);
        for (int i = 0; i < appModuleList.size(); i++) {
            System.out.println(appModuleList.get(i));
        }
        String appModule = JsonUtil.serialize(appModuleList);
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
