package com.ecmp.flow.controller.maindata;

import com.ecmp.config.util.ApiClient;
import com.ecmp.context.ContextUtil;
import com.ecmp.core.json.JsonUtil;
import com.ecmp.core.search.PageResult;
import com.ecmp.core.search.Search;
import com.ecmp.core.search.SearchFilter;
import com.ecmp.core.search.SearchUtil;
import com.ecmp.core.vo.OperateStatus;
import com.ecmp.flow.api.IFlowTaskService;
import com.ecmp.flow.entity.FlowTask;
import com.ecmp.flow.vo.FlowTaskCompleteVO;
import com.ecmp.flow.vo.TodoBusinessSummaryVO;
import com.ecmp.vo.OperateResult;
import com.ecmp.vo.OperateResultWithData;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.ServletRequest;
import java.text.ParseException;
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
     * 查询流程任务列表
     * @param request
     * @return 流程任务列表清单
     * @throws JsonProcessingException
     * @throws ParseException
     */
    @RequestMapping(value = "listFlowTask")
    @ResponseBody
    public String listFlowTask(ServletRequest request) throws JsonProcessingException, ParseException {
        Search search = SearchUtil.genSearch(request);
        String modelId = request.getParameter("modelId");
        String executorId = ContextUtil.getUserId();
//        if("admin".equalsIgnoreCase(account)){
//            account = "666666";
//        }
        search.addFilter(new SearchFilter("executorId", executorId, SearchFilter.Operator.EQ));
        IFlowTaskService proxy = ApiClient.createProxy(IFlowTaskService.class);
        PageResult<FlowTask> flowTaskPageResult = proxy.findByBusinessModelId(modelId,search);
        return JsonUtil.serialize(flowTaskPageResult,JsonUtil.DATE_TIME);
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
    public String listFlowTaskHeader(ServletRequest request) throws JsonProcessingException, ParseException {
        OperateStatus status = OperateStatus.defaultSuccess();
        IFlowTaskService proxy = ApiClient.createProxy(IFlowTaskService.class);
        List<TodoBusinessSummaryVO>  result = proxy.findTaskSumHeader();
        status.setData(result);
        return JsonUtil.serialize(status);
    }


    /**
     * 通过流程
     * @param id
     * @return 操作结果
     */
    @RequestMapping(value = "completeTask")
    @ResponseBody
    public String completeTask(String id)  {
        IFlowTaskService proxy = ApiClient.createProxy(IFlowTaskService.class);
        Map<String,Object> variables = new HashMap<String,Object>();
        variables.put("intput","2");
        variables.put("reject",0);
        FlowTaskCompleteVO flowTaskCompleteVO = new FlowTaskCompleteVO();
        flowTaskCompleteVO.setTaskId(id);
        flowTaskCompleteVO.setVariables(variables);
        OperateResultWithData result = proxy.complete(flowTaskCompleteVO);
        OperateStatus status=new OperateStatus(result.successful(),result.getMessage());
        return JsonUtil.serialize(status);
    }

    /**
     * 驳回流程
     * @param id
     * @return 操作结果
     */
    @RequestMapping(value = "rejectTask")
    @ResponseBody
    public String rejectTask(String id)  {
        IFlowTaskService proxy = ApiClient.createProxy(IFlowTaskService.class);
        Map<String,Object> variables = new HashMap<String,Object>();
        variables.put("reject",1);

        FlowTaskCompleteVO flowTaskCompleteVO = new FlowTaskCompleteVO();
        flowTaskCompleteVO.setTaskId(id);
        flowTaskCompleteVO.setVariables(variables);
        OperateResultWithData result = proxy.complete(flowTaskCompleteVO);
        OperateStatus status=new OperateStatus(result.successful(),result.getMessage());
        return JsonUtil.serialize(status);
    }
}
