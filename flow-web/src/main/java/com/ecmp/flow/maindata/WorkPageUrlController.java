package com.ecmp.flow.maindata;

import com.ecmp.config.util.ApiClient;
import com.ecmp.core.json.JsonUtil;
import com.ecmp.core.search.PageResult;
import com.ecmp.core.search.Search;
import com.ecmp.core.search.SearchUtil;
import com.ecmp.core.vo.OperateStatus;
import com.ecmp.flow.api.IWorkPageUrlService;
import com.ecmp.flow.entity.WorkPageUrl;
import com.ecmp.vo.OperateResult;
import com.ecmp.vo.OperateResultWithData;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.ServletRequest;
import java.text.ParseException;
import java.util.List;

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
@RequestMapping(value = "/maindata/workPageUrl")
public class WorkPageUrlController {

    @RequestMapping(value = "show", method = RequestMethod.GET)
    public String show() {
        return "maindata/WorkPageUrlView";
    }

    @RequestMapping(value = "find")
    @ResponseBody
    public String find(ServletRequest request) throws JsonProcessingException, ParseException {
        Search searchConfig = SearchUtil.genSearch(request);
        IWorkPageUrlService proxy = ApiClient.createProxy(IWorkPageUrlService.class);
        PageResult<WorkPageUrl> workPageUrlPage = proxy.findByPage(searchConfig);
        String workPageUrl = JsonUtil.serialize(workPageUrlPage);
        return workPageUrl;
    }

    @RequestMapping(value = "delete")
    @ResponseBody
    public String delete(String id) throws JsonProcessingException {
        System.out.println("---------------------------------------------");
        System.out.println(id);
        IWorkPageUrlService proxy = ApiClient.createProxy(IWorkPageUrlService.class);
        OperateResult result = proxy.delete(id);
        OperateStatus operateStatus = new OperateStatus(result.successful(), result.getMessage());
        String delSuccess = JsonUtil.serialize(operateStatus);
        return delSuccess;
    }

    @RequestMapping(value = "findAllAppModuleName")
    @ResponseBody
    public String findAllAppModuleName() throws JsonProcessingException {
        System.out.println("---------------------------------------------");
        com.ecmp.basic.api.IAppModuleService proxy = ApiClient.createProxy(com.ecmp.basic.api.IAppModuleService.class);
        List< com.ecmp.basic.entity.AppModule> appModuleList = proxy.findAll();
        OperateStatus operateStatus = new OperateStatus(true, "ok", appModuleList);
        operateStatus.setData(appModuleList);
        String findAppModuleName = JsonUtil.serialize(operateStatus);
        System.out.println(findAppModuleName);
        return findAppModuleName;
    }

    @RequestMapping(value = "update")
    @ResponseBody
    public String update(WorkPageUrl workPageUrl) throws JsonProcessingException {
        System.out.println("---------------------------------------------");
        System.out.println(workPageUrl);
        IWorkPageUrlService proxy = ApiClient.createProxy(IWorkPageUrlService.class);
        OperateResultWithData<WorkPageUrl> result = proxy.save(workPageUrl);
        OperateStatus operateStatus = new OperateStatus(result.successful(), result.getMessage());
        String updateSuccess = JsonUtil.serialize(operateStatus);
        return updateSuccess;
    }

}
