package com.ecmp.flow.service;

import com.ecmp.config.util.ApiClient;
import com.ecmp.context.ContextUtil;
import com.ecmp.flow.api.IDefaultFlowBaseService;
import com.ecmp.flow.api.IFlowSolidifyExecutorService;
import com.ecmp.flow.basic.vo.Executor;
import com.ecmp.flow.constant.FlowStatus;
import com.ecmp.flow.dao.FlowInstanceDao;
import com.ecmp.flow.dao.FlowTaskDao;
import com.ecmp.flow.entity.FlowInstance;
import com.ecmp.flow.entity.FlowTask;
import com.ecmp.flow.entity.FlowType;
import com.ecmp.flow.vo.*;
import com.ecmp.log.util.LogUtil;
import com.ecmp.vo.OperateResult;
import com.ecmp.vo.OperateResultWithData;
import com.ecmp.vo.ResponseData;
import net.sf.json.JSONArray;
import org.apache.commons.lang.StringUtils;
import org.apache.http.protocol.ResponseDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.concurrent.Executors;

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
    @Autowired
    private FlowInstanceService flowInstanceService;
    @Autowired
    private FlowInstanceDao flowInstanceDao;
    @Autowired
    private FlowTaskDao flowTaskDao;
    @Autowired
    private FlowTypeService flowTypeService;

    @Override
    public  ResponseData startFlowByBusinessAndType(StartFlowBusinessAndTypeVo startParam){
        String businessModelCode = startParam.getBusinessModelCode();
        if(StringUtils.isEmpty(businessModelCode)){
             return ResponseData.operationFailure("业务实体类全路径不能为空！");
        }
        String businessKey = startParam.getBusinessKey();
        if(StringUtils.isEmpty(businessKey)){
            return ResponseData.operationFailure("业务实体ID不能为空！");
        }
        String flowTypeCode = startParam.getFlowTypeCode();
        if(StringUtils.isEmpty(flowTypeCode)){
            return ResponseData.operationFailure("流程类型代码不能为空！");
        }
        FlowType  flowType = flowTypeService.findByProperty("code",flowTypeCode);
        if(flowType==null){
           return  ResponseData.operationFailure("找不到流程类型！");
        }
        FlowStartVO startVO = new FlowStartVO();
        startVO.setBusinessModelCode(startParam.getBusinessModelCode());
        startVO.setBusinessKey(startParam.getBusinessKey());
        startVO.setFlowTypeId(flowType.getId());
        OperateResultWithData<FlowStartResultVO> flowStartTypeResult;
        //第一次调用，获取指定流程类型的第一步节点信息，以便下一次正常启动流程
        try {
            flowStartTypeResult = flowDefinationService.startByVO(startVO);
        } catch (NoSuchMethodException e) {
            LogUtil.error("获取流程类型和节点信息异常！", e);
            return ResponseData.operationFailure("获取流程类型和节点信息异常!");
        }
        FlowStartResultVO flowStartResultVO = flowStartTypeResult.getData();
        if (CollectionUtils.isEmpty(flowStartResultVO.getFlowTypeList())) {
            return ResponseData.operationFailure("业务实体没有找到合规的流程定义！");
        }
        if (CollectionUtils.isEmpty(flowStartResultVO.getNodeInfoList())) {
            return ResponseData.operationFailure("获取第一步节点信息为空！");
        }
        StartFlowTypeVO flowTypeVo = flowStartResultVO.getFlowTypeList().get(0);
        NodeInfo nodeInfo = flowStartResultVO.getNodeInfoList().get(0);
        startVO.setFlowDefKey(flowTypeVo.getFlowDefKey());
        startVO.setPoolTask(Boolean.FALSE);
        // 判断是否为工作池节点
        if (nodeInfo.getType().equalsIgnoreCase("PoolTask")) {
            startVO.setPoolTask(Boolean.TRUE);
        }
        // 确定默认的下一步执行人
        Map<String, Object> userMap = new HashMap<>();
        Map<String, List<String>> selectedNodesUserMap = new HashMap<>();
        Map<String, Object> variables = new HashMap<>();
        if (startVO.getPoolTask()) {
            userMap.put("anonymous", "anonymous");
            selectedNodesUserMap.put(nodeInfo.getId(), new ArrayList<>());
        } else {
            Set<Executor> executors = nodeInfo.getExecutorSet();
            if (!CollectionUtils.isEmpty(executors)) {
                String  uiType  =  nodeInfo.getUiType();
                List<String> userList = new ArrayList<String>();
                if(uiType.equalsIgnoreCase("checkbox")){
                    for(Executor  executor:executors){
                        userList.add(executor.getId());
                    }
                    userMap.put(nodeInfo.getUserVarName(), userList);
                }else{
                    Executor executor = executors.iterator().next();
                    String userIds =  executor.getId();
                    userList.add(userIds);
                    userMap.put(nodeInfo.getUserVarName(), userIds);
                }
                selectedNodesUserMap.put(nodeInfo.getUserVarName(), userList);
            }
        }
        startVO.setUserMap(userMap);
        variables.put("selectedNodesUserMap", selectedNodesUserMap);
        startVO.setVariables(variables);
        // 尝试启动流程
        try {
            flowStartTypeResult = flowDefinationService.startByVO(startVO);
        } catch (NoSuchMethodException e) {
            LogUtil.error("业务流程启动异常！", e);
            // 业务流程启动异常！{0}
            return ResponseData.operationFailure("10068", e.getMessage());
        }
        if (flowStartTypeResult.notSuccessful()) {
            return ResponseData.operationFailure(flowStartTypeResult.getMessage());
        }
        return ResponseData.operationSuccessWithData("启动流程成功！");
    }



    @Override
    public ResponseData startFlow(StartFlowVo startFlowVo)throws NoSuchMethodException, SecurityException {
        return  this.startFlow(startFlowVo.getBusinessModelCode(),startFlowVo.getBusinessKey(),
                startFlowVo.getOpinion(),startFlowVo.getTypeId(),startFlowVo.getFlowDefKey(),
                startFlowVo.getTaskList(),startFlowVo.getAnonymousNodeId());
    }

    @Override
    public ResponseData startFlow(String businessModelCode, String businessKey, String opinion,
                                  String typeId, String flowDefKey, String taskList, String anonymousNodeId) throws NoSuchMethodException, SecurityException {
        Map<String, Object> userMap = new HashMap<String, Object>();
        FlowStartVO flowStartVO = new FlowStartVO();
        flowStartVO.setBusinessKey(businessKey);
        flowStartVO.setBusinessModelCode(businessModelCode);
        flowStartVO.setFlowTypeId(typeId);
        flowStartVO.setFlowDefKey(flowDefKey);
        Map<String, Object> variables = new HashMap<String, Object>();
        flowStartVO.setVariables(variables);
        if (StringUtils.isNotEmpty(taskList)) {
            variables.put("additionRemark", opinion);
            if ("anonymous".equalsIgnoreCase(taskList)) {
                flowStartVO.setPoolTask(true);
                userMap.put("anonymous", "anonymous");
                Map<String, List<String>> selectedNodesUserMap = new HashMap<>();//选择的用户信息
                List<String> userList = new ArrayList<String>();
                selectedNodesUserMap.put(anonymousNodeId, userList);
                variables.put("selectedNodesUserMap", selectedNodesUserMap);
            } else {
                JSONArray jsonArray = JSONArray.fromObject(taskList);//把String转换为json
                List<FlowTaskCompleteWebVO> flowTaskCompleteList = (List<FlowTaskCompleteWebVO>) JSONArray.toCollection(jsonArray, FlowTaskCompleteWebVO.class);
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
                        if(f.getUserIds()==null){ //react的工作池任务参数不是anonymous，而是userIds为null
                            if(flowTaskType.equalsIgnoreCase("PoolTask")){
                                userMap.put("anonymous", "anonymous");
                            }
                            selectedNodesUserMap.put(f.getNodeId(), new ArrayList<>());
                        }else{
                            String userIds = f.getUserIds();
                            String[] idArray = userIds.split(",");
                            List<String> userList = Arrays.asList(idArray);
                            if ("common".equalsIgnoreCase(flowTaskType) || "approve".equalsIgnoreCase(flowTaskType)) {
                                userMap.put(f.getUserVarName(), userIds);
                            } else {
                                userMap.put(f.getUserVarName(), userList);
                            }
                            selectedNodesUserMap.put(f.getNodeId(), userList);
                        }
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
                    new Thread(new Runnable() {//检测待办是否自动执行
                        @Override
                        public void run() {
                            flowSolidifyExecutorService.selfMotionExecuteTask(businessKey);
                        }
                    }).start();
                    return  ResponseData.operationSuccessWithData(flowStartResultVO);
                } else {
                    return  ResponseData.operationFailure("启动流程失败,启动检查服务返回false!");
                }
            } else {
                return  ResponseData.operationFailure("启动流程失败");
            }
        } else {
            return  ResponseData.operationFailure(operateResultWithData.getMessage());
        }
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
    public ResponseData completeTask(CompleteTaskVo completeTaskVo)throws Exception {
        return  this.completeTask(completeTaskVo.getTaskId(),completeTaskVo.getBusinessId(),
                completeTaskVo.getOpinion(),completeTaskVo.getTaskList(),completeTaskVo.getEndEventId(),
                completeTaskVo.isManualSelected(),completeTaskVo.getApproved(),completeTaskVo.getLoadOverTime());
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
                ResponseData solidifyData = flowSolidifyExecutorService.setInstancyAndIdsByTaskList(flowTaskCompleteList, businessId);
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
                    selectedNodesMap.put(callActivityPath, f.getNodeId());
                    List<String> userVarNameList = (List) v.get(callActivityPath + "_sonProcessSelectNodeUserV");
                    if (userVarNameList != null) {
                        userVarNameList.add(f.getUserVarName());
                    } else {
                        userVarNameList = new ArrayList<>();
                        userVarNameList.add(f.getUserVarName());
                        v.put(callActivityPath + "_sonProcessSelectNodeUserV", userVarNameList);//选择的变量名,子流程存在选择了多个的情况
                    }
                    String userIds = f.getUserIds()==null?"":f.getUserIds();
                    if ("common".equalsIgnoreCase(flowTaskType) || "approve".equalsIgnoreCase(flowTaskType)) {
                        v.put(callActivityPath + "/" + f.getUserVarName(), userIds);
                    } else {
                        String[] idArray = userIds.split(",");
                        v.put(callActivityPath + "/" + f.getUserVarName(), Arrays.asList(idArray));
                    }
                    //注意：针对子流程选择的用户信息-待后续进行扩展--------------------------
                } else {
                    if(f.getUserIds()==null){
                        selectedNodesUserMap.put(f.getNodeId(), new ArrayList<>());
                    }else{
                        String userIds = f.getUserIds();
                        selectedNodesMap.put(f.getNodeId(), f.getNodeId());
                        String[] idArray = userIds.split(",");
                        userList = Arrays.asList(idArray);
                        if ("common".equalsIgnoreCase(flowTaskType) || "approve".equalsIgnoreCase(flowTaskType)) {
                            v.put(f.getUserVarName(), userIds);
                        } else if (!"poolTask".equalsIgnoreCase(flowTaskType)) {
                            v.put(f.getUserVarName(), userList);
                        }
                    }
                    selectedNodesUserMap.put(f.getNodeId(), userList);
                }
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
        OperateResultWithData<FlowStatus> operateResult = flowTaskService.complete(flowTaskCompleteVO);
        if (operateResult.successful() && StringUtils.isEmpty(endEventId)) { //处理成功并且不是结束节点调用
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
        List<NodeInfo> nodeInfoList = flowTaskService.findNextNodes(taskId);
        if (nodeInfoList != null && !nodeInfoList.isEmpty()) {
            return  ResponseData.operationSuccessWithData(nodeInfoList);
        } else {
            return  ResponseData.operationFailure("任务不存在，可能已经被处理");
        }
    }


    @Override
    public ResponseData getSelectedNodesInfo(String taskId, String approved, String includeNodeIdsStr, Boolean solidifyFlow) throws NoSuchMethodException {
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
            LogUtil.error("获取下一节点信息错误，详情请查看日志！",e);
            return ResponseData.operationFailure("获取下一节点信息错误，详情请查看日志！");
        }
        if (nodeInfoList != null && !nodeInfoList.isEmpty()) {
            if(nodeInfoList.size()==1&&"EndEvent".equalsIgnoreCase(nodeInfoList.get(0).getType())){//只存在结束节点
                return  ResponseData.operationSuccessWithData("EndEvent");
            }else if(nodeInfoList.size()==1&&"CounterSignNotEnd".equalsIgnoreCase(nodeInfoList.get(0).getType())){
                return  ResponseData.operationSuccessWithData("CounterSignNotEnd");
            }else {
                if(solidifyFlow!=null&&solidifyFlow==true){ //表示为固化流程（不返回下一步执行人信息）
                    nodeInfoList.forEach(nodeInfo->nodeInfo.setExecutorSet(null));
                }
                return  ResponseData.operationSuccessWithData(nodeInfoList);
            }
        }else if(nodeInfoList == null) {
            return ResponseData.operationFailure("任务不存在，可能已经被处理！");
        }else{
            return ResponseData.operationFailure("当前规则找不到符合条件的分支！");
        }
    }


    @Override
    public ResponseData nextNodesInfoWithUser(String taskId) throws NoSuchMethodException {
        List<NodeInfo> nodeInfoList = flowTaskService.findNexNodesWithUserSet(taskId);
        if (nodeInfoList != null && !nodeInfoList.isEmpty()) {
            return ResponseData.operationSuccessWithData(nodeInfoList);
        } else {
            return ResponseData.operationFailure("任务不存在，可能已经被处理");
        }
    }


    @Override
    public ResponseData getApprovalHeaderByInstanceId(String instanceId) {
        ApprovalHeaderVO approvalHeaderVO = flowInstanceService.getApprovalHeaderVo(instanceId);
        if (approvalHeaderVO != null) {
            return ResponseData.operationSuccessWithData(approvalHeaderVO);
        } else {
            return  ResponseData.operationFailure("任务不存在，可能已经被处理");
        }
    }

    @Override
    public ResponseData getApprovalHeaderInfo(String taskId) {
        ApprovalHeaderVO approvalHeaderVO = flowTaskService.getApprovalHeaderVO(taskId);
        if (approvalHeaderVO != null) {
            return ResponseData.operationSuccessWithData(approvalHeaderVO);
        } else {
            return ResponseData.operationFailure("任务不存在，可能已经被处理");
        }
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


    @Override
    public ResponseData getExecutorsByBusinessIdList(List<String> businessIdLists) {
        if(businessIdLists==null||businessIdLists.size()==0){
            return ResponseData.operationFailure("参数不能为空！");
        }
        Map<String,List<Executor>> map = new HashMap<>();
        businessIdLists.forEach(businessId->{
            ResponseData res =   this.getExecutorsByBusinessId(businessId);
            if(res.getSuccess()){
                map.put(businessId,(List<Executor>)res.getData());
            }else{
                map.put(businessId,null);
            }
        });
        return ResponseData.operationSuccessWithData(map);
    }

    public ResponseData getExecutorsByBusinessId(String businessId) {
        if (StringUtils.isEmpty(businessId)) {
            return ResponseData.operationFailure("参数不能为空！");
        }
        //通过业务单据id查询没有结束并且没有挂起的流程实例
        List<FlowInstance> flowInstanceList = flowInstanceDao.findNoEndByBusinessIdOrder(businessId);
        if (flowInstanceList != null && flowInstanceList.size() > 0) {
            FlowInstance instance = flowInstanceList.get(0);
            //根据流程实例id查询待办
            List<FlowTask> addList = flowTaskService.findByInstanceId(instance.getId());
            if(addList!=null&& addList.size()>0){
               List<Executor>  listExecutors = new ArrayList<Executor>();
                addList.forEach(a->{
                    Executor e = new Executor();
                    e.setId(a.getExecutorId());
                    e.setCode(a.getExecutorAccount());
                    e.setName(a.getExecutorName());
                    listExecutors.add(e);
                });
               return  ResponseData.operationSuccessWithData(listExecutors);
            }
            return ResponseData.operationFailure("未找到执行人！");
        }
        return ResponseData.operationFailure("单据未在流程中！");
    }


}
