package com.ecmp.flow.controller.maindata;

import com.ecmp.config.util.ApiClient;
import com.ecmp.core.json.JsonUtil;
import com.ecmp.core.search.PageResult;
import com.ecmp.core.search.Search;
import com.ecmp.core.search.SearchUtil;
import com.ecmp.core.vo.OperateStatus;
import com.ecmp.flow.api.IFlowTaskService;
import com.ecmp.flow.entity.FlowTask;
import com.ecmp.flow.vo.FlowTaskCompleteVO;
import com.ecmp.vo.OperateResult;
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
        IFlowTaskService proxy = ApiClient.createProxy(IFlowTaskService.class);
        PageResult<FlowTask> flowTaskPageResult = proxy.findByPage(search);
        return JsonUtil.serialize(flowTaskPageResult,JsonUtil.DATE_TIME);
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
        OperateResult result = proxy.complete(flowTaskCompleteVO);
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
        OperateResult result = proxy.complete(flowTaskCompleteVO);
        OperateStatus status=new OperateStatus(result.successful(),result.getMessage());
        return JsonUtil.serialize(status);
    }
}
