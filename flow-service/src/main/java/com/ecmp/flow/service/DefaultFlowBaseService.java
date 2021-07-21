package com.ecmp.flow.service;

import com.ecmp.context.ContextUtil;
import com.ecmp.flow.api.IDefaultFlowBaseService;
import com.ecmp.flow.basic.vo.Executor;
import com.ecmp.flow.common.util.Constants;
import com.ecmp.flow.constant.FlowStatus;
import com.ecmp.flow.dao.*;
import com.ecmp.flow.entity.*;
import com.ecmp.flow.util.ExpressionUtil;
import com.ecmp.flow.vo.*;
import com.ecmp.log.util.LogUtil;
import com.ecmp.util.JsonUtils;
import com.ecmp.vo.OperateResult;
import com.ecmp.vo.OperateResultWithData;
import com.ecmp.vo.ResponseData;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

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
    @Autowired
    private FlowInstanceService flowInstanceService;
    @Autowired
    private FlowInstanceDao flowInstanceDao;
    @Autowired
    private FlowTypeService flowTypeService;
    @Autowired
    private FlowDefinationDao flowDefinationDao;
    @Autowired
    private FlowDefVersionDao flowDefVersionDao;
    @Autowired
    private BusinessModelDao businessModelDao;


    /**
     * 固化检查设置并启动流程
     */
    @Override
    public ResponseData solidifyCheckAndSetAndStart(SolidifyStartFlowVo solidifyStartFlowVo) throws Exception {
        String businessId = solidifyStartFlowVo.getBusinessId();
        if (StringUtils.isEmpty(businessId)) {
            return ResponseData.operationFailure("业务实体ID不能为空！");
        }
        String businessModelCode = solidifyStartFlowVo.getBusinessModelCode();
        if (StringUtils.isEmpty(businessModelCode)) {
            return ResponseData.operationFailure("业务实体类全路径不能为空！");
        }
        String flowDefinationId = solidifyStartFlowVo.getFlowDefinationId();
        if (StringUtils.isEmpty(flowDefinationId)) {
            return ResponseData.operationFailure("流程定义ID不能为空！");
        }
        FlowDefination flowDefination = flowDefinationDao.findOne(flowDefinationId);
        if (flowDefination == null) {
            return ResponseData.operationFailure("流程定义不存在！");
        }
        FlowDefVersion flowDefVersion = flowDefVersionDao.findOne(flowDefination.getLastDeloyVersionId());
        if (!flowDefVersion.getSolidifyFlow()) {
            return ResponseData.operationFailure("当前流程不是固化流程，默认启动失败！");
        }
        Map<String, SolidifyStartExecutorVo> map =
                this.checkAndgetSolidifyExecutorsInfo(flowDefVersion, businessModelCode, businessId);
        if (map.containsKey("humanIntervention")) {  //存在需要人为干涉的情况
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("humanIntervention", true);
            resultMap.put("nodeInfoList", null);
            resultMap.put("version", 6);
            return ResponseData.operationSuccessWithData(resultMap);
        }

        if (MapUtils.isNotEmpty(map)) {
            //如果不需要人工干涉，保存系统默认选择的固化执行人信息
            ResponseData responseData = this.saveAutoSolidifyExecutorInfo(map, businessModelCode, businessId);
            if (!responseData.successful()) {
                return ResponseData.operationFailure("自动保存固化执行人失败！");
            }
        }

        //自动启动固化流程（返回第一个节点执行信息）
        ResponseData responseData = this.autoStartSolidifyFlow(businessId, businessModelCode, flowDefination.getFlowType().getId(), flowDefination.getDefKey());
        if (!responseData.successful()) {
            return responseData;
        }
        if (responseData.getData() != null) {
            List<NodeInfo> nodeInfoList = (List<NodeInfo>) responseData.getData();
            //设置固化执行人信息(只是前台展示使用)
            nodeInfoList = flowSolidifyExecutorService.
                    setNodeExecutorByBusinessId(nodeInfoList, businessId);
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("humanIntervention", false);
            resultMap.put("nodeInfoList", nodeInfoList);
            resultMap.put("version", 6);
            return ResponseData.operationSuccessWithData(resultMap);
        }
        return ResponseData.operationSuccess();
    }


    /**
     * 通过参数自动启动固化流程
     */
    public ResponseData autoStartSolidifyFlow(String businessId, String businessModelCode, String typeId, String flowDefKey) throws Exception {
        if (StringUtils.isEmpty(businessId)) {
            return ResponseData.operationFailure("业务实体ID不能为空！");
        }
        if (StringUtils.isEmpty(businessModelCode)) {
            return ResponseData.operationFailure("业务实体类全路径不能为空！");
        }
        if (StringUtils.isEmpty(typeId)) {
            return ResponseData.operationFailure("流程类型ID不能为空！");
        }
        if (StringUtils.isEmpty(flowDefKey)) {
            return ResponseData.operationFailure("流程定义代码不能为空！");
        }
        StartFlowVo startFlowVo = new StartFlowVo();
        startFlowVo.setBusinessKey(businessId);
        startFlowVo.setBusinessModelCode(businessModelCode);
        startFlowVo.setFlowDefKey(flowDefKey);
        startFlowVo.setTypeId(typeId);
        List<NodeInfo> nodeList;
        ResponseData oneResInfo = this.startFlow(startFlowVo);
        if (!oneResInfo.successful()) {
            return oneResInfo;
        }
        FlowStartResultVO flowStartResultVO = (FlowStartResultVO) oneResInfo.getData();
        nodeList = flowStartResultVO.getNodeInfoList();
        List<Map<String, Object>> taskList = new ArrayList<>();
        nodeList.forEach(node -> {
            Map<String, Object> map = new HashMap<>();
            map.put("nodeId", node.getId());
            map.put("userVarName", node.getUserVarName());
            map.put("flowTaskType", node.getFlowTaskType());
            map.put("instancyStatus", false);
            map.put("userIds", "");
            map.put("solidifyFlow", true);
            taskList.add(map);
        });
        startFlowVo.setTaskList(JsonUtils.toJson(taskList));
        //真正启动
        ResponseData responseData = this.startFlow(startFlowVo);
        if (!responseData.successful()) {
            return responseData;
        }
        return ResponseData.operationSuccessWithData(nodeList);
    }


    /**
     * 报错固化执行人
     */
    public ResponseData saveAutoSolidifyExecutorInfo(Map<String, SolidifyStartExecutorVo> map, String businessModelCode, String businessId) {
        List<SolidifyStartExecutorVo> list = new ArrayList<>();
        map.keySet().forEach(a -> {
            list.add(map.get(a));
        });
        FindSolidifyExecutorVO vo = new FindSolidifyExecutorVO();
        vo.setExecutorsVos(JsonUtils.toJson(list));
        vo.setBusinessModelCode(businessModelCode);
        vo.setBusinessId(businessId);
        return flowSolidifyExecutorService.saveSolidifyInfoByExecutorVos(vo);
    }


    /**
     * 检查并得到所有满足节点执行人信息（单候选人和单签多候选人）
     */
    public Map<String, SolidifyStartExecutorVo> checkAndgetSolidifyExecutorsInfo(FlowDefVersion flowDefVersion, String businessModelCode, String businessId) {
        BusinessModel businessModel = businessModelDao.findByProperty("className", businessModelCode);
        Map<String, Object> businessV = ExpressionUtil.getPropertiesValuesMap(businessModel, businessId, true);
        String orgId = (String) businessV.get(Constants.ORG_ID);

        Map<String, SolidifyStartExecutorVo> map = new HashMap<>();

        String defJson = flowDefVersion.getDefJson();
        JSONObject defObj = JSONObject.fromObject(defJson);
        JSONObject pocessObj = JSONObject.fromObject(defObj.get("process"));
        JSONObject nodeObj = JSONObject.fromObject(pocessObj.get("nodes"));

        nodeObj.keySet().forEach(obj -> {
            JSONObject positionObj = JSONObject.fromObject(nodeObj.get(obj));
            String id = (String) positionObj.get("id");
            String nodeType = (String) positionObj.get("nodeType");
            if (id.contains("UserTask")) {
                JSONObject nodeConfigObj = JSONObject.fromObject(positionObj.get("nodeConfig"));
                List<Map<String, String>> executorList = (List<Map<String, String>>) nodeConfigObj.get("executor");
                List<RequestExecutorsVo> requestExecutorsList = new ArrayList<RequestExecutorsVo>();
                executorList.forEach(list -> {
                    RequestExecutorsVo bean = new RequestExecutorsVo();
                    String userType = list.get("userType");
                    if (!"AnyOne".equals(userType)) {
                        String ids;
                        if (userType.contains("SelfDefinition")) {
                            ids = (list.get("selfDefId") != null ? list.get("selfDefId") : list.get("selfDefOfOrgAndSelId"));
                        } else {
                            ids = list.get("ids");
                        }
                        bean.setUserType(userType);
                        bean.setIds(ids);
                        requestExecutorsList.add(bean);
                    }
                });

                ResponseData responseData = flowTaskService.getExecutorsByRequestExecutorsVoAndOrg(requestExecutorsList, businessId, orgId);
                Set<Executor> setExecutors = (Set<Executor>) responseData.getData();
                List<Executor> executors = null;
                if (setExecutors != null) {
                    executors = new ArrayList<>(setExecutors);
                }
                if (!CollectionUtils.isEmpty(executors)) {
                    if (executors.size() == 1) { //固化选人的时候，只有单个人才进行默认设置
                        SolidifyStartExecutorVo bean = new SolidifyStartExecutorVo();
                        bean.setActTaskDefKey(id);
                        bean.setExecutorIds(executors.get(0).getId());
                        bean.setNodeType(nodeType);
                        map.put(id, bean);
                    } else if (executors.size() > 1 && "SingleSign".equalsIgnoreCase(nodeType)) { //单签任务默认全选
                        String userIds = "";
                        for (int i = 0; i < executors.size(); i++) {
                            if (i == 0) {
                                userIds += executors.get(i).getId();
                            } else {
                                userIds += "," + executors.get(i).getId();
                            }
                        }
                        SolidifyStartExecutorVo bean = new SolidifyStartExecutorVo();
                        bean.setActTaskDefKey(id);
                        bean.setExecutorIds(userIds);
                        bean.setNodeType(nodeType);
                        map.put(id, bean);
                    } else {
                        //需要人工干涉
                        map.put("humanIntervention", null);
                    }
                } else {
                    //需要人工干涉
                    map.put("humanIntervention", null);
                }
            }
        });
        return map;
    }


    @Override
    public ResponseData startFlowByBusinessAndType(StartFlowBusinessAndTypeVo startParam) {
        String businessModelCode = startParam.getBusinessModelCode();
        if (StringUtils.isEmpty(businessModelCode)) {
            return ResponseData.operationFailure("业务实体类全路径不能为空！");
        }
        String businessKey = startParam.getBusinessKey();
        if (StringUtils.isEmpty(businessKey)) {
            return ResponseData.operationFailure("业务实体ID不能为空！");
        }
        String flowTypeCode = startParam.getFlowTypeCode();
        if (StringUtils.isEmpty(flowTypeCode)) {
            return ResponseData.operationFailure("流程类型代码不能为空！");
        }
        FlowType flowType = flowTypeService.findByProperty("code", flowTypeCode);
        if (flowType == null) {
            return ResponseData.operationFailure("找不到流程类型！");
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
                String uiType = nodeInfo.getUiType();
                List<String> userList = new ArrayList<>();
                if (uiType.equalsIgnoreCase("checkbox")) {
                    for (Executor executor : executors) {
                        userList.add(executor.getId());
                    }
                    userMap.put(nodeInfo.getUserVarName(), userList);
                } else {
                    Executor executor = executors.iterator().next();
                    String userIds = executor.getId();
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
    public ResponseData startFlow(StartFlowVo startFlowVo) throws NoSuchMethodException, SecurityException {
        return this.startFlow(startFlowVo.getBusinessModelCode(), startFlowVo.getBusinessKey(),
                startFlowVo.getOpinion(), startFlowVo.getTypeId(), startFlowVo.getFlowDefKey(),
                startFlowVo.getTaskList(), startFlowVo.getAnonymousNodeId());
    }

    @Override
    public ResponseData startFlow(String businessModelCode, String businessKey, String opinion,
                                  String typeId, String flowDefKey, String taskList, String anonymousNodeId) throws NoSuchMethodException, SecurityException {
        Map<String, Object> userMap = new HashMap<>();
        FlowStartVO flowStartVO = new FlowStartVO();
        flowStartVO.setBusinessKey(businessKey);
        flowStartVO.setBusinessModelCode(businessModelCode);
        flowStartVO.setFlowTypeId(typeId);
        flowStartVO.setFlowDefKey(flowDefKey);
        Map<String, Object> variables = new HashMap<>();
        flowStartVO.setVariables(variables);
        if (StringUtils.isNotEmpty(taskList)) {
            variables.put("additionRemark", opinion);
            if ("anonymous".equalsIgnoreCase(taskList)) {
                flowStartVO.setPoolTask(true);
                userMap.put("anonymous", "anonymous");
                Map<String, List<String>> selectedNodesUserMap = new HashMap<>();//选择的用户信息
                List<String> userList = new ArrayList<>();
                selectedNodesUserMap.put(anonymousNodeId, userList);
                variables.put("selectedNodesUserMap", selectedNodesUserMap);
            } else {
                JSONArray jsonArray = JSONArray.fromObject(taskList);//把String转换为json
                List<FlowTaskCompleteWebVO> flowTaskCompleteList = (List<FlowTaskCompleteWebVO>) JSONArray.toCollection(jsonArray, FlowTaskCompleteWebVO.class);
                if (!CollectionUtils.isEmpty(flowTaskCompleteList)) {
                    //如果是固化流程的启动，设置参数里面的紧急状态和执行人列表
                    FlowTaskCompleteWebVO firstBean = flowTaskCompleteList.get(0);
                    if (firstBean.getSolidifyFlow() != null && firstBean.getSolidifyFlow() && StringUtils.isEmpty(firstBean.getUserIds())) {
                        ResponseData solidifyData = flowSolidifyExecutorService.setInstancyAndIdsByTaskList(flowTaskCompleteList, businessKey);
                        if (!solidifyData.getSuccess()) {
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
                        //如果不是工作池任务，又没有选择用户的，提示错误
                        if (!"poolTask".equalsIgnoreCase(flowTaskType) && (StringUtils.isEmpty(f.getUserIds()) || "null".equalsIgnoreCase(f.getUserIds()))) {
                            return ResponseData.operationFailure("请选择下一节点用户！");
                        }
                        if (f.getUserIds() == null) { //react的工作池任务参数不是anonymous，而是userIds为null
                            if (flowTaskType.equalsIgnoreCase("PoolTask")) {
                                userMap.put("anonymous", "anonymous");
                            }
                            selectedNodesUserMap.put(f.getNodeId(), new ArrayList<>());
                        } else {
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
                    return ResponseData.operationSuccessWithData(flowStartResultVO);
                } else {
                    return ResponseData.operationFailure("启动流程失败,启动检查服务返回false!");
                }
            } else {
                return ResponseData.operationFailure("启动流程失败");
            }
        } else {
            return ResponseData.operationFailure(operateResultWithData.getMessage());
        }
    }


    @Override
    public ResponseData claimTask(String taskId) {
        String userId = ContextUtil.getUserId();
        OperateResult result = flowTaskService.claim(taskId, userId);
        if (!result.successful()) {
            return ResponseData.operationFailure(result.getMessage());
        }
        return ResponseData.operationSuccess();
    }

    @Override
    public ResponseData completeTask(CompleteTaskVo completeTaskVo) throws Exception {
        return this.completeTask(completeTaskVo.getTaskId(), completeTaskVo.getBusinessId(),
                completeTaskVo.getOpinion(), completeTaskVo.getTaskList(), completeTaskVo.getEndEventId(),
                completeTaskVo.getDisagreeReasonCode(), completeTaskVo.isManualSelected(),
                completeTaskVo.getApproved(), completeTaskVo.getLoadOverTime());
    }


    @Override
    public ResponseData completeTask(String taskId, String businessId, String opinion, String taskList, String endEventId,
                                     String disagreeReasonCode, boolean manualSelected, String approved, Long loadOverTime) throws Exception {
        List<FlowTaskCompleteWebVO> flowTaskCompleteList = null;
        if (StringUtils.isNotEmpty(taskList)) {
            JSONArray jsonArray = JSONArray.fromObject(taskList);//把String转换为json
            flowTaskCompleteList = (List<FlowTaskCompleteWebVO>) JSONArray.toCollection(jsonArray, FlowTaskCompleteWebVO.class);
        }
        FlowTaskCompleteVO flowTaskCompleteVO = new FlowTaskCompleteVO();
        flowTaskCompleteVO.setTaskId(taskId);
        if (StringUtils.isEmpty(opinion)) {
            opinion = "";
        }
        flowTaskCompleteVO.setOpinion(opinion);
        Map<String, String> selectedNodesMap = new HashMap<>();
        Map<String, Object> v = new HashMap<>();
        if (!CollectionUtils.isEmpty(flowTaskCompleteList)) {
            //如果是固化流程的提交，设置参数里面的紧急状态和执行人列表
            FlowTaskCompleteWebVO firstBean = flowTaskCompleteList.get(0);
            if (firstBean.getSolidifyFlow() != null && firstBean.getSolidifyFlow() && StringUtils.isEmpty(firstBean.getUserIds())) {
                ResponseData solidifyData = flowSolidifyExecutorService.setInstancyAndIdsByTaskList(flowTaskCompleteList, businessId);
                if (!solidifyData.getSuccess()) {
                    return solidifyData;
                }
                flowTaskCompleteList = (List<FlowTaskCompleteWebVO>) solidifyData.getData();
                JSONArray jsonArray2 = JSONArray.fromObject(flowTaskCompleteList.toArray());
                flowTaskCompleteList = (List<FlowTaskCompleteWebVO>) JSONArray.toCollection(jsonArray2, FlowTaskCompleteWebVO.class);
                v.put("manageSolidifyFlow", true); //需要维护固化表
            } else {
                v.put("manageSolidifyFlow", false);
            }

            Map<String, Boolean> allowChooseInstancyMap = new HashMap<>();//选择任务的紧急处理状态
            Map<String, List<String>> selectedNodesUserMap = new HashMap<>();//选择的用户信息
            for (FlowTaskCompleteWebVO f : flowTaskCompleteList) {
                allowChooseInstancyMap.put(f.getNodeId(), f.getInstancyStatus());
                String flowTaskType = f.getFlowTaskType();
                String callActivityPath = f.getCallActivityPath();
                List<String> userList = new ArrayList<>();
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
                    String userIds = f.getUserIds() == null ? "" : f.getUserIds();
                    if ("common".equalsIgnoreCase(flowTaskType) || "approve".equalsIgnoreCase(flowTaskType)) {
                        v.put(callActivityPath + "/" + f.getUserVarName(), userIds);
                    } else {
                        String[] idArray = userIds.split(",");
                        v.put(callActivityPath + "/" + f.getUserVarName(), Arrays.asList(idArray));
                    }
                    //注意：针对子流程选择的用户信息-待后续进行扩展--------------------------
                } else {
                    //如果不是工作池任务，又没有选择用户的，提示错误
                    if (!"poolTask".equalsIgnoreCase(flowTaskType) && (StringUtils.isEmpty(f.getUserIds()) || "null".equalsIgnoreCase(f.getUserIds()))) {
                        return ResponseData.operationFailure("请选择下一节点用户！");
                    }

                    if (f.getUserIds() == null) {
                        selectedNodesUserMap.put(f.getNodeId(), new ArrayList<>());
                    } else {
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

                //允许返回上一节点（只判断普通任务类型）
                if ("common".equalsIgnoreCase(f.getFlowTaskType()) && BooleanUtils.isTrue(f.getAllowJumpBack())) {
                    v.put("allowJumpBack", true);
                } else {
                    v.put("allowJumpBack", false);
                }
            }
            v.put("allowChooseInstancyMap", allowChooseInstancyMap);
            v.put("selectedNodesUserMap", selectedNodesUserMap);
        } else {
            if (StringUtils.isNotEmpty(endEventId) && !"false".equals(endEventId)) {
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
        if (StringUtils.isNotEmpty(disagreeReasonCode)) {
            v.put("disagreeReasonCode", disagreeReasonCode);
        }
        v.put("approved", approved);//针对会签时同意、不同意、弃权等操作
        flowTaskCompleteVO.setVariables(v);
        OperateResultWithData<FlowStatus> operateResult = flowTaskService.complete(flowTaskCompleteVO);
        if (operateResult.successful() && (StringUtils.isEmpty(endEventId) || "false".equals(endEventId))) { //处理成功并且不是结束节点调用
            new Thread(new Runnable() {//检测待办是否自动执行
                @Override
                public void run() {
                    flowSolidifyExecutorService.selfMotionExecuteTask(businessId);
                }
            }).start();
        }

        if (!operateResult.successful()) {
            return ResponseData.operationFailure(operateResult.getMessage());
        }
        return ResponseData.operationSuccess();
    }


    @Override
    public ResponseData rollBackTo(String preTaskId, String opinion) throws CloneNotSupportedException {
        OperateResult result = flowTaskService.rollBackTo(preTaskId, opinion);
        if (!result.successful()) {
            return ResponseData.operationFailure(result.getMessage());
        }
        return ResponseData.operationSuccess();
    }


    @Override
    public ResponseData rejectTask(String taskId, String opinion) throws Exception {
        OperateResult result = flowTaskService.taskReject(taskId, opinion, null);
        if (!result.successful()) {
            return ResponseData.operationFailure(result.getMessage());
        }
        return ResponseData.operationSuccess();
    }


    @Override
    public ResponseData nextNodesInfo(String taskId) throws NoSuchMethodException {
        List<NodeInfo> nodeInfoList = flowTaskService.findNextNodes(taskId);
        if (!CollectionUtils.isEmpty(nodeInfoList)) {
            return ResponseData.operationSuccessWithData(nodeInfoList);
        } else {
            return ResponseData.operationFailure("任务不存在，可能已经被处理");
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
            approved = "true";
        }
        List<NodeInfo> nodeInfoList = flowTaskService.findNexNodesWithUserSet(taskId, approved, includeNodeIds);

        if (!CollectionUtils.isEmpty(nodeInfoList)) {
            if (nodeInfoList.size() == 1 && "EndEvent".equalsIgnoreCase(nodeInfoList.get(0).getType())) {//只存在结束节点
                return ResponseData.operationSuccessWithData("EndEvent");
            } else if (nodeInfoList.size() == 1 && "CounterSignNotEnd".equalsIgnoreCase(nodeInfoList.get(0).getType())) {
                return ResponseData.operationSuccessWithData("CounterSignNotEnd");
            } else {
                return ResponseData.operationSuccessWithData(nodeInfoList);
            }
        } else if (nodeInfoList == null) {
            return ResponseData.operationFailure("任务不存在，可能已经被处理！");
        } else {
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
            return ResponseData.operationFailure("任务不存在，可能已经被处理");
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
        ResponseData responseData = flowTaskService.findTasksByBusinessId(businessId, true);
        return responseData;
    }


    @Override
    public ResponseData findTasksByBusinessIdList(List<String> businessIdLists) {
        if (businessIdLists == null || businessIdLists.size() == 0) {
            return ResponseData.operationFailure("请求参数不能为空！");
        }
        Map<String, List<FlowTask>> map = new HashMap<>();
        businessIdLists.forEach(businessId -> {
            ResponseData response = flowTaskService.findTasksByBusinessId(businessId, false);
            map.put(businessId, (List<FlowTask>) response.getData());
        });
        return ResponseData.operationSuccessWithData(map);
    }

    @Override
    public ResponseData getExecutorsByBusinessIdList(List<String> businessIdLists) {
        if (businessIdLists == null || businessIdLists.size() == 0) {
            return ResponseData.operationFailure("参数不能为空！");
        }
        Map<String, List<Executor>> map = new HashMap<>();
        businessIdLists.forEach(businessId -> {
            ResponseData res = this.getExecutorsByBusinessId(businessId);
            if (res.getSuccess()) {
                map.put(businessId, (List<Executor>) res.getData());
            } else {
                map.put(businessId, null);
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
        if (!CollectionUtils.isEmpty(flowInstanceList)) {
            FlowInstance instance = flowInstanceList.get(0);
            //根据流程实例id查询待办
            List<FlowTask> addList = flowTaskService.findByInstanceId(instance.getId());
            if (!CollectionUtils.isEmpty(addList)) {
                List<Executor> listExecutors = new ArrayList<>();
                addList.forEach(a -> {
                    Executor e = new Executor();
                    e.setId(a.getExecutorId());
                    e.setCode(a.getExecutorAccount());
                    e.setName(a.getExecutorName());
                    listExecutors.add(e);
                });
                return ResponseData.operationSuccessWithData(listExecutors);
            }
            return ResponseData.operationFailure("未找到执行人！");
        }
        return ResponseData.operationFailure("单据未在流程中！");
    }


    @Override
    public ResponseData getWhetherLastByBusinessId(String businessId) {
        if (StringUtils.isEmpty(businessId)) {
            return ResponseData.operationFailure("参数不能为空！");
        }
        //通过业务单据id查询没有结束并且没有挂起的流程实例
        List<FlowInstance> flowInstanceList = flowInstanceDao.findNoEndByBusinessIdOrder(businessId);
        if (!CollectionUtils.isEmpty(flowInstanceList)) {
            FlowInstance instance = flowInstanceList.get(0);
            //根据流程实例id查询待办（不包含虚拟任务）
            List<FlowTask> nowTaskList = flowTaskService.findByInstanceIdNoVirtual(instance.getId());
            if (!CollectionUtils.isEmpty(nowTaskList)) {
                FlowTask flowTask;
                if (nowTaskList.size() == 1) {
                    flowTask = nowTaskList.get(0);
                } else {
                    String firstKey = null;
                    for (FlowTask task : nowTaskList) {
                        if (firstKey == null) {
                            firstKey = task.getActTaskDefKey();
                        } else {
                            if (!firstKey.equals(task.getActTaskDefKey())) {
                                return OperateResultWithData.operationFailure("查询出多节点待办，不能进行判断");
                            }
                        }
                    }
                    flowTask = nowTaskList.get(0);
                }

                List<NodeInfo> nodeInfoList;
                try {
                    nodeInfoList = flowTaskService.findNexNodesWithUserSet(flowTask.getId(), "true", null);
                } catch (Exception e) {
                    LogUtil.error("查询是否为最后节点，通过任务获取下一节点信息错误！", e);
                    return OperateResultWithData.operationFailure("查询是否为最后节点，通过任务获取下一节点信息错误:" + e.getMessage());
                }

                if (!CollectionUtils.isEmpty(nodeInfoList)) {
                    if (nodeInfoList.size() == 1 && "EndEvent".equalsIgnoreCase(nodeInfoList.get(0).getType())) {//只存在结束节点
                        return OperateResultWithData.operationSuccessWithData("最后节点！");
                    } else {
                        return OperateResultWithData.operationFailure("不是最后一个节点");
                    }
                } else if (nodeInfoList == null) {
                    return OperateResultWithData.operationFailure("任务不存在，可能已经被处理！");
                } else {
                    return OperateResultWithData.operationFailure("当前规则找不到符合条件的分支！");
                }
            }
            return ResponseData.operationFailure("未找到符合条件的待办信息！");
        }
        return ResponseData.operationFailure("单据未在流程中！");
    }
}
