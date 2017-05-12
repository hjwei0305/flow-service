package com.ecmp.flow.controller.maindata;

import com.ecmp.config.util.ApiClient;
import com.ecmp.core.json.JsonUtil;
import com.ecmp.core.search.PageResult;
import com.ecmp.core.search.Search;
import com.ecmp.core.search.SearchUtil;
import com.ecmp.core.vo.OperateStatus;
import com.ecmp.flow.api.*;
import com.ecmp.flow.entity.*;
import com.ecmp.vo.OperateResult;
import com.ecmp.vo.OperateResultWithData;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
@RequestMapping(value = "/maindata/flowInstance")
public class FlowInstanceController {

    @RequestMapping(value = "show", method = RequestMethod.GET)
    public String show() {
        return "maindata/FlowInstanceView";
    }

    /**
     * 查询流程实例
     * @param request
     * @return 流程实例分页清单
     * @throws JsonProcessingException
     * @throws ParseException
     */
    @RequestMapping(value = "find")
    @ResponseBody
    public String find(ServletRequest request) throws JsonProcessingException, ParseException {
        Search search = SearchUtil.genSearch(request);
        IFlowInstanceService proxy = ApiClient.createProxy(IFlowInstanceService.class);
        PageResult<FlowInstance> flowInstancePageResult = proxy.findByPage(search);
        return JsonUtil.serialize(flowInstancePageResult,JsonUtil.DATE_TIME);
    }

    /**
     * 根据id删除流程实例
     * @param id
     * @return 操作结果
     */
    @RequestMapping(value = "delete")
    @ResponseBody
    public String delete(String id) {
        IFlowInstanceService proxy = ApiClient.createProxy(IFlowInstanceService.class);
        OperateResult result = proxy.delete(id);
        OperateStatus operateStatus = new OperateStatus(result.successful(), result.getMessage());
        return JsonUtil.serialize(operateStatus);
    }

    /**
     * 查询流程历史
     * @param request
     * @return  流程历史分页清单
     * @throws JsonProcessingException
     * @throws ParseException
     */
    @RequestMapping(value = "findFlowHistory")
    @ResponseBody
    public String findFlowHistory(ServletRequest request) throws JsonProcessingException, ParseException {
        Search search = SearchUtil.genSearch(request);
        IFlowHistoryService proxy = ApiClient.createProxy(IFlowHistoryService.class);
        PageResult<FlowHistory> flowHistoryPageResult = proxy.findByPage(search);
        return JsonUtil.serialize(flowHistoryPageResult,JsonUtil.DATE_TIME);
    }

    /**
     * 查询流程定义版本
     * @return 操作结果
     */
    @RequestMapping(value = "findAllFlowDefVersionName")
    @ResponseBody
    public String findAllFlowDefVersionName() {
        IFlowDefVersionService proxy = ApiClient.createProxy(IFlowDefVersionService.class);
        List<FlowDefVersion> flowDefVersions = proxy.findAll();
        OperateStatus operateStatus = new OperateStatus(true, OperateStatus.COMMON_SUCCESS_MSG, flowDefVersions);
        return JsonUtil.serialize(operateStatus);
    }

}
