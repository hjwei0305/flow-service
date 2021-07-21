package com.ecmp.flow.util;

import com.ecmp.context.ContextUtil;
import com.ecmp.core.search.PageResult;
import com.ecmp.flow.activiti.ext.PvmNodeInfo;
import com.ecmp.flow.basic.vo.Executor;
import com.ecmp.flow.basic.vo.Organization;
import com.ecmp.flow.common.util.Constants;
import com.ecmp.flow.constant.FlowDefinationStatus;
import com.ecmp.flow.constant.FlowExecuteStatus;
import com.ecmp.flow.dao.*;
import com.ecmp.flow.entity.*;
import com.ecmp.flow.service.DisagreeReasonService;
import com.ecmp.flow.service.FlowDefinationService;
import com.ecmp.flow.service.FlowTaskService;
import com.ecmp.flow.service.TaskMakeOverPowerService;
import com.ecmp.flow.vo.FlowStartVO;
import com.ecmp.flow.vo.NodeInfo;
import com.ecmp.flow.vo.bpmn.Definition;
import com.ecmp.flow.vo.bpmn.StartEvent;
import com.ecmp.log.util.LogUtil;
import com.ecmp.util.JsonUtils;
import com.ecmp.vo.OperateResult;
import com.ecmp.vo.OperateResultWithData;
import com.ecmp.vo.ResponseData;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.activiti.engine.*;
import org.activiti.engine.history.*;
import org.activiti.engine.impl.Condition;
import org.activiti.engine.impl.RepositoryServiceImpl;
import org.activiti.engine.impl.persistence.entity.*;
import org.activiti.engine.impl.pvm.PvmActivity;
import org.activiti.engine.impl.pvm.PvmTransition;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.impl.pvm.process.ProcessDefinitionImpl;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.IdentityLink;
import org.activiti.engine.task.Task;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.*;

/**
 * *************************************************************************************************
 * <p/>
 * 实现功能：待办处理工具类
 * <p>
 * ------------------------------------------------------------------------------------------------
 * 版本          变更时间             变更人                     变更原因
 * ------------------------------------------------------------------------------------------------
 * 1.0.00      2017/8/25 13:57      谭军(tanjun)                    新建
 * <p/>
 * *************************************************************************************************
 */
@Component
public class FlowTaskTool {

    @Autowired
    private FlowTaskDao flowTaskDao;

    @Autowired
    private FlowInstanceDao flowInstanceDao;

    @Autowired
    private FlowHistoryDao flowHistoryDao;

    @Autowired
    private FlowCommonUtil flowCommonUtil;

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private IdentityService identityService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private FlowDefVersionDao flowDefVersionDao;

    @Autowired
    private HistoryService historyService;

    @Autowired
    private FlowDefinationService flowDefinationService;

    @Autowired
    private WorkPageUrlDao workPageUrlDao;

    @Autowired
    private FlowTaskService flowTaskService;

    @Autowired
    private TaskMakeOverPowerService taskMakeOverPowerService;

    @Autowired
    private DisagreeReasonService disagreeReasonService;

    public FlowTaskTool() {
        System.out.println("FlowTaskTool init------------------------------------------");
    }

    /**
     * 检查是否下一节点存在网关
     *
     * @param flowTask
     * @return
     */
    public boolean checkGateway(FlowTask flowTask) {
        boolean result = false;
        Definition definition = flowCommonUtil.flowDefinition(flowTask.getFlowInstance().getFlowDefVersion());
        JSONObject currentNode = definition.getProcess().getNodes().getJSONObject(flowTask.getActTaskDefKey());
        JSONArray targetNodes = currentNode.getJSONArray("target");
        for (int i = 0; i < targetNodes.size(); i++) {
            JSONObject jsonObject = targetNodes.getJSONObject(i);
            String targetId = jsonObject.getString("targetId");
            JSONObject nextNode = definition.getProcess().getNodes().getJSONObject(targetId);
            String busType = nextNode.getString("busType");
            if ("ManualExclusiveGateway".equalsIgnoreCase(busType) ||  //人工排他网关
                    "exclusiveGateway".equalsIgnoreCase(busType) ||  //排他网关
                    "inclusiveGateway".equalsIgnoreCase(busType)  //包容网关
                    || "parallelGateWay".equalsIgnoreCase(busType)) { //并行网关
                result = true;
                break;
            }
        }
        return result;
    }

    /**
     * 检查是否下一节点存在人工排他网关
     *
     * @param flowTask
     * @return
     */
    public boolean checkManualExclusiveGateway(FlowTask flowTask) {
        boolean result = false;
        Definition definition = flowCommonUtil.flowDefinition(flowTask.getFlowInstance().getFlowDefVersion());
        JSONObject currentNode = definition.getProcess().getNodes().getJSONObject(flowTask.getActTaskDefKey());
        JSONArray targetNodes = currentNode.getJSONArray("target");
        for (int i = 0; i < targetNodes.size(); i++) {
            JSONObject jsonObject = targetNodes.getJSONObject(i);
            String targetId = jsonObject.getString("targetId");
            JSONObject nextNode = definition.getProcess().getNodes().getJSONObject(targetId);
            try {
                if ("ManualExclusiveGateway".equalsIgnoreCase(nextNode.getString("busType"))) {
                    result = true;
                    break;
                }
            } catch (Exception e) {
                LogUtil.error(e.getMessage(), e);
            }
        }
        return result;
    }

    /**
     * 检查是否下一节点存在系统排他网关/系统包容
     *
     * @param flowTask
     * @return
     */
    public boolean checkSystemExclusiveGateway(FlowTask flowTask) {
        boolean result = false;
        Definition definition = flowCommonUtil.flowDefinition(flowTask.getFlowInstance().getFlowDefVersion());
        JSONObject currentNode = definition.getProcess().getNodes().getJSONObject(flowTask.getActTaskDefKey());
        JSONArray targetNodes = currentNode.getJSONArray("target");
        for (int i = 0; i < targetNodes.size(); i++) {
            JSONObject jsonObject = targetNodes.getJSONObject(i);
            String targetId = jsonObject.getString("targetId");
            JSONObject nextNode = definition.getProcess().getNodes().getJSONObject(targetId);
            String busType = null;
            try {
                busType = nextNode.getString("busType");
            } catch (Exception e) {
            }
            if (busType != null && ("exclusiveGateway".equalsIgnoreCase(busType) || "inclusiveGateway".equalsIgnoreCase(busType))) {
                result = true;
                break;
            }
        }
        return result;
    }

    /**
     * 检查是否下一节点存在人工排他网关
     *
     * @param flowTask
     * @return
     */
    public boolean checkManualExclusiveGateway(FlowTask flowTask, String manualExclusiveGatewayId) {
        boolean result = false;
        Definition definition = flowCommonUtil.flowDefinition(flowTask.getFlowInstance().getFlowDefVersion());
        JSONObject nextNode = definition.getProcess().getNodes().getJSONObject(manualExclusiveGatewayId);
        if ("ManualExclusiveGateway".equalsIgnoreCase(nextNode.getString("busType"))) {
            result = true;
        }
        return result;
    }

    /**
     * 检查是否下一节点存在系统排他网关
     *
     * @param flowTask
     * @return
     */
    public boolean checkExclusiveGateway(FlowTask flowTask, String manualExclusiveGatewayId) {
        boolean result = false;
        Definition definition = flowCommonUtil.flowDefinition(flowTask.getFlowInstance().getFlowDefVersion());
        JSONObject nextNode = definition.getProcess().getNodes().getJSONObject(manualExclusiveGatewayId);
        if ("ExclusiveGateway".equalsIgnoreCase(nextNode.getString("busType"))) {
            result = true;
        }
        return result;
    }


    /**
     * 获取所有出口节点信息,包含网关迭代
     *
     * @return
     */
    public List<NodeInfo> selectNextAllNodesWithGateWay(FlowTask flowTask, PvmActivity currActivity, Map<String, Object> v, List<String> includeNodeIds) throws NoSuchMethodException, SecurityException {
        Definition definition = flowCommonUtil.flowDefinition(flowTask.getFlowInstance().getFlowDefVersion());
        JSONObject currentNode = definition.getProcess().getNodes().getJSONObject(flowTask.getActTaskDefKey());
        String nodeType = currentNode.get("nodeType") + "";

        Map<PvmActivity, List> nextNodes = new LinkedHashMap<>();
        initNextNodes(false, flowTask, currActivity, nextNodes, 0, nodeType, null);
        //前端需要的数据出口任务数据
        List<NodeInfo> nodeInfoList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(nextNodes)) {
            //判断网关
            Object[] nextNodesKeyArray = nextNodes.keySet().toArray();
            PvmActivity firstActivity = (PvmActivity) nextNodesKeyArray[0];
            Boolean isSizeBigTwo = nextNodes.size() > 1 ? true : false;
            String nextActivtityType = firstActivity.getProperty("type").toString();
            String uiType = "readOnly";
            if ("CounterSign".equalsIgnoreCase(nodeType)) {//如果是会签
                for (int i = 0; i < nextNodes.size(); i++) {
                    PvmActivity tempActivity = (PvmActivity) nextNodesKeyArray[i];
                    if (!CollectionUtils.isEmpty(includeNodeIds)) {
                        if (!includeNodeIds.contains(tempActivity.getId())) {
                            continue;
                        }
                    }
                    if (ifGageway(tempActivity)) {
                        List<NodeInfo> currentNodeInf = this.selectQualifiedNode(flowTask, tempActivity, v, null);
                        nodeInfoList.addAll(currentNodeInf);
                        continue;
                    }
                    NodeInfo tempNodeInfo = new NodeInfo();
                    tempNodeInfo = convertNodes(flowTask, tempNodeInfo, tempActivity);
                    tempNodeInfo.setUiType(uiType);
                    tempNodeInfo.setPreLineName(nextNodes.get(tempActivity).get(0) + "");
                    nodeInfoList.add(tempNodeInfo);
                }
            }
            if ("Approve".equalsIgnoreCase(nodeType)) {//如果是审批结点
                uiType = "radiobox";
                for (int i = 0; i < nextNodes.size(); i++) {
                    PvmActivity tempActivity = (PvmActivity) nextNodesKeyArray[i];
                    if (!CollectionUtils.isEmpty(includeNodeIds)) {
                        if (!includeNodeIds.contains(tempActivity.getId())) {
                            continue;
                        }
                    }
                    if (ifGageway(tempActivity)) {
                        List<NodeInfo> currentNodeInf = this.selectQualifiedNode(flowTask, tempActivity, v, null);
                        nodeInfoList.addAll(currentNodeInf);
                        continue;
                    }
                    NodeInfo tempNodeInfo = new NodeInfo();
                    tempNodeInfo = convertNodes(flowTask, tempNodeInfo, tempActivity);
                    tempNodeInfo.setUiType(uiType);
                    tempNodeInfo.setPreLineName(nextNodes.get(tempActivity).get(0) + "");
                    nodeInfoList.add(tempNodeInfo);
                }
            } else if ("exclusiveGateway".equalsIgnoreCase(nextActivtityType)) {// 排他网关，radiobox,有且只能选择一个
                if (this.checkManualExclusiveGateway(flowTask, firstActivity.getId())) {//如果人工网关
                    uiType = "radiobox";
                    if (isSizeBigTwo) {
                        for (int i = 1; i < nextNodes.size(); i++) {
                            PvmActivity tempActivity = (PvmActivity) nextNodesKeyArray[i];
                            if (!CollectionUtils.isEmpty(includeNodeIds)) {
                                if (!includeNodeIds.contains(tempActivity.getId())) {
                                    continue;
                                }
                            }
                            if (ifGageway(tempActivity)) {
                                List<NodeInfo> currentNodeInf = this.selectQualifiedNode(flowTask, tempActivity, v, null);
                                nodeInfoList.addAll(currentNodeInf);
                                continue;
                            }
                            NodeInfo tempNodeInfo = new NodeInfo();
                            tempNodeInfo = convertNodes(flowTask, tempNodeInfo, tempActivity);
                            String gateWayName = firstActivity.getProperty("name") + "";
                            // tempNodeInfo.setName(gateWayName +"->" + tempNodeInfo.getName());
                            tempNodeInfo.setGateWayName(gateWayName);
                            tempNodeInfo.setUiType(uiType);
                            tempNodeInfo.setPreLineName(nextNodes.get(tempActivity).get(0) + "");
                            nodeInfoList.add(tempNodeInfo);
                        }
                    }
                } else {
                    List<NodeInfo> currentNodeInf = this.selectQualifiedNode(flowTask, firstActivity, v, null);
                    nodeInfoList.addAll(currentNodeInf);
                }

            } else if ("inclusiveGateway".equalsIgnoreCase(nextActivtityType)) { // 包容网关,checkbox,至少选择一个
                if (isSizeBigTwo) {
                    if (CollectionUtils.isEmpty(includeNodeIds)) {
                        List<NodeInfo> currentNodeInf = this.selectQualifiedNode(flowTask, firstActivity, v, null);
                        nodeInfoList.addAll(currentNodeInf);
                        return nodeInfoList;
                    }
                    for (int i = 1; i < nextNodes.size(); i++) {
                        PvmActivity tempActivity = (PvmActivity) nextNodesKeyArray[i];
                        if (!CollectionUtils.isEmpty(includeNodeIds)) {
                            if (!includeNodeIds.contains(tempActivity.getId())) {
                                continue;
                            }
                        }

                        if (ifGageway(tempActivity)) {
                            List<NodeInfo> currentNodeInf = this.selectQualifiedNode(flowTask, tempActivity, v, null);
                            nodeInfoList.addAll(currentNodeInf);
                            continue;
                        }
                        NodeInfo tempNodeInfo = new NodeInfo();
                        tempNodeInfo = convertNodes(flowTask, tempNodeInfo, tempActivity);
                        tempNodeInfo.setUiType(uiType);
                        tempNodeInfo.setPreLineName(nextNodes.get(tempActivity).get(0) + "");
                        nodeInfoList.add(tempNodeInfo);
                    }
                }

            } else if ("parallelGateWay".equalsIgnoreCase(nextActivtityType)) { // 并行网关,checkbox,默认全部选中显示不能修改
                if (isSizeBigTwo) {
                    for (int i = 1; i < nextNodes.size(); i++) {
                        PvmActivity tempActivity = (PvmActivity) nextNodesKeyArray[i];
                        if (!CollectionUtils.isEmpty(includeNodeIds)) {
                            if (!includeNodeIds.contains(tempActivity.getId())) {
                                continue;
                            }
                        }
                        NodeInfo tempNodeInfo = new NodeInfo();
                        tempNodeInfo = convertNodes(flowTask, tempNodeInfo, tempActivity);
                        tempNodeInfo.setUiType(uiType);
                        tempNodeInfo.setPreLineName(nextNodes.get(tempActivity).get(0) + "");
                        nodeInfoList.add(tempNodeInfo);
                    }
                }

            } else if ("CallActivity".equalsIgnoreCase(nextActivtityType)) {
                nodeInfoList = getCallActivityNodeInfo(flowTask, firstActivity.getId(), nodeInfoList);
            } else {
                if (isSizeBigTwo) {//当下步节点大于一个时，按照并行网关处理。checkbox,默认全部选中显示不能修改
                    for (int i = 0; i < nextNodes.size(); i++) {
                        PvmActivity tempActivity = (PvmActivity) nextNodesKeyArray[i];
                        NodeInfo tempNodeInfo = new NodeInfo();
                        tempNodeInfo = convertNodes(flowTask, tempNodeInfo, tempActivity);
                        tempNodeInfo.setUiType(uiType);
                        tempNodeInfo.setPreLineName(nextNodes.get(tempActivity).get(0) + "");
                        nodeInfoList.add(tempNodeInfo);
                    }
                } else {//按照惟一分支任务处理，显示一个，只读
                    PvmActivity tempActivity = (PvmActivity) nextNodesKeyArray[0];
                    if (!CollectionUtils.isEmpty(includeNodeIds)) {
                        if (!includeNodeIds.contains(tempActivity.getId())) {
                            throw new RuntimeException("惟一分支未选中");
                        }
                    }
                    NodeInfo tempNodeInfo = new NodeInfo();
                    tempNodeInfo = convertNodes(flowTask, tempNodeInfo, tempActivity);
                    tempNodeInfo.setUiType(uiType);
                    tempNodeInfo.setPreLineName(nextNodes.get(tempActivity).get(0) + "");
                    nodeInfoList.add(tempNodeInfo);
                }
            }
        }
        return nodeInfoList;
    }

    /**
     * 获取所有出口节点信息
     *
     * @param flowTask
     * @return
     */
    public List<NodeInfo> selectNextAllNodes(FlowTask flowTask, List<String> includeNodeIds) {
        String defJson = flowTask.getTaskJsonDef();
        JSONObject defObj = JSONObject.fromObject(defJson);
        String nodeType = defObj.get("nodeType") + "";

        String actTaskDefKey = flowTask.getActTaskDefKey();
        if (actTaskDefKey.contains("-virtual")) { //虚拟待办通知专属
            actTaskDefKey = actTaskDefKey.replace("-virtual", "");
        }
        String actProcessDefinitionId = flowTask.getFlowInstance().getFlowDefVersion().getActDefId();
        ProcessDefinitionEntity definition = (ProcessDefinitionEntity) ((RepositoryServiceImpl) repositoryService)
                .getDeployedProcessDefinition(actProcessDefinitionId);

        PvmActivity currActivity = this.getActivitNode(definition, actTaskDefKey);
        //前端需要的数据出口任务数据
        List<NodeInfo> nodeInfoList = new ArrayList<NodeInfo>();
        String uiType = "readOnly";
        Boolean counterSignLastTask = false;
        if ("Approve".equalsIgnoreCase(nodeType)) {
            NodeInfo tempNodeInfo = new NodeInfo();
            tempNodeInfo.setCurrentTaskType(nodeType);
            tempNodeInfo = convertNodes(flowTask, tempNodeInfo, currActivity);
            tempNodeInfo.setUiType(uiType);
            nodeInfoList.add(tempNodeInfo);
            return nodeInfoList;
        }

        if ("CounterSign".equalsIgnoreCase(nodeType)
                || "ParallelTask".equalsIgnoreCase(nodeType)
                || "SerialTask".equalsIgnoreCase(nodeType)) {//多实例节点，直接返回当前会签节点信息

            NodeInfo tempNodeInfo = new NodeInfo();
            tempNodeInfo.setCurrentTaskType(nodeType);
            tempNodeInfo = convertNodes(flowTask, tempNodeInfo, currActivity);
            tempNodeInfo.setUiType(uiType);
            ProcessInstance instance = runtimeService.createProcessInstanceQuery()
                    .processInstanceId(flowTask.getFlowInstance().getActInstanceId()).singleResult();
            if (!TaskStatus.VIRTUAL.toString().equals(flowTask.getTaskStatus())) { //不是虚拟待办通知（虚拟待办actTaskId为空）
                // 取得当前任务
                HistoricTaskInstance currTask = historyService
                        .createHistoricTaskInstanceQuery().taskId(flowTask.getActTaskId())
                        .singleResult();
                String executionId = currTask.getExecutionId();

                Map<String, VariableInstance> processVariables = runtimeService.getVariableInstances(executionId);

                //完成会签的次数
                Integer completeCounter = (Integer) processVariables.get("nrOfCompletedInstances").getValue();
                //总循环次数
                Integer instanceOfNumbers = (Integer) processVariables.get("nrOfInstances").getValue();
                if (completeCounter + 1 == instanceOfNumbers) {//会签,串，并最后一个执行人
                    tempNodeInfo.setCounterSignLastTask(true);
                    counterSignLastTask = true;
                    if ("CounterSign".equalsIgnoreCase(nodeType)) {
                        nodeInfoList.add(tempNodeInfo);
                        return nodeInfoList;
                    }
                } else {
                    nodeInfoList.add(tempNodeInfo);
                    return nodeInfoList;
                }
            } else {
                nodeInfoList.add(tempNodeInfo);
                return nodeInfoList;
            }
        }

        Map<PvmActivity, List> nextNodes = new LinkedHashMap<>();

        //执行后直接返回上一（审批）节点
        if (flowTask.getJumpBackPrevious() != null && flowTask.getJumpBackPrevious()) {
            NodeInfo tempNodeInfo = this.getParentNodeInfoByTask(flowTask, definition);
            nodeInfoList.add(tempNodeInfo);
            return nodeInfoList;
        } else {
            initNextNodes(false, flowTask, currActivity, nextNodes, 0, nodeType, null);
        }

        if (!CollectionUtils.isEmpty(nextNodes)) {
            //判断网关
            Object[] nextNodesKeyArray = nextNodes.keySet().toArray();
            PvmActivity firstActivity = (PvmActivity) nextNodesKeyArray[0];
            Boolean isSizeBigTwo = nextNodes.size() > 1 ? true : false;
            String nextActivtityType = firstActivity.getProperty("type").toString();
            if ("Approve".equalsIgnoreCase(nodeType)) {//如果是审批结点
                uiType = "radiobox";
                if (isSizeBigTwo) {
                    for (int i = 1; i < nextNodes.size(); i++) {
                        PvmActivity tempActivity = (PvmActivity) nextNodesKeyArray[i];
                        if (!CollectionUtils.isEmpty(includeNodeIds)) {
                            if (!includeNodeIds.contains(tempActivity.getId())) {
                                continue;
                            }
                        }

                        NodeInfo tempNodeInfo = new NodeInfo();
                        tempNodeInfo.setCurrentTaskType(nodeType);
                        tempNodeInfo = convertNodes(flowTask, tempNodeInfo, tempActivity);
                        String gateWayName = firstActivity.getProperty("name") + "";
                        // tempNodeInfo.setName(gateWayName +"->" + tempNodeInfo.getName());
                        tempNodeInfo.setGateWayName(gateWayName);
                        tempNodeInfo.setUiType(uiType);
                        tempNodeInfo.setPreLineName(nextNodes.get(tempActivity).get(0) + "");
                        nodeInfoList.add(tempNodeInfo);
                    }
                }
            } else if ("CallActivity".equalsIgnoreCase(nextActivtityType)) {
                nodeInfoList = getCallActivityNodeInfo(flowTask, firstActivity.getId(), nodeInfoList);
                if (!CollectionUtils.isEmpty(nodeInfoList)) {
                    for (NodeInfo nodeInfo : nodeInfoList) {
                        nodeInfo.setUiType("readOnly");
                    }
                }
            } else if ("exclusiveGateway".equalsIgnoreCase(nextActivtityType)) {// 排他网关，radiobox,有且只能选择一个
                if (this.checkManualExclusiveGateway(flowTask, firstActivity.getId())) {//如果人工网关
                    uiType = "radiobox";
                }
                if (isSizeBigTwo) {
                    for (int i = 1; i < nextNodes.size(); i++) {
                        PvmActivity tempActivity = (PvmActivity) nextNodesKeyArray[i];
                        if (!CollectionUtils.isEmpty(includeNodeIds)) {
                            if (!includeNodeIds.contains(tempActivity.getId())) {
                                continue;
                            }
                        }

                        NodeInfo tempNodeInfo = new NodeInfo();
                        tempNodeInfo.setCurrentTaskType(nodeType);
                        tempNodeInfo = convertNodes(flowTask, tempNodeInfo, tempActivity);
                        String gateWayName = firstActivity.getProperty("name") + "";
                        // tempNodeInfo.setName(gateWayName +"->" + tempNodeInfo.getName());
                        tempNodeInfo.setGateWayName(gateWayName);
                        tempNodeInfo.setUiType(uiType);
                        tempNodeInfo.setPreLineName(nextNodes.get(tempActivity).get(0) + "");
                        if (nextNodes.get(tempActivity) != null && nextNodes.get(tempActivity).size() >= 3) {
                            tempNodeInfo.setPreLineCode(nextNodes.get(tempActivity).get(2) + "");
                        }
                        nodeInfoList.add(tempNodeInfo);
                    }
                }

            } else if ("inclusiveGateway".equalsIgnoreCase(nextActivtityType)) { // 包容网关,checkbox,至少选择一个
                if (isSizeBigTwo) {
                    for (int i = 1; i < nextNodes.size(); i++) {
                        PvmActivity tempActivity = (PvmActivity) nextNodesKeyArray[i];
                        if (!CollectionUtils.isEmpty(includeNodeIds)) {
                            if (!includeNodeIds.contains(tempActivity.getId())) {
                                continue;
                            }
                        }
                        NodeInfo tempNodeInfo = new NodeInfo();
                        tempNodeInfo.setCurrentTaskType(nodeType);
                        tempNodeInfo = convertNodes(flowTask, tempNodeInfo, tempActivity);
                        tempNodeInfo.setUiType(uiType);
                        tempNodeInfo.setPreLineName(nextNodes.get(tempActivity).get(0) + "");
                        nodeInfoList.add(tempNodeInfo);
                    }
                }

            } else if ("parallelGateWay".equalsIgnoreCase(nextActivtityType)) { // 并行网关,checkbox,默认全部选中显示不能修改
                if (isSizeBigTwo) {
                    for (int i = 1; i < nextNodes.size(); i++) {
                        PvmActivity tempActivity = (PvmActivity) nextNodesKeyArray[i];
                        if (!CollectionUtils.isEmpty(includeNodeIds)) {
                            if (!includeNodeIds.contains(tempActivity.getId())) {
                                continue;
                            }
                        }
                        NodeInfo tempNodeInfo = new NodeInfo();
                        tempNodeInfo.setCurrentTaskType(nodeType);
                        tempNodeInfo = convertNodes(flowTask, tempNodeInfo, tempActivity);
                        tempNodeInfo.setUiType(uiType);
                        tempNodeInfo.setPreLineName(nextNodes.get(tempActivity).get(0) + "");
                        nodeInfoList.add(tempNodeInfo);
                    }
                }

            } else {
                if (isSizeBigTwo) {//当下步节点大于一个时，按照并行网关处理。checkbox,默认全部选中显示不能修改
                    for (int i = 0; i < nextNodes.size(); i++) {
                        PvmActivity tempActivity = (PvmActivity) nextNodesKeyArray[i];
                        if (!CollectionUtils.isEmpty(includeNodeIds)) {
                            if (!includeNodeIds.contains(tempActivity.getId())) {
                                continue;
                            }
                        }
                        NodeInfo tempNodeInfo = new NodeInfo();
                        tempNodeInfo.setCurrentTaskType(nodeType);
                        tempNodeInfo = convertNodes(flowTask, tempNodeInfo, tempActivity);
                        tempNodeInfo.setUiType(uiType);
                        tempNodeInfo.setPreLineName(nextNodes.get(tempActivity).get(0) + "");
                        nodeInfoList.add(tempNodeInfo);
                    }
                } else {//按照惟一分支任务处理，显示一个，只读
                    PvmActivity tempActivity = (PvmActivity) nextNodesKeyArray[0];
                    if (!CollectionUtils.isEmpty(includeNodeIds)) {
                        if (!includeNodeIds.contains(tempActivity.getId())) {
                            throw new RuntimeException("惟一分支未选中");
                        }
                    }
                    NodeInfo tempNodeInfo = new NodeInfo();
                    tempNodeInfo.setCurrentTaskType(nodeType);
                    tempNodeInfo = convertNodes(flowTask, tempNodeInfo, tempActivity);
                    tempNodeInfo.setUiType(uiType);
                    tempNodeInfo.setPreLineName(nextNodes.get(tempActivity).get(0) + "");
                    nodeInfoList.add(tempNodeInfo);
                }
            }
        }
        if (counterSignLastTask && !CollectionUtils.isEmpty(nodeInfoList)) {
            for (NodeInfo nodeInfo : nodeInfoList) {
                nodeInfo.setCounterSignLastTask(true);
            }
        }
        return nodeInfoList;
    }

    /**
     * 获取所有出口任务
     *
     * @param currActivity
     * @param nextNodes
     */
    public void initNextNodes(Boolean needKnowRealPath, FlowTask flowTask, PvmActivity currActivity, Map<PvmActivity, List> nextNodes, int index, String nodeType, List lineInfo) {
        List<PvmTransition> nextTransitionList = currActivity.getOutgoingTransitions();
        if (!CollectionUtils.isEmpty(nextTransitionList)) {
            for (PvmTransition pv : nextTransitionList) {
                PvmActivity currTempActivity = pv.getDestination();
                String lineName = pv.getProperty("name") + "";//线的名称
                String documentation = (String) pv.getProperty("documentation");
                List value = null;
                if (lineInfo != null) {
                    value = lineInfo;
                } else {
                    value = new ArrayList<>();
                    value.add(lineName);
                    value.add(index);
                    value.add(documentation);
                }
                Boolean ifGateWay = ifGageway(currTempActivity);
                String type = currTempActivity.getProperty("type") + "";
                if (ifGateWay || "ManualTask".equalsIgnoreCase(type)) {//如果是网关，其他直绑节点自行忽略
                    if (ifGateWay && index < 1) {
                        nextNodes.put(currTempActivity, value);//把网关放入第一个节点
                        index++;
                        //如果第一个节点是人工网关，设计到后面如果是系统排他网关需要找确切路径
                        if (this.checkManualExclusiveGateway(flowTask, currTempActivity.getId())) {
                            needKnowRealPath = true;
                        }
                        initNextNodes(needKnowRealPath, flowTask, currTempActivity, nextNodes, index, nodeType, null);
                    } else {
                        if (needKnowRealPath && ifGateWay && this.checkExclusiveGateway(flowTask, currTempActivity.getId())) {
                            //如果网关后面还是系统排他网关，需要找到确定的路径
                            String businessId = flowTask.getFlowInstance().getBusinessId();
                            BusinessModel businessModel = flowTask.getFlowInstance().getFlowDefVersion().getFlowDefination().getFlowType().getBusinessModel();
                            Map<String, Object> v = ExpressionUtil.getPropertiesValuesMap(businessModel, businessId, false);
                            List<NodeInfo> currentNodeInf = null;
                            try {
                                currentNodeInf = this.selectQualifiedNode(flowTask, currTempActivity, v, null);
                            } catch (Exception e) {
                                nextNodes = null;
                            }

                            if (!CollectionUtils.isEmpty(currentNodeInf)) {
                                String actProcessDefinitionId = flowTask.getFlowInstance().getFlowDefVersion().getActDefId();
                                ProcessDefinitionEntity definition = (ProcessDefinitionEntity) ((RepositoryServiceImpl) repositoryService)
                                        .getDeployedProcessDefinition(actProcessDefinitionId);
                                for (int k = 0; k < currentNodeInf.size(); k++) {
                                    NodeInfo bean = currentNodeInf.get(k);
                                    String actTaskDefKey = bean.getId();
                                    PvmActivity akActivity = this.getActivitNode(definition, actTaskDefKey);
                                    nextNodes.put(akActivity, value);
                                }
                            }
                        } else {
                            index++;
                            initNextNodes(needKnowRealPath, flowTask, currTempActivity, nextNodes, index, nodeType, value);
                        }
                    }
                } else {
                    nextNodes.put(currTempActivity, value);
                }

            }
        }
    }

    /**
     * 将流程引擎的流程节点转换为前端需要的节点信息
     *
     * @param tempNodeInfo
     * @param tempActivity
     * @return
     */
    public NodeInfo convertNodes(FlowTask flowTask, NodeInfo tempNodeInfo, PvmActivity tempActivity) {
        tempNodeInfo.setFlowDefVersionId(flowTask.getFlowInstance().getFlowDefVersion().getId());
        tempNodeInfo.setFlowDefVersionName(flowTask.getFlowInstance().getFlowDefVersion().getName());
        tempNodeInfo.setFlowDefVersionCode(flowTask.getFlowInstance().getFlowDefVersion().getVersionCode());
        tempNodeInfo.setFlowTaskId(flowTask.getId());
        if ("CounterSignNotEnd".equalsIgnoreCase(tempNodeInfo.getType())) {
            tempNodeInfo.setName(tempActivity.getProperty("name").toString());
            tempNodeInfo.setId(tempActivity.getId());
            return tempNodeInfo;
        }

        tempNodeInfo.setName(tempActivity.getProperty("name").toString());
        tempNodeInfo.setType(tempActivity.getProperty("type").toString());

        tempNodeInfo.setId(tempActivity.getId());
        String assignee = null;
        String candidateUsers = null;
        if ("endEvent".equalsIgnoreCase(tempActivity.getProperty("type") + "")) {
            tempNodeInfo.setType("EndEvent");
            return tempNodeInfo;
        } else if (ifGageway(tempActivity)) {
            tempNodeInfo.setType("gateWay");
            tempNodeInfo.setUiType("radiobox");
            tempNodeInfo.setFlowTaskType("common");
            return tempNodeInfo;
        }

        Definition definition = flowCommonUtil.flowDefinition(flowTask.getFlowInstance().getFlowDefVersion());
        JSONObject currentNode = definition.getProcess().getNodes().getJSONObject(tempActivity.getId());
        String nodeType = currentNode.get("nodeType") + "";
//        tempNodeInfo.setCurrentTaskType(nodeType);
        if ("CounterSign".equalsIgnoreCase(nodeType)) {//会签任务
            tempNodeInfo.setUiType("readOnly");
            tempNodeInfo.setFlowTaskType(nodeType);
        } else if ("ParallelTask".equalsIgnoreCase(nodeType) || "SerialTask".equalsIgnoreCase(nodeType)) {
            tempNodeInfo.setUiType("readOnly");
            tempNodeInfo.setFlowTaskType(nodeType);
        } else if ("Normal".equalsIgnoreCase(nodeType)) {//普通任务
            tempNodeInfo.setUiType("radiobox");
            tempNodeInfo.setFlowTaskType("common");
        } else if ("SingleSign".equalsIgnoreCase(nodeType)) {//单签任务
            tempNodeInfo.setFlowTaskType("singleSign");
            tempNodeInfo.setUiType("checkbox");
        } else if ("Approve".equalsIgnoreCase(nodeType)) {//审批任务
            tempNodeInfo.setUiType("radiobox");
            tempNodeInfo.setFlowTaskType("approve");
        } else if ("ServiceTask".equals(nodeType)) {//服务任务
            tempNodeInfo.setUiType("radiobox");
            tempNodeInfo.setFlowTaskType("serviceTask");
        } else if ("ReceiveTask".equals(nodeType)) {//服务任务
            tempNodeInfo.setUiType("radiobox");
            tempNodeInfo.setFlowTaskType("receiveTask");
        } else if ("CallActivity".equalsIgnoreCase(nodeType)) {
        } else if ("PoolTask".equalsIgnoreCase(nodeType)) {
            tempNodeInfo.setUiType("radiobox");
            tempNodeInfo.setFlowTaskType("poolTask");
        } else {
            throw new RuntimeException("流程任务节点配置有错误");
        }
        return tempNodeInfo;
    }


    public PvmNodeInfo pvmNodeInfoGateWayInit(Boolean ifGateWay, PvmNodeInfo pvmNodeInfo, PvmActivity nextTempActivity, Map<String, Object> v)
            throws NoSuchMethodException, SecurityException {
        if (ifGateWay) {
            PvmNodeInfo pvmNodeInfoTemp = checkFuHeConditon(nextTempActivity, v);
            pvmNodeInfoTemp.setParent(pvmNodeInfo);
            if (pvmNodeInfoTemp != null && CollectionUtils.isEmpty(pvmNodeInfoTemp.getChildren())) {
                String defaultSequenceId = nextTempActivity.getProperty("default") + "";
                if (StringUtils.isNotEmpty(defaultSequenceId)) {
                    PvmTransition pvmTransition = nextTempActivity.findOutgoingTransition(defaultSequenceId);
                    if (pvmTransition != null) {
                        PvmNodeInfo pvmNodeInfoDefault = new PvmNodeInfo();
                        pvmNodeInfoDefault.setCurrActivity(pvmTransition.getDestination());
                        pvmNodeInfoDefault.setParent(pvmNodeInfoTemp);
                        pvmNodeInfoTemp.getChildren().add(pvmNodeInfoDefault);
                    }
                }
            }
            pvmNodeInfo.getChildren().add(pvmNodeInfoTemp);
        } else {
            PvmNodeInfo pvmNodeInfoTemp = new PvmNodeInfo();
            pvmNodeInfoTemp.setParent(pvmNodeInfo);
            pvmNodeInfoTemp.setCurrActivity(nextTempActivity);
            pvmNodeInfo.getChildren().add(pvmNodeInfoTemp);
        }
        return pvmNodeInfo;
    }

    /**
     * 注入符合条件的下一步节点
     *
     * @param currActivity
     * @param v
     * @throws NoSuchMethodException
     * @throws SecurityException
     */
    public PvmNodeInfo checkFuHeConditon(PvmActivity currActivity, Map<String, Object> v)
            throws NoSuchMethodException, SecurityException {

        PvmNodeInfo pvmNodeInfo = new PvmNodeInfo();
        pvmNodeInfo.setCurrActivity(currActivity);

        List<PvmTransition> nextTransitionList = currActivity.getOutgoingTransitions();
        if (!CollectionUtils.isEmpty(nextTransitionList)) {
            String currentActivtityType = currActivity.getProperty("type").toString();
            for (PvmTransition pv : nextTransitionList) {
                String conditionText = (String) pv.getProperty("conditionText");
                PvmActivity nextTempActivity = pv.getDestination();
                Boolean ifGateWay = ifGageway(nextTempActivity);//当前节点的子节点是否为网关

                if ("ExclusiveGateway".equalsIgnoreCase(currentActivtityType)) {
                    if (conditionText != null) {
                        if (conditionText.startsWith("#{")) {// #{开头代表自定义的groovy表达式
                            String conditonFinal = conditionText.substring(conditionText.indexOf("#{") + 2,
                                    conditionText.lastIndexOf("}"));
                            Boolean boo = ConditionUtil.groovyTest(conditonFinal, v);
                            if (boo == null) {
                                throw new FlowException("验证表达式失败！表达式：【" + conditonFinal + "】,带入参数：【" + JsonUtils.toJson(v) + "】");
                            } else if (boo) {
                                pvmNodeInfo = pvmNodeInfoGateWayInit(ifGateWay, pvmNodeInfo, nextTempActivity, v);
                                break;
                            }
                        } else {//其他的用UEL表达式验证
                            Object tempResult = ConditionUtil.uelResult(conditionText, v);
                            if (tempResult instanceof Boolean) {
                                Boolean resultB = (Boolean) tempResult;
                                if (resultB == true) {
                                    pvmNodeInfo = pvmNodeInfoGateWayInit(ifGateWay, pvmNodeInfo, nextTempActivity, v);
                                    break;
                                }
                            }
                        }
                    }
                } else if ("inclusiveGateway".equalsIgnoreCase(currentActivtityType)) {
                    if (conditionText != null) {
                        if (conditionText.startsWith("#{")) {// #{开头代表自定义的groovy表达式
                            String conditonFinal = conditionText.substring(conditionText.indexOf("#{") + 2,
                                    conditionText.lastIndexOf("}"));
                            Boolean boo = ConditionUtil.groovyTest(conditonFinal, v);
                            if (boo == null) {
                                throw new FlowException("验证表达式失败！表达式：【" + conditonFinal + "】,带入参数：【" + JsonUtils.toJson(v) + "】");
                            } else if (boo) {
                                pvmNodeInfo = pvmNodeInfoGateWayInit(ifGateWay, pvmNodeInfo, nextTempActivity, v);
                            }
                        } else {//其他的用UEL表达式验证
                            Object tempResult = ConditionUtil.uelResult(conditionText, v);
                            if (tempResult instanceof Boolean) {
                                Boolean resultB = (Boolean) tempResult;
                                if (resultB == true) {
                                    pvmNodeInfo = pvmNodeInfoGateWayInit(ifGateWay, pvmNodeInfo, nextTempActivity, v);
                                }
                            }
                        }
                    }
                } else {
                    pvmNodeInfo = pvmNodeInfoGateWayInit(ifGateWay, pvmNodeInfo, nextTempActivity, v);
                }
            }
            if (("ExclusiveGateway".equalsIgnoreCase(currentActivtityType) || "inclusiveGateway".equalsIgnoreCase(currentActivtityType)) && CollectionUtils.isEmpty(pvmNodeInfo.getChildren())) {
                String defaultSequenceId = currActivity.getProperty("default") + "";
                if (StringUtils.isNotEmpty(defaultSequenceId)) {
                    PvmTransition pvmTransition = currActivity.findOutgoingTransition(defaultSequenceId);
                    if (pvmTransition != null) {
                        PvmNodeInfo pvmNodeInfoDefault = new PvmNodeInfo();
                        pvmNodeInfoDefault.setCurrActivity(pvmTransition.getDestination());
                        pvmNodeInfoDefault.setParent(pvmNodeInfo);
                        pvmNodeInfo.getChildren().add(pvmNodeInfoDefault);
                    }
                }
            }
        }
        return pvmNodeInfo;
    }

    public List<PvmActivity> initPvmActivityList(PvmNodeInfo pvmNodeInfo, List<PvmActivity> results) {
        if (pvmNodeInfo != null && !CollectionUtils.isEmpty(pvmNodeInfo.getChildren())) {
            Set<PvmNodeInfo> children = pvmNodeInfo.getChildren();
            for (PvmNodeInfo p : children) {
                PvmActivity currActivity = p.getCurrActivity();
                if (currActivity != null) {
                    if (ifGageway(currActivity)) {
                        results = this.initPvmActivityList(p, results);
                    } else {
                        results.add(currActivity);
                    }
                }
            }
        }
        return results;
    }

    /**
     * 检查是否允许对流程实例进行终止、驳回,针对并行网关，包容网关的情况下
     *
     * @param flowTaskPageResult
     */
    public static void changeTaskStatue(PageResult<FlowTask> flowTaskPageResult) {
        List<FlowTask> flowTaskList = flowTaskPageResult.getRows();
        Map<FlowInstance, List<FlowTask>> flowInstanceListMap = new HashMap<>();
        if (!CollectionUtils.isEmpty(flowTaskList)) {
            for (FlowTask flowTask : flowTaskList) {
                List<FlowTask> flowTaskListTemp = flowInstanceListMap.get(flowTask.getFlowInstance());
                if (flowTaskListTemp == null) {
                    flowTaskListTemp = new ArrayList<>();
                }
                flowTaskListTemp.add(flowTask);
                flowInstanceListMap.put(flowTask.getFlowInstance(), flowTaskListTemp);
            }
        }
        if (!CollectionUtils.isEmpty(flowInstanceListMap)) {
            for (Map.Entry<FlowInstance, List<FlowTask>> temp : flowInstanceListMap.entrySet()) {
                List<FlowTask> flowTaskListTemp = temp.getValue();
                if (!CollectionUtils.isEmpty(flowTaskListTemp)) {
                    boolean canEnd = true;
                    for (FlowTask flowTask : flowTaskListTemp) {
                        Boolean canCancel = flowTask.getCanSuspension();
                        if (canCancel == null || !canCancel) {
                            canEnd = false;
                            break;
                        }
                    }
                    if (!canEnd) {
                        for (FlowTask flowTask : flowTaskListTemp) {
                            flowTask.setCanSuspension(false);
                        }
                    }
                }
            }
        }
    }

    /**
     * 检查撤回按钮是否应该显示（如果下一步已经执行就不应该再显示）
     *
     * @param flowHistory
     * @return
     */
    public Boolean checkoutTaskRollBack(FlowHistory flowHistory) {

        Boolean resultCheck = false;
        try {
            String taskId = flowHistory.getActHistoryId();
            // 取得当前任务
            HistoricTaskInstance currTask = historyService.createHistoricTaskInstanceQuery().taskId(taskId).singleResult();
            if (currTask.getEndTime() == null) {// 当前任务可能已经被还原成待办（表示已经撤回了）
                return false; //当前任务可能已经被还原
            }

            // 取得流程实例
            ProcessInstance instance = runtimeService.createProcessInstanceQuery().processInstanceId(currTask.getProcessInstanceId()).singleResult();
            if (instance == null) {
                return false;  //流程实例不存在或者已经结束
            }

            // 取得流程定义
            ProcessDefinitionEntity definition = (ProcessDefinitionEntity) ((RepositoryServiceImpl) repositoryService).getDeployedProcessDefinition(currTask.getProcessDefinitionId());
            if (definition == null) {
                return false;  //流程定义未找到找到
            }

            String executionId = currTask.getExecutionId();
            Execution execution = runtimeService.createExecutionQuery().executionId(executionId).singleResult();
            if (execution == null) {
                return false; //当前任务不允许撤回
            }

            // 取得下一步活动
//            ActivityImpl currActivity = ((ProcessDefinitionImpl) definition).findActivity(currTask.getTaskDefinitionKey());
            //只判断下一步是否已经执行（如果执行了，不显示撤回按钮）
//            resultCheck = checkIfTheNextNodeHasBeenProcessed(currActivity, instance, currTask);
            //现在存在不按照流程图设计走的情况(处理后直接返回审批，节点跳转)，以前的依据流程图划线判断不行，重新构造判断条件
            resultCheck = checkIfTheNextNodeHasBeenProcessed_new(flowHistory.getFlowInstance(), flowHistory.getId());
        } catch (Exception e) {
            LogUtil.error("检查是否可以撤回报错：{}", e.getMessage(), e);
        }

        return resultCheck;
    }


    /**
     * 回退任务
     *
     * @return
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public OperateResult taskRollBack(FlowHistory flowHistory, String opinion) {
        // 流程成功撤回！
        OperateResult result = OperateResult.operationSuccess("撤回成功！");
        String taskId = flowHistory.getActHistoryId();
        try {
            // 取得当前任务
            HistoricTaskInstance currTask = historyService.createHistoricTaskInstanceQuery().taskId(taskId).singleResult();
            if (currTask.getEndTime() == null) {// 当前任务可能已经被还原成待办（表示已经撤回了）
                return OperateResult.operationFailure("撤回失败：当前任务已经还原！");
            }

            // 取得流程实例
            ProcessInstance instance = runtimeService.createProcessInstanceQuery().processInstanceId(currTask.getProcessInstanceId()).singleResult();
            if (instance == null) {
                return OperateResult.operationFailure("撤回失败：流程实例不存在或者已经结束！");
            }

            // 取得流程定义
            ProcessDefinitionEntity definition = (ProcessDefinitionEntity) ((RepositoryServiceImpl) repositoryService).getDeployedProcessDefinition(currTask.getProcessDefinitionId());
            if (definition == null) {
                return OperateResult.operationFailure("撤回失败：流程定义未找到！");
            }

            String executionId = currTask.getExecutionId();
            Execution execution = runtimeService.createExecutionQuery().executionId(executionId).singleResult();
            if (execution == null) {
                return OperateResult.operationFailure("撤回失败：当前任务已经流转，不允许撤回！");
            }

            // 取得下一步活动
            ActivityImpl currActivity = ((ProcessDefinitionImpl) definition).findActivity(currTask.getTaskDefinitionKey());

//            OperateResult resultCheck = checkNextNodeNotCompleted(currActivity, instance, currTask);
//            if (!resultCheck.successful()) {
//                return resultCheck;
//            }

            OperateResult resultCheck = checkNextNodeNotCompleted_new(flowHistory.getFlowInstance(), flowHistory.getId());
            if (!resultCheck.successful()) {
                return resultCheck;
            }


            HistoricActivityInstance historicActivityInstance = null;
            HistoricActivityInstanceQuery his = historyService.createHistoricActivityInstanceQuery().executionId(executionId);
            if (his != null) {
                List<HistoricActivityInstance> historicActivityInstanceList = his.activityId(currTask.getTaskDefinitionKey()).orderByHistoricActivityInstanceEndTime().desc().list();
                if (!CollectionUtils.isEmpty(historicActivityInstanceList)) {
                    historicActivityInstance = historicActivityInstanceList.get(0);
                }
                if (historicActivityInstance == null) {
                    his = historyService.createHistoricActivityInstanceQuery().processInstanceId(instance.getId())
                            .taskAssignee(currTask.getAssignee());
                    if (his != null) {
                        historicActivityInstance = his.activityId(currTask.getTaskDefinitionKey()).singleResult();
                    }
                }
            }

            if (historicActivityInstance == null) {
                return OperateResult.operationFailure("撤回失败：当前任务找不到！");//当前任务找不到
            }
            if (!currTask.getTaskDefinitionKey().equalsIgnoreCase(execution.getActivityId())) {
                if (execution.getActivityId() != null) {
                    List<HistoricActivityInstance> historicActivityInstanceList = historyService
                            .createHistoricActivityInstanceQuery().executionId(execution.getId())
                            .activityId(execution.getActivityId()).list();
                    if (historicActivityInstanceList != null) {
                        for (HistoricActivityInstance hTemp : historicActivityInstanceList) {
                            if (hTemp.getEndTime() == null) {
                                historyService.deleteHistoricActivityInstanceById(hTemp.getId());
                            }
                        }

                    }
                }
            }

            HistoricActivityInstanceEntity he = (HistoricActivityInstanceEntity) historicActivityInstance;
            he.setEndTime(null);
            he.setDurationInMillis(null);
            historyService.updateHistoricActivityInstance(he);

            historyService.deleteHistoricTaskInstanceOnly(currTask.getId());
            ExecutionEntity executionEntity = (ExecutionEntity) execution;
            executionEntity.setActivity(currActivity);


            TaskEntity newTask = TaskEntity.create(new Date());
            newTask.setAssignee(currTask.getAssignee());
            newTask.setCategory(currTask.getCategory());
            newTask.setDescription(currTask.getDescription());
            newTask.setDueDate(currTask.getDueDate());
            newTask.setFormKey(currTask.getFormKey());
            newTask.setName(currTask.getName());
            newTask.setOwner(currTask.getOwner());
            newTask.setParentTaskId(currTask.getParentTaskId());
            newTask.setPriority(currTask.getPriority());
            newTask.setTenantId(currTask.getTenantId());
            newTask.setCreateTime(new Date());

            newTask.setId(currTask.getId());
            newTask.setExecutionId(currTask.getExecutionId());
            newTask.setProcessDefinitionId(currTask.getProcessDefinitionId());
            newTask.setProcessInstanceId(currTask.getProcessInstanceId());
            newTask.setVariables(currTask.getProcessVariables());
            newTask.setTaskDefinitionKey(currTask.getTaskDefinitionKey());

            taskService.callBackTask(newTask, execution);

            callBackRunIdentityLinkEntity(currTask.getId());//还原候选人等信

            // 删除其他到达的节点
//            deleteOtherNode(currActivity, instance, definition, currTask, flowHistory.getFlowInstance());
            // 删除其他到达的节点
            deleteOtherNode_new(flowHistory.getId());


            //记录历史
            if (result.successful()) {
                flowHistory.setTaskStatus(TaskStatus.CANCEL.toString());
                flowHistoryDao.save(flowHistory);
                FlowHistory flowHistoryNew = (FlowHistory) flowHistory.clone();
                flowHistoryNew.setId(null);
                Date now = new Date();
                flowHistoryNew.setActEndTime(now);
                flowHistoryNew.setDepict("【被撤回】" + opinion);
                flowHistoryNew.setFlowExecuteStatus(FlowExecuteStatus.RECALL.getCode());//撤回
                flowHistoryNew.setActDurationInMillis(now.getTime() - flowHistory.getActEndTime().getTime());
                flowHistoryNew.setTenantCode(ContextUtil.getTenantCode());
                flowHistoryDao.save(flowHistoryNew);
            }
            //初始化回退后的新任务
            initTask(flowHistory.getFlowInstance(), flowHistory, currTask.getTaskDefinitionKey(), null);
            return result;
        } catch (Exception e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            LogUtil.error("撤回失败：" + e.getMessage(), e);
            return OperateResult.operationFailure("撤回失败：详情请查看日志！");
        }
    }

    /**
     * 还原执行人、候选人
     *
     * @param taskId
     */

    @Transactional(propagation = Propagation.REQUIRED)
    public void callBackRunIdentityLinkEntity(String taskId) {
        List<HistoricIdentityLink> historicIdentityLinks = historyService.getHistoricIdentityLinksForTask(taskId);

        for (HistoricIdentityLink hlink : historicIdentityLinks) {
            HistoricIdentityLinkEntity historicIdentityLinkEntity = (HistoricIdentityLinkEntity) hlink;
            if (historicIdentityLinkEntity.getId() == null) {
                continue;
            }
            IdentityLinkEntity identityLinkEntity = new IdentityLinkEntity();
            identityLinkEntity.setGroupId(historicIdentityLinkEntity.getGroupId());
            identityLinkEntity.setId(historicIdentityLinkEntity.getId());
            identityLinkEntity.setProcessInstanceId(historicIdentityLinkEntity.getProcessInstanceId());
            identityLinkEntity.setTaskId(historicIdentityLinkEntity.getTaskId());
            identityLinkEntity.setType(historicIdentityLinkEntity.getType());
            identityLinkEntity.setUserId(historicIdentityLinkEntity.getUserId());
            try {
                identityService.save(identityLinkEntity);
            } catch (Exception e) {
                e.printStackTrace();
                throw e;
            }

        }
    }


    /**
     * 删除其他到达的节点
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public void deleteOtherNode_new(String hisId) {
        List<FlowTask> taskList = flowTaskDao.findListByProperty("preId", hisId);
        //是否推送信息到baisc
        Boolean pushBasic = flowTaskService.getBooleanPushTaskToBasic();
        List<FlowTask> needDelList = new ArrayList<>();
        for (FlowTask flowTask : taskList) {
            taskService.deleteRuningTask(flowTask.getActTaskId(), false);
            historyService.deleteHistoricActivityInstancesByTaskId(flowTask.getActTaskId());
            historyService.deleteHistoricTaskInstance(flowTask.getActTaskId());
            if (pushBasic) {
                needDelList.add(flowTask);
            }
            flowTaskDao.delete(flowTask);
        }

        if (pushBasic) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    flowTaskService.pushToBasic(null, null, needDelList, null);
                }
            }).start();
        }
    }


    /**
     * 删除其他到达的节点
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public Boolean deleteOtherNode(PvmActivity currActivity, ProcessInstance instance,
                                   ProcessDefinitionEntity definition, HistoricTaskInstance destnetionTask, FlowInstance flowInstance) {
        List<PvmTransition> nextTransitionList = currActivity.getOutgoingTransitions();
        Boolean result = true;
        for (PvmTransition nextTransition : nextTransitionList) {
            PvmActivity nextActivity = nextTransition.getDestination();
            Boolean ifGateWay = ifGageway(nextActivity);
            boolean ifMultiInstance = ifMultiInstance(nextActivity);
            if (ifGateWay) {
                result = deleteOtherNode(nextActivity, instance, definition, destnetionTask, flowInstance);
                if (!result) {
                    return result;
                }
            }

            List<Task> nextTasks = taskService.createTaskQuery().processInstanceId(instance.getId())
                    .taskDefinitionKey(nextActivity.getId()).list();
            //是否推送信息到baisc
            Boolean pushBasic = flowTaskService.getBooleanPushTaskToBasic();

            List<FlowTask> needDelList = new ArrayList<FlowTask>();
            for (Task nextTask : nextTasks) {
                taskService.deleteRuningTask(nextTask.getId(), false);
                historyService.deleteHistoricActivityInstancesByTaskId(nextTask.getId());
                historyService.deleteHistoricTaskInstance(nextTask.getId());
                if (pushBasic) {
                    List<FlowTask> needDel = flowTaskDao.findListByProperty("actTaskId", nextTask.getId());
                    needDelList.addAll(needDel);
                }
                flowTaskDao.deleteByActTaskId(nextTask.getId());//删除关联的流程新任务
            }
            if (pushBasic) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        flowTaskService.pushToBasic(null, null, needDelList, null);
                    }
                }).start();
            }
            if (!CollectionUtils.isEmpty(nextTasks) && (ifGageway(currActivity))) {

                List<HistoricActivityInstance> gateWayActivityList = historyService.createHistoricActivityInstanceQuery()
                        .processInstanceId(destnetionTask.getProcessInstanceId()).activityId(currActivity.getId())
                        .list();
                if (!CollectionUtils.isEmpty(gateWayActivityList)) {
                    for (HistoricActivityInstance gateWayActivity : gateWayActivityList) {
                        historyService.deleteHistoricActivityInstanceById(gateWayActivity.getId());
                    }
                }
            }
            if (ifMultiInstance) {//多实例任务，清除父执行分支
                ExecutionEntity pExecution = (ExecutionEntity) runtimeService.createExecutionQuery().processInstanceId(instance.getId()).activityIdNoActive(nextActivity.getId()).singleResult();
                if (pExecution != null) {
                    runtimeService.deleteExecution(pExecution);
                }
            }

        }
        return true;
    }


    /**
     * 判断服务任务是否已经执行
     *
     * @param instance
     * @param currActivity
     */
    public Boolean serviceTaskHasExecute(ProcessInstance instance, PvmActivity currActivity) {
        FlowInstance flowInstance = flowInstanceDao.findByActInstanceId(instance.getId());
        if (flowInstance != null) {
            List<FlowHistory> flowHistoryList = flowHistoryDao.findByInstanceIdNoVirtual(flowInstance.getId());
            FlowHistory serHistory = flowHistoryList.stream().filter(a -> currActivity.getId().equalsIgnoreCase(a.getActTaskDefKey())).findFirst().orElse(null);
            if (serHistory != null) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断接收任务是否已经执行
     *
     * @param instance
     * @param currActivity
     */
    public Boolean receiveTaskHasExecute(ProcessInstance instance, PvmActivity currActivity) {
        FlowInstance flowInstance = flowInstanceDao.findByActInstanceId(instance.getId());
        if (flowInstance != null) {
            List<FlowTask> taskList = flowTaskDao.findByInstanceIdNoVirtual(flowInstance.getId());
            if (!CollectionUtils.isEmpty(taskList)) {
                FlowTask receiveTask = taskList.stream().filter(a -> currActivity.getId().equalsIgnoreCase(a.getActTaskDefKey())).findFirst().orElse(null);
                if (receiveTask != null) { //接收任务还未触发
                    return false;
                }
            }

            List<FlowHistory> flowHistoryList = flowHistoryDao.findByInstanceIdNoVirtual(flowInstance.getId());
            if (!CollectionUtils.isEmpty(flowHistoryList)) {
                FlowHistory serHistory = flowHistoryList.stream().filter(a -> currActivity.getId().equalsIgnoreCase(a.getActTaskDefKey())).findFirst().orElse(null);
                if (serHistory != null) { //接收任务已经触发执行过
                    return true;
                }
            }
        }
        return false;
    }


    /**
     * 只判断下一步是否已经执行
     */
    public Boolean checkIfTheNextNodeHasBeenProcessed_new(FlowInstance flowInstance, String hisId) {
        if (flowInstance != null) {
            List<FlowHistory> flowHistoryList = flowHistoryDao.findListByProperty("preId", hisId);
            if (CollectionUtils.isEmpty(flowHistoryList)) {  //已办关联不等于空表示下一步已经有处理的了
                List<FlowTask> taskList = flowTaskDao.findListByProperty("preId", hisId);
                //待办不等于空表达下一步还在（默认服务任务和接收任务是不可逆的）
                return !CollectionUtils.isEmpty(taskList);
            }
        }
        return false;
    }

    /**
     * 只判断下一步是否已经执行
     *
     * @param currActivity
     * @param instance
     * @param destnetionTask
     * @return
     */
    public Boolean checkIfTheNextNodeHasBeenProcessed(PvmActivity currActivity, ProcessInstance instance, HistoricTaskInstance destnetionTask) {
        List<PvmTransition> nextTransitionList = currActivity.getOutgoingTransitions();
        boolean result = true;

        for (PvmTransition nextTransition : nextTransitionList) {
            PvmActivity nextActivity = nextTransition.getDestination();
            Boolean ifGateWay = ifGageway(nextActivity);
            String type = nextActivity.getProperty("type") + "";
            if ("ServiceTask".equalsIgnoreCase(type)) { //服务任务（说明撤回任务连接有服务任务，服务任务会自动执行，底层没有记录）
                //判断服务任务是否已经执行
                Boolean boo = this.serviceTaskHasExecute(instance, nextActivity);
                if (boo) {
                    return false;
                }
            }
            if ("ReceiveTask".equalsIgnoreCase(type)) { //接收任务（说明撤回任务连接有接收任务，判断接收任务是否执行）
                //判断接收任务是否已经执行
                Boolean boo = this.receiveTaskHasExecute(instance, nextActivity);
                if (boo) {
                    return false;
                }
            }
            if ("callActivity".equalsIgnoreCase(type)) { //子流程程（撤回任务连接有子流程，直接不允许撤回，先不判断）
                return false;
            }
            if (ifGateWay || "ManualTask".equalsIgnoreCase(type)) {
                result = checkIfTheNextNodeHasBeenProcessed(nextActivity, instance, destnetionTask);
                if (!result) {
                    return result;
                }
            }
            List<HistoricTaskInstance> completeTasks = historyService.createHistoricTaskInstanceQuery()
                    .processInstanceId(instance.getId()).taskDefinitionKey(nextActivity.getId()).finished().list();
            for (HistoricTaskInstance h : completeTasks) {
                if (h.getEndTime().after(destnetionTask.getEndTime())) {
                    return false;
                }
            }
            if (ifMultiInstance(currActivity)) {// 如果是多实例任务,判断当前任务是否已经流转到下一节点
                List<HistoricTaskInstance> unCompleteTasks = historyService.createHistoricTaskInstanceQuery()
                        .processInstanceId(instance.getId()).taskDefinitionKey(nextActivity.getId()).unfinished().list();
                for (HistoricTaskInstance h : unCompleteTasks) {
                    if (h.getStartTime().after(destnetionTask.getStartTime())) {
                        return result;
                    }
                }
            }
        }
        return result;
    }


    /**
     * 检查下一节点是否已经执行完成
     */
    public OperateResult checkNextNodeNotCompleted_new(FlowInstance flowInstance, String hisId) {
        if (flowInstance != null) {
            List<FlowHistory> flowHistoryList = flowHistoryDao.findListByProperty("preId", hisId);
            if (CollectionUtils.isEmpty(flowHistoryList)) {  //已办关联不等于空表示下一步已经有处理的了
                List<FlowTask> taskList = flowTaskDao.findListByProperty("preId", hisId);
                if (!CollectionUtils.isEmpty(taskList)) {//待办不等于空表达下一步还在（默认服务任务和接收任务是不可逆的）
                    return OperateResult.operationSuccess();
                } else {
                    return OperateResult.operationFailure("撤回失败：下一步为不可逆节点！");
                }
            } else {
                return OperateResult.operationFailure("撤回失败：下一步已经执行！");
            }
        } else {
            return OperateResult.operationFailure("撤回失败：流程实例为空!");
        }
    }


    /**
     * 检查下一节点是否已经执行完成
     *
     * @param currActivity
     * @param instance
     * @param destnetionTask
     * @return
     */
    public OperateResult checkNextNodeNotCompleted(PvmActivity currActivity, ProcessInstance instance, HistoricTaskInstance destnetionTask) {
        List<PvmTransition> nextTransitionList = currActivity.getOutgoingTransitions();
        OperateResult result = OperateResult.operationSuccess();

        for (PvmTransition nextTransition : nextTransitionList) {
            PvmActivity nextActivity = nextTransition.getDestination();
            Boolean ifGateWay = ifGageway(nextActivity);
            String type = nextActivity.getProperty("type") + "";
            if ("ServiceTask".equalsIgnoreCase(type)) { //服务任务（说明撤回任务连接有服务任务，服务任务会自动执行，底层没有记录）
                //判断服务任务是否已经执行
                Boolean boo = this.serviceTaskHasExecute(instance, nextActivity);
                if (boo) {
                    return OperateResult.operationFailure("撤回失败：服务任务已执行，不允许撤回！");
                }
            }
            if ("ReceiveTask".equalsIgnoreCase(type)) { //接收任务（说明撤回任务连接有接收任务，判断接收任务是否执行）
                //判断接收任务是否已经执行
                Boolean boo = this.receiveTaskHasExecute(instance, nextActivity);
                if (boo) {
                    return OperateResult.operationFailure("撤回失败：接收任务已执行或执行中，不允许撤回！");
                }
            }
            if ("callActivity".equalsIgnoreCase(type)) { //子流程程（撤回任务连接有子流程，直接不允许撤回，先不判断）
                return OperateResult.operationFailure("撤回失败：撤回节点连接有子流程，不允许撤回！");
            }
            if (ifGateWay || "ManualTask".equalsIgnoreCase(type)) {
                result = checkNextNodeNotCompleted(nextActivity, instance, destnetionTask);
                if (!result.successful()) {
                    return result;
                }
            }
            List<HistoricTaskInstance> completeTasks = historyService.createHistoricTaskInstanceQuery()
                    .processInstanceId(instance.getId()).taskDefinitionKey(nextActivity.getId()).finished().list();
            for (HistoricTaskInstance h : completeTasks) {
                if (h.getEndTime().after(destnetionTask.getEndTime())) {
                    return OperateResult.operationFailure("撤回失败：下一节点已执行，不允许撤回！");
                }
            }
            if (ifMultiInstance(currActivity)) {// 如果是多实例任务,判断当前任务是否已经流转到下一节点

                List<HistoricTaskInstance> unCompleteTasks = historyService.createHistoricTaskInstanceQuery()
                        .processInstanceId(instance.getId()).taskDefinitionKey(nextActivity.getId()).unfinished().list();
                for (HistoricTaskInstance h : unCompleteTasks) {
                    if (h.getStartTime().after(destnetionTask.getStartTime())) {
                        return result;
                    }
                }
            }
        }
        return result;
    }


    /**
     * 判断是否是网关节点
     *
     * @param pvmActivity
     * @return
     */
    public static boolean ifGageway(PvmActivity pvmActivity) {
        String nextActivtityType = pvmActivity.getProperty("type").toString();
        Boolean result = false;
        if ("exclusiveGateway".equalsIgnoreCase(nextActivtityType) ||  //排他网关
                "inclusiveGateway".equalsIgnoreCase(nextActivtityType)  //包容网关
                || "parallelGateWay".equalsIgnoreCase(nextActivtityType)//并行网关
        ) { //手工节点
            result = true;
        }
        return result;
    }

    /**
     * 判断是否是排他网关节点
     *
     * @param pvmActivity
     * @return
     */
    public static boolean ifExclusiveGateway(PvmActivity pvmActivity) {
        String nextActivtityType = pvmActivity.getProperty("type").toString();
        Boolean result = false;
        if ("exclusiveGateway".equalsIgnoreCase(nextActivtityType)) { //排他网关
            result = true;
        }
        return result;
    }

    /**
     * 判断是否是多实例任务（会签）
     *
     * @param pvmActivity
     * @return
     */
    public static boolean ifMultiInstance(PvmActivity pvmActivity) {
        Object nextActivtityType = pvmActivity.getProperty("multiInstance");
        Boolean result = false;
        if (nextActivtityType != null && !"".equals(nextActivtityType)) { //多实例任务
            result = true;
        }
        return result;
    }

    public static ResponseData checkCanReject(PvmActivity currActivity, PvmActivity preActivity) {
        if (preActivity == null) {
            return ResponseData.operationFailure("驳回失败：前置节点参数为空!");
        }
        String type = preActivity.getProperty("type") + "";
        if ("callActivity".equalsIgnoreCase(type)) {
            return ResponseData.operationFailure("驳回失败：前置节点是子任务，不能驳回!");
        } else if ("ServiceTask".equalsIgnoreCase(type)) {
            return ResponseData.operationFailure("驳回失败：前置节点是服务任务，不能驳回!");
        } else if ("ReceiveTask".equalsIgnoreCase(type)) {
            return ResponseData.operationFailure("驳回失败：前置节点是接收任务，不能驳回!");
        }
        boolean result = ifMultiInstance(currActivity);
        if (result) {//多任务实例不允许驳回
            return ResponseData.operationFailure("驳回失败：前置节点是多任务实例，不能驳回!");
        }
        List<PvmTransition> currentInTransitionList = currActivity.getIncomingTransitions();
        for (PvmTransition currentInTransition : currentInTransitionList) {
            PvmActivity currentInActivity = currentInTransition.getSource();
            if (currentInActivity.getId().equals(preActivity.getId())) {
                return ResponseData.operationSuccess();
            }
            boolean ifExclusiveGateway = ifExclusiveGateway(currentInActivity);
            if (ifExclusiveGateway) {
                ResponseData responseData = checkCanReject(currentInActivity, preActivity);
                if (responseData.successful()) {
                    return responseData;
                }
            }
        }
        return ResponseData.operationFailure("驳回失败：没有找到符合的驳回条件！");
    }

    private void taskPropertityInit(FlowTask flowTask, FlowHistory preTask, JSONObject currentNode, Map<String, Object> variables) {
        JSONObject normalInfo = currentNode.getJSONObject("nodeConfig").getJSONObject("normal");
        Boolean canReject = null;
        Boolean canSuspension = null;
        WorkPageUrl workPageUrl = null;
        flowTask.setTenantCode(ContextUtil.getTenantCode());
        if (!CollectionUtils.isEmpty(normalInfo)) {
            canReject = normalInfo.get("allowReject") != null ? (Boolean) normalInfo.get("allowReject") : null;
            canSuspension = normalInfo.get("allowTerminate") != null ? (Boolean) normalInfo.get("allowTerminate") : null;
            String workPageUrlId = (String) normalInfo.get("id");
            workPageUrl = workPageUrlDao.findOne(workPageUrlId);
            if (workPageUrl == null) {
                String errorName = normalInfo.get("name") != null ? (String) normalInfo.get("name") : "";
                String workPageName = normalInfo.get("workPageName") != null ? (String) normalInfo.get("workPageName") : "";
                LogUtil.error("节点【" + errorName + "】配置的工作界面【" + workPageName + "】不存在！【workPageId=" + workPageUrlId + "】");
                throw new FlowException("节点【" + errorName + "】配置的工作界面【" + workPageName + "】不存在！");
            }
            flowTask.setWorkPageUrl(workPageUrl);
        }
        try {
            if (variables != null && variables.get("allowChooseInstancyMap") != null) {
                Map<String, Boolean> allowChooseInstancyMap = (Map<String, Boolean>) variables.get("allowChooseInstancyMap");
                if (!CollectionUtils.isEmpty(allowChooseInstancyMap)) {//判断是否定义了紧急处理
                    Boolean allowChooseInstancy = allowChooseInstancyMap.get(flowTask.getActTaskDefKey());
                    if (allowChooseInstancy != null && allowChooseInstancy == true) {
                        flowTask.setPriority(3);
                    }
                }
            }
        } catch (Exception e) {
            LogUtil.error("allowChooseInstancyMap解析错误：{}", e.getMessage(), e);
        }

        flowTask.setCanReject(canReject);
        flowTask.setCanSuspension(canSuspension);
        if (preTask != null) {//初始化上一步的执行历史信息
            if (TaskStatus.REJECT.toString().equalsIgnoreCase(preTask.getTaskStatus())) {
                flowTask.setTaskStatus(TaskStatus.REJECT.toString());
                flowTask.setPriority(1);
            } else if (TaskStatus.CANCEL.toString().equalsIgnoreCase(preTask.getTaskStatus())) {
                flowTask.setPriority(2);
            } else {
                flowTask.setPreId(preTask.getId());
            }
            flowTask.setPreId(preTask.getId());
        }
        String nodeType = (String) currentNode.get("nodeType");
        if ("CounterSign".equalsIgnoreCase(nodeType) || "Approve".equalsIgnoreCase(nodeType) || "Normal".equalsIgnoreCase(nodeType) || "SingleSign".equalsIgnoreCase(nodeType)
                || "ParallelTask".equalsIgnoreCase(nodeType) || "SerialTask".equalsIgnoreCase(nodeType)) {//能否由移动端审批
            Boolean mustCommit = workPageUrl.getMustCommit();
            if (mustCommit == null || !mustCommit) {
                flowTask.setCanMobile(true);
            }
            if ("CounterSign".equalsIgnoreCase(nodeType) || "Approve".equalsIgnoreCase(nodeType)) {//能否批量审批
                //不需要判断当前节点执行人类型，只需要判断是否允许批量，和下一节点信息是否允许当前节点批量提交
                if (mustCommit == null || !mustCommit) {
                    if (checkNextNodesCanAprool(flowTask, null)) {
                        flowTask.setCanBatchApproval(true);
                    } else {
                        flowTask.setCanBatchApproval(false);
                    }
                } else {
                    flowTask.setCanBatchApproval(false);
                }
            }
        }

        //任务额定工时设置(换算成小时，保留两位小数)
        try {
            int executeDay = currentNode.getJSONObject("nodeConfig").getJSONObject("normal").getInt("executeDay");
            int executeHour = currentNode.getJSONObject("nodeConfig").getJSONObject("normal").getInt("executeHour");
            int executeMinute = currentNode.getJSONObject("nodeConfig").getJSONObject("normal").getInt("executeMinute");
            int minute = executeDay * 24 * 60 + executeHour * 60 + executeMinute;
            Double hour = new BigDecimal((float) minute / 60).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
            flowTask.setTiming(hour);
        } catch (Exception e) {
        }

    }


    /**
     * 将新的流程任务初始化
     *
     * @param flowInstance
     * @param actTaskDefKeyCurrent
     */
    public void initCounterSignAddTask(FlowInstance flowInstance, String actTaskDefKeyCurrent, String userId, String preId) {
        List<Task> taskList = null;
        String actProcessInstanceId = flowInstance.getActInstanceId();
        if (StringUtils.isNotEmpty(actTaskDefKeyCurrent)) {
            taskList = taskService.createTaskQuery().processInstanceId(actProcessInstanceId).taskDefinitionKey(actTaskDefKeyCurrent).active().list();
        }

        if (!CollectionUtils.isEmpty(taskList)) {
            Definition definition = flowCommonUtil.flowDefinition(flowInstance.getFlowDefVersion());
            String flowName = definition.getProcess().getName();
            for (Task task : taskList) {
                String actTaskDefKey = task.getTaskDefinitionKey();
                JSONObject currentNode = definition.getProcess().getNodes().getJSONObject(actTaskDefKey);
                List<FlowTask> tempFlowTasks = flowTaskDao.findByActTaskId(task.getId());
                FlowTask tempFlowTask = tempFlowTasks.get(0);
                if (tempFlowTask != null) {
                    continue;
                }
                if (StringUtils.isNotEmpty(userId)) {
                    Executor executor = flowCommonUtil.getBasicUserExecutor(userId);
                    if (executor != null) {
                        FlowTask flowTask = new FlowTask();
                        flowTask.setPreId(preId);
                        flowTask.setTaskJsonDef(currentNode.toString());
                        flowTask.setFlowDefinitionId(flowInstance.getFlowDefVersion().getFlowDefination().getId());
                        flowTask.setActTaskDefKey(actTaskDefKey);
                        flowTask.setFlowName(flowName);
                        flowTask.setTaskName(task.getName());
                        flowTask.setActTaskId(task.getId());
                        flowTask.setOwnerId(executor.getId());
                        flowTask.setOwnerAccount(executor.getCode());
                        flowTask.setOwnerName(executor.getName());
                        flowTask.setExecutorAccount(executor.getCode());
                        flowTask.setExecutorId(executor.getId());
                        flowTask.setExecutorName(executor.getName());
                        //添加组织机构信息
                        flowTask.setExecutorOrgId(executor.getOrganizationId());
                        flowTask.setExecutorOrgCode(executor.getOrganizationCode());
                        flowTask.setExecutorOrgName(executor.getOrganizationName());
                        flowTask.setOwnerOrgId(executor.getOrganizationId());
                        flowTask.setOwnerOrgCode(executor.getOrganizationCode());
                        flowTask.setOwnerOrgName(executor.getOrganizationName());

                        flowTask.setActType("candidate");
                        if (StringUtils.isEmpty(task.getDescription())) {
                            flowTask.setDepict("加签的任务");
                        } else {
                            flowTask.setDepict(task.getDescription());
                        }
                        flowTask.setTaskStatus(TaskStatus.INIT.toString());
                        flowTask.setPriority(0);
                        flowTask.setFlowInstance(flowInstance);
                        taskPropertityInit(flowTask, null, currentNode, null);
                        flowTaskDao.save(flowTask);
                    }
                }
            }
        }

    }


    /**
     * 初始化虚拟待办任务
     */
    public void initVirtualTask(String actInstanceId, String actTaskDefKey, String content, List<String> receiverIds) throws Exception {
        FlowInstance flowInstance = flowInstanceDao.findByActInstanceId(actInstanceId);
        if (flowInstance != null) {
            FlowDefVersion flowDefVersion = flowInstance.getFlowDefVersion();
            String flowDefJson = flowDefVersion.getDefJson();
            JSONObject defObj = JSONObject.fromObject(flowDefJson);
            Definition definition = (Definition) JSONObject.toBean(defObj, Definition.class);
            net.sf.json.JSONObject currentNode = definition.getProcess().getNodes().getJSONObject(actTaskDefKey);

            FlowTask virtualTask = new FlowTask();

            virtualTask.setTaskStatus(TaskStatus.VIRTUAL.toString());//待办状态
            virtualTask.setActType("virtual"); //引擎任务类型
            virtualTask.setActTaskId(null);//流程引擎ID（直接用虚拟单词代替）
            virtualTask.setTaskName(currentNode.get("name") + "(虚拟)"); //任务名称
            virtualTask.setActTaskDefKey(actTaskDefKey + "-virtual");//节点代码
            if (StringUtils.isNotEmpty(content)) {
                virtualTask.setDepict(content); //描述（通知里面写的内容）
            } else {
                virtualTask.setDepict("虚拟待办通知"); //描述（通知里面写的内容）
            }
            virtualTask.setTaskJsonDef(currentNode.toString());//当前节点json信息

            JSONObject normalInfo = currentNode.getJSONObject("nodeConfig").getJSONObject("normal");
            String workPageUrlId = (String) normalInfo.get("id");
            WorkPageUrl workPageUrl = workPageUrlDao.findOne(workPageUrlId);
            if (workPageUrl == null) {
                String errorName = normalInfo.get("name") != null ? (String) normalInfo.get("name") : "";
                String workPageName = normalInfo.get("workPageName") != null ? (String) normalInfo.get("workPageName") : "";
                throw new FlowException("生产虚拟待办失败：节点【" + errorName + "】配置的工作界面【" + workPageName + "】不存在！");
            }
            virtualTask.setWorkPageUrl(workPageUrl); //处理表单页面

            virtualTask.setFlowInstance(flowInstance);
            virtualTask.setFlowName(flowInstance.getFlowName()); //流程名称
            virtualTask.setVersion(0);
            virtualTask.setProxyStatus(null);//代理状态
            virtualTask.setFlowDefinitionId(flowDefVersion.getFlowDefination().getId());//流程定义ID
            virtualTask.setActClaimTime(null);//签收时间
            virtualTask.setPriority(0);//优先级
            virtualTask.setActDueDate(null);//流程引擎的实际触发时间
            virtualTask.setActTaskKey(null);//流程引擎的实际任务定义KEY
            virtualTask.setPreId(null);//记录上一个流程历史任务的id
            virtualTask.setCanReject(false);//是否允许驳回
            virtualTask.setCanSuspension(false);//是否允许流程终止
            virtualTask.setExecuteTime(null);//额定工时
            virtualTask.setCanBatchApproval(false);//是否批量
            virtualTask.setCanMobile(false);//能否移动端
            virtualTask.setTrustState(null);//转办委托状态
            virtualTask.setTrustOwnerTaskId(null);//被委托任务的ID
            virtualTask.setAllowAddSign(false);//允许加签
            virtualTask.setAllowSubtractSign(false);//允许减签
            virtualTask.setTenantCode(ContextUtil.getTenantCode());//租户
            virtualTask.setTiming(0.00);//任务额定工时
            List<Executor> executorList = flowCommonUtil.getBasicUserExecutors(receiverIds);
            List<FlowTask> needAddList = new ArrayList<>(); //需要新增的待办
            for (Executor executor : executorList) {
                FlowTask bean = new FlowTask();
                BeanUtils.copyProperties(virtualTask, bean);
                bean.setExecutorId(executor.getId());
                bean.setExecutorAccount(executor.getCode());
                bean.setExecutorName(executor.getName());
                bean.setExecutorOrgId(executor.getOrganizationId());
                bean.setExecutorOrgCode(executor.getOrganizationCode());
                bean.setExecutorOrgName(executor.getOrganizationName());
                bean.setOwnerId(executor.getId());
                bean.setOwnerAccount(executor.getCode());
                bean.setOwnerName(executor.getName());
                bean.setOwnerOrgId(executor.getOrganizationId());
                bean.setOwnerOrgCode(executor.getOrganizationCode());
                bean.setOwnerOrgName(executor.getOrganizationName());
                flowTaskDao.save(bean);
                needAddList.add(bean);
            }
            //是否推送信息到baisc
            Boolean pushBasic = flowTaskService.getBooleanPushTaskToBasic();
            if (pushBasic) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        flowTaskService.pushToBasic(needAddList, null, null, null);
                    }
                }).start();
            }
        } else {
            throw new FlowException("生产虚拟待办失败：该流程实例不存在！");
        }
    }


    /**
     * 将新的流程任务初始化
     *
     * @param flowInstance
     * @param actTaskDefKeyCurrent
     */
    public void initTask(FlowInstance flowInstance, FlowHistory preTask, String actTaskDefKeyCurrent, Map<String, Object> variables) {

        if (flowInstance == null || flowInstance.isEnded()) {
            return;
        }
        List<Task> taskList;
        String actProcessInstanceId = flowInstance.getActInstanceId();
        if (StringUtils.isNotEmpty(actTaskDefKeyCurrent)) {
            taskList = taskService.createTaskQuery().processInstanceId(actProcessInstanceId).taskDefinitionKey(actTaskDefKeyCurrent).active().list();
        } else {
            List<FlowInstance> flowInstanceSonList = flowInstanceDao.findByParentId(flowInstance.getId());
            if (!CollectionUtils.isEmpty(flowInstanceSonList)) {//初始化子流程的任务
                for (FlowInstance son : flowInstanceSonList) {
                    initTask(son, preTask, null, variables);
                }
            }
            taskList = taskService.createTaskQuery().processInstanceId(actProcessInstanceId).active().list();
        }
        if (!CollectionUtils.isEmpty(taskList)) {
            Boolean allowAddSign = null;//允许加签
            Boolean allowSubtractSign = null;//允许减签
            Definition definition = flowCommonUtil.flowDefinition(flowInstance.getFlowDefVersion());
            String flowName = definition.getProcess().getName();
            //是否推送信息到baisc
            Boolean pushBasic = flowTaskService.getBooleanPushTaskToBasic();

            List<FlowTask> pushTaskList = new ArrayList<>();  //需要推送到basic的待办
            for (Task task : taskList) {
                String actTaskDefKey = task.getTaskDefinitionKey();
                JSONObject currentNode = definition.getProcess().getNodes().getJSONObject(actTaskDefKey);
                String nodeType = (String) currentNode.get("nodeType");
                if (("CounterSign".equalsIgnoreCase(nodeType) || "ParallelTask".equalsIgnoreCase(nodeType) || "SerialTask".equalsIgnoreCase(nodeType))) {

                    List<FlowTask> tempFlowTasks = flowTaskDao.findByActTaskId(task.getId());
                    if (!CollectionUtils.isEmpty(tempFlowTasks)) {
                        continue;
                    }
                    //串行会签，将上一步执行历史，换成真实的上一步执行节点信息
                    //判断是否是串行会签
                    try {
                        Boolean isSequential = currentNode.getJSONObject("nodeConfig").getJSONObject("normal").getBoolean("isSequential");
                        if (isSequential && preTask != null) {
                            // 取得当前任务
                            HistoricTaskInstance currTask = historyService.createHistoricTaskInstanceQuery().taskId(actTaskDefKey)
                                    .singleResult();
                            String executionId = currTask.getExecutionId();
                            Integer nrOfCompletedInstances = (Integer) runtimeService.getVariable(executionId, "nrOfCompletedInstances");
                            if (nrOfCompletedInstances > 1) {
                                FlowHistory flowHistory = flowHistoryDao.findOne(preTask.getPreId());
                                if (flowHistory != null) {
                                    preTask = flowHistory;
                                }
                            }
                        }
                    } catch (Exception e) {
                    }
                    try {
                        allowAddSign = currentNode.getJSONObject("nodeConfig").getJSONObject("normal").getBoolean("allowAddSign");
                        allowSubtractSign = currentNode.getJSONObject("nodeConfig").getJSONObject("normal").getBoolean("allowSubtractSign");
                    } catch (Exception eSign) {
                    }

                } else {
                    String taskActKey = task.getTaskDefinitionKey();
                    Integer flowTaskNow = flowTaskDao.findCountByActTaskDefKeyAndActInstanceId(taskActKey, flowInstance.getActInstanceId());
                    if (flowTaskNow != null && flowTaskNow > 0) {
                        continue;
                    }
                }

                List<IdentityLink> identityLinks;

                try {
                    identityLinks = taskService.getIdentityLinksForTask(task.getId());
                } catch (Exception e) {
                    return;
                }

                if (CollectionUtils.isEmpty(identityLinks)) {//多实例任务为null
                    /** 获取流程变量 **/
                    String executionId = task.getExecutionId();
                    String variableName = "" + actTaskDefKey + "_CounterSign";
                    String userId = runtimeService.getVariable(executionId, variableName) + "";//使用执行对象Id和流程变量名称，获取值
                    if (StringUtils.isNotEmpty(userId)) {
                        Executor executor = flowCommonUtil.getBasicUserExecutor(userId);
                        if (executor != null) {
                            FlowTask flowTask = new FlowTask();
                            flowTask.setAllowAddSign(allowAddSign);
                            flowTask.setAllowSubtractSign(allowSubtractSign);
                            flowTask.setTenantCode(ContextUtil.getTenantCode());
                            flowTask.setTaskJsonDef(currentNode.toString());
                            flowTask.setFlowDefinitionId(flowInstance.getFlowDefVersion().getFlowDefination().getId());
                            flowTask.setActTaskDefKey(actTaskDefKey);
                            flowTask.setFlowName(flowName);
                            flowTask.setTaskName(task.getName());
                            flowTask.setActTaskId(task.getId());
                            flowTask.setOwnerId(executor.getId());
                            flowTask.setOwnerAccount(executor.getCode());
                            flowTask.setOwnerName(executor.getName());
                            //添加组织机构信息
                            flowTask.setOwnerOrgId(executor.getOrganizationId());
                            flowTask.setOwnerOrgCode(executor.getOrganizationCode());
                            flowTask.setOwnerOrgName(executor.getOrganizationName());
                            //通过授权用户ID和流程类型返回转授权信息（转办模式）
                            TaskMakeOverPower taskMakeOverPower = taskMakeOverPowerService.getMakeOverPowerByTypeAndUserId(executor.getId(), flowInstance.getFlowDefVersion().getFlowDefination().getFlowType().getId());
                            if (taskMakeOverPower != null) {
                                flowTask.setExecutorAccount(taskMakeOverPower.getPowerUserAccount());
                                flowTask.setExecutorId(taskMakeOverPower.getPowerUserId());
                                flowTask.setExecutorName(taskMakeOverPower.getPowerUserName());
                                //添加组织机构信息
                                flowTask.setExecutorOrgId(taskMakeOverPower.getPowerUserOrgId());
                                flowTask.setExecutorOrgCode(taskMakeOverPower.getPowerUserOrgCode());
                                flowTask.setExecutorOrgName(taskMakeOverPower.getPowerUserOrgName());
                                if (StringUtils.isEmpty(task.getDescription())) {
                                    flowTask.setDepict("【转授权-" + executor.getName() + "授权】" + "流程启动");
                                } else {
                                    flowTask.setDepict("【转授权-" + executor.getName() + "授权】" + task.getDescription());
                                }
                            } else {
                                flowTask.setExecutorAccount(executor.getCode());
                                flowTask.setExecutorId(executor.getId());
                                flowTask.setExecutorName(executor.getName());
                                //添加组织机构信息
                                flowTask.setExecutorOrgId(executor.getOrganizationId());
                                flowTask.setExecutorOrgCode(executor.getOrganizationCode());
                                flowTask.setExecutorOrgName(executor.getOrganizationName());
                                if (StringUtils.isEmpty(task.getDescription())) {
                                    flowTask.setDepict("流程启动");
                                } else {
                                    flowTask.setDepict(task.getDescription());
                                }
                            }
                            flowTask.setActType("candidate");
                            flowTask.setTaskStatus(TaskStatus.INIT.toString());
                            flowTask.setPriority(0);
                            flowTask.setFlowInstance(flowInstance);
                            taskPropertityInit(flowTask, preTask, currentNode, variables);
                            flowTaskDao.save(flowTask);
                            if (pushBasic) {
                                pushTaskList.add(flowTask);
                            }
                        }
                    }
                } else {
                    List<String> checkChongfu = new ArrayList<>();
                    for (IdentityLink identityLink : identityLinks) {
                        String key = identityLink.getTaskId() + identityLink.getUserId();
                        if (checkChongfu.contains(key)) {
                            continue;
                        } else {
                            checkChongfu.add(key);
                        }
                        Executor executor = null;
                        if (!Constants.ANONYMOUS.equalsIgnoreCase(identityLink.getUserId())) {
                            String linkIds = identityLink.getUserId();
                            linkIds = XmlUtil.trimFirstAndLastChar(linkIds, '[');
                            linkIds = XmlUtil.trimFirstAndLastChar(linkIds, ']');
                            List<String> userIds = Arrays.asList(StringUtils.split(linkIds, ','));
                            executor = flowCommonUtil.getBasicUserExecutor(userIds.get(0));
                        }
                        if ("poolTask".equalsIgnoreCase(nodeType) && executor == null) {
                            Map<String, VariableInstance> processVariables = runtimeService.getVariableInstances(actProcessInstanceId);
                            String userId = null;
                            if (processVariables.get(Constants.POOL_TASK_CALLBACK_USER_ID + actTaskDefKey) != null) {
                                userId = (String) processVariables.get(Constants.POOL_TASK_CALLBACK_USER_ID + actTaskDefKey).getValue();//是否直接返回了执行人
                            }

                            List<Executor> executorList = null;
                            if (StringUtils.isNotEmpty(userId)) {
                                //为了兼容以前版本，工作池任务添加多执行人直接用逗号隔开进行添加
                                String[] idArray = userId.split(",");
                                List<String> userList = Arrays.asList(idArray);
                                executorList = flowCommonUtil.getBasicUserExecutors(userList);
                            }
                            if (executorList != null && executorList.size() != 0) {
                                for (Executor man : executorList) {
                                    FlowTask flowTask = new FlowTask();
                                    flowTask.setTenantCode(ContextUtil.getTenantCode());
                                    flowTask.setOwnerId(man.getId());
                                    flowTask.setOwnerAccount(man.getCode());
                                    flowTask.setOwnerName(man.getName());
                                    flowTask.setExecutorAccount(man.getCode());
                                    flowTask.setExecutorId(man.getId());
                                    flowTask.setExecutorName(man.getName());
                                    //添加组织机构信息
                                    flowTask.setExecutorOrgId(man.getOrganizationId());
                                    flowTask.setExecutorOrgCode(man.getOrganizationCode());
                                    flowTask.setExecutorOrgName(man.getOrganizationName());
                                    flowTask.setOwnerOrgId(man.getOrganizationId());
                                    flowTask.setOwnerOrgCode(man.getOrganizationCode());
                                    flowTask.setOwnerOrgName(man.getOrganizationName());

                                    flowTask.setTaskJsonDef(currentNode.toString());
                                    flowTask.setFlowDefinitionId(flowInstance.getFlowDefVersion().getFlowDefination().getId());
                                    flowTask.setActTaskDefKey(actTaskDefKey);
                                    flowTask.setFlowName(flowName);
                                    flowTask.setTaskName(task.getName());
                                    flowTask.setActTaskId(task.getId());
                                    flowTask.setActType(identityLink.getType());
                                    flowTask.setDepict(task.getDescription());
                                    flowTask.setTaskStatus(TaskStatus.INIT.toString());
                                    flowTask.setPriority(0);
                                    flowTask.setFlowInstance(flowInstance);
                                    taskPropertityInit(flowTask, preTask, currentNode, variables);
                                    flowTaskDao.save(flowTask);
                                    if (pushBasic) {
                                        pushTaskList.add(flowTask);
                                    }
                                }
                            } else {
                                FlowTask flowTask = new FlowTask();
                                flowTask.setTenantCode(ContextUtil.getTenantCode());
                                flowTask.setOwnerAccount(Constants.ANONYMOUS);
                                flowTask.setOwnerId(Constants.ANONYMOUS);
                                flowTask.setOwnerName(Constants.ANONYMOUS);
                                flowTask.setExecutorAccount(Constants.ANONYMOUS);
                                flowTask.setExecutorId(Constants.ANONYMOUS);
                                flowTask.setExecutorName(Constants.ANONYMOUS);
                                flowTask.setTaskJsonDef(currentNode.toString());
                                flowTask.setFlowDefinitionId(flowInstance.getFlowDefVersion().getFlowDefination().getId());
                                flowTask.setActTaskDefKey(actTaskDefKey);
                                flowTask.setFlowName(flowName);
                                flowTask.setTaskName(task.getName());
                                flowTask.setActTaskId(task.getId());
                                flowTask.setActType(identityLink.getType());
                                flowTask.setDepict(task.getDescription());
                                flowTask.setTaskStatus(TaskStatus.INIT.toString());
                                flowTask.setPriority(0);
                                flowTask.setFlowInstance(flowInstance);
                                taskPropertityInit(flowTask, preTask, currentNode, variables);
                                flowTaskDao.save(flowTask);
                                if (pushBasic) {
                                    pushTaskList.add(flowTask);
                                }
                            }
                        } else {

                            if (executor != null) {
                                FlowTask flowTask = new FlowTask();
                                flowTask.setAllowAddSign(allowAddSign);
                                flowTask.setAllowSubtractSign(allowSubtractSign);
                                flowTask.setTenantCode(ContextUtil.getTenantCode());
                                flowTask.setTaskJsonDef(currentNode.toString());
                                flowTask.setFlowDefinitionId(flowInstance.getFlowDefVersion().getFlowDefination().getId());
                                flowTask.setActTaskDefKey(actTaskDefKey);
                                flowTask.setFlowName(flowName);
                                flowTask.setTaskName(task.getName());
                                flowTask.setActTaskId(task.getId());
                                flowTask.setOwnerAccount(executor.getCode());
                                flowTask.setOwnerId(executor.getId());
                                flowTask.setOwnerName(executor.getName());
                                //添加组织机构信息
                                flowTask.setOwnerOrgId(executor.getOrganizationId());
                                flowTask.setOwnerOrgCode(executor.getOrganizationCode());
                                flowTask.setOwnerOrgName(executor.getOrganizationName());
                                //通过授权用户ID和流程类型返回转授权信息（转办模式）
                                TaskMakeOverPower taskMakeOverPower = taskMakeOverPowerService.getMakeOverPowerByTypeAndUserId(executor.getId(), flowInstance.getFlowDefVersion().getFlowDefination().getFlowType().getId());
                                if (taskMakeOverPower != null) {
                                    flowTask.setExecutorId(taskMakeOverPower.getPowerUserId());
                                    flowTask.setExecutorAccount(taskMakeOverPower.getPowerUserAccount());
                                    flowTask.setExecutorName(taskMakeOverPower.getPowerUserName());
                                    //添加组织机构信息
                                    flowTask.setExecutorOrgId(taskMakeOverPower.getPowerUserOrgId());
                                    flowTask.setExecutorOrgCode(taskMakeOverPower.getPowerUserOrgCode());
                                    flowTask.setExecutorOrgName(taskMakeOverPower.getPowerUserOrgName());
                                    if (StringUtils.isEmpty(task.getDescription())) {
                                        flowTask.setDepict("【转授权-" + executor.getName() + "授权】");
                                    } else {
                                        flowTask.setDepict("【转授权-" + executor.getName() + "授权】" + task.getDescription());
                                    }
                                } else {
                                    flowTask.setExecutorAccount(executor.getCode());
                                    flowTask.setExecutorId(executor.getId());
                                    flowTask.setExecutorName(executor.getName());
                                    flowTask.setDepict(task.getDescription());
                                    //添加组织机构信息
                                    flowTask.setExecutorOrgId(executor.getOrganizationId());
                                    flowTask.setExecutorOrgCode(executor.getOrganizationCode());
                                    flowTask.setExecutorOrgName(executor.getOrganizationName());
                                }
                                flowTask.setActType(identityLink.getType());
                                flowTask.setTaskStatus(TaskStatus.INIT.toString());
                                flowTask.setPriority(0);
                                flowTask.setFlowInstance(flowInstance);
                                try {
                                    Boolean allowJumpBack = (Boolean) variables.get("allowJumpBack");
                                    if (allowJumpBack) {
                                        flowTask.setJumpBackPrevious(true);
                                    }
                                } catch (Exception e) {
                                }
                                taskPropertityInit(flowTask, preTask, currentNode, variables);
                                flowTaskDao.save(flowTask);
                                if (pushBasic) {
                                    pushTaskList.add(flowTask);
                                }
                            } else {
                                throw new RuntimeException("id=" + identityLink.getUserId() + "的用户找不到！");
                            }
                        }
                    }
                }
            }
            //需要异步推送待办到baisc
            if (pushBasic && !CollectionUtils.isEmpty(pushTaskList)) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            //为推送的待办添加是否为自动执行的标记
                            flowTaskService.addTaskAutoStatus(pushTaskList);
                        } catch (Exception e) {
                            LogUtil.error("为推送的待办添加自动执行标记失败：{}", e.getMessage(), e);
                        } finally {
                            flowTaskService.pushToBasic(pushTaskList, null, null, null);
                        }
                    }
                }).start();
            }
        }
    }


    /**
     * 检查当前任务的出口节点线上是否存在条件表达式
     *
     * @param currActivity 当前任务
     * @return
     */
    public boolean checkHasConditon(PvmActivity currActivity) {
        boolean result = false;
        List<PvmTransition> nextTransitionList = currActivity.getOutgoingTransitions();
        // 判断出口线上是否存在condtion表达式
        if (!CollectionUtils.isEmpty(nextTransitionList)) {
            for (PvmTransition pv : nextTransitionList) {
                PvmActivity currTempActivity = pv.getDestination();
                String nextActivtityType = currTempActivity.getProperty("type").toString();
                if ("parallelGateWay".equalsIgnoreCase(nextActivtityType)) { //并行网关,直接忽略
                    return false;
                }
                Boolean ifGateWay = ifGageway(currTempActivity);
                if (ifGateWay) {
                    result = checkHasConditon(currTempActivity);
                    if (result) {
                        return result;
                    }
                }
                String type = (String) pv.getDestination().getProperty("type");
                List<PvmTransition> nextTransitionList2 = currTempActivity.getOutgoingTransitions();
                String pvId = pv.getId();
                String conditionText = (String) pv.getProperty("conditionText");
                String name = (String) pv.getProperty("name");
                Condition conditon = (Condition) pv.getProperty("condition");
                if (conditon != null || conditionText != null) {
                    result = true;
                    break;
                }
            }
        }

        return result;
    }


    /**
     * 选择符合条件的节点
     *
     * @param v
     * @return
     * @throws SecurityException
     * @throws NoSuchMethodException
     */
    public List<NodeInfo> selectQualifiedNode(FlowTask flowTask, PvmActivity currActivity, Map<String, Object> v, List<String> includeNodeIds)
            throws NoSuchMethodException, SecurityException {
        List<NodeInfo> qualifiedNode = new ArrayList<>();
        List<PvmActivity> results = new ArrayList<>();
        PvmNodeInfo pvmNodeInfo = checkFuHeConditon(currActivity, v);
        initPvmActivityList(pvmNodeInfo, results);
        // 前端需要的数据
        if (!CollectionUtils.isEmpty(results)) {
            for (PvmActivity tempActivity : results) {
                NodeInfo tempNodeInfo = new NodeInfo();
                if (!CollectionUtils.isEmpty(includeNodeIds)) {
                    if (!includeNodeIds.contains(tempActivity.getId())) {
                        continue;
                    }
                }
                tempNodeInfo = convertNodes(flowTask, tempNodeInfo, tempActivity);
                qualifiedNode.add(tempNodeInfo);
            }
        }
        return qualifiedNode;
    }

    private List<String> initIncludeNodeIds(List<String> includeNodeIds, String actTaskId, Map<String, Object> v) throws NoSuchMethodException, SecurityException {

        //检查是否包含的节点中是否有网关，有则进行替换
        List<String> includeNodeIdsNew = new ArrayList<>();
        if (!CollectionUtils.isEmpty(includeNodeIds)) {
            includeNodeIdsNew.addAll(includeNodeIds);
            // 取得当前任务
            HistoricTaskInstance currTask = historyService
                    .createHistoricTaskInstanceQuery().taskId(actTaskId)
                    .singleResult();
            ProcessDefinitionEntity definition = (ProcessDefinitionEntity) ((RepositoryServiceImpl) repositoryService)
                    .getDeployedProcessDefinition(currTask
                            .getProcessDefinitionId());
            if (definition == null) {
                LogUtil.error(ContextUtil.getMessage("10003"));
            }
            for (String includeNodeId : includeNodeIds) {
                // 取得当前活动定义节点
                ActivityImpl tempActivity = ((ProcessDefinitionImpl) definition)
                        .findActivity(includeNodeId);
                if (tempActivity != null && ifGageway(tempActivity)) {
                    List<PvmActivity> results = new ArrayList<PvmActivity>();
                    includeNodeIdsNew.remove(includeNodeId);
                    PvmNodeInfo pvmNodeInfo = this.checkFuHeConditon(tempActivity, v);
                    this.initPvmActivityList(pvmNodeInfo, results);
                    if (results != null) {
                        for (PvmActivity p : results) {
                            includeNodeIdsNew.add(p.getId());
                        }
                        includeNodeIdsNew = initIncludeNodeIds(includeNodeIdsNew, actTaskId, v);
                    }
                }
            }
        }
        return includeNodeIdsNew;
    }

    private List<NodeInfo> getNodeInfo(List<String> includeNodeIds, FlowTask flowTask) {
        List<NodeInfo> result = new ArrayList<>();
        if (!CollectionUtils.isEmpty(includeNodeIds)) {
            // 取得当前任务
            HistoricTaskInstance currTask = historyService
                    .createHistoricTaskInstanceQuery().taskId(flowTask.getActTaskId())
                    .singleResult();
            ProcessDefinitionEntity definition = (ProcessDefinitionEntity) ((RepositoryServiceImpl) repositoryService)
                    .getDeployedProcessDefinition(currTask
                            .getProcessDefinitionId());
            if (definition == null) {
                LogUtil.error(ContextUtil.getMessage("10003"));
            }
            for (String includeNodeId : includeNodeIds) {
                // 取得当前活动定义节点
                ActivityImpl tempActivity = ((ProcessDefinitionImpl) definition)
                        .findActivity(includeNodeId);
                if (tempActivity != null) {
                    NodeInfo tempNodeInfo = new NodeInfo();
//                    tempNodeInfo.setCurrentTaskType(flowTask.);
                    this.convertNodes(flowTask, tempNodeInfo, tempActivity);
                    result.add(tempNodeInfo);
                }
            }
        }
        return result;
    }

    public List<NodeInfo> getCallActivityNodeInfo(FlowTask flowTask, String currNodeId, List<NodeInfo> result) {
        FlowInstance flowInstance = flowTask.getFlowInstance();
        Definition definitionP = flowCommonUtil.flowDefinition(flowInstance.getFlowDefVersion());
        JSONObject currentNode = definitionP.getProcess().getNodes().getJSONObject(currNodeId);
        JSONObject normal = currentNode.getJSONObject("nodeConfig").getJSONObject("normal");
        String currentVersionId = (String) normal.get("currentVersionId");
        FlowDefVersion flowDefVersion = flowDefVersionDao.findOne(currentVersionId);
        if (flowDefVersion != null && flowDefVersion.getFlowDefinationStatus() == FlowDefinationStatus.Activate) {
            Definition definitionSon = flowCommonUtil.flowDefinition(flowDefVersion);
            List<StartEvent> startEventList = definitionSon.getProcess().getStartEvent();
            if (startEventList != null && startEventList.size() == 1) {
                StartEvent startEvent = startEventList.get(0);
                JSONObject startEventNode = definitionSon.getProcess().getNodes().getJSONObject(startEvent.getId());
                FlowStartVO flowStartVO = new FlowStartVO();
                flowStartVO.setBusinessKey(flowInstance.getBusinessId());
                try {
                    String callActivityDefKey = (String) normal.get("callActivityDefKey");
                    String businessVName = "/" + definitionP.getProcess().getId() + "/" + currentNode.get("id");
                    if (StringUtils.isNotEmpty(flowInstance.getCallActivityPath())) {
                        businessVName = flowInstance.getCallActivityPath() + businessVName;
                    }
                    result = flowDefinationService.findXunFanNodesInfo(result, flowStartVO, flowDefVersion.getFlowDefination(), definitionSon, startEventNode, businessVName);
                    if (!CollectionUtils.isEmpty(result)) {
                        for (NodeInfo nodeInfo : result) {
                            if (StringUtils.isEmpty(nodeInfo.getCallActivityPath())) {
                                businessVName += "/" + callActivityDefKey;
                                nodeInfo.setCallActivityPath(businessVName);
                            }
                        }
                    }
                } catch (Exception e) {

                }
            }
        } else {
            throw new RuntimeException("找不到子流程");
        }
        return result;
    }

    private void shenPiNodesInit(PvmActivity currActivity, List<NodeInfo> result, boolean approved, FlowTask flowTask, Map<String, Object> v)
            throws NoSuchMethodException, SecurityException {
        PvmActivity gateWayIn = currActivity.getOutgoingTransitions().get(0).getDestination();
        List<PvmTransition> nextTransitionList = gateWayIn.getOutgoingTransitions();
        if (!CollectionUtils.isEmpty(nextTransitionList)) {
            for (PvmTransition pv : nextTransitionList) {
                String conditionText = (String) pv.getProperty("conditionText");
                Boolean mark = false;
                if (approved) {
                    if ("${approveResult == true}".equalsIgnoreCase(conditionText)) {
                        mark = true;
                    }
                } else {
                    if (StringUtils.isEmpty(conditionText)) {
                        mark = true;
                    }
                }
                if (mark) {
                    PvmActivity currTempActivity = pv.getDestination();
                    String type = currTempActivity.getProperty("type") + "";
                    if (ifGageway(currTempActivity) || "ManualTask".equalsIgnoreCase(type)) {
                        List<NodeInfo> temp = this.selectQualifiedNode(flowTask, currTempActivity, v, null);
                        result.addAll(temp);
                    } else if ("CallActivity".equalsIgnoreCase(type)) {
                        result = getCallActivityNodeInfo(flowTask, currTempActivity.getId(), result);
                    } else {
                        NodeInfo tempNodeInfo = new NodeInfo();
                        tempNodeInfo = convertNodes(flowTask, tempNodeInfo, currTempActivity);
                        result.add(tempNodeInfo);
                    }
                }
            }
        }
    }

    /**
     * 选择下一步执行的节点信息
     *
     * @param flowTask
     * @return
     * @throws NoSuchMethodException
     */
    public List<NodeInfo> findNextNodesWithCondition(FlowTask flowTask, String approved, List<String> includeNodeIds) throws NoSuchMethodException {
        String actTaskId = flowTask.getActTaskId();
        String businessId = flowTask.getFlowInstance().getBusinessId();
        String actTaskDefKey = flowTask.getActTaskDefKey();

        String actProcessDefinitionId = flowTask.getFlowInstance().getFlowDefVersion().getActDefId();
        ProcessDefinitionEntity definition = (ProcessDefinitionEntity) ((RepositoryServiceImpl) repositoryService)
                .getDeployedProcessDefinition(actProcessDefinitionId);
        PvmActivity currActivity = this.getActivitNode(definition, actTaskDefKey);
        FlowInstance flowInstanceReal = flowTask.getFlowInstance();
        BusinessModel businessModel = flowInstanceReal.getFlowDefVersion().getFlowDefination().getFlowType().getBusinessModel();
        Map<String, Object> v = ExpressionUtil.getPropertiesValuesMap(businessModel, businessId, false);

        List<String> includeNodeIdsNew = initIncludeNodeIds(includeNodeIds, actTaskId, v);

//        if(ifMultiInstance(currActivity)){//如果是多实例任务
        String defJson = flowTask.getTaskJsonDef();
        JSONObject defObj = JSONObject.fromObject(defJson);
        String nodeType = (String) defObj.get("nodeType");
        List<NodeInfo> result = new ArrayList<NodeInfo>();

        //是否直接返回上一步
        if (flowTask.getJumpBackPrevious() != null && flowTask.getJumpBackPrevious() == true) {
            NodeInfo tempNodeInfo = this.getParentNodeInfoByTask(flowTask, definition);
            result.add(tempNodeInfo);
            return result;
        }

        if ("CounterSign".equalsIgnoreCase(nodeType)) {//会签任务
            int counterDecision = 100;
            try {
                counterDecision = defObj.getJSONObject("nodeConfig").getJSONObject("normal").getInt("counterDecision");
            } catch (Exception e) {
            }
            //会签结果是否即时生效
            Boolean immediatelyEnd = false;
            try {
                immediatelyEnd = defObj.getJSONObject("nodeConfig").getJSONObject("normal").getBoolean("immediatelyEnd");
            } catch (Exception e) {
            }

            // 取得当前任务
            HistoricTaskInstance currTask = historyService
                    .createHistoricTaskInstanceQuery().taskId(flowTask.getActTaskId())
                    .singleResult();
            String executionId = currTask.getExecutionId();
            Map<String, VariableInstance> processVariables = runtimeService.getVariableInstances(executionId);
            //完成会签的次数
            Integer completeCounter = (Integer) processVariables.get("nrOfCompletedInstances").getValue();
            //总循环次数
            Integer instanceOfNumbers = (Integer) processVariables.get("nrOfInstances").getValue();
            if (completeCounter + 1 == instanceOfNumbers) {//会签最后一个执行人
                Boolean approveResult = null;
                //通过票数
                Integer counterSignAgree = 0;
                //completeCounter==0 表示会签的第一人(会签第一人不取数据库参数，因为可能是上一次会签的数据)
                if (processVariables.get(Constants.COUNTER_SIGN_AGREE + currTask.getTaskDefinitionKey()) != null && completeCounter != 0) {
                    counterSignAgree = (Integer) processVariables.get(Constants.COUNTER_SIGN_AGREE + currTask.getTaskDefinitionKey()).getValue();
                }
                Integer value = 0;//默认弃权
                if ("true".equalsIgnoreCase(approved)) {
                    counterSignAgree++;
                }
                if (counterDecision <= ((counterSignAgree / (instanceOfNumbers + 0.0)) * 100)) {//获取通过节点
                    approveResult = true;
                    shenPiNodesInit(currActivity, result, approveResult, flowTask, v);
                } else {//获取不通过节点
                    approveResult = false;
                    shenPiNodesInit(currActivity, result, approveResult, flowTask, v);
                }
                return result;
            } else if (immediatelyEnd) { //会签结果是否即时生效
                if ("true".equalsIgnoreCase(approved)) {
                    //通过票数
                    Integer counterSignAgree = 0;
                    if (processVariables.get(Constants.COUNTER_SIGN_AGREE + currTask.getTaskDefinitionKey()) != null && completeCounter != 0) {
                        counterSignAgree = (Integer) processVariables.get(Constants.COUNTER_SIGN_AGREE + currTask.getTaskDefinitionKey()).getValue();
                    }
                    counterSignAgree++;
                    if (counterDecision <= ((counterSignAgree / (instanceOfNumbers + 0.0)) * 100)) {//获取通过节点
                        shenPiNodesInit(currActivity, result, true, flowTask, v);
                        return result;
                    }
                } else {
                    //不通过票数
                    Integer counterSignOpposition = 0;
                    if (processVariables.get(Constants.COUNTER_SIGN_OPPOSITION + currTask.getTaskDefinitionKey()) != null && completeCounter != 0) {
                        counterSignOpposition = (Integer) processVariables.get(Constants.COUNTER_SIGN_OPPOSITION + currTask.getTaskDefinitionKey()).getValue();
                    }
                    counterSignOpposition++;
                    if ((100 - counterDecision) < ((counterSignOpposition / (instanceOfNumbers + 0.0)) * 100)) {//获取不通过节点
                        shenPiNodesInit(currActivity, result, false, flowTask, v);
                        return result;
                    }
                }
                NodeInfo tempNodeInfo = new NodeInfo();
                tempNodeInfo.setType("CounterSignNotEnd");
                tempNodeInfo = convertNodes(flowTask, tempNodeInfo, currActivity);
                result.add(tempNodeInfo);
            } else {
                NodeInfo tempNodeInfo = new NodeInfo();
                tempNodeInfo.setType("CounterSignNotEnd");
                tempNodeInfo = convertNodes(flowTask, tempNodeInfo, currActivity);
                result.add(tempNodeInfo);
            }
            return result;
        } else if ("Approve".equalsIgnoreCase(nodeType)) {//审批任务

            if ("true".equalsIgnoreCase(approved)) { //获取通过节点
                shenPiNodesInit(currActivity, result, true, flowTask, v);
            } else {//获取不通过节点
                shenPiNodesInit(currActivity, result, false, flowTask, v);
            }
            return result;
        } else if ("ParallelTask".equalsIgnoreCase(nodeType) || "SerialTask".equalsIgnoreCase(nodeType)) {
            // 取得当前任务
            HistoricTaskInstance currTask = historyService
                    .createHistoricTaskInstanceQuery().taskId(flowTask.getActTaskId())
                    .singleResult();
            String executionId = currTask.getExecutionId();
            Map<String, VariableInstance> processVariables = runtimeService.getVariableInstances(executionId);
            //完成的次数
            Integer completeCounter = (Integer) processVariables.get("nrOfCompletedInstances").getValue();
            //总循环次数
            Integer instanceOfNumbers = (Integer) processVariables.get("nrOfInstances").getValue();
            if (completeCounter + 1 == instanceOfNumbers) {//最后一个执行人
            } else {
                NodeInfo tempNodeInfo = new NodeInfo();
                tempNodeInfo.setType("CounterSignNotEnd");
                tempNodeInfo = convertNodes(flowTask, tempNodeInfo, currActivity);
                result.add(tempNodeInfo);
                return result;
            }
        }
        if (this.checkSystemExclusiveGateway(flowTask)) {//判断是否存在系统排他网关、系统包容网关
            if (StringUtils.isEmpty(businessId)) {
                throw new RuntimeException("任务出口节点包含条件表达式，请指定业务ID");
            }
            if (!CollectionUtils.isEmpty(includeNodeIdsNew)) {
                result = getNodeInfo(includeNodeIdsNew, flowTask);
            } else {
                result = this.selectNextAllNodesWithGateWay(flowTask, currActivity, v, includeNodeIdsNew);
            }
            return result;
        } else {
            return this.selectNextAllNodesWithGateWay(flowTask, currActivity, v, includeNodeIds);
        }
    }

    /**
     * 获取活动节点
     *
     * @param definition
     * @param taskDefinitionKey
     * @return
     */
    public static PvmActivity getActivitNode(ProcessDefinitionEntity definition, String taskDefinitionKey) {
        if (definition == null) {
            throw new RuntimeException("definition is null!");
        }
        // 取得当前活动定义节点
        ActivityImpl currActivity = ((ProcessDefinitionImpl) definition)
                .findActivity(taskDefinitionKey);
        return currActivity;
    }

    public static boolean checkNextHas(PvmActivity curActivity, PvmActivity destinationActivity) {
        boolean result = false;
        if (curActivity != null) {
            if (curActivity.getId().equals(destinationActivity.getId())) {
                return true;
            } else if (FlowTaskTool.ifGageway(curActivity) || "ManualTask".equalsIgnoreCase(curActivity.getProperty("type") + "")) {
                List<PvmTransition> pvmTransitionList = curActivity.getOutgoingTransitions();
                if (!CollectionUtils.isEmpty(pvmTransitionList)) {
                    for (PvmTransition pv : pvmTransitionList) {
                        result = checkNextHas(pv.getDestination(), destinationActivity);
                        if (result) {
                            return result;
                        }
                    }
                }
            }
        }
        return result;
    }

    public List<Executor> getExecutors(String userType, String ids, String orgId) {
        String[] idsShuZhu = ids.split(",");
        List<String> idList = Arrays.asList(idsShuZhu);
        List<Executor> executors = null;
        if ("Position".equalsIgnoreCase(userType)) {
            //调用岗位获取用户接口
            executors = flowCommonUtil.getBasicExecutorsByPositionIds(idList, orgId);
        } else if ("PositionType".equalsIgnoreCase(userType)) {
            //调用岗位类型获取用户接口
            executors = flowCommonUtil.getBasicExecutorsByPostCatIds(idList, orgId);
        } else if ("AnyOne".equalsIgnoreCase(userType)) {//任意执行人不添加用户
        }
        return executors;
    }


    boolean checkNextNodesCanAprool(FlowTask flowTask, JSONObject currentNode) {
        boolean result = true;
        Definition definition = flowCommonUtil.flowDefinition(flowTask.getFlowInstance().getFlowDefVersion());
        boolean approvePath = false;
        if (currentNode == null) {
            approvePath = true;
            currentNode = definition.getProcess().getNodes().getJSONObject(flowTask.getActTaskDefKey());
        }
        JSONArray targetNodes = currentNode.getJSONArray("target");
        String nodeType = currentNode.get("nodeType") + "";
        if ("ServiceTask".equals(nodeType)) {//服务任务不允许批量审批
            return false;
        } else if ("ReceiveTask".equals(nodeType)) {//接收任务不允许批量审批
            return false;
        } else if ("CallActivity".equalsIgnoreCase(nodeType)) {//子流程不允许批量审批
            return false;
        }
        for (int i = 0; i < targetNodes.size(); i++) {
            JSONObject jsonObject = targetNodes.getJSONObject(i);
            String targetId = jsonObject.getString("targetId");
            if (approvePath) { //需要判断是否是同意分支
                try {
                    JSONObject uelJsonObject = jsonObject.getJSONObject("uel");
                    if (!uelJsonObject.has("agree") || !uelJsonObject.getBoolean("agree")) {
                        continue;
                    }
                } catch (Exception e) {
                    continue;
                }
            }
            JSONObject nextNode = definition.getProcess().getNodes().getJSONObject(targetId);
            try {
                if (nextNode.has("busType")) {
                    String busType = nextNode.getString("busType");
                    if ("ManualExclusiveGateway".equalsIgnoreCase(busType)) {
                        return false;
                    } else if ("exclusiveGateway".equalsIgnoreCase(busType) ||  //排他网关
                            "inclusiveGateway".equalsIgnoreCase(busType)  //包容网关
                            || "parallelGateWay".equalsIgnoreCase(busType)) { //并行网关
                        result = checkNextNodesCanAprool(flowTask, nextNode);
                        if (result == false) {
                            return false;
                        }
                    }
                }

                if (nextNode.getJSONObject("nodeConfig").has("executor")) {
                    String executorJson = nextNode.getJSONObject("nodeConfig").getString("executor");
                    if (StringUtils.isNotBlank(executorJson)) {
                        JSONObject executor = null;
                        // 先判断是否为数组对象
                        try {
                            JSONArray executors = nextNode.getJSONObject("nodeConfig").getJSONArray("executor");
                            executor = executors.getJSONObject(0);
                        } catch (Exception e) {
                        }
                        // 再判断是否为单一对象
                        if (Objects.isNull(executor)) {
                            executor = nextNode.getJSONObject("nodeConfig").getJSONObject("executor");
                        }
                        String userType = (String) executor.get("userType");
                        if ("AnyOne".equalsIgnoreCase(userType)) {
                            return false;
                        }
                    }
                }
            } catch (Exception e) {
                LogUtil.error(e.getMessage(), e);
                result = false;
            }
        }
        return result;
    }


    public FlowHistory initVirtualFlowHistory(FlowTask flowTask) {
        FlowHistory flowHistory = new FlowHistory();
        flowTask.setFlowDefinitionId(flowTask.getFlowDefinitionId());
        flowHistory.setActType(flowTask.getActType());
        flowHistory.setTaskJsonDef(flowTask.getTaskJsonDef());
        flowHistory.setFlowDefId(flowTask.getFlowDefinitionId());
        flowHistory.setCanCancel(null);
        flowHistory.setFlowName(flowTask.getFlowName());
        flowHistory.setDepict(flowTask.getDepict());
        flowHistory.setActClaimTime(flowTask.getActClaimTime());
        flowHistory.setFlowTaskName(flowTask.getTaskName());
        flowHistory.setFlowInstance(flowTask.getFlowInstance());
        flowHistory.setOwnerAccount(flowTask.getOwnerAccount());
        flowHistory.setOwnerId(flowTask.getOwnerId());
        flowHistory.setOwnerName(flowTask.getOwnerName());
        flowHistory.setExecutorAccount(flowTask.getExecutorAccount());
        flowHistory.setExecutorId(flowTask.getExecutorId());
        flowHistory.setExecutorName(flowTask.getExecutorName());
        flowHistory.setCandidateAccount(flowTask.getCandidateAccount());
        //添加组织机构信息
        flowHistory.setExecutorOrgId(flowTask.getExecutorOrgId());
        flowHistory.setExecutorOrgCode(flowTask.getExecutorOrgCode());
        flowHistory.setExecutorOrgName(flowTask.getExecutorOrgName());
        flowHistory.setOwnerOrgId(flowTask.getOwnerOrgId());
        flowHistory.setOwnerOrgCode(flowTask.getOwnerOrgCode());
        flowHistory.setOwnerOrgName(flowTask.getOwnerOrgName());

        flowHistory.setActWorkTimeInMillis(null);
        flowHistory.setActStartTime(flowTask.getCreatedDate());
        flowHistory.setActEndTime(new Date());
        Long actDurationInMillis = flowHistory.getActEndTime().getTime() - flowHistory.getActStartTime().getTime();
        flowHistory.setActDurationInMillis(actDurationInMillis);
        flowHistory.setActHistoryId(null);
        flowHistory.setActTaskDefKey(flowTask.getActTaskDefKey());
        flowHistory.setPreId(flowTask.getPreId());
        flowHistory.setDepict(flowTask.getDepict());
        flowHistory.setTaskStatus(flowTask.getTaskStatus());
        flowHistory.setOldTaskId(flowTask.getId());
        flowHistory.setTiming(flowTask.getTiming() == null ? 0.00 : flowTask.getTiming());
        flowHistory.setDisagreeReasonId(null);
        flowHistory.setDisagreeReasonCode(null);
        flowHistory.setDisagreeReasonName(null);
        flowHistory.setFlowExecuteStatus(FlowExecuteStatus.HAVEREAD.getCode());
        flowHistory.setTenantCode(ContextUtil.getTenantCode());
        return flowHistory;
    }

    public FlowHistory initFlowHistory(FlowTask flowTask, HistoricTaskInstance historicTaskInstance, Boolean canCancel, Map<String, Object> variables) {
        FlowHistory flowHistory = new FlowHistory();
        flowTask.setFlowDefinitionId(flowTask.getFlowDefinitionId());
        flowHistory.setActType(flowTask.getActType());
        flowHistory.setTaskJsonDef(flowTask.getTaskJsonDef());
        flowHistory.setFlowDefId(flowTask.getFlowDefinitionId());
        flowHistory.setCanCancel(canCancel);
        flowHistory.setFlowName(flowTask.getFlowName());
        flowHistory.setDepict(flowTask.getDepict());
        flowHistory.setActClaimTime(flowTask.getActClaimTime());
        flowHistory.setFlowTaskName(flowTask.getTaskName());
        flowHistory.setFlowInstance(flowTask.getFlowInstance());
        flowHistory.setOwnerAccount(flowTask.getOwnerAccount());
        flowHistory.setOwnerId(flowTask.getOwnerId());
        flowHistory.setOwnerName(flowTask.getOwnerName());
        flowHistory.setExecutorAccount(flowTask.getExecutorAccount());
        flowHistory.setExecutorId(flowTask.getExecutorId());
        flowHistory.setExecutorName(flowTask.getExecutorName());
        flowHistory.setCandidateAccount(flowTask.getCandidateAccount());
        //添加组织机构信息
        flowHistory.setExecutorOrgId(flowTask.getExecutorOrgId());
        flowHistory.setExecutorOrgCode(flowTask.getExecutorOrgCode());
        flowHistory.setExecutorOrgName(flowTask.getExecutorOrgName());
        flowHistory.setOwnerOrgId(flowTask.getOwnerOrgId());
        flowHistory.setOwnerOrgCode(flowTask.getOwnerOrgCode());
        flowHistory.setOwnerOrgName(flowTask.getOwnerOrgName());

        flowHistory.setActDurationInMillis(historicTaskInstance.getDurationInMillis());
        flowHistory.setActWorkTimeInMillis(historicTaskInstance.getWorkTimeInMillis());
        flowHistory.setActStartTime(historicTaskInstance.getStartTime());
        flowHistory.setActEndTime(historicTaskInstance.getEndTime());
        flowHistory.setActHistoryId(historicTaskInstance.getId());
        flowHistory.setActTaskDefKey(historicTaskInstance.getTaskDefinitionKey());
        flowHistory.setPreId(flowTask.getPreId());
        flowHistory.setDepict(flowTask.getDepict());
        flowHistory.setTaskStatus(flowTask.getTaskStatus());
        flowHistory.setOldTaskId(flowTask.getId());
        flowHistory.setTiming(flowTask.getTiming() == null ? 0.00 : flowTask.getTiming());

        if (TaskStatus.REJECT.toString().equalsIgnoreCase(flowTask.getTaskStatus())) { //驳回
            flowHistory.setFlowExecuteStatus(FlowExecuteStatus.REJECT.getCode());
        } else { //其他就是TaskStatus.COMPLETED.toString()
            try {
                String approved = (String) variables.get("approved");
                if (approved == null || "null".equalsIgnoreCase(approved)) { //提交
                    String defJson = flowTask.getTaskJsonDef();
                    JSONObject defObj = JSONObject.fromObject(defJson);
                    JSONObject normalInfo = defObj.getJSONObject("nodeConfig").getJSONObject("normal");
                    String nodeType = (String) defObj.get("nodeType");
                    if (nodeType.equalsIgnoreCase("ParallelTask")
                            && normalInfo.has("carbonCopyOrReport")
                            && normalInfo.getBoolean("carbonCopyOrReport")) {
                        flowHistory.setFlowExecuteStatus(FlowExecuteStatus.HAVEREAD.getCode());
                    } else {
                        flowHistory.setFlowExecuteStatus(FlowExecuteStatus.SUBMIT.getCode());
                    }
                } else if ("true".equalsIgnoreCase(approved)) { //同意
                    flowHistory.setFlowExecuteStatus(FlowExecuteStatus.AGREE.getCode());
                } else if ("false".equalsIgnoreCase(approved)) {  //不同意
                    flowHistory.setFlowExecuteStatus(FlowExecuteStatus.DISAGREE.getCode());
                }
            } catch (Exception e) {
            }
        }

        if (variables != null && variables.get("disagreeReasonCode") != null) {
            String disagreeReasonCode = (String) variables.get("disagreeReasonCode");
            DisagreeReason disagreeReason = disagreeReasonService.getDisagreeReasonByCode(disagreeReasonCode);
            if (disagreeReason == null) {
                throw new FlowException("不同意原因代码错误：【" + disagreeReasonCode + "】");
            } else {
                flowHistory.setDisagreeReasonId(disagreeReason.getId());
                flowHistory.setDisagreeReasonCode(disagreeReason.getCode());
                flowHistory.setDisagreeReasonName(disagreeReason.getName());
            }
        }


        if (flowHistory.getActEndTime() == null) {
            flowHistory.setActEndTime(new Date());
        }
        if (flowHistory.getActDurationInMillis() == null) {
            Long actDurationInMillis = flowHistory.getActEndTime().getTime() - flowHistory.getActStartTime().getTime();
            flowHistory.setActDurationInMillis(actDurationInMillis);
        }

        Long loadOverTime = null;
        try {
            loadOverTime = (Long) variables.get("loadOverTime");
        } catch (Exception e) {
        }

        if (loadOverTime != null) {
            Long actWorkTimeInMillis = System.currentTimeMillis() - loadOverTime;
            flowHistory.setActWorkTimeInMillis(actWorkTimeInMillis);
        }

        flowHistory.setTenantCode(ContextUtil.getTenantCode());
        return flowHistory;
    }

    public OperateResultWithData statusCheck(FlowDefinationStatus status, FlowDefinationStatus statusCurrent) {
        OperateResultWithData resultWithData = null;
        if (status == FlowDefinationStatus.Freeze) {
            if (statusCurrent != FlowDefinationStatus.Activate) {
                //10021=当前非激活状态，禁止冻结！
                resultWithData = OperateResultWithData.operationFailure("10021");
            }
        } else if (status == FlowDefinationStatus.Activate) {
            if (statusCurrent != FlowDefinationStatus.Freeze) {
                //10020=当前非冻结状态，禁止激活！
                resultWithData = OperateResultWithData.operationFailure("10020");
            }
        }
        return resultWithData;
    }


    //    @Cacheable(value = "FLowGetParentCodes", key = "'FLowOrgParentCodes_' + #nodeId")
    public List<String> getParentOrgCodes(String nodeId) {
        if (StringUtils.isEmpty(nodeId)) {
            //获取业务数据条件属性值接口【orgId未赋值】
            throw new FlowException("10069");
        }
        List<Organization> organizationsList = flowCommonUtil.getParentOrganizations(nodeId);
        List<String> orgCodesList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(organizationsList)) {
            for (Organization organization : organizationsList) {
                orgCodesList.add(organization.getCode());
            }
        }
        return orgCodesList;
    }


    /**
     * 会签即时结束（达到了会签的通过率或不通过率）
     *
     * @param flowTask     当前待办任务
     * @param flowInstance 当前流程实例
     * @param variables    当前提交参数
     * @param isSequential 并串行（false为并行）
     */
    public void counterSignImmediatelyEnd(FlowTask flowTask, FlowInstance flowInstance, Map<String, Object> variables,
                                          Boolean isSequential, String executionId, Integer instanceOfNumbers) {
        String actInstanceId = flowInstance.getActInstanceId();
        String taskActKey = flowTask.getActTaskDefKey();
        String currentExecutorId = flowTask.getExecutorId();

        String userListDesc = taskActKey + "_List_CounterSign";
        List<String> userListArray = (List<String>) runtimeService.getVariableLocal(actInstanceId, userListDesc);
        List<String> userList = new ArrayList<>(userListArray);
        userList.remove(currentExecutorId);
        if (isSequential) {//串行，其他人待办未生产，将弃权人直接记录到最后审批人意见中
            List<Executor> executorList = flowCommonUtil.getBasicUserExecutors(userList);
            String mes = null;
            if (!CollectionUtils.isEmpty(executorList)) {
                for (Executor bean : executorList) {
                    if (mes == null) {
                        mes = "【自动弃权:" + bean.getName();
                    } else {
                        mes += "," + bean.getName();
                    }
                }
                mes += "】";
            }
            if (StringUtils.isNotEmpty(flowTask.getDepict())) {
                flowTask.setDepict(flowTask.getDepict() + mes);
            } else {
                flowTask.setDepict(mes);
            }
        } else {//并行有待办，将弃权人待办转已办(底层直接删除)
            List<FlowTask> flowTaskList = flowTaskDao.findByActTaskDefKeyAndActInstanceId(taskActKey, actInstanceId);
            //是否推送信息到baisc
            Boolean pushBasic = flowTaskService.getBooleanPushTaskToBasic();
            //需要删除的待办
            List<FlowTask> delList = new ArrayList<>();
            for (FlowTask bean : flowTaskList) {
                if (!bean.getId().equals(flowTask.getId())) {
                    HistoricTaskInstance historicTaskInstance = historyService.createHistoricTaskInstanceQuery().taskId(bean.getActTaskId()).singleResult();
                    FlowHistory flowHistory = this.initFlowHistory(bean, historicTaskInstance, null, variables);
                    flowHistory.setFlowExecuteStatus(FlowExecuteStatus.AUTO.getCode());
                    flowHistory.setActHistoryId(null);
                    //如果是转授权转办模式（获取转授权记录信息）
                    String overPowerStr = taskMakeOverPowerService.getOverPowerStrByDepict(flowHistory.getDepict());
                    if (bean.getTrustState() != null && bean.getTrustState() == 2) {
                        flowHistory.setDepict(overPowerStr + "【会签委托自动清除】");
                    } else {
                        flowHistory.setDepict(overPowerStr + "【系统自动弃权】");
                    }
                    flowHistoryDao.save(flowHistory);
                    delList.add(bean);
                    flowTaskDao.delete(bean);
                }
            }
            if (pushBasic) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        flowTaskService.pushToBasic(null, null, delList, null);
                    }
                }).start();
            }
        }
        //完成总次数 = 总循环次数 - 1
        runtimeService.setVariable(executionId, "nrOfCompletedInstances", instanceOfNumbers - 1);
        //如果是并行，设置当前活动个数为1
        if (!isSequential) {
            runtimeService.setVariable(executionId, "nrOfActiveInstances", 1);
        }
    }


    /**
     * 目标节点基础信息
     */
    public NodeInfo getNodeInfoByTarget(FlowTask flowTask, String targetNodeId, FlowDefVersion flowDefVersion) {
        String currentDefJson = flowTask.getTaskJsonDef();
        JSONObject currentDefObj = JSONObject.fromObject(currentDefJson);
        String currentNodeType = currentDefObj.get("nodeType") + "";

        String actProcessDefinitionId = flowDefVersion.getActDefId();
        ProcessDefinitionEntity actDefinition = (ProcessDefinitionEntity) ((RepositoryServiceImpl) repositoryService)
                .getDeployedProcessDefinition(actProcessDefinitionId);

        PvmActivity lastActivity = this.getActivitNode(actDefinition, targetNodeId);
        NodeInfo tempNodeInfo = new NodeInfo();
        tempNodeInfo.setCurrentTaskType(currentNodeType);
        tempNodeInfo = convertNodes(flowTask, tempNodeInfo, lastActivity);
        tempNodeInfo.setName(tempNodeInfo.getName());
        return tempNodeInfo;
    }

    /**
     * 通过流程任务得到返回上一步的节点信息
     *
     * @param flowTask
     * @param definition
     * @return
     */
    public NodeInfo getParentNodeInfoByTask(FlowTask flowTask, ProcessDefinitionEntity definition) {
        String defJson = flowTask.getTaskJsonDef();
        JSONObject defObj = JSONObject.fromObject(defJson);
        String nodeType = defObj.get("nodeType") + "";

        String flowHistoryId = flowTask.getPreId();
        FlowHistory flowHistory = flowHistoryDao.findOne(flowHistoryId);
        String actTaskKey = flowHistory.getActTaskDefKey();
        PvmActivity lastActivity = this.getActivitNode(definition, actTaskKey);
        NodeInfo tempNodeInfo = new NodeInfo();
        tempNodeInfo.setCurrentTaskType(nodeType);
        tempNodeInfo = convertNodes(flowTask, tempNodeInfo, lastActivity);
        tempNodeInfo.setName("[返回]" + tempNodeInfo.getName());
        tempNodeInfo.setUiType("readOnly");
        return tempNodeInfo;
    }


}
