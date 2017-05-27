package com.ecmp.flow.common.web.controller;

import com.ecmp.config.util.ApiClient;
import com.ecmp.context.ContextUtil;
import com.ecmp.core.json.JsonUtil;
import com.ecmp.core.search.PageResult;
import com.ecmp.core.search.Search;
import com.ecmp.core.search.SearchUtil;
import com.ecmp.core.vo.OperateStatus;
import com.ecmp.flow.api.IFlowDefinationService;
import com.ecmp.flow.api.IFlowTaskService;
import com.ecmp.flow.api.common.api.IBaseService;
import com.ecmp.flow.constant.FlowStatus;
import com.ecmp.flow.entity.AbstractBusinessModel;
import com.ecmp.flow.entity.FlowInstance;
import com.ecmp.flow.vo.ApprovalHeaderVO;
import com.ecmp.flow.vo.FlowTaskCompleteVO;
import com.ecmp.flow.vo.FlowTaskCompleteWebVO;
import com.ecmp.flow.vo.NodeInfo;
import com.ecmp.vo.OperateResult;
import com.ecmp.vo.OperateResultWithData;
import org.springframework.web.bind.annotation.RequestMapping;
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
 *
 * <br>
 * ------------------------------------------------------------------------------------------------
 * <br>
 * 1.0.00      2017/5/26 9:32      谭军（tanjun）                    流程Conral抽象类
 * <br>
 * *************************************************************************************************<br>
 */
public abstract class FlowBaseController<T extends IBaseService,V extends AbstractBusinessModel> {


    protected Class<T> apiClass;
    public FlowBaseController(){

    }

    public FlowBaseController(Class<T> apiClass){
        this.apiClass = apiClass;
    }

    /**
     * 查询默认业务实体
     *
     * @param request
     * @return
     * @throws ParseException
     */
    @RequestMapping(value = "list")
    @ResponseBody
    public String list(ServletRequest request) {
        IBaseService  baseService =  ApiClient.createProxy(apiClass);
        Search search = SearchUtil.genSearch(request);
        PageResult<V> defaultBusinessModelPageResult = baseService.findByPage(search);
        return JsonUtil.serialize(defaultBusinessModelPageResult);
    }

    /**
     * 删除默认业务实体
     *
     * @param id
     * @return
     */
    @RequestMapping(value = "delete")
    @ResponseBody
    public String delete(String id) {
        IBaseService  baseService =  ApiClient.createProxy(apiClass);
        OperateResult result = baseService.delete(id);
        OperateStatus operateStatus = new OperateStatus(result.successful(), result.getMessage());
        return JsonUtil.serialize(operateStatus);
    }


    /**
     * 保存默认业务实体
     *
     * @param defaultBusinessModel
     * @return
     */
    @RequestMapping(value = "save")
    @ResponseBody
    public String save(V defaultBusinessModel) {
        IBaseService  baseService =  ApiClient.createProxy(apiClass);
        defaultBusinessModel.setFlowStatus(FlowStatus.INIT);
        OperateResultWithData<V> result = baseService.save(defaultBusinessModel);
        OperateStatus operateStatus = new OperateStatus(result.successful(), result.getMessage(), result.getData());
        return JsonUtil.serialize(operateStatus);
    }

    /**
     * 通过流程定义key启动流程
     * @param key
     * @return 操作结果
     */
    @RequestMapping(value = "startFlow")
    @ResponseBody
    public String startFlow(String key,String businessKey) {
        IBaseService  baseService =  ApiClient.createProxy(apiClass);
        OperateStatus operateStatus = null;
        V defaultBusinessModel = (V)baseService.findOne(businessKey);
        if(defaultBusinessModel != null){
            defaultBusinessModel.setFlowStatus(FlowStatus.INPROCESS);
            String startUserId = "admin";
                    String startUserIdTest = ContextUtil.getSessionUser().getUserId();
            IFlowDefinationService proxy = ApiClient.createProxy(IFlowDefinationService.class);
            Map<String,Object> variables = new HashMap<String,Object>();//UserTask_1_Normal
            variables.put("UserTask_1_Normal",startUserId);
            FlowInstance result = proxy.startByKey( key,  startUserId, businessKey,variables);
            if(result != null){
                baseService.save(defaultBusinessModel);
                operateStatus = new OperateStatus(true, "启动流程："+result.getFlowName()+",成功");
            }else{
                new OperateStatus(false, "启动流程失败");
            }
        }else {
            operateStatus =  new OperateStatus(false, "业务对象不存在");
        }
        return JsonUtil.serialize(operateStatus);
    }

    /**
     * 完成任务
     * @param taskId
     * @return 操作结果
     */
    @RequestMapping(value = "completeTask")
    @ResponseBody
    public String completeTask(String taskId, String businessId, List<FlowTaskCompleteWebVO> FlowTaskCompleteList) {
        IBaseService  baseService =  ApiClient.createProxy(apiClass);
        OperateStatus operateStatus = null;
        V defaultBusinessModel = (V)baseService.findOne(businessId);
        if(defaultBusinessModel != null){
            FlowTaskCompleteVO flowTaskCompleteVO = new FlowTaskCompleteVO();
            flowTaskCompleteVO.setTaskId(taskId);
            List<String> selectedNodeIds = new ArrayList<String>();
            Map<String,Object> v = new HashMap<String,Object>();
            for(FlowTaskCompleteWebVO f  :FlowTaskCompleteList){
                selectedNodeIds.add(f.getNodeId());
               String flowTaskType = f.getFlowTaskType();
               if("common".equalsIgnoreCase(flowTaskType)){
                   v.put(f.getUserVarName(),f.getUserIds());
               }else{
                   String[] idArray = f.getUserIds().split(",");
                   v.put(f.getUserVarName(),idArray);
               }
            }
            flowTaskCompleteVO.setManualSelectedNodeIds(selectedNodeIds);
          //  Map<String,Object> v = new HashMap<String,Object>();
            flowTaskCompleteVO.setVariables(v);
            IFlowTaskService proxy = ApiClient.createProxy(IFlowTaskService.class);
            OperateResultWithData operateResult = proxy.complete(flowTaskCompleteVO);
            if(FlowStatus.COMPLETED.toString().equalsIgnoreCase(operateResult.getData()+"")){
                defaultBusinessModel.setFlowStatus(FlowStatus.COMPLETED);
                baseService.save(defaultBusinessModel);
            }
            operateStatus = new OperateStatus(true,operateResult.getMessage());
        }else {
            operateStatus =  new OperateStatus(false, "业务对象不存在");
        }
        return JsonUtil.serialize(operateStatus);
    }

    /**
     * 获取下一步的节点信息任务
     * @param taskId
     * @return 操作结果
     */
    @RequestMapping(value = "nextNodesInfo")
    @ResponseBody
    public String nextNodesInfo(String taskId) throws NoSuchMethodException {
        OperateStatus operateStatus = null;
        IFlowTaskService proxy = ApiClient.createProxy(IFlowTaskService.class);
        List<NodeInfo> nodeInfoList = proxy.findNexNodesWithUserSet(taskId);
        if(nodeInfoList != null && !nodeInfoList.isEmpty()){
            operateStatus = new OperateStatus(true,"成功");
            operateStatus.setData(nodeInfoList);
        }else {
            operateStatus =  new OperateStatus(false, "不存在");
        }
        return JsonUtil.serialize(operateStatus);
    }

    /**
     * 获取任务抬头信息信息任务
     * @param taskId
     * @return 操作结果
     */
    @RequestMapping(value = "getApprovalHeaderInfo")
    @ResponseBody
    public String getApprovalHeaderInfo(String taskId){
        OperateStatus operateStatus = null;
        IFlowTaskService proxy = ApiClient.createProxy(IFlowTaskService.class);
        ApprovalHeaderVO approvalHeaderVO = proxy.getApprovalHeaderVO(taskId);
        if(approvalHeaderVO != null){
            operateStatus = new OperateStatus(true,"成功");
            operateStatus.setData(approvalHeaderVO);
        }else {
            operateStatus =  new OperateStatus(false, "不存在");
        }
        return JsonUtil.serialize(operateStatus);
    }

}

