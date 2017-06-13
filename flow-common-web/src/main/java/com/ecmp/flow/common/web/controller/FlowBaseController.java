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
import com.ecmp.flow.vo.*;
import com.ecmp.vo.OperateResult;
import com.ecmp.vo.OperateResultWithData;
import com.fasterxml.jackson.core.JsonProcessingException;
import net.sf.json.JSONArray;
import org.apache.commons.lang.StringUtils;
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
 * <p>
 * <br>
 * ------------------------------------------------------------------------------------------------
 * <br>
 * 1.0.00      2017/5/26 9:32      谭军（tanjun）                    流程Conral抽象类
 * <br>
 * *************************************************************************************************<br>
 */
public abstract class FlowBaseController<T extends IBaseService, V extends AbstractBusinessModel> {


    protected Class<T> apiClass;

    public FlowBaseController() {

    }

    public FlowBaseController(Class<T> apiClass) {
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
        IBaseService baseService = ApiClient.createProxy(apiClass);
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
        IBaseService baseService = ApiClient.createProxy(apiClass);
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
        IBaseService baseService = ApiClient.createProxy(apiClass);
        defaultBusinessModel.setFlowStatus(FlowStatus.INIT);
        OperateResultWithData<V> result = baseService.save(defaultBusinessModel);
        OperateStatus operateStatus = new OperateStatus(result.successful(), result.getMessage(), result.getData());
        return JsonUtil.serialize(operateStatus);
    }

    /**
     * 通过流程定义key启动流程
     *
     * @param businessModelCode
     * @return 操作结果
     */
    @RequestMapping(value = "startFlow")
    @ResponseBody
    public String startFlow(String businessModelCode, String businessKey,String opinion, String typeId,String taskList) {
        IBaseService baseService = ApiClient.createProxy(apiClass);
        OperateStatus operateStatus = null;
        V defaultBusinessModel = (V) baseService.findOne(businessKey);
        List<FlowTaskCompleteWebVO> flowTaskCompleteList = null;
        if (defaultBusinessModel != null) {
            defaultBusinessModel.setFlowStatus(FlowStatus.INPROCESS);
            String startUserId = "admin";
            String startUserIdContext = ContextUtil.getSessionUser().getUserId();
            if (!StringUtils.isEmpty(startUserIdContext)) {
                startUserId = startUserIdContext;
            }
            IFlowDefinationService proxy = ApiClient.createProxy(IFlowDefinationService.class);
            Map<String, Object> userMap = new HashMap<String, Object>();//UserTask_1_Normal
//            userMap.put("UserTask_1_Normal", startUserId);
            FlowStartVO flowStartVO = new FlowStartVO();
            flowStartVO.setBusinessKey(businessKey);
            flowStartVO.setBusinessModelCode(businessModelCode);
            flowStartVO.setFlowTypeId(typeId);
            if (StringUtils.isNotEmpty(taskList)) {
                JSONArray jsonArray = JSONArray.fromObject(taskList);//把String转换为json
                flowTaskCompleteList = (List<FlowTaskCompleteWebVO>) JSONArray.toCollection(jsonArray, FlowTaskCompleteWebVO.class);

                if(flowTaskCompleteList!=null && !flowTaskCompleteList.isEmpty()){
                    for (FlowTaskCompleteWebVO f : flowTaskCompleteList) {
                        String flowTaskType = f.getFlowTaskType();
                        if ("common".equalsIgnoreCase(flowTaskType)) {
                            userMap.put(f.getUserVarName(), f.getUserIds());
                        } else {
                            String[] idArray = f.getUserIds().split(",");
                            userMap.put(f.getUserVarName(), idArray);
                        }
                    }
                }
            }
            flowStartVO.setVariables(userMap);
            FlowStartResultVO flowStartResultVO = proxy.startByVO(flowStartVO);
            if (flowStartResultVO != null) {
                baseService.save(defaultBusinessModel);
                operateStatus = new OperateStatus(true, "成功");
                operateStatus.setData(flowStartResultVO);
            } else {
                new OperateStatus(false, "启动流程失败");
            }
        } else {
            operateStatus = new OperateStatus(false, "业务对象不存在");
        }
        return JsonUtil.serialize(operateStatus);
    }


    /**
     * 签收任务
     * @param taskId  任务id
     * @param userId  用户id
     * @return
     */
    @RequestMapping(value = "listFlowTask")
    @ResponseBody
    public String claimTask(String taskId, String userId){
        IFlowTaskService proxy = ApiClient.createProxy(IFlowTaskService.class);
        OperateResult result =  proxy.claim(taskId,userId);
        OperateStatus operateStatus = new OperateStatus(result.successful(), result.getMessage());
        return JsonUtil.serialize(operateStatus);
    }

    /**
     * 完成任务
     *
     * @param taskId
     * @param businessId 业务表单ID
     * @param opinion    审批意见
     * @param taskList   任务完成传输对象
     * @return 操作结果
     */
    @RequestMapping(value = "completeTask")
    @ResponseBody
    public String completeTask(String taskId, String businessId, String opinion, String taskList) {
        List<FlowTaskCompleteWebVO> flowTaskCompleteList = null;
        if (StringUtils.isNotEmpty(taskList)) {
            JSONArray jsonArray = JSONArray.fromObject(taskList);//把String转换为json
            flowTaskCompleteList = (List<FlowTaskCompleteWebVO>) JSONArray.toCollection(jsonArray, FlowTaskCompleteWebVO.class);
        }
        IBaseService baseService = ApiClient.createProxy(apiClass);
        OperateStatus operateStatus = null;
        V defaultBusinessModel = (V) baseService.findOne(businessId);
        if (defaultBusinessModel != null) {
            FlowTaskCompleteVO flowTaskCompleteVO = new FlowTaskCompleteVO();
            flowTaskCompleteVO.setTaskId(taskId);
            flowTaskCompleteVO.setOpinion(opinion);
            List<String> selectedNodeIds = new ArrayList<String>();
            Map<String, Object> v = new HashMap<String, Object>();
            if(flowTaskCompleteList!=null && !flowTaskCompleteList.isEmpty()){
                for (FlowTaskCompleteWebVO f : flowTaskCompleteList) {
                    selectedNodeIds.add(f.getNodeId());
                    String flowTaskType = f.getFlowTaskType();
                    if ("common".equalsIgnoreCase(flowTaskType)) {
                        v.put(f.getUserVarName(), f.getUserIds());
                    } else {
                        String[] idArray = f.getUserIds().split(",");
                        v.put(f.getUserVarName(), idArray);
                    }
                }
            }

            flowTaskCompleteVO.setManualSelectedNodeIds(selectedNodeIds);
            //  Map<String,Object> v = new HashMap<String,Object>();
            flowTaskCompleteVO.setVariables(v);
            IFlowTaskService proxy = ApiClient.createProxy(IFlowTaskService.class);
            OperateResultWithData operateResult = proxy.complete(flowTaskCompleteVO);
            if (FlowStatus.COMPLETED.toString().equalsIgnoreCase(operateResult.getData() + "")) {
                defaultBusinessModel.setFlowStatus(FlowStatus.COMPLETED);
                baseService.save(defaultBusinessModel);
            }
            operateStatus = new OperateStatus(true, operateResult.getMessage());
        } else {
            operateStatus = new OperateStatus(false, "业务对象不存在");
        }
        return JsonUtil.serialize(operateStatus);
    }

    /**
     * 回退（撤销）任务
     *
     * @param preTaskId 上一个任务ID
     * @param opinion   意见
     * @return 操作结果
     */
    @RequestMapping(value = "cancelTask")
    @ResponseBody
    public String rollBackTo(String preTaskId, String opinion) {
        OperateStatus operateStatus = null;
        IFlowTaskService proxy = ApiClient.createProxy(IFlowTaskService.class);
        OperateResult result = proxy.rollBackTo(preTaskId);
        operateStatus = new OperateStatus(true, result.getMessage());
        return JsonUtil.serialize(operateStatus);
    }

    /**
     * 任务驳回
     *
     * @param taskId  任务ID
     * @param opinion 意见
     * @return 操作结果
     */
    @RequestMapping(value = "rejectTask")
    @ResponseBody
    public String rejectTask(String taskId, String opinion) {
        OperateStatus operateStatus = null;
        IFlowTaskService proxy = ApiClient.createProxy(IFlowTaskService.class);
        OperateResult result = proxy.taskReject(taskId, opinion, null);
        operateStatus = new OperateStatus(true, result.getMessage());
        return JsonUtil.serialize(operateStatus);
    }


    /**
     * 获取当前审批任务的决策信息
     *
     * @param taskId
     * @return 操作结果
     */
    @RequestMapping(value = "nextNodesInfo")
    @ResponseBody
    public String nextNodesInfo(String taskId) throws NoSuchMethodException {
        OperateStatus operateStatus = null;
        IFlowTaskService proxy = ApiClient.createProxy(IFlowTaskService.class);
        List<NodeInfo> nodeInfoList = proxy.findNextNodes(taskId);
        if (nodeInfoList != null && !nodeInfoList.isEmpty()) {
            operateStatus = new OperateStatus(true, "成功");
            operateStatus.setData(nodeInfoList);
        } else {
            operateStatus = new OperateStatus(false, "不存在");
        }
        return JsonUtil.serialize(operateStatus);
    }

    /**
     * 获取下一步的节点信息任务
     *
     * @param taskId
     * @return 操作结果
     */
    @RequestMapping(value = "getSelectedNodesInfo")
    @ResponseBody
    public String getSelectedNodesInfo(String taskId, String includeNodeIdsStr) throws NoSuchMethodException {
        OperateStatus operateStatus = null;
        IFlowTaskService proxy = ApiClient.createProxy(IFlowTaskService.class);
        List<String> includeNodeIds = null;
        if (!StringUtils.isEmpty(includeNodeIdsStr)) {
            String[] includeNodeIdsStringArray = includeNodeIdsStr.split(",");
            includeNodeIds = java.util.Arrays.asList(includeNodeIdsStringArray);
        } else {
            throw new RuntimeException("至少要传入一个节点ID！");
        }
        List<NodeInfo> nodeInfoList = proxy.findNexNodesWithUserSet(taskId, includeNodeIds);
        if (nodeInfoList != null && !nodeInfoList.isEmpty()) {
            operateStatus = new OperateStatus(true, "成功");
            operateStatus.setData(nodeInfoList);
        } else {
            operateStatus = new OperateStatus(false, "不存在");
        }
        return JsonUtil.serialize(operateStatus);
    }

    /**
     * 获取下一步的节点信息任务(带用户信息)
     *
     * @param taskId
     * @return 操作结果
     */
    @RequestMapping(value = "nextNodesInfoWithUser")
    @ResponseBody
    public String nextNodesInfoWithUser(String taskId) throws NoSuchMethodException {
        OperateStatus operateStatus = null;
        IFlowTaskService proxy = ApiClient.createProxy(IFlowTaskService.class);
        List<NodeInfo> nodeInfoList = proxy.findNexNodesWithUserSet(taskId);
        if (nodeInfoList != null && !nodeInfoList.isEmpty()) {
            operateStatus = new OperateStatus(true, "成功");
            operateStatus.setData(nodeInfoList);
        } else {
            operateStatus = new OperateStatus(false, "不存在");
        }
        return JsonUtil.serialize(operateStatus);
    }

    /**
     * 获取任务抬头信息信息任务
     *
     * @param taskId
     * @return 操作结果
     */
    @RequestMapping(value = "getApprovalHeaderInfo")
    @ResponseBody
    public String getApprovalHeaderInfo(String taskId) {
        OperateStatus operateStatus = null;
        IFlowTaskService proxy = ApiClient.createProxy(IFlowTaskService.class);
        ApprovalHeaderVO approvalHeaderVO = proxy.getApprovalHeaderVO(taskId);
        if (approvalHeaderVO != null) {
            operateStatus = new OperateStatus(true, "成功");
            operateStatus.setData(approvalHeaderVO);
        } else {
            operateStatus = new OperateStatus(false, "不存在");
        }
        return JsonUtil.serialize(operateStatus);
    }

}

