package com.ecmp.flow.controller.maindata;

import com.ecmp.annotation.IgnoreCheckAuth;
import com.ecmp.config.util.ApiClient;
import com.ecmp.context.ContextUtil;
import com.ecmp.core.search.PageResult;
import com.ecmp.core.search.Search;
import com.ecmp.core.search.SearchFilter;
import com.ecmp.core.search.SearchUtil;
import com.ecmp.core.vo.OperateStatus;
import com.ecmp.flow.api.IFlowDefVersionService;
import com.ecmp.flow.api.IFlowHistoryService;
import com.ecmp.flow.api.IFlowInstanceService;
import com.ecmp.flow.api.IFlowTypeService;
import com.ecmp.flow.entity.FlowDefVersion;
import com.ecmp.flow.entity.FlowHistory;
import com.ecmp.flow.entity.FlowInstance;
import com.ecmp.flow.entity.FlowType;
import com.ecmp.flow.vo.MyBillVO;
import com.ecmp.vo.OperateResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.ServletRequest;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * *************************************************************************************************
 * <br>
 * 实现功能：流程实例管理
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
@RequestMapping(value = "/flowInstance")
@IgnoreCheckAuth
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
    @RequestMapping(value = "listFlowInstance")
    @ResponseBody
    public PageResult<FlowInstance> listFlowInstance(ServletRequest request) throws JsonProcessingException, ParseException {
        Search search = SearchUtil.genSearch(request);
        search.addQuickSearchProperty("flowName");
        search.addQuickSearchProperty("businessId");
        search.addQuickSearchProperty("businessCode");
        search.addQuickSearchProperty("businessModelRemark");
        search.addQuickSearchProperty("creatorName");
        IFlowInstanceService proxy = ApiClient.createProxy(IFlowInstanceService.class);
        PageResult<FlowInstance> flowInstancePageResult = proxy.findByPage(search);
        return flowInstancePageResult;
    }

    /**
     * 根据id删除流程实例
     * @param id
     * @return 操作结果
     */
    @RequestMapping(value = "delete")
    @ResponseBody
    public OperateStatus delete(String id) {
        IFlowInstanceService proxy = ApiClient.createProxy(IFlowInstanceService.class);
        OperateResult result = proxy.delete(id);
        OperateStatus operateStatus = new OperateStatus(result.successful(), result.getMessage());
        return operateStatus;
    }

    /**
     * 查询流程历史
     * @param request
     * @return  流程历史分页清单
     * @throws JsonProcessingException
     * @throws ParseException
     */
    @RequestMapping(value = "listFlowHistory")
    @ResponseBody
    public PageResult<FlowHistory> listFlowHistory(ServletRequest request) throws JsonProcessingException, ParseException {
        Search search = SearchUtil.genSearch(request);
        IFlowHistoryService proxy = ApiClient.createProxy(IFlowHistoryService.class);
        PageResult<FlowHistory> flowHistoryPageResult = proxy.findByPage(search);
        return flowHistoryPageResult;
    }

    /**
     * 查询流程定义版本
     * @return 操作结果
     */
    @RequestMapping(value = "listAllFlowDefVersion")
    @ResponseBody
    public OperateStatus listAllFlowDefVersion() {
        IFlowDefVersionService proxy = ApiClient.createProxy(IFlowDefVersionService.class);
        List<FlowDefVersion> flowDefVersions = proxy.findAll();
        OperateStatus operateStatus = new OperateStatus(true, OperateStatus.COMMON_SUCCESS_MSG, flowDefVersions);
        return operateStatus;
    }


    /**
     * 获取我的单据（已办/待办）
     * @return
     */
    @RequestMapping(value = "getMyBills")
    @ResponseBody
    public PageResult<MyBillVO> getMyBills(ServletRequest request)  throws JsonProcessingException, ParseException{
        String creatorId = ContextUtil.getUserId();
        Search search = SearchUtil.genSearch(request);
        SearchFilter searchFilterCreatorId = new SearchFilter("creatorId",creatorId, SearchFilter.Operator.EQ);
        search.addFilter(searchFilterCreatorId);
        //根据业务单据名称、业务单据号、业务工作说明快速查询
        search.addQuickSearchProperty("businessName");
        search.addQuickSearchProperty("businessCode");
        search.addQuickSearchProperty("businessModelRemark");
        IFlowInstanceService proxy = ApiClient.createProxy(IFlowInstanceService.class);
        PageResult<FlowInstance> flowInstancePageResult = proxy.findByPage(search);
        List<FlowInstance>  flowInstanceList = flowInstancePageResult.getRows();
        PageResult<MyBillVO> results  = new PageResult<MyBillVO>();
        ArrayList<MyBillVO> data=new ArrayList<MyBillVO>();
        if(flowInstanceList!=null && !flowInstanceList.isEmpty()){
            List<String> flowInstanceIds = new ArrayList<String>();
            for(FlowInstance f:flowInstanceList){
                FlowInstance parent = f.getParent();
                if(parent!=null){
                    flowInstancePageResult.setRecords( flowInstancePageResult.getRecords()-1);
                    //flowInstancePageResult.setTotal( flowInstancePageResult.getRecords()-1);
                    //flowInstancePageResult.setPage(flowInstancePageResult.getPage()-1);
                    continue;
                }
                flowInstanceIds.add(f.getId());
                MyBillVO  myBillVO = new MyBillVO();
                myBillVO.setBusinessCode(f.getBusinessCode());
                myBillVO.setBusinessId(f.getBusinessId());
                myBillVO.setBusinessModelRemark(f.getBusinessModelRemark());
                myBillVO.setBusinessName(f.getBusinessName());
                myBillVO.setCreatedDate(f.getCreatedDate());
                myBillVO.setCreatorAccount(f.getCreatorAccount());
                myBillVO.setCreatorName(f.getCreatorName());
                myBillVO.setCreatorId(f.getCreatorId());
                myBillVO.setFlowName(f.getFlowName());
                String lookUrl = f.getFlowDefVersion().getFlowDefination().getFlowType().getLookUrl();
                String businessDetailServiceUrl =  f.getFlowDefVersion().getFlowDefination().getFlowType().getBusinessDetailServiceUrl();
                if(StringUtils.isEmpty(lookUrl)){
                    lookUrl = f.getFlowDefVersion().getFlowDefination().getFlowType().getBusinessModel().getLookUrl();
                }
                if(StringUtils.isEmpty(businessDetailServiceUrl)){
                    businessDetailServiceUrl = f.getFlowDefVersion().getFlowDefination().getFlowType().getBusinessModel().getBusinessDetailServiceUrl();
                }
                myBillVO.setBusinessDetailServiceUrl(businessDetailServiceUrl);
                myBillVO.setBusinessModelCode(f.getFlowDefVersion().getFlowDefination().getFlowType().getBusinessModel().getClassName());
                myBillVO.setLookUrl(lookUrl);
                myBillVO.setEndDate(f.getEndDate());
                myBillVO.setFlowInstanceId(f.getId());
                myBillVO.setWebBaseAddress(f.getWebBaseAddress());
                myBillVO.setWebBaseAddressAbsolute(f.getWebBaseAddressAbsolute());
                myBillVO.setApiBaseAddress(f.getApiBaseAddress());
                myBillVO.setApiBaseAddressAbsolute(f.getApiBaseAddressAbsolute());
//                Boolean canEnd = proxy.checkCanEnd(f.getId());
//                myBillVO.setCanManuallyEnd(canEnd);
                data.add(myBillVO);
            }

            List<Boolean> canEnds = proxy.checkIdsCanEnd(flowInstanceIds);
            if(canEnds!=null && !canEnds.isEmpty()){
                for(int i=0;i<canEnds.size();i++){
                    data.get(i).setCanManuallyEnd(canEnds.get(i));
                }
            }
        }
        results.setRows(data);
        results.setRecords(flowInstancePageResult.getRecords());
        results.setPage(flowInstancePageResult.getPage());
        results.setTotal(flowInstancePageResult.getTotal());
        return results;
    }

    /**
     * 根据id终止流程实例,用于待办任务上直接终止流程实例
     * @param id
     * @return 操作结果
     */
    @RequestMapping(value = "endFlowInstance")
    @ResponseBody
    public OperateStatus endFlowInstance(String id) {
        IFlowInstanceService proxy = ApiClient.createProxy(IFlowInstanceService.class);
        OperateResult result = proxy.end(id);
        OperateStatus operateStatus = new OperateStatus(result.successful(), result.getMessage());
        return operateStatus;
    }
    /**
     * 根据id强制终止流程实例,用于待办任务上直接终止流程实例
     * @param id
     * @return 操作结果
     */
    @RequestMapping(value = "endForceFlowInstance")
    @ResponseBody
    public OperateStatus endForceFlowInstance(String id) {
        IFlowInstanceService proxy = ApiClient.createProxy(IFlowInstanceService.class);
        OperateResult result = proxy.endForce(id);
        OperateStatus operateStatus = new OperateStatus(result.successful(), result.getMessage());
        return operateStatus;
    }
    /**
     * 根据单据业务id终止流程实例，用于我的待办单据
     * @param businessId
     * @return 操作结果
     */
    @RequestMapping(value = "endFlowInstanceByBusinessId")
    @ResponseBody
    public OperateStatus endFlowInstanceByBusinessId(String businessId) {
        IFlowInstanceService proxy = ApiClient.createProxy(IFlowInstanceService.class);
        OperateResult result = proxy.endByBusinessId(businessId);
        OperateStatus operateStatus = new OperateStatus(result.successful(), result.getMessage());
        return operateStatus;
    }

    /**
     * 根据业务实体id查询流程类型
     * @param businessModelId
     * @return
     * @throws JsonProcessingException
     * @throws ParseException
     */
    @RequestMapping(value = "listFlowTypeByBusinessModelId")
    @ResponseBody
    public OperateStatus listFlowTypeByBusinessModelId(String businessModelId) throws JsonProcessingException, ParseException {
        IFlowTypeService proxy = ApiClient.createProxy(IFlowTypeService.class);
        List<FlowType> flowTypeList = proxy.findByBusinessModelId(businessModelId);
        OperateStatus operateStatus = new OperateStatus(true, OperateStatus.COMMON_SUCCESS_MSG, flowTypeList);
        return operateStatus;
    }
}
