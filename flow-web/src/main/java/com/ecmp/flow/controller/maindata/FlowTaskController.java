package com.ecmp.flow.controller.maindata;

import com.ecmp.annotation.IgnoreCheckAuth;
import com.ecmp.config.util.ApiClient;
import com.ecmp.core.search.PageResult;
import com.ecmp.core.search.Search;
import com.ecmp.core.search.SearchUtil;
import com.ecmp.core.vo.OperateStatus;
import com.ecmp.flow.api.IFlowTaskService;
import com.ecmp.flow.entity.FlowInstance;
import com.ecmp.flow.entity.FlowTask;
import com.ecmp.flow.vo.FlowTaskCompleteVO;
import com.ecmp.flow.vo.FlowTaskPageResultVO;
import com.ecmp.flow.vo.TodoBusinessSummaryVO;
import com.ecmp.vo.OperateResultWithData;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.ServletRequest;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


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
@RequestMapping(value = "/flowTask")
@IgnoreCheckAuth
public class FlowTaskController {


    @RequestMapping(value = "todo", method = RequestMethod.GET)
    public String todo() {
        return "task/MainPageView";
    }

    @RequestMapping(value = "show", method = RequestMethod.GET)
    public String show() {
        return "maindata/FlowTaskView";
    }


    /**
     * 查询流程任务列表,带用户所有待办总数
     * @param request
     * @return 流程任务列表清单
     * @throws JsonProcessingException
     * @throws ParseException
     */
    @RequestMapping(value = "listFlowTaskWithAllCount")
    @ResponseBody
    public FlowTaskPageResultVO<FlowTask> listFlowTaskWithAllCount(ServletRequest request) throws JsonProcessingException, ParseException {
        Search search = SearchUtil.genSearch(request);
        String modelId = request.getParameter("modelId");
        search.addQuickSearchProperty("flowName");
        search.addQuickSearchProperty("taskName");
        search.addQuickSearchProperty("flowInstance.businessCode");
        search.addQuickSearchProperty("flowInstance.businessModelRemark");
        search.addQuickSearchProperty("creatorName");
        IFlowTaskService proxy = ApiClient.createProxy(IFlowTaskService.class);
        FlowTaskPageResultVO<FlowTask> flowTaskPageResult = proxy.findByBusinessModelIdWithAllCount(modelId,search);
        return flowTaskPageResult;
    }


    /**
     * 查询流程任务列表
     * @param request
     * @return 流程任务列表清单
     * @throws JsonProcessingException
     * @throws ParseException
     */
    @RequestMapping(value = "listFlowTask")
    @ResponseBody
    public PageResult<FlowTask> listFlowTask(ServletRequest request) throws JsonProcessingException, ParseException {
        Search search = SearchUtil.genSearch(request);
        String modelId = request.getParameter("modelId");
        search.addQuickSearchProperty("flowName");
        search.addQuickSearchProperty("taskName");
        search.addQuickSearchProperty("flowInstance.businessCode");
        search.addQuickSearchProperty("flowInstance.businessModelRemark");
        search.addQuickSearchProperty("creatorName");
        IFlowTaskService proxy = ApiClient.createProxy(IFlowTaskService.class);
        PageResult<FlowTask> flowTaskPageResult = proxy.findByBusinessModelId(modelId,search);
        return flowTaskPageResult;
    }



    /**
     * 查询可以批量审批任务列表
     * @param request
     * @return 可以批量审批任务列表
     * @throws JsonProcessingException
     * @throws ParseException
     */
    @RequestMapping(value = "listCanBatchApprovalFlowTask")
    @ResponseBody
    public PageResult<FlowTask> listCanBatchApprovalFlowTask(ServletRequest request) throws JsonProcessingException, ParseException {
        Search search = SearchUtil.genSearch(request);
        String modelId = request.getParameter("modelId");
        search.addQuickSearchProperty("flowName");
        search.addQuickSearchProperty("taskName");
        search.addQuickSearchProperty("flowInstance.businessCode");
        search.addQuickSearchProperty("flowInstance.businessModelRemark");
        search.addQuickSearchProperty("creatorName");
        IFlowTaskService proxy = ApiClient.createProxy(IFlowTaskService.class);
        PageResult<FlowTask> flowTaskPageResult = proxy.findByPageCanBatchApprovalByBusinessModelId(modelId,search);
        return flowTaskPageResult;
    }



    /**
     * 查询流程待办和任务汇总列表
     * @param request
     * @return
     * @throws JsonProcessingException
     * @throws ParseException
     */
    @RequestMapping(value = "listFlowTaskHeader")
    @ResponseBody
    public OperateStatus listFlowTaskHeader(ServletRequest request) throws JsonProcessingException, ParseException {
        OperateStatus status = OperateStatus.defaultSuccess();
        IFlowTaskService proxy = ApiClient.createProxy(IFlowTaskService.class);
        List<TodoBusinessSummaryVO>  result = proxy.findTaskSumHeader();
        status.setData(result);
        return status;
    }


    /**
     * 通过流程
     * @param id
     * @return 操作结果
     */
    @RequestMapping(value = "completeTask")
    @ResponseBody
    public OperateStatus completeTask(String id)  {
        IFlowTaskService proxy = ApiClient.createProxy(IFlowTaskService.class);
        Map<String,Object> variables = new HashMap<String,Object>();
        variables.put("intput","2");
        variables.put("reject",0);
        FlowTaskCompleteVO flowTaskCompleteVO = new FlowTaskCompleteVO();
        flowTaskCompleteVO.setTaskId(id);
        flowTaskCompleteVO.setVariables(variables);
        OperateResultWithData result = proxy.complete(flowTaskCompleteVO);
        OperateStatus status=new OperateStatus(result.successful(),result.getMessage());
        return status;
    }

    /**
     * 驳回流程
     * @param id
     * @return 操作结果
     */
    @RequestMapping(value = "rejectTask")
    @ResponseBody
    public OperateStatus rejectTask(String id)  {
        IFlowTaskService proxy = ApiClient.createProxy(IFlowTaskService.class);
        Map<String,Object> variables = new HashMap<String,Object>();
        variables.put("reject",1);

        FlowTaskCompleteVO flowTaskCompleteVO = new FlowTaskCompleteVO();
        flowTaskCompleteVO.setTaskId(id);
        flowTaskCompleteVO.setVariables(variables);
        OperateResultWithData result = proxy.complete(flowTaskCompleteVO);
        OperateStatus status=new OperateStatus(result.successful(),result.getMessage());
        return status;
    }
}
