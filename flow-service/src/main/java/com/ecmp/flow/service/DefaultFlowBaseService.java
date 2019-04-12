package com.ecmp.flow.service;

import com.ecmp.config.util.ApiClient;
import com.ecmp.context.ContextUtil;
import com.ecmp.flow.api.IDefaultFlowBaseService;
import com.ecmp.flow.api.IFlowDefinationService;
import com.ecmp.flow.api.IFlowSolidifyExecutorService;
import com.ecmp.flow.api.IFlowTaskService;
import com.ecmp.flow.constant.FlowStatus;
import com.ecmp.flow.vo.*;
import com.ecmp.vo.OperateResult;
import com.ecmp.vo.OperateResultWithData;
import com.ecmp.vo.ResponseData;
import net.sf.json.JSONArray;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * *************************************************************************************************
 * <p/>
 * 实现功能：
 * 流程的默认服务类（原FlowBaseController的方法）
 * <p>
 * ------------------------------------------------------------------------------------------------
 * 版本          变更时间             变更人                     变更原因
 * ------------------------------------------------------------------------------------------------
 * 1.0.00      2018/11/30            何灿坤                      新建
 * <p/>
 * *************************************************************************************************
 */

@Service
public class DefaultFlowBaseService implements IDefaultFlowBaseService {

    @Autowired
    private FlowDefinationService flowDefinationService;
    @Autowired
    private FlowTaskService flowTaskService;
    @Autowired
    private FlowSolidifyExecutorService flowSolidifyExecutorService;


    @Override
    public ResponseData startFlowNew(StartFlowVo startFlowVo)throws NoSuchMethodException, SecurityException {
        ResponseData responseData = new ResponseData();
        List<FlowTaskCompleteWebVO> flowTaskCompleteList = null;
        Map<String, Object> userMap = new HashMap<String, Object>();//UserTask_1_Normal
        FlowStartVO flowStartVO = new FlowStartVO();
        flowStartVO.setBusinessKey(startFlowVo.getBusinessKey());
        flowStartVO.setBusinessModelCode(startFlowVo.getBusinessModelCode());
        flowStartVO.setFlowTypeId(startFlowVo.getTypeId());
        flowStartVO.setFlowDefKey(startFlowVo.getFlowDefKey());
        Map<String, Object> variables = new HashMap<String, Object>();
        flowStartVO.setVariables(variables);
        if (StringUtils.isNotEmpty(startFlowVo.getTaskList())) {
            if ("anonymous".equalsIgnoreCase(startFlowVo.getTaskList())) {
                flowStartVO.setPoolTask(true);
                userMap.put("anonymous", "anonymous");
                Map<String, List<String>> selectedNodesUserMap = new HashMap<>();//选择的用户信息
                List<String> userList = new ArrayList<String>();
                selectedNodesUserMap.put(startFlowVo.getAnonymousNodeId(), userList);
                variables.put("selectedNodesUserMap", selectedNodesUserMap);
            } else {
                JSONArray jsonArray = JSONArray.fromObject(startFlowVo.getTaskList());//把String转换为json
                flowTaskCompleteList = (List<FlowTaskCompleteWebVO>) JSONArray.toCollection(jsonArray, FlowTaskCompleteWebVO.class);
                if (flowTaskCompleteList != null && !flowTaskCompleteList.isEmpty()) {
                    //如果是固化流程的启动，设置参数里面的紧急状态和执行人列表
                    FlowTaskCompleteWebVO  firstBean = flowTaskCompleteList.get(0);
                    if (firstBean.getSolidifyFlow()!=null&&firstBean.getSolidifyFlow()==true&&StringUtils.isEmpty(firstBean.getUserIds())) {
                        IFlowSolidifyExecutorService solidifyProxy = ContextUtil.getBean(IFlowSolidifyExecutorService.class);
                        ResponseData solidifyData = solidifyProxy.setInstancyAndIdsByTaskList(flowTaskCompleteList, startFlowVo.getBusinessKey());
                        if (solidifyData.getSuccess() == false) {
                            return solidifyData;
                        }
                        flowTaskCompleteList = (List<FlowTaskCompleteWebVO>) solidifyData.getData();
                        JSONArray jsonArray2 = JSONArray.fromObject(flowTaskCompleteList.toArray());
                        flowTaskCompleteList = (List<FlowTaskCompleteWebVO>) JSONArray.toCollection(jsonArray2, FlowTaskCompleteWebVO.class);
                    }
                    Map<String, Boolean> allowChooseInstancyMap = new HashMap<>();//选择任务的紧急处理状态
                    Map<String, List<String>> selectedNodesUserMap = new HashMap<>();//选择的用户信息
                    for (FlowTaskCompleteWebVO f : flowTaskCompleteList) {
                        String flowTaskType = f.getFlowTaskType();
                        allowChooseInstancyMap.put(f.getNodeId(), f.getInstancyStatus());
                        String[] idArray = f.getUserIds().split(",");
                        if ("common".equalsIgnoreCase(flowTaskType) || "approve".equalsIgnoreCase(flowTaskType)) {
                            userMap.put(f.getUserVarName(), f.getUserIds());
                        } else {
                            userMap.put(f.getUserVarName(), idArray);
                        }
                        List<String> userList = Arrays.asList(idArray);
                        selectedNodesUserMap.put(f.getNodeId(), userList);
                    }
                    variables.put("selectedNodesUserMap", selectedNodesUserMap);
                    variables.put("allowChooseInstancyMap", allowChooseInstancyMap);
                }
            }
        }

        flowStartVO.setUserMap(userMap);
        OperateResultWithData<FlowStartResultVO> operateResultWithData = flowDefinationService.startByVO(flowStartVO);
        if (operateResultWithData.successful()) {
            FlowStartResultVO flowStartResultVO = operateResultWithData.getData();
            if (flowStartResultVO != null) {
                if (flowStartResultVO.getCheckStartResult()) {
                    if (flowStartResultVO.getFlowTypeList() == null && flowStartResultVO.getNodeInfoList() == null) {//真正启动流程
                        new Thread(new Runnable() {//异步推送待办
                            @Override
                            public void run() {
                                flowTaskService.pushTaskToBusinessModel(startFlowVo.getBusinessModelCode(), startFlowVo.getBusinessKey(),null);
                            }
                        }).start();
                    }
                    responseData.setMessage("成功");
                    responseData.setData(flowStartResultVO);

                } else {
                    responseData.setSuccess(false);
                    responseData.setMessage("启动流程失败,启动检查服务返回false!");
                }
            } else {
                responseData.setSuccess(false);
                responseData.setMessage("启动流程失败");
            }
        } else {
            responseData.setSuccess(false);
            responseData.setMessage(operateResultWithData.getMessage());
        }

        return responseData;
    }

    @Override
    public ResponseData startFlow(String businessModelCode, String businessKey, String opinion,
                                  String typeId, String flowDefKey, String taskList, String anonymousNodeId) throws NoSuchMethodException, SecurityException {
        ResponseData responseData = new ResponseData();
        List<FlowTaskCompleteWebVO> flowTaskCompleteList = null;
        IFlowDefinationService proxy = ApiClient.createProxy(IFlowDefinationService.class);
        Map<String, Object> userMap = new HashMap<String, Object>();//UserTask_1_Normal
        FlowStartVO flowStartVO = new FlowStartVO();
        flowStartVO.setBusinessKey(businessKey);
        flowStartVO.setBusinessModelCode(businessModelCode);
        flowStartVO.setFlowTypeId(typeId);
        flowStartVO.setFlowDefKey(flowDefKey);
        Map<String, Object> variables = new HashMap<String, Object>();
        flowStartVO.setVariables(variables);
        if (StringUtils.isNotEmpty(taskList)) {
            if ("anonymous".equalsIgnoreCase(taskList)) {
                flowStartVO.setPoolTask(true);
                userMap.put("anonymous", "anonymous");
                Map<String, List<String>> selectedNodesUserMap = new HashMap<>();//选择的用户信息
                List<String> userList = new ArrayList<String>();
                selectedNodesUserMap.put(anonymousNodeId, userList);
                variables.put("selectedNodesUserMap", selectedNodesUserMap);
            } else {
                JSONArray jsonArray = JSONArray.fromObject(taskList);//把String转换为json
                flowTaskCompleteList = (List<FlowTaskCompleteWebVO>) JSONArray.toCollection(jsonArray, FlowTaskCompleteWebVO.class);
                if (flowTaskCompleteList != null && !flowTaskCompleteList.isEmpty()) {
                    //如果是固化流程的启动，设置参数里面的紧急状态和执行人列表
                    FlowTaskCompleteWebVO  firstBean = flowTaskCompleteList.get(0);
                    if (firstBean.getSolidifyFlow()!=null&&firstBean.getSolidifyFlow()==true&&StringUtils.isEmpty(firstBean.getUserIds())) {
                        IFlowSolidifyExecutorService solidifyProxy = ApiClient.createProxy(IFlowSolidifyExecutorService.class);
                        ResponseData solidifyData = solidifyProxy.setInstancyAndIdsByTaskList(flowTaskCompleteList, businessKey);
                        if (solidifyData.getSuccess() == false) {
                            return solidifyData;
                        }
                        flowTaskCompleteList = (List<FlowTaskCompleteWebVO>) solidifyData.getData();
                        JSONArray jsonArray2 = JSONArray.fromObject(flowTaskCompleteList.toArray());
                        flowTaskCompleteList = (List<FlowTaskCompleteWebVO>) JSONArray.toCollection(jsonArray2, FlowTaskCompleteWebVO.class);
                    }
                    Map<String, Boolean> allowChooseInstancyMap = new HashMap<>();//选择任务的紧急处理状态
                    Map<String, List<String>> selectedNodesUserMap = new HashMap<>();//选择的用户信息
                    for (FlowTaskCompleteWebVO f : flowTaskCompleteList) {
                        String flowTaskType = f.getFlowTaskType();
                        allowChooseInstancyMap.put(f.getNodeId(), f.getInstancyStatus());
                        String[] idArray = f.getUserIds().split(",");
                        if ("common".equalsIgnoreCase(flowTaskType) || "approve".equalsIgnoreCase(flowTaskType)) {
                            userMap.put(f.getUserVarName(), f.getUserIds());
                        } else {
                            userMap.put(f.getUserVarName(), idArray);
                        }
                        List<String> userList = Arrays.asList(idArray);
                        selectedNodesUserMap.put(f.getNodeId(), userList);
                    }
                    variables.put("selectedNodesUserMap", selectedNodesUserMap);
                    variables.put("allowChooseInstancyMap", allowChooseInstancyMap);
                }
            }
        }

        flowStartVO.setUserMap(userMap);
        OperateResultWithData<FlowStartResultVO> operateResultWithData = proxy.startByVO(flowStartVO);
        if (operateResultWithData.successful()) {
            FlowStartResultVO flowStartResultVO = operateResultWithData.getData();
            if (flowStartResultVO != null) {
                if (flowStartResultVO.getCheckStartResult()) {
                    if (flowStartResultVO.getFlowTypeList() == null && flowStartResultVO.getNodeInfoList() == null) {//真正启动流程
                        new Thread(new Runnable() {//异步推送待办
                            @Override
                            public void run() {
                                flowTaskService.pushTaskToBusinessModel(businessModelCode, businessKey,null);
                            }
                        }).start();
                    }
                    responseData.setMessage("成功");
                    responseData.setData(flowStartResultVO);

                } else {
                    responseData.setSuccess(false);
                    responseData.setMessage("启动流程失败,启动检查服务返回false!");
                }
            } else {
                responseData.setSuccess(false);
                responseData.setMessage("启动流程失败");
            }
        } else {
            responseData.setSuccess(false);
            responseData.setMessage(operateResultWithData.getMessage());
        }

        return responseData;
    }


    @Override
    public ResponseData claimTask(String taskId) {
        String userId = ContextUtil.getUserId();
        OperateResult result = flowTaskService.claim(taskId, userId);
        ResponseData responseData = new ResponseData();
        responseData.setSuccess(result.successful());
        responseData.setMessage(result.getMessage());
        return responseData;
    }


    @Override
    public ResponseData completeTask(String taskId, String businessId, String opinion, String taskList, String endEventId,
                                     boolean manualSelected, String approved, Long loadOverTime) throws Exception {
        ResponseData responseData = new ResponseData();
        List<FlowTaskCompleteWebVO> flowTaskCompleteList = null;
        if (StringUtils.isNotEmpty(taskList)) {
            JSONArray jsonArray = JSONArray.fromObject(taskList);//把String转换为json
            flowTaskCompleteList = (List<FlowTaskCompleteWebVO>) JSONArray.toCollection(jsonArray, FlowTaskCompleteWebVO.class);
        }
        FlowTaskCompleteVO flowTaskCompleteVO = new FlowTaskCompleteVO();
        flowTaskCompleteVO.setTaskId(taskId);
        flowTaskCompleteVO.setOpinion(opinion);
        Map<String, String> selectedNodesMap = new HashMap<>();
        Map<String, Object> v = new HashMap<String, Object>();
        if (flowTaskCompleteList != null && !flowTaskCompleteList.isEmpty()) {

            //如果是固化流程的提交，设置参数里面的紧急状态和执行人列表
            FlowTaskCompleteWebVO  firstBean = flowTaskCompleteList.get(0);
            if (firstBean.getSolidifyFlow()!=null&&firstBean.getSolidifyFlow()==true&&StringUtils.isEmpty(firstBean.getUserIds())) {
                IFlowSolidifyExecutorService solidifyProxy = ApiClient.createProxy(IFlowSolidifyExecutorService.class);
                ResponseData solidifyData = solidifyProxy.setInstancyAndIdsByTaskList(flowTaskCompleteList, businessId);
                if (solidifyData.getSuccess() == false) {
                    return solidifyData;
                }
                flowTaskCompleteList = (List<FlowTaskCompleteWebVO>) solidifyData.getData();
                JSONArray jsonArray2 = JSONArray.fromObject(flowTaskCompleteList.toArray());
                flowTaskCompleteList = (List<FlowTaskCompleteWebVO>) JSONArray.toCollection(jsonArray2, FlowTaskCompleteWebVO.class);
                v.put("manageSolidifyFlow", true); //需要维护固化表
            }else{
                v.put("manageSolidifyFlow", false);
            }

            Map<String, Boolean> allowChooseInstancyMap = new HashMap<>();//选择任务的紧急处理状态
            Map<String, List<String>> selectedNodesUserMap = new HashMap<>();//选择的用户信息
            for (FlowTaskCompleteWebVO f : flowTaskCompleteList) {
                allowChooseInstancyMap.put(f.getNodeId(), f.getInstancyStatus());
                String flowTaskType = f.getFlowTaskType();
                String callActivityPath = f.getCallActivityPath();
                List<String> userList = new ArrayList<String>();
                if (StringUtils.isNotEmpty(callActivityPath)) {
//                        Map<String, String> callActivityPathMap = initCallActivtiy(callActivityPath,true);
                    selectedNodesMap.put(callActivityPath, f.getNodeId());
                    List<String> userVarNameList = (List) v.get(callActivityPath + "_sonProcessSelectNodeUserV");
                    if (userVarNameList != null) {
                        userVarNameList.add(f.getUserVarName());
                    } else {
                        userVarNameList = new ArrayList<>();
                        userVarNameList.add(f.getUserVarName());
                        v.put(callActivityPath + "_sonProcessSelectNodeUserV", userVarNameList);//选择的变量名,子流程存在选择了多个的情况
                    }
                    if ("common".equalsIgnoreCase(flowTaskType) || "approve".equalsIgnoreCase(flowTaskType)) {
                        v.put(callActivityPath + "/" + f.getUserVarName(), f.getUserIds());
                    } else {
                        String[] idArray = f.getUserIds().split(",");
                        v.put(callActivityPath + "/" + f.getUserVarName(), Arrays.asList(idArray));
                    }
                    //注意：针对子流程选择的用户信息-待后续进行扩展--------------------------
                } else {
                    selectedNodesMap.put(f.getNodeId(), f.getNodeId());
                    String[] idArray = f.getUserIds().split(",");
                    if ("common".equalsIgnoreCase(flowTaskType) || "approve".equalsIgnoreCase(flowTaskType)) {
                        v.put(f.getUserVarName(), f.getUserIds());
                    } else if (!"poolTask".equalsIgnoreCase(flowTaskType)) {

                        v.put(f.getUserVarName(), Arrays.asList(idArray));
                    }
                    userList = Arrays.asList(idArray);
                }
                selectedNodesUserMap.put(f.getNodeId(), userList);
            }
            v.put("allowChooseInstancyMap", allowChooseInstancyMap);
            v.put("selectedNodesUserMap", selectedNodesUserMap);
        } else {
            if (StringUtils.isNotEmpty(endEventId)) {
                selectedNodesMap.put(endEventId, endEventId);
            }
            Map<String, Boolean> allowChooseInstancyMap = new HashMap<>();//选择任务的紧急处理状态
            Map<String, List<String>> selectedNodesUserMap = new HashMap<>();//选择的用户信息
            v.put("selectedNodesUserMap", selectedNodesUserMap);
            v.put("allowChooseInstancyMap", allowChooseInstancyMap);
            v.put("manageSolidifyFlow", false); //会签未完成和结束节点不需要维护固化流程执行人列表
        }
        if (manualSelected) {
            flowTaskCompleteVO.setManualSelectedNode(selectedNodesMap);
        }
        if (loadOverTime != null) {
            v.put("loadOverTime", loadOverTime);
        }
        v.put("approved", approved);//针对会签时同意、不同意、弃权等操作
        flowTaskCompleteVO.setVariables(v);
        IFlowTaskService proxy = ApiClient.createProxy(IFlowTaskService.class);
        OperateResultWithData<FlowStatus> operateResult = proxy.complete(flowTaskCompleteVO);
        if (operateResult.successful() && StringUtils.isEmpty(endEventId)) { //处理成功并且不是结束节点调用
            new Thread(new Runnable() {//异步推送待办
                @Override
                public void run() {
                    flowTaskService.pushTaskToBusinessModel(null, businessId,taskId);
                }
            }).start();

            new Thread(new Runnable() {//检测待办是否自动执行
                @Override
                public void run() {
                    flowSolidifyExecutorService.selfMotionExecuteTask(businessId);
                }
            }).start();
        }
        responseData.setSuccess(operateResult.successful());
        responseData.setMessage(operateResult.getMessage());
        return responseData;
    }


    @Override
    public ResponseData rollBackTo(String preTaskId, String opinion) throws CloneNotSupportedException {
        ResponseData responseData = new ResponseData();
        OperateResult result = flowTaskService.rollBackTo(preTaskId, opinion);
        responseData.setSuccess(result.successful());
        responseData.setMessage(result.getMessage());
        return responseData;
    }


    @Override
    public ResponseData rejectTask(String taskId, String opinion) throws Exception {
        ResponseData responseData = new ResponseData();
        OperateResult result = flowTaskService.taskReject(taskId, opinion, null);
        responseData.setSuccess(result.successful());
        responseData.setMessage(result.getMessage());
        return responseData;
    }


    @Override
    public ResponseData nextNodesInfo(String taskId) throws NoSuchMethodException {
        ResponseData responseData = new ResponseData();
        List<NodeInfo> nodeInfoList = flowTaskService.findNextNodes(taskId);
        if (nodeInfoList != null && !nodeInfoList.isEmpty()) {
            responseData.setSuccess(true);
            responseData.setMessage("成功");
            responseData.setData(nodeInfoList);
        } else {
            responseData.setSuccess(false);
            responseData.setMessage("任务不存在，可能已经被处理");
        }
        return responseData;
    }


    @Override
    public ResponseData getSelectedNodesInfo(String taskId, String approved, String includeNodeIdsStr, Boolean solidifyFlow) throws NoSuchMethodException {
        ResponseData responseData = new ResponseData();
        List<String> includeNodeIds = null;
        if (StringUtils.isNotEmpty(includeNodeIdsStr)) {
            String[] includeNodeIdsStringArray = includeNodeIdsStr.split(",");
            includeNodeIds = Arrays.asList(includeNodeIdsStringArray);
        }
        if (StringUtils.isEmpty(approved)) {
            approved="true";
        }
        List<NodeInfo> nodeInfoList = null;
        try {
            nodeInfoList = flowTaskService.findNexNodesWithUserSet(taskId, approved, includeNodeIds);
        }catch (Exception e){
            responseData.setSuccess(false);
            responseData.setMessage("任务不存在，可能已经被处理");
            return responseData;
        }
        if (nodeInfoList != null && !nodeInfoList.isEmpty()) {
            responseData.setSuccess(true);
            responseData.setMessage("成功");
            if(nodeInfoList.size()==1&&"EndEvent".equalsIgnoreCase(nodeInfoList.get(0).getType())){//只存在结束节点
                responseData.setData("EndEvent");
            }else if(nodeInfoList.size()==1&&"CounterSignNotEnd".equalsIgnoreCase(nodeInfoList.get(0).getType())){
                responseData.setData("CounterSignNotEnd");
            }else {
                if(solidifyFlow!=null&&solidifyFlow==true){ //表示为固化流程（不返回下一步执行人信息）
                    nodeInfoList.forEach(nodeInfo->nodeInfo.setExecutorSet(null));
                }
                responseData.setData(nodeInfoList);
            }
        }else if(nodeInfoList == null) {
            responseData.setSuccess(false);
            responseData.setMessage("任务不存在，可能已经被处理");
        }else{
            responseData.setSuccess(false);
            responseData.setMessage("当前表单规则找不到符合条件的分支");
        }
        return responseData;
    }


    @Override
    public ResponseData nextNodesInfoWithUser(String taskId) throws NoSuchMethodException {
        ResponseData responseData = new ResponseData();
        List<NodeInfo> nodeInfoList = flowTaskService.findNexNodesWithUserSet(taskId);
        if (nodeInfoList != null && !nodeInfoList.isEmpty()) {
            responseData.setSuccess(true);
            responseData.setMessage("成功");
            responseData.setData(nodeInfoList);
        } else {
            responseData.setSuccess(false);
            responseData.setMessage("任务不存在，可能已经被处理");
        }
        return responseData;
    }


    @Override
    public ResponseData getApprovalHeaderInfo(String taskId) {
        ResponseData responseData = new ResponseData();
        ApprovalHeaderVO approvalHeaderVO = flowTaskService.getApprovalHeaderVO(taskId);
        if (approvalHeaderVO != null) {
            responseData.setSuccess(true);
            responseData.setMessage("成功");
            responseData.setData(approvalHeaderVO);
        } else {
            responseData.setSuccess(false);
            responseData.setMessage("任务不存在，可能已经被处理");
        }
        return responseData;
    }


    /**
     * 通过业务单据Id获取待办任务
     *
     * @param businessId 业务单据id
     * @return 待办任务集合
     */
    @Override
    public ResponseData findTasksByBusinessId(String businessId) {
        ResponseData responseData = flowTaskService.findTasksByBusinessId(businessId);
        return responseData;
    }


}
