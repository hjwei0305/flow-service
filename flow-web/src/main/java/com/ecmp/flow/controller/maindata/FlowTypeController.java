package com.ecmp.flow.controller.maindata;

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
@RequestMapping(value = "/flowType")
public class FlowTypeController {

    @RequestMapping(value = "show", method = RequestMethod.GET)
    public String show() {
        return "maindata/FlowTypeView";
    }

    /**
     * 查询流程类型
     * @param request
     * @return 流程类型清单
     * @throws ParseException
     */
    @RequestMapping(value = "listFlowType")
    @ResponseBody
    public String listFlowType(ServletRequest request) throws  ParseException {
        Search search = SearchUtil.genSearch(request);
        IFlowTypeService proxy = ApiClient.createProxy(IFlowTypeService.class);
        PageResult<FlowType> flowTypePageResult = proxy.findByPage(search);
        return JsonUtil.serialize(flowTypePageResult);
    }

    /**
     * 根据id删除流程类型
     * @param id
     * @return 操作结果
     */
    @RequestMapping(value = "delete")
    @ResponseBody
    public String delete(String id) {
        IFlowTypeService proxy = ApiClient.createProxy(IFlowTypeService.class);
        OperateResult result = proxy.delete(id);
        OperateStatus operateStatus = new OperateStatus(result.successful(), result.getMessage());
        return JsonUtil.serialize(operateStatus);
    }

    /**
     * 查询所有业务实体
     * @return 操作结果
     */
    @RequestMapping(value = "listAllBusinessModel")
    @ResponseBody
    public List<BusinessModel> listAllBusinessModel() {
        IBusinessModelService proxy = ApiClient.createProxy(IBusinessModelService.class);
        List<BusinessModel> businessModelList = proxy.findAll();
        OperateStatus operateStatus = new OperateStatus(true, OperateStatus.COMMON_SUCCESS_MSG, businessModelList);
        return businessModelList;
    }

    /**
     * 修改流程类型
     * @param flowType
     * @return 操作结果
     */
    @RequestMapping(value = "save")
    @ResponseBody
    public String save(FlowType flowType) {
        IFlowTypeService proxy = ApiClient.createProxy(IFlowTypeService.class);
        OperateResultWithData<FlowType> result = proxy.save(flowType);
        OperateStatus operateStatus = new OperateStatus(result.successful(), result.getMessage(),result.getData());
        return JsonUtil.serialize(operateStatus);
    }
}
