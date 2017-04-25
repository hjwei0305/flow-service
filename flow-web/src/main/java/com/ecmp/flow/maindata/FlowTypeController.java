package com.ecmp.flow.maindata;

import com.ecmp.config.util.ApiClient;
import com.ecmp.core.json.JsonUtil;
import com.ecmp.core.search.PageResult;
import com.ecmp.core.search.Search;
import com.ecmp.core.search.SearchUtil;
import com.ecmp.core.vo.OperateStatus;
import com.ecmp.flow.api.IBusinessModelService;
import com.ecmp.flow.api.IFlowTypeService;
import com.ecmp.flow.entity.BusinessModel;
import com.ecmp.flow.entity.FlowType;
import com.ecmp.vo.OperateResult;
import com.ecmp.vo.OperateResultWithData;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.ServletRequest;
import java.util.List;

/**
 * *************************************************************************************************
 * <p/>
 * 实现功能：
 * 流程类型控制器
 * <p>
 * ------------------------------------------------------------------------------------------------
 * 版本          变更时间             变更人                     变更原因
 * ------------------------------------------------------------------------------------------------
 * 1.0.00      2017/3/30 16:10      陈飞(Vision.Mac)            新建
 * <p/>
 * *************************************************************************************************
 */
@Controller
@RequestMapping(value = "/maindata/flowType")
public class FlowTypeController {

    @RequestMapping()
    public String showFlowType() {
        return "maindata/FlowTypeView";
    }

    @RequestMapping(value = "find")
    @ResponseBody
    public String find(ServletRequest request) throws JsonProcessingException {
        System.out.println("---------------------------------------------");
        Search search = SearchUtil.genSearch(request);
        IFlowTypeService proxy = ApiClient.createProxy(IFlowTypeService.class);
        PageResult<FlowType> flowTypePageResult = proxy.findByPage(search);
        String flowType = JsonUtil.serialize(flowTypePageResult);
        return flowType;
    }


    @RequestMapping(value = "delete")
    @ResponseBody
    public String delete(String id) throws JsonProcessingException {
        System.out.println("---------------------------------------------");
        System.out.println("delete--------------" + id);
        ObjectMapper objectMapper = new ObjectMapper();
        IFlowTypeService proxy = ApiClient.createProxy(IFlowTypeService.class);
        OperateResult result = proxy.delete(id);
        OperateStatus operateStatus = new OperateStatus(result.successful(), result.getMessage());
        String delSuccess = JsonUtil.serialize(operateStatus);
        return delSuccess;
    }

    @RequestMapping(value = "findAllBusinessModelName")
    @ResponseBody
    public String findAllBusinessModelName() throws JsonProcessingException {
        System.out.println("---------------------------------------------");
        IBusinessModelService proxy = ApiClient.createProxy(IBusinessModelService.class);
        List<BusinessModel> businessModelList = proxy.findAll();
        OperateStatus operateStatus = new OperateStatus(true, "ok", businessModelList);
        String findbusinessModelName = JsonUtil.serialize(operateStatus);
        return findbusinessModelName;
    }


    @RequestMapping(value = "update")
    @ResponseBody
    public String update(FlowType flowType) throws JsonProcessingException {
        System.out.println("---------------------------------------------");
        System.out.println(flowType);

        IFlowTypeService proxy = ApiClient.createProxy(IFlowTypeService.class);
        OperateResultWithData<FlowType> result = proxy.save(flowType);
        OperateStatus operateStatus = new OperateStatus(result.successful(), result.getMessage());
        String updateSuccess = JsonUtil.serialize(operateStatus);
        return updateSuccess;
    }
}
