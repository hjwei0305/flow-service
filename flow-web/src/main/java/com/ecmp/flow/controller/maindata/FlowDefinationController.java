package com.ecmp.flow.controller.maindata;

import com.ecmp.annotation.IgnoreCheckAuth;
import com.ecmp.flow.basic.vo.Organization;
import com.ecmp.config.util.ApiClient;
import com.ecmp.core.search.PageResult;
import com.ecmp.core.search.Search;
import com.ecmp.core.search.SearchUtil;
import com.ecmp.core.vo.OperateStatus;
import com.ecmp.flow.api.IFlowDefVersionService;
import com.ecmp.flow.api.IFlowDefinationService;
import com.ecmp.flow.api.IFlowTypeService;
import com.ecmp.flow.common.util.Constants;
import com.ecmp.flow.constant.FlowDefinationStatus;
import com.ecmp.flow.entity.FlowDefVersion;
import com.ecmp.flow.entity.FlowDefination;
import com.ecmp.flow.entity.FlowType;
import com.ecmp.vo.OperateResult;
import com.ecmp.vo.OperateResultWithData;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.ServletRequest;
import javax.ws.rs.core.GenericType;
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
@RequestMapping(value = "/flowDefination")
@IgnoreCheckAuth
public class FlowDefinationController {

    @RequestMapping(value = "show", method = RequestMethod.GET)
    public String show() {
        return "maindata/FlowDefinationView";
    }


    /**
     * 获取所有组织机构
     * @return 组织机构清单
     */
    @ResponseBody
    @RequestMapping("listAllOrgs")
    public OperateStatus listAllOrgs() {
//        IOrganizationService proxy = ApiClient.createProxy(IOrganizationService.class);
//        List<Organization> result = proxy.findOrgTreeWithoutFrozen();
        String url = Constants.BASIC_SERVICE_URL + Constants.BASIC_ORG_LISTALLORGS_URL;
        List<Organization> result = ApiClient.getEntityViaProxy(url,new GenericType<List<Organization>>() {},null);
        OperateStatus operateStatus = new OperateStatus(true, OperateStatus.COMMON_SUCCESS_MSG, result);
        return operateStatus;
    }

    /**
     * 查询流程定义
     * @param request
     * @return 流程定义清单
     * @throws ParseException
     */
    @RequestMapping(value = "listFlowDefination")
    @ResponseBody
    public PageResult<FlowDefination> listFlowDefination(ServletRequest request) throws ParseException {
        Search search = SearchUtil.genSearch(request);
        search.addQuickSearchProperty("defKey");
        search.addQuickSearchProperty("name");
        IFlowDefinationService proxy = ApiClient.createProxy(IFlowDefinationService.class);
        PageResult<FlowDefination> flowDefinationPageResult = proxy.findByPage(search);
        return flowDefinationPageResult;
    }

    /**
     * 删除流程定义
     * @param id
     * @return操作结果
     */
    @RequestMapping(value = "delete")
    @ResponseBody
    public OperateStatus delete(String id){
        IFlowDefinationService proxy = ApiClient.createProxy(IFlowDefinationService.class);
        OperateResult result = proxy.delete(id);
        OperateStatus operateStatus = new OperateStatus(result.successful(), result.getMessage());
        return operateStatus;
    }

    /**
     * 查询所有流程类型
     * @return 操作结果
     */
    @RequestMapping(value = "listAllFlowType")
    @ResponseBody
    public OperateStatus listAllFlowType() {
        IFlowTypeService proxy = ApiClient.createProxy(IFlowTypeService.class);
        List<FlowType> flowTypeList = proxy.findAll();
        OperateStatus operateStatus = new OperateStatus(true, OperateStatus.COMMON_SUCCESS_MSG, flowTypeList);
        return operateStatus;
    }

    /**
     * 修改流程定义
     * @param flowDefination
     * @return 操作结果
     */
    @RequestMapping(value = "save")
    @ResponseBody
    public OperateStatus save(FlowDefination flowDefination)  {
        IFlowDefinationService proxy = ApiClient.createProxy(IFlowDefinationService.class);
        OperateResultWithData<FlowDefination> result = proxy.save(flowDefination);
        OperateStatus operateStatus = new OperateStatus(result.successful(), result.getMessage(), result.getData());
        return operateStatus;
    }

    /**
     * 查询流程定义版本
     * @param request
     * @return 流程定义版本分页结果
     * @throws ParseException
     */
    @RequestMapping(value = "listDefVersion")
    @ResponseBody
    public PageResult listDefVersion(ServletRequest request) throws  ParseException {
        Search search = SearchUtil.genSearch(request);
        IFlowDefVersionService proxy = ApiClient.createProxy(IFlowDefVersionService.class);
        PageResult<FlowDefVersion> flowDefVersionPageResult = proxy.findByPage(search);
        return flowDefVersionPageResult;
    }

    /**
     * 修改流程定义版本
     * @param flowDefVersion
     * @return 操作结果
     */
    @RequestMapping(value = "saveDefVersion")
    @ResponseBody
    public OperateStatus saveDefVersion(FlowDefVersion flowDefVersion) {
        IFlowDefVersionService proxy = ApiClient.createProxy(IFlowDefVersionService.class);
        OperateResultWithData<FlowDefVersion> result = proxy.save(flowDefVersion);
        OperateStatus operateStatus = new OperateStatus(result.successful(), result.getMessage(), result.getData());
        return operateStatus;
    }

    /**
     * 根据id删除流程定义版本
     * @param id
     * @return 操作结果
     */
    @RequestMapping(value = "deleteDefVieson")
    @ResponseBody
    public OperateStatus deleteDefVieson(String id) {
        IFlowDefVersionService proxy = ApiClient.createProxy(IFlowDefVersionService.class);
        OperateResult result = proxy.delete(id);
        OperateStatus operateStatus = new OperateStatus(result.successful(), result.getMessage());
        return operateStatus;
    }

    /**
     * 激活或冻结流程定义
     * @param id 流程定义id
     * @param status 状态
     * @return 操作结果
     */
    @RequestMapping(value = "activateOrFreezeFlowDef")
    @ResponseBody
    public OperateStatus activateOrFreezeFlowDef(String id, FlowDefinationStatus  status) {
        IFlowDefinationService proxy = ApiClient.createProxy(IFlowDefinationService.class);
        OperateResultWithData<FlowDefination> result = proxy.changeStatus(id, status);
        OperateStatus operateStatus = new OperateStatus(result.successful(), result.getMessage());
        return operateStatus;
    }

    /**
     * 激活或冻结流程版本
     * @param id 流程定义版本id
     * @param status 状态
     * @return 操作结果
     */
    @RequestMapping(value = "activateOrFreezeFlowVer")
    @ResponseBody
    public OperateStatus activateOrFreezeFlowVer(String id, FlowDefinationStatus  status){
        IFlowDefVersionService  proxy = ApiClient.createProxy(IFlowDefVersionService .class);
        OperateResultWithData<FlowDefVersion> result = proxy.changeStatus(id, status);
        OperateStatus operateStatus = new OperateStatus(result.successful(), result.getMessage());
        return operateStatus;
    }
}
