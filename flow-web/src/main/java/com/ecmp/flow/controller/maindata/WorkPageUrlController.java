package com.ecmp.flow.controller.maindata;

import com.ecmp.annotation.IgnoreCheckAuth;
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
@RequestMapping(value = "/workPageUrl")
@IgnoreCheckAuth
public class WorkPageUrlController {

    @RequestMapping(value = "show", method = RequestMethod.GET)
    public String show() {
        return "maindata/WorkPageUrlView";
    }

    /**
     * 查询工作界面列表
     * @param request
     * @return 工作界面清单
     * @throws ParseException
     */
    @RequestMapping(value = "listWorkPageUrl")
    @ResponseBody
    public String listWorkPageUrl(ServletRequest request) throws ParseException {
        Search searchConfig = SearchUtil.genSearch(request);
        IWorkPageUrlService proxy = ApiClient.createProxy(IWorkPageUrlService.class);
        PageResult<WorkPageUrl> workPageUrlPage = proxy.findByPage(searchConfig);
        return JsonUtil.serialize(workPageUrlPage);
    }

    /**
     * 根据id删除工作界面
     * @param id
     * @return 操作结果
     */
    @RequestMapping(value = "delete")
    @ResponseBody
    public String delete(String id) {
        IWorkPageUrlService proxy = ApiClient.createProxy(IWorkPageUrlService.class);
        OperateResult result = proxy.delete(id);
        OperateStatus operateStatus = new OperateStatus(result.successful(), result.getMessage());
        return JsonUtil.serialize(operateStatus);
    }

    /**
     * 查询所有应用模块
     * @return 应该模块清单
     */
    @RequestMapping(value = "listAllAppModule")
    @ResponseBody
    public String listAllAppModule(){
        com.ecmp.basic.api.IAppModuleService proxy = ApiClient.createProxy(com.ecmp.basic.api.IAppModuleService.class);
        List<com.ecmp.basic.entity.AppModule> appModuleList = proxy.findAll();
        OperateStatus operateStatus = new OperateStatus(true, OperateStatus.COMMON_SUCCESS_MSG, appModuleList);
        return JsonUtil.serialize(operateStatus);
    }

    /**
     * 修改工作界面
     * @param workPageUrl
     * @return 操作结果
     */
    @RequestMapping(value = "save")
    @ResponseBody
    public String update(WorkPageUrl workPageUrl) {
        IWorkPageUrlService proxy = ApiClient.createProxy(IWorkPageUrlService.class);
        OperateResultWithData<WorkPageUrl> result = proxy.save(workPageUrl);
        OperateStatus operateStatus = new OperateStatus(result.successful(), result.getMessage());
        return JsonUtil.serialize(operateStatus);
    }

}
