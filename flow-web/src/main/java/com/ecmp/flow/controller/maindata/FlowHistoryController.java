package com.ecmp.flow.controller.maindata;

import com.ecmp.annotation.IgnoreCheckAuth;
import com.ecmp.config.util.ApiClient;
import com.ecmp.context.ContextUtil;
import com.ecmp.core.search.PageResult;
import com.ecmp.core.search.Search;
import com.ecmp.core.search.SearchFilter;
import com.ecmp.core.search.SearchUtil;
import com.ecmp.flow.api.IFlowHistoryService;
import com.ecmp.flow.common.web.controller.FlowBaseController;
import com.ecmp.flow.entity.FlowHistory;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.ServletRequest;
import java.text.ParseException;


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
 * 1.0.00      2017/5/3 9:32      谭军(tanjun)                    新建
 * <br>
 * *************************************************************************************************<br>
 */
@Controller
@RequestMapping(value = "/flowHistory")
@IgnoreCheckAuth
public class FlowHistoryController extends FlowBaseController{

//    @RequestMapping(value = "show", method = RequestMethod.GET)
//    public String show() {
//        return "maindata/FlowHistoryView";
//    }

    /**
     * 查询流程任务列表
     * @param request
     * @return 流程任务清单
     * @throws JsonProcessingException
     * @throws ParseException
     */
    @RequestMapping(value = "listFlowHistory")
    @ResponseBody
    public PageResult<FlowHistory> listFlowHistory(ServletRequest request) throws JsonProcessingException, ParseException {
        Search search = SearchUtil.genSearch(request);
        String executorId = ContextUtil.getUserId();
        String modelId = request.getParameter("modelId");
         search.addFilter(new SearchFilter("executorId", executorId, SearchFilter.Operator.EQ));
        //根据业务单据名称、业务单据号、业务工作说明快速查询
        search.addQuickSearchProperty("flowName");
        search.addQuickSearchProperty("flowTaskName");
        search.addQuickSearchProperty("flowInstance.businessCode");
        search.addQuickSearchProperty("flowInstance.businessModelRemark");
        search.addQuickSearchProperty("creatorName");
        IFlowHistoryService proxy = ApiClient.createProxy(IFlowHistoryService.class);
//        PageResult<FlowHistory> flowTaskPageResult = proxy.findByPage(search);
        PageResult<FlowHistory> flowTaskPageResult = proxy.findByBusinessModelId(modelId,search);
        return flowTaskPageResult;

    }

}
