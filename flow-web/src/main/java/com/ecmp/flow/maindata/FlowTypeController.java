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
@RequestMapping(value = "/maindata/flowType")
public class FlowTypeController {

    @RequestMapping(value = "show", method = RequestMethod.GET)
    public String show() {
        return "maindata/FlowTypeView";
    }

    @RequestMapping(value = "find")
    @ResponseBody
    public String find(ServletRequest request) throws JsonProcessingException, ParseException {
        Search search = SearchUtil.genSearch(request);
        IFlowTypeService proxy = ApiClient.createProxy(IFlowTypeService.class);
        PageResult<FlowType> flowTypePageResult = proxy.findByPage(search);
        return JsonUtil.serialize(flowTypePageResult);
    }


    @RequestMapping(value = "delete")
    @ResponseBody
    public String delete(String id) throws JsonProcessingException {
        IFlowTypeService proxy = ApiClient.createProxy(IFlowTypeService.class);
        OperateResult result = proxy.delete(id);
        OperateStatus operateStatus = new OperateStatus(result.successful(), result.getMessage());
        return JsonUtil.serialize(operateStatus);
    }

    @RequestMapping(value = "findAllBusinessModelName")
    @ResponseBody
    public String findAllBusinessModelName() throws JsonProcessingException {
        IBusinessModelService proxy = ApiClient.createProxy(IBusinessModelService.class);
        List<BusinessModel> businessModelList = proxy.findAll();
        OperateStatus operateStatus = new OperateStatus(true, OperateStatus.COMMON_SUCCESS_MSG, businessModelList);
        return JsonUtil.serialize(operateStatus);
    }


    @RequestMapping(value = "update")
    @ResponseBody
    public String update(FlowType flowType) throws JsonProcessingException {
        IFlowTypeService proxy = ApiClient.createProxy(IFlowTypeService.class);
        OperateResultWithData<FlowType> result = proxy.save(flowType);
        OperateStatus operateStatus = new OperateStatus(result.successful(), result.getMessage(),result.getData());
        return JsonUtil.serialize(operateStatus);
    }
}
