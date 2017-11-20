package com.ecmp.flow.util;

import com.ecmp.config.util.ApiClient;
import com.ecmp.context.ContextUtil;
import com.ecmp.core.search.PageResult;
import com.ecmp.flow.activiti.ext.PvmNodeInfo;
import com.ecmp.flow.basic.vo.Executor;
import com.ecmp.flow.common.util.*;
import com.ecmp.flow.constant.FlowDefinationStatus;
import com.ecmp.flow.dao.*;
import com.ecmp.flow.entity.*;
import com.ecmp.flow.service.FlowDefinationService;
import com.ecmp.flow.service.FlowInstanceService;
import com.ecmp.flow.vo.FlowStartVO;
import com.ecmp.flow.vo.NodeInfo;
import com.ecmp.flow.vo.bpmn.Definition;
import com.ecmp.flow.vo.bpmn.StartEvent;
import com.ecmp.vo.OperateResult;
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
import org.activiti.engine.impl.pvm.process.TransitionImpl;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.IdentityLink;
import org.activiti.engine.task.Task;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.ws.rs.core.GenericType;
import java.util.*;

/**
 * *************************************************************************************************
 * <p/>
 * 实现功能：
 * <p>
 * ------------------------------------------------------------------------------------------------
 * 版本          变更时间             变更人                     变更原因
 * ------------------------------------------------------------------------------------------------
 * 1.0.00      2017/10/25 13:57      谭军(tanjun)                    新建
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
    private FlowVariableDao flowVariableDao;

    @Autowired
    private FlowExecutorConfigDao flowExecutorConfigDao;

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private IdentityService identityService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private FlowDefinationDao flowDefinationDao;

    @Autowired
    private FlowDefVersionDao flowDefVersionDao;

    @Autowired
    private HistoryService historyService;

    @Autowired
    private ProcessEngine processEngine;

    @Autowired
    private FlowInstanceService flowInstanceService;

    @Autowired
    private FlowDefinationService flowDefinationService;

    @Autowired
    private AppModuleDao appModuleDao;

    @Autowired
    private WorkPageUrlDao workPageUrlDao;

    private final Logger logger = LoggerFactory.getLogger(FlowTaskTool.class);

    public FlowTaskTool(){
        System.out.println("FlowTaskTool init------------------------------------------");
    }
    /**
     * 检查是否下一节点存在网关
     *
     * @param flowTask
     * @return
     */
    public  boolean checkGateway(FlowTask flowTask) {
        boolean result = false;
        String defObjStr = flowTask.getFlowInstance().getFlowDefVersion().getDefJson();
        JSONObject defObj = JSONObject.fromObject(defObjStr);
        Definition definition = (Definition) JSONObject.toBean(defObj, Definition.class);
        net.sf.json.JSONObject currentNode = definition.getProcess().getNodes().getJSONObject(flowTask.getActTaskDefKey());
        JSONArray targetNodes = currentNode.getJSONArray("target");
        for (int i = 0; i < targetNodes.size(); i++) {
            JSONObject jsonObject = targetNodes.getJSONObject(i);
            String targetId = jsonObject.getString("targetId");
            net.sf.json.JSONObject nextNode = definition.getProcess().getNodes().getJSONObject(targetId);
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
    public   boolean checkManualExclusiveGateway(FlowTask flowTask) {
        boolean result = false;
        String defObjStr = flowTask.getFlowInstance().getFlowDefVersion().getDefJson();
        JSONObject defObj = JSONObject.fromObject(defObjStr);
        Definition definition = (Definition) JSONObject.toBean(defObj, Definition.class);
        net.sf.json.JSONObject currentNode = definition.getProcess().getNodes().getJSONObject(flowTask.getActTaskDefKey());
        JSONArray targetNodes = currentNode.getJSONArray("target");
        for (int i = 0; i < targetNodes.size(); i++) {
            JSONObject jsonObject = targetNodes.getJSONObject(i);
            String targetId = jsonObject.getString("targetId");
            net.sf.json.JSONObject nextNode = definition.getProcess().getNodes().getJSONObject(targetId);
            try{
                  if ("ManualExclusiveGateway".equalsIgnoreCase(nextNode.getString("busType"))) {
                       result = true;
                       break;
                  }
            }catch(Exception e){
                logger.error(e.getMessage());
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
    public   boolean checkSystemExclusiveGateway(FlowTask flowTask) {
        boolean result = false;
        String defObjStr = flowTask.getFlowInstance().getFlowDefVersion().getDefJson();
        JSONObject defObj = JSONObject.fromObject(defObjStr);
        Definition definition = (Definition) JSONObject.toBean(defObj, Definition.class);
        net.sf.json.JSONObject currentNode = definition.getProcess().getNodes().getJSONObject(flowTask.getActTaskDefKey());
        JSONArray targetNodes = currentNode.getJSONArray("target");
        for (int i = 0; i < targetNodes.size(); i++) {
            JSONObject jsonObject = targetNodes.getJSONObject(i);
            String targetId = jsonObject.getString("targetId");
            net.sf.json.JSONObject nextNode = definition.getProcess().getNodes().getJSONObject(targetId);
            String busType = null;
            try {
                busType = nextNode.getString("busType");
            } catch (Exception e) {
                logger.error(e.getMessage());
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
    public   boolean checkManualExclusiveGateway(FlowTask flowTask, String manualExclusiveGatewayId) {
        boolean result = false;
        String defObjStr = flowTask.getFlowInstance().getFlowDefVersion().getDefJson();
        JSONObject defObj = JSONObject.fromObject(defObjStr);
        Definition definition = (Definition) JSONObject.toBean(defObj, Definition.class);
        net.sf.json.JSONObject nextNode = definition.getProcess().getNodes().getJSONObject(manualExclusiveGatewayId);
        if ("ManualExclusiveGateway".equalsIgnoreCase(nextNode.getString("busType"))) {
            result = true;
        }
        return result;
    }


    /**
     * 获取所有出口节点信息,包含网关迭代
     *
     * @return
     */
    public   List<NodeInfo> selectNextAllNodesWithGateWay(FlowTask flowTask, PvmActivity currActivity, Map<String, Object> v, List<String> includeNodeIds) throws NoSuchMethodException, SecurityException {
       String defObjStr = flowTask.getFlowInstance().getFlowDefVersion().getDefJson();
        JSONObject defObj = JSONObject.fromObject(defObjStr);
        Definition definition = (Definition) JSONObject.toBean(defObj, Definition.class);
        net.sf.json.JSONObject currentNode = definition.getProcess().getNodes().getJSONObject(flowTask.getActTaskDefKey());
        String nodeType = currentNode.get("nodeType") + "";

        Map<PvmActivity, List> nextNodes = new LinkedHashMap<PvmActivity, List>();
        initNextNodes(currActivity, nextNodes, 0,nodeType,null);
        //前端需要的数据出口任务数据
        List<NodeInfo> nodeInfoList = new ArrayList<NodeInfo>();
        if (!nextNodes.isEmpty()) {
            //判断网关
            Object[] nextNodesKeyArray = nextNodes.keySet().toArray();
            PvmActivity firstActivity = (PvmActivity) nextNodesKeyArray[0];
            Boolean isSizeBigTwo = nextNodes.size() > 1 ? true : false;
            String nextActivtityType = firstActivity.getProperty("type").toString();
            String uiType = "readOnly";
            if("CounterSign".equalsIgnoreCase(nodeType)){//如果是会签
                for (int i = 0; i < nextNodes.size(); i++) {
                    PvmActivity tempActivity = (PvmActivity) nextNodesKeyArray[i];
                    if (includeNodeIds != null && !includeNodeIds.isEmpty()) {
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
                    tempNodeInfo.setPreLineName(nextNodes.get(tempActivity).get(0)+"");
                    nodeInfoList.add(tempNodeInfo);
                }
            }
            if ("Approve".equalsIgnoreCase(nodeType)) {//如果是审批结点
                uiType = "radiobox";
                for (int i = 0; i < nextNodes.size(); i++) {
                    PvmActivity tempActivity = (PvmActivity) nextNodesKeyArray[i];
                    if (includeNodeIds != null && !includeNodeIds.isEmpty()) {
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
                    tempNodeInfo.setPreLineName(nextNodes.get(tempActivity).get(0)+"");
                    nodeInfoList.add(tempNodeInfo);
                }
            } else if ("exclusiveGateway".equalsIgnoreCase(nextActivtityType)) {// 排他网关，radiobox,有且只能选择一个
                if (this.checkManualExclusiveGateway(flowTask, firstActivity.getId())) {//如果人工网关
                    uiType = "radiobox";
                    if (isSizeBigTwo) {
                        for (int i = 1; i < nextNodes.size(); i++) {
                            PvmActivity tempActivity = (PvmActivity) nextNodesKeyArray[i];
                            if (includeNodeIds != null  && !includeNodeIds.isEmpty()) {
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
                            tempNodeInfo.setPreLineName(nextNodes.get(tempActivity).get(0)+"");
                            nodeInfoList.add(tempNodeInfo);
                        }
                    }
                }else{
                    List<NodeInfo> currentNodeInf = this.selectQualifiedNode(flowTask, firstActivity, v, null);
                    nodeInfoList.addAll(currentNodeInf);
                }

            } else if ("inclusiveGateway".equalsIgnoreCase(nextActivtityType)) { // 包容网关,checkbox,至少选择一个
                if (isSizeBigTwo) {
                    if (includeNodeIds == null || includeNodeIds.isEmpty()) {
                        List<NodeInfo> currentNodeInf = this.selectQualifiedNode(flowTask, firstActivity, v, null);
                        nodeInfoList.addAll(currentNodeInf);
                        return nodeInfoList;
                    }
                    for (int i = 1; i < nextNodes.size(); i++) {
                        PvmActivity tempActivity = (PvmActivity) nextNodesKeyArray[i];
                        if (includeNodeIds != null && !includeNodeIds.isEmpty()) {
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
                        tempNodeInfo.setPreLineName(nextNodes.get(tempActivity).get(0)+"");
                        nodeInfoList.add(tempNodeInfo);
                    }
                }

            } else if ("parallelGateWay".equalsIgnoreCase(nextActivtityType)) { // 并行网关,checkbox,默认全部选中显示不能修改
                if (isSizeBigTwo) {
                    for (int i = 1; i < nextNodes.size(); i++) {
                        PvmActivity tempActivity = (PvmActivity) nextNodesKeyArray[i];
                        if (includeNodeIds != null  && !includeNodeIds.isEmpty()) {
                            if (!includeNodeIds.contains(tempActivity.getId())) {
                                continue;
                            }
                        }
                        NodeInfo tempNodeInfo = new NodeInfo();
                        tempNodeInfo = convertNodes(flowTask, tempNodeInfo, tempActivity);
                        tempNodeInfo.setUiType(uiType);
                        tempNodeInfo.setPreLineName(nextNodes.get(tempActivity).get(0)+"");
                        nodeInfoList.add(tempNodeInfo);
                    }
                }

            }
            else   if("CallActivity".equalsIgnoreCase(nextActivtityType)){
                nodeInfoList =  getCallActivityNodeInfo(flowTask,firstActivity.getId(),nodeInfoList);
            } else {
                if (isSizeBigTwo) {//当下步节点大于一个时，按照并行网关处理。checkbox,默认全部选中显示不能修改
                    for (int i = 0; i < nextNodes.size(); i++) {
                        PvmActivity tempActivity = (PvmActivity) nextNodesKeyArray[i];
                        NodeInfo tempNodeInfo = new NodeInfo();
                        tempNodeInfo = convertNodes(flowTask, tempNodeInfo, tempActivity);
                        tempNodeInfo.setUiType(uiType);
                        tempNodeInfo.setPreLineName(nextNodes.get(tempActivity).get(0)+"");
                        nodeInfoList.add(tempNodeInfo);
                    }
                } else {//按照惟一分支任务处理，显示一个，只读
                    PvmActivity tempActivity = (PvmActivity) nextNodesKeyArray[0];
                    if (includeNodeIds != null  && !includeNodeIds.isEmpty()) {
                        if (!includeNodeIds.contains(tempActivity.getId())) {
                            throw new RuntimeException("惟一分支未选中");
                        }
                    }
                    NodeInfo tempNodeInfo = new NodeInfo();
                    tempNodeInfo = convertNodes(flowTask, tempNodeInfo, tempActivity);
                    tempNodeInfo.setUiType(uiType);
                    tempNodeInfo.setPreLineName(nextNodes.get(tempActivity).get(0)+"");
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
    public   List<NodeInfo> selectNextAllNodes(FlowTask flowTask, List<String> includeNodeIds) {
        String defJson = flowTask.getTaskJsonDef();
        JSONObject defObj = JSONObject.fromObject(defJson);
        String nodeType = defObj.get("nodeType") + "";

        String actTaskDefKey = flowTask.getActTaskDefKey();
        String actProcessDefinitionId = flowTask.getFlowInstance().getFlowDefVersion().getActDefId();
        ProcessDefinitionEntity definition = (ProcessDefinitionEntity) ((RepositoryServiceImpl) repositoryService)
                .getDeployedProcessDefinition(actProcessDefinitionId);

        PvmActivity currActivity = this.getActivitNode(definition,actTaskDefKey);
        //前端需要的数据出口任务数据
        List<NodeInfo> nodeInfoList = new ArrayList<NodeInfo>();
        String uiType = "readOnly";
        Boolean counterSignLastTask = false;
        if("Approve".equalsIgnoreCase(nodeType)){
            NodeInfo tempNodeInfo = new NodeInfo();
            tempNodeInfo.setCurrentTaskType(nodeType);
            tempNodeInfo = convertNodes(flowTask, tempNodeInfo, currActivity);
            tempNodeInfo.setUiType(uiType);
            nodeInfoList.add(tempNodeInfo);
            return nodeInfoList;
        }

        if("CounterSign".equalsIgnoreCase(nodeType)||"ParallelTask".equalsIgnoreCase(nodeType)||"SerialTask".equalsIgnoreCase(nodeType)){//多实例节点，直接返回当前会签节点信息

            NodeInfo tempNodeInfo = new NodeInfo();
            tempNodeInfo.setCurrentTaskType(nodeType);
            tempNodeInfo = convertNodes(flowTask, tempNodeInfo, currActivity);
            tempNodeInfo.setUiType(uiType);
            ProcessInstance instance = runtimeService.createProcessInstanceQuery()
                    .processInstanceId(flowTask.getFlowInstance().getActInstanceId()).singleResult();
            // 取得当前任务
            HistoricTaskInstance currTask = historyService
                    .createHistoricTaskInstanceQuery().taskId(flowTask.getActTaskId())
                    .singleResult();
            String executionId = currTask.getExecutionId();

            Map<String, VariableInstance>      processVariables= runtimeService.getVariableInstances(executionId);

            //完成会签的次数
            Integer completeCounter=(Integer)processVariables.get("nrOfCompletedInstances").getValue();
            //总循环次数
            Integer instanceOfNumbers=(Integer)processVariables.get("nrOfInstances").getValue();
            if(completeCounter+1==instanceOfNumbers){//会签,串，并最后一个执行人
                tempNodeInfo.setCounterSignLastTask(true);
                counterSignLastTask = true;
                if("CounterSign".equalsIgnoreCase(nodeType)){
                    nodeInfoList.add(tempNodeInfo);
                    return nodeInfoList;
                }
            }else{
                nodeInfoList.add(tempNodeInfo);
                return nodeInfoList;
            }

        }
        Map<PvmActivity, List> nextNodes = new LinkedHashMap<PvmActivity, List>();
        initNextNodes(currActivity, nextNodes, 0,nodeType,null);
        if (!nextNodes.isEmpty()) {
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
                        if (includeNodeIds != null  && !includeNodeIds.isEmpty()) {
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
                        tempNodeInfo.setPreLineName(nextNodes.get(tempActivity).get(0)+"");
                        nodeInfoList.add(tempNodeInfo);
                    }
                }
            }
            else   if("CallActivity".equalsIgnoreCase(nextActivtityType)){
                nodeInfoList =  getCallActivityNodeInfo(flowTask,firstActivity.getId(),nodeInfoList);
            }
            else if ("exclusiveGateway".equalsIgnoreCase(nextActivtityType)) {// 排他网关，radiobox,有且只能选择一个
                if (this.checkManualExclusiveGateway(flowTask, firstActivity.getId())) {//如果人工网关
                    uiType = "radiobox";
                }
                if (isSizeBigTwo) {
                    for (int i = 1; i < nextNodes.size(); i++) {
                        PvmActivity tempActivity = (PvmActivity) nextNodesKeyArray[i];
                        if (includeNodeIds != null  && !includeNodeIds.isEmpty()) {
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
                        tempNodeInfo.setPreLineName(nextNodes.get(tempActivity).get(0)+"");
                        nodeInfoList.add(tempNodeInfo);
                    }
                }

            } else if ("inclusiveGateway".equalsIgnoreCase(nextActivtityType)) { // 包容网关,checkbox,至少选择一个
                if (isSizeBigTwo) {
                    for (int i = 1; i < nextNodes.size(); i++) {
                        PvmActivity tempActivity = (PvmActivity) nextNodesKeyArray[i];
                        if (includeNodeIds != null  && !includeNodeIds.isEmpty()) {
                            if (!includeNodeIds.contains(tempActivity.getId())) {
                                continue;
                            }
                        }
                        NodeInfo tempNodeInfo = new NodeInfo();
                        tempNodeInfo.setCurrentTaskType(nodeType);
                        tempNodeInfo = convertNodes(flowTask, tempNodeInfo, tempActivity);
                        tempNodeInfo.setUiType(uiType);
                        tempNodeInfo.setPreLineName(nextNodes.get(tempActivity).get(0)+"");
                        nodeInfoList.add(tempNodeInfo);
                    }
                }

            } else if ("parallelGateWay".equalsIgnoreCase(nextActivtityType)) { // 并行网关,checkbox,默认全部选中显示不能修改
                if (isSizeBigTwo) {
                    for (int i = 1; i < nextNodes.size(); i++) {
                        PvmActivity tempActivity = (PvmActivity) nextNodesKeyArray[i];
                        if (includeNodeIds != null  && !includeNodeIds.isEmpty()) {
                            if (!includeNodeIds.contains(tempActivity.getId())) {
                                continue;
                            }
                        }
                        NodeInfo tempNodeInfo = new NodeInfo();
                        tempNodeInfo.setCurrentTaskType(nodeType);
                        tempNodeInfo = convertNodes(flowTask, tempNodeInfo, tempActivity);
                        tempNodeInfo.setUiType(uiType);
                        tempNodeInfo.setPreLineName(nextNodes.get(tempActivity).get(0)+"");
                        nodeInfoList.add(tempNodeInfo);
                    }
                }

            } else {
                if (isSizeBigTwo) {//当下步节点大于一个时，按照并行网关处理。checkbox,默认全部选中显示不能修改
                    for (int i = 0; i < nextNodes.size(); i++) {
                        PvmActivity tempActivity = (PvmActivity) nextNodesKeyArray[i];
                        if (includeNodeIds != null  && !includeNodeIds.isEmpty()) {
                            if (!includeNodeIds.contains(tempActivity.getId())) {
                                continue;
                            }
                        }
                        NodeInfo tempNodeInfo = new NodeInfo();
                        tempNodeInfo.setCurrentTaskType(nodeType);
                        tempNodeInfo = convertNodes(flowTask, tempNodeInfo, tempActivity);
                        tempNodeInfo.setUiType(uiType);
                        tempNodeInfo.setPreLineName(nextNodes.get(tempActivity).get(0)+"");
                        nodeInfoList.add(tempNodeInfo);
                    }
                } else {//按照惟一分支任务处理，显示一个，只读
                    PvmActivity tempActivity = (PvmActivity) nextNodesKeyArray[0];
                    if (includeNodeIds != null  && !includeNodeIds.isEmpty()) {
                        if (!includeNodeIds.contains(tempActivity.getId())) {
                            throw new RuntimeException("惟一分支未选中");
                        }
                    }
                    NodeInfo tempNodeInfo = new NodeInfo();
                    tempNodeInfo.setCurrentTaskType(nodeType);
                    tempNodeInfo = convertNodes(flowTask, tempNodeInfo, tempActivity);
                    tempNodeInfo.setUiType(uiType);
                    tempNodeInfo.setPreLineName(nextNodes.get(tempActivity).get(0)+"");
                    nodeInfoList.add(tempNodeInfo);
                }
            }
        }
        if(counterSignLastTask && nodeInfoList!=null && !nodeInfoList.isEmpty()){
            for(NodeInfo nodeInfo:nodeInfoList){
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
    public   void initNextNodes(PvmActivity currActivity, Map<PvmActivity, List> nextNodes, int index,String nodeType, List lineInfo) {
        List<PvmTransition> nextTransitionList = currActivity.getOutgoingTransitions();
        if (nextTransitionList != null && !nextTransitionList.isEmpty()) {
            for (PvmTransition pv : nextTransitionList) {
                PvmActivity currTempActivity = pv.getDestination();
                String lineName = pv.getProperty("name") + "";//线的名称
                List value = null;
                if(lineInfo!=null){
                    value = lineInfo;
                }else {
                    value = new ArrayList<>();
                    value.add(lineName);
                    value.add(index);
                }
                Boolean ifGateWay = ifGageway(currTempActivity);
                String type = currTempActivity.getProperty("type")+"";
                if (ifGateWay || "ManualTask".equalsIgnoreCase(type)) {//如果是网关，其他直绑节点自行忽略
                    if(ifGateWay && index < 1){
                        nextNodes.put(currTempActivity, value);//把网关放入第一个节点
                        index++;
                        initNextNodes(currTempActivity, nextNodes, index,nodeType,null);
                    }else {
                        index++;
                        initNextNodes(currTempActivity, nextNodes, index,nodeType,value);
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
    public   NodeInfo convertNodes(FlowTask flowTask, NodeInfo tempNodeInfo, PvmActivity tempActivity) {
        tempNodeInfo.setFlowDefVersionId(flowTask.getFlowInstance().getFlowDefVersion().getId());
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
        }  else if (ifGageway(tempActivity)) {
            tempNodeInfo.setType("gateWay");
            tempNodeInfo.setUiType("radiobox");
            tempNodeInfo.setFlowTaskType("common");
            return tempNodeInfo;
        }

        String defObjStr = flowTask.getFlowInstance().getFlowDefVersion().getDefJson();
        JSONObject defObj = JSONObject.fromObject(defObjStr);
        Definition definition = (Definition) JSONObject.toBean(defObj, Definition.class);
        net.sf.json.JSONObject currentNode = definition.getProcess().getNodes().getJSONObject(tempActivity.getId());
        String nodeType = currentNode.get("nodeType") + "";
//        tempNodeInfo.setCurrentTaskType(nodeType);
        if ("CounterSign".equalsIgnoreCase(nodeType)) {//会签任务
            tempNodeInfo.setUiType("readOnly");
            tempNodeInfo.setFlowTaskType(nodeType);
        } else if ("ParallelTask".equalsIgnoreCase(nodeType)||"SerialTask".equalsIgnoreCase(nodeType)) {
            tempNodeInfo.setUiType("readOnly");
            tempNodeInfo.setFlowTaskType(nodeType);
        }
        else if ("Normal".equalsIgnoreCase(nodeType)) {//普通任务
            tempNodeInfo.setUiType("radiobox");
            tempNodeInfo.setFlowTaskType("common");
        } else if ("SingleSign".equalsIgnoreCase(nodeType)) {//单签任务
            tempNodeInfo.setFlowTaskType("singleSign");
            tempNodeInfo.setUiType("checkbox");
        } else if ("Approve".equalsIgnoreCase(nodeType)) {//审批任务
            tempNodeInfo.setUiType("radiobox");
            tempNodeInfo.setFlowTaskType("approve");
        } else if("ServiceTask".equals(nodeType)){//服务任务
            tempNodeInfo.setUiType("radiobox");
            tempNodeInfo.setFlowTaskType("serviceTask");
        }else if("ReceiveTask".equals(nodeType)){//服务任务
            tempNodeInfo.setUiType("radiobox");
            tempNodeInfo.setFlowTaskType("receiveTask");
        }else if("CallActivity".equalsIgnoreCase(nodeType)){
        }else {
            throw new RuntimeException("流程任务节点配置有错误");
        }
        return tempNodeInfo;
    }




    public   PvmNodeInfo pvmNodeInfoGateWayInit(Boolean ifGateWay, PvmNodeInfo pvmNodeInfo, PvmActivity nextTempActivity, Map<String,Object> v)
            throws NoSuchMethodException, SecurityException {
        if (ifGateWay) {
            PvmNodeInfo pvmNodeInfoTemp =  checkFuHeConditon(nextTempActivity, v);
            pvmNodeInfoTemp.setParent(pvmNodeInfo);
            if(pvmNodeInfoTemp!=null && pvmNodeInfoTemp.getChildren().isEmpty()){
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
    public   PvmNodeInfo checkFuHeConditon(PvmActivity currActivity, Map<String, Object> v)
            throws NoSuchMethodException, SecurityException {

        PvmNodeInfo pvmNodeInfo = new PvmNodeInfo();
        pvmNodeInfo.setCurrActivity(currActivity);

        List<PvmTransition> nextTransitionList = currActivity.getOutgoingTransitions();
        if (nextTransitionList != null && !nextTransitionList.isEmpty()) {
            String currentActivtityType = currActivity.getProperty("type").toString();
            for (PvmTransition pv : nextTransitionList) {
                String conditionText = (String) pv.getProperty("conditionText");
                PvmActivity nextTempActivity = pv.getDestination();
                Boolean ifGateWay = ifGageway(nextTempActivity);//当前节点的子节点是否为网关

                if ("ExclusiveGateway".equalsIgnoreCase(currentActivtityType) || "inclusiveGateway".equalsIgnoreCase(currentActivtityType)) {
                    if (conditionText != null) {
                        if (conditionText.startsWith("#{")) {// #{开头代表自定义的groovy表达式
                            String conditonFinal = conditionText.substring(conditionText.indexOf("#{") + 2,
                                    conditionText.lastIndexOf("}"));
                            if (ConditionUtil.groovyTest(conditonFinal, v)) {
                                pvmNodeInfo =  pvmNodeInfoGateWayInit( ifGateWay, pvmNodeInfo, nextTempActivity, v);
                            }
                        } else {//其他的用UEL表达式验证
                            Object tempResult = ConditionUtil.uelResult(conditionText, v);
                            if (tempResult instanceof Boolean) {
                                Boolean resultB = (Boolean) tempResult;
                                if(resultB == true){
                                    pvmNodeInfo =  pvmNodeInfoGateWayInit( ifGateWay, pvmNodeInfo, nextTempActivity, v);
                                }
                            }
                        }
                    }
                }
                else {
                    pvmNodeInfo =  pvmNodeInfoGateWayInit( ifGateWay, pvmNodeInfo, nextTempActivity, v);
                }
            }
            if (("ExclusiveGateway".equalsIgnoreCase(currentActivtityType) || "inclusiveGateway".equalsIgnoreCase(currentActivtityType) ) && pvmNodeInfo.getChildren().isEmpty() ) {
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
    public    List<PvmActivity> initPvmActivityList(PvmNodeInfo pvmNodeInfo,List<PvmActivity> results){
        if(pvmNodeInfo!=null && !pvmNodeInfo.getChildren().isEmpty()){
            Set<PvmNodeInfo> children = pvmNodeInfo.getChildren();
            for(PvmNodeInfo p:children){
                PvmActivity currActivity =  p.getCurrActivity();
                if(currActivity!=null){
                    if(ifGageway(currActivity)){
                        results=this.initPvmActivityList(p,results);
                    }else {
                        results.add(currActivity);
                    }
                }
            }
        }
        return results;
    }

    /**
     * 检查是否允许对流程实例进行终止、驳回,针对并行网关，包容网关的情况下
     * @param flowTaskPageResult
     */
    public static void  changeTaskStatue(PageResult<FlowTask> flowTaskPageResult){
        List<FlowTask> flowTaskList = flowTaskPageResult.getRows();
        Map<FlowInstance,List<FlowTask>> flowInstanceListMap = new HashMap<FlowInstance, List<FlowTask>>();
        if(flowTaskList!=null && !flowTaskList.isEmpty()){
            for(FlowTask flowTask:flowTaskList){
                List<FlowTask> flowTaskListTemp =  flowInstanceListMap.get(flowTask.getFlowInstance());
                if(flowTaskListTemp==null){
                    flowTaskListTemp = new ArrayList<FlowTask>();
                }
                flowTaskListTemp.add(flowTask);
                flowInstanceListMap.put(flowTask.getFlowInstance(),flowTaskListTemp);
            }
        }
        if(!flowInstanceListMap.isEmpty()){
            for(Map.Entry<FlowInstance,List<FlowTask>> temp:flowInstanceListMap.entrySet()){
                List<FlowTask> flowTaskListTemp = temp.getValue();
                if(flowTaskListTemp!=null && !flowTaskListTemp.isEmpty()){
                    boolean canEnd = true;
                    for(FlowTask flowTask:flowTaskListTemp){
                        Boolean canCancel = flowTask.getCanSuspension();
                        if(canCancel==null || !canCancel){
                            canEnd = false;
                            break;
                        }
                    }
                    if(!canEnd){
                        for(FlowTask flowTask:flowTaskListTemp){
                            flowTask.setCanSuspension(false);
                        }
                    }
                }
            }
        }
    }



    /**
     * 回退任务
     *
     * @return
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public OperateResult taskRollBack(FlowHistory flowHistory, String opinion) {
        OperateResult result = OperateResult.operationSuccess("core_00003");
        String taskId = flowHistory.getActHistoryId();
        try {
            Map<String, Object> variables;
            // 取得当前任务
            HistoricTaskInstance currTask = historyService.createHistoricTaskInstanceQuery().taskId(taskId)
                    .singleResult();
            // 取得流程实例
            ProcessInstance instance = runtimeService.createProcessInstanceQuery()
                    .processInstanceId(currTask.getProcessInstanceId()).singleResult();
            if (instance == null) {
                return OperateResult.operationFailure("10002");//流程实例不存在或者已经结束
            }
            variables = instance.getProcessVariables();
            Map variablesTask = currTask.getTaskLocalVariables();
            // 取得流程定义
            ProcessDefinitionEntity definition = (ProcessDefinitionEntity) ((RepositoryServiceImpl) repositoryService)
                    .getDeployedProcessDefinition(currTask.getProcessDefinitionId());
            if (definition == null) {
                logger.error(ContextUtil.getMessage("10003"));
                return OperateResult.operationFailure("10003");//流程定义未找到找到");
            }

            String executionId = currTask.getExecutionId();
            Execution execution = runtimeService.createExecutionQuery().executionId(executionId).singleResult();
            List<HistoricVariableInstance> historicVariableInstances = historyService.createHistoricVariableInstanceQuery().executionId(executionId).list();
            if (execution == null) {
                return OperateResult.operationFailure("10014");//当前任务不允许撤回
            }
            // 取得下一步活动
            ActivityImpl currActivity = ((ProcessDefinitionImpl) definition)
                    .findActivity(currTask.getTaskDefinitionKey());
            if (currTask.getEndTime() == null) {// 当前任务可能已经被还原
                logger.error(ContextUtil.getMessage("10008"));
                return OperateResult.operationFailure("10008");//当前任务可能已经被还原
            }

            Boolean resultCheck = checkNextNodeNotCompleted(currActivity, instance, definition, currTask);
            if (!resultCheck) {
                logger.info(ContextUtil.getMessage("10005"));
                return OperateResult.operationFailure("10005");//下一任务正在执行或者已经执行完成，退回失败
            }

            HistoricActivityInstance historicActivityInstance = null;
            HistoricActivityInstanceQuery his = historyService.createHistoricActivityInstanceQuery()
                    .executionId(executionId);
            if (his != null) {
                List<HistoricActivityInstance> historicActivityInstanceList = his.activityId(currTask.getTaskDefinitionKey()).orderByHistoricActivityInstanceEndTime().desc().list();
                if (historicActivityInstanceList != null && !historicActivityInstanceList.isEmpty()) {
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
                logger.error(ContextUtil.getMessage("10009"));
                return OperateResult.operationFailure("10009");//当前任务找不到
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
            // newTask.setDelegationState(delegationState);
            newTask.setDescription(currTask.getDescription());
            newTask.setDueDate(currTask.getDueDate());
            newTask.setFormKey(currTask.getFormKey());
            // newTask.setLocalizedDescription(currTask.getl);
            // newTask.setLocalizedName(currTask.get);
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
            deleteOtherNode(currActivity, instance, definition, currTask);

            //记录历史
            if (result.successful()) {
                flowHistory.setTaskStatus(TaskStatus.CANCLE.toString());
                flowHistoryDao.save(flowHistory);
                FlowHistory flowHistoryNew = (FlowHistory) flowHistory.clone();
                flowHistoryNew.setId(null);
                Date now = new Date();
                flowHistoryNew.setActEndTime(now);
                flowHistoryNew.setDepict("【被撤回】" + opinion);
                flowHistoryNew.setActDurationInMillis(now.getTime() - flowHistory.getActEndTime().getTime());
                flowHistoryDao.save(flowHistoryNew);
            }
            //初始化回退后的新任务
            initTask(flowHistory.getFlowInstance(), flowHistory,currTask.getTaskDefinitionKey());
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            e.printStackTrace();
            logger.error(e.getMessage());

            return OperateResult.operationFailure("10004");//流程取回失败，未知错误
        }
    }

    /**
     * 还原执行人、候选人
     *
     * @param taskId
     */
    public  void callBackRunIdentityLinkEntity(String taskId) {
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
    public  Boolean deleteOtherNode(PvmActivity currActivity, ProcessInstance instance,
                                    ProcessDefinitionEntity definition, HistoricTaskInstance destnetionTask) {
        List<PvmTransition> nextTransitionList = currActivity.getOutgoingTransitions();
        Boolean result = true;
        for (PvmTransition nextTransition : nextTransitionList) {
            PvmActivity nextActivity = nextTransition.getDestination();
            Boolean ifGateWay = ifGageway(nextActivity);
            boolean ifMultiInstance = ifMultiInstance(nextActivity);
            if (ifGateWay) {
                result = deleteOtherNode(nextActivity, instance, definition, destnetionTask);
                if (!result) {
                    return result;
                }
            }

            List<Task> nextTasks = taskService.createTaskQuery().processInstanceId(instance.getId())
                    .taskDefinitionKey(nextActivity.getId()).list();
            for (Task nextTask : nextTasks) {
                //
                taskService.deleteRuningTask(nextTask.getId(), false);
                historyService.deleteHistoricActivityInstancesByTaskId(nextTask.getId());
                historyService.deleteHistoricTaskInstance(nextTask.getId());
                flowTaskDao.deleteByActTaskId(nextTask.getId());//删除关联的流程新任务
            }
            if ((nextTasks != null) && (!nextTasks.isEmpty()) && (ifGageway(currActivity))) {

                HistoricActivityInstance gateWayActivity = historyService.createHistoricActivityInstanceQuery()
                        .processInstanceId(destnetionTask.getProcessInstanceId()).activityId(currActivity.getId())
                        .singleResult();
                if (gateWayActivity != null) {
                    historyService.deleteHistoricActivityInstanceById(gateWayActivity.getId());
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
     * 检查下一节点是否已经执行完成
     *
     * @param currActivity
     * @param instance
     * @param definition
     * @param destnetionTask
     * @return
     */
    public  Boolean checkNextNodeNotCompleted(PvmActivity currActivity, ProcessInstance instance,
                                              ProcessDefinitionEntity definition, HistoricTaskInstance destnetionTask) {
        List<PvmTransition> nextTransitionList = currActivity.getOutgoingTransitions();
        boolean result = true;

        for (PvmTransition nextTransition : nextTransitionList) {
            PvmActivity nextActivity = nextTransition.getDestination();
            Boolean ifGateWay = ifGageway(nextActivity);
            String type = nextActivity.getProperty("type")+"";
            if("callActivity".equalsIgnoreCase(type)|| "ServiceTask".equalsIgnoreCase(type) || "ReceiveTask".equalsIgnoreCase(type)){//服务任务/接收任务不允许撤回
                return false;
            }
            if (ifGateWay|| "ManualTask".equalsIgnoreCase(type)) {
                result = checkNextNodeNotCompleted(nextActivity, instance, definition, destnetionTask);
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
                        .processInstanceId(instance.getId()).taskDefinitionKey(nextActivity.getId()).unfinished()
                        .list();
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

    public  static  Boolean checkCanReject(PvmActivity currActivity, PvmActivity preActivity, ProcessInstance instance,
                                   ProcessDefinitionEntity definition) {
        if(preActivity==null){
            return false;
        }
        String type = preActivity.getProperty("type")+"";
        if("callActivity".equalsIgnoreCase(type)|| "ServiceTask".equalsIgnoreCase(type) || "ReceiveTask".equalsIgnoreCase(type)){//上一步如果是子任务、服务任务/接收任务不允许驳回
            return false;
        }
        Boolean result = ifMultiInstance(currActivity);
        if (result) {//多任务实例不允许驳回
            return false;
        }
        List<PvmTransition> currentInTransitionList = currActivity.getIncomingTransitions();
        for (PvmTransition currentInTransition : currentInTransitionList) {
            PvmActivity currentInActivity = currentInTransition.getSource();
            if (currentInActivity.getId().equals(preActivity.getId())) {
                result = true;
                break;
            }
            Boolean ifExclusiveGateway = ifExclusiveGateway(currentInActivity);
            if (ifExclusiveGateway) {
                result = checkCanReject(currentInActivity, preActivity, instance, definition);
                if (result) {
                    return result;
                }
            }
        }
        return result;
    }

    private void taskPropertityInit( FlowTask flowTask,FlowHistory preTask,net.sf.json.JSONObject currentNode){
        net.sf.json.JSONObject normalInfo = currentNode.getJSONObject("nodeConfig").getJSONObject("normal");
        Boolean canReject = null;
        Boolean canSuspension = null;
        WorkPageUrl workPageUrl =null;
        if(normalInfo!=null && !normalInfo.isEmpty() ){
            canReject = normalInfo.get("allowReject")!=null?(Boolean)normalInfo.get("allowReject"):null;
            canSuspension =normalInfo.get("allowTerminate")!=null?(Boolean) normalInfo.get("allowTerminate"):null;
            String workPageUrlId = (String)normalInfo.get("id");
            workPageUrl = workPageUrlDao.findOne(workPageUrlId);
            flowTask.setWorkPageUrl(workPageUrl);
//            String appModuleId = workPageUrl.getAppModuleId();
//            AppModule appModule = appModuleDao.findOne(appModuleId);
//            String taskFormUrl = appModule.getWebBaseAddress()+workPageUrl.getUrl();
//            flowTask.setTaskFormUrl(taskFormUrl);
        }
        flowTask.setCanReject(canReject);
        flowTask.setCanSuspension(canSuspension);
        if (preTask != null) {//初始化上一步的执行历史信息
            if (TaskStatus.REJECT.toString().equalsIgnoreCase(preTask.getTaskStatus())) {
                flowTask.setTaskStatus(TaskStatus.REJECT.toString());
            } else {
                flowTask.setPreId(preTask.getId());
            }
            flowTask.setPreId(preTask.getId());
            flowTask.setDepict(preTask.getDepict());
        }
        String nodeType = (String)currentNode.get("nodeType");
        if("CounterSign".equalsIgnoreCase(nodeType)||"Approve".equalsIgnoreCase(nodeType)||"Normal".equalsIgnoreCase(nodeType)||"SingleSign".equalsIgnoreCase(nodeType)
        ||"ParallelTask".equalsIgnoreCase(nodeType)||"SerialTask".equalsIgnoreCase(nodeType)){//能否由移动端审批
            Boolean mustCommit = workPageUrl.getMustCommit();
            if(mustCommit==null || !mustCommit){
                flowTask.setCanMobile(true);
            }
            if("CounterSign".equalsIgnoreCase(nodeType)||"Approve".equalsIgnoreCase(nodeType)){//能否批量审批
                net.sf.json.JSONObject executor = currentNode.getJSONObject("nodeConfig").getJSONObject("executor");
                String userType = (String) executor.get("userType");
                if("StartUser".equalsIgnoreCase(userType)||"Position".equalsIgnoreCase(userType)||"PositionType".equalsIgnoreCase(userType))
                {
                    if(mustCommit==null || !mustCommit){
                        //判断下一步如果为人工网关，不允许批量审批
                        if(!checkManualExclusiveGateway(flowTask)) {
                            flowTask.setCanBatchApproval(true);
                        }
                     }
                }
            }
        }

    }

    /**
     * 将新的流程任务初始化
     *
     * @param flowInstance
     * @param actTaskDefKeyCurrent
     */
    public  void initTask(FlowInstance flowInstance,  FlowHistory preTask,String actTaskDefKeyCurrent) {
        if(flowInstance == null || flowInstance.isEnded()){
            return;
        }
        List<Task> taskList = null;
        String actProcessInstanceId = flowInstance.getActInstanceId();
        if(StringUtils.isNotEmpty(actTaskDefKeyCurrent)){
            taskList = taskService.createTaskQuery().processInstanceId(actProcessInstanceId).taskDefinitionKey(actTaskDefKeyCurrent).active().list();
        }else{
            List<FlowInstance> flowInstanceSonList = flowInstanceDao.findByParentId(flowInstance.getId());
            if (flowInstanceSonList != null && !flowInstanceSonList.isEmpty()) {//初始化子流程的任务
                for (FlowInstance son : flowInstanceSonList) {
                    initTask(son, preTask,null);
                }
            }
            taskList = taskService.createTaskQuery().processInstanceId(actProcessInstanceId).active().list();
        }
        if (taskList != null && taskList.size() > 0) {
            String flowName = null;
            String flowDefJson = flowInstance.getFlowDefVersion().getDefJson();
            JSONObject defObj = JSONObject.fromObject(flowDefJson);
            Definition definition = (Definition) JSONObject.toBean(defObj, Definition.class);
            flowName = definition.getProcess().getName();
            for (Task task : taskList) {
                String actTaskDefKey = task.getTaskDefinitionKey();
                net.sf.json.JSONObject currentNode = definition.getProcess().getNodes().getJSONObject(actTaskDefKey);
                String nodeType = (String)currentNode.get("nodeType");
                if(("CounterSign".equalsIgnoreCase(nodeType)||"ParallelTask".equalsIgnoreCase(nodeType)||"SerialTask".equalsIgnoreCase(nodeType))){
                    FlowTask tempFlowTask = flowTaskDao.findByActTaskId(task.getId());
                    if(tempFlowTask!=null){
                        continue;
                    }
                }else{
                    String taskActKey = task.getTaskDefinitionKey();
                    Integer flowTaskNow =  flowTaskDao.findCountByActTaskDefKeyAndActInstanceId(taskActKey,flowInstance.getActInstanceId());
                    if(flowTaskNow != null && flowTaskNow>0){
                        continue;
                    }
                }
                List<IdentityLink> identityLinks = taskService.getIdentityLinksForTask(task.getId());
                if(identityLinks==null || identityLinks.isEmpty()){//多实例任务为null
                    /** 获取流程变量 **/
                    String executionId = task.getExecutionId();
                    String variableName = "" + actTaskDefKey + "_CounterSign";
                    String  userId = runtimeService.getVariable(executionId,variableName)+"";//使用执行对象Id和流程变量名称，获取值
                    if(StringUtils.isNotEmpty(userId)){
                        Map<String,Object> params = new HashMap();
                        params.put("employeeIds",java.util.Arrays.asList(userId));
                        String url = com.ecmp.flow.common.util.Constants.BASIC_SERVICE_URL+ com.ecmp.flow.common.util.Constants.BASIC_EMPLOYEE_GETEXECUTORSBYEMPLOYEEIDS_URL;
                        List<Executor> employees= ApiClient.getEntityViaProxy(url,new GenericType<List<Executor>>() {},params);
                        if(employees!=null && !employees.isEmpty()){
                            Executor executor = employees.get(0);
                            FlowTask flowTask = new FlowTask();
                            flowTask.setTaskJsonDef(currentNode.toString());
                            flowTask.setFlowDefinitionId(flowInstance.getFlowDefVersion().getFlowDefination().getId());
                            flowTask.setActTaskDefKey(actTaskDefKey);
                            flowTask.setFlowName(flowName);
                            flowTask.setTaskName(task.getName());
                            flowTask.setActTaskId(task.getId());
                            flowTask.setOwnerAccount(executor.getCode());
                            flowTask.setOwnerName(executor.getName());
                            flowTask.setExecutorAccount(executor.getCode());
                            flowTask.setExecutorId(executor.getId());
                            flowTask.setExecutorName(executor.getName());
                            flowTask.setPriority(task.getPriority());
                            flowTask.setActType("candidate");
                            if(StringUtils.isEmpty(task.getDescription())){
                                flowTask.setDepict("流程启动");
                            }else{
                                flowTask.setDepict(task.getDescription());
                            }
                            flowTask.setTaskStatus(TaskStatus.INIT.toString());
                            flowTask.setFlowInstance(flowInstance);
                            taskPropertityInit(flowTask,preTask,currentNode);
                            flowTaskDao.save(flowTask);
                        }
                    }
                }else{
                    for (IdentityLink identityLink : identityLinks) {
                        Map<String,Object> params = new HashMap();
                        params.put("employeeIds",
                                java.util.Arrays.asList(identityLink.getUserId()));
                        String url = com.ecmp.flow.common.util.Constants.BASIC_SERVICE_URL+ com.ecmp.flow.common.util.Constants.BASIC_EMPLOYEE_GETEXECUTORSBYEMPLOYEEIDS_URL;
                        List<Executor> employees=ApiClient.getEntityViaProxy(url,new GenericType<List<Executor>>() {},params);
                        if (employees != null && !employees.isEmpty()) {
                            Executor executor = employees.get(0);
                            FlowTask flowTask = new FlowTask();
                            flowTask.setTaskJsonDef(currentNode.toString());
                            flowTask.setFlowDefinitionId(flowInstance.getFlowDefVersion().getFlowDefination().getId());
                            flowTask.setActTaskDefKey(actTaskDefKey);
                            flowTask.setFlowName(flowName);
                            flowTask.setTaskName(task.getName());
                            flowTask.setActTaskId(task.getId());
                            flowTask.setOwnerAccount(executor.getCode());
                            flowTask.setOwnerId(executor.getId());
                            flowTask.setOwnerName(executor.getName());
                            flowTask.setExecutorAccount(executor.getCode());
                            flowTask.setExecutorId(executor.getId());
                            flowTask.setExecutorName(executor.getName());
                            flowTask.setPriority(task.getPriority());
                            flowTask.setActType(identityLink.getType());
                            flowTask.setDepict(task.getDescription());
                            flowTask.setTaskStatus(TaskStatus.INIT.toString());
                            flowTask.setFlowInstance(flowInstance);
                            taskPropertityInit(flowTask,preTask,currentNode);
                            flowTaskDao.save(flowTask);
                        }
                    }
                }
            }
            flowInstanceService.checkCanEnd(flowInstance.getId());
        }
    }

    /**
     * 记录任务执行过程中传入的参数
     *
     * @param variables 参数map
     * @param flowTask  关联的工作任务
     */
    public  void saveVariables(FlowVariableDao flowVariableDao, Map<String, Object> variables, FlowTask flowTask) {
        if ((variables != null) && (!variables.isEmpty()) && (flowTask != null)) {
            FlowVariable flowVariable = new FlowVariable();
            for (Map.Entry<String, Object> vs : variables.entrySet()) {
                String key = vs.getKey();
                Object value = vs.getValue();
                Long longV = null;
                Double doubleV = null;
                String strV = null;
                flowVariable.setName(key);
                flowVariable.setFlowTask(flowTask);
                try {
                    longV = Long.parseLong(value.toString());
                    flowVariable.setType(Long.class.getName());
                    flowVariable.setVLong(longV);
                } catch (RuntimeException e1) {
                    try {
                        doubleV = Double.parseDouble(value.toString());
                        flowVariable.setType(Double.class.getName());
                        flowVariable.setVDouble(doubleV);
                    } catch (RuntimeException e2) {
                        strV = value.toString();
                    }
                }
                flowVariable.setVText(strV);
            }
            flowVariableDao.save(flowVariable);
        }
    }

    /**
     * 检查当前任务的出口节点线上是否存在条件表达式
     *
     * @param currActivity 当前任务
     * @return
     */
    public  boolean checkHasConditon(PvmActivity currActivity) {
        boolean result = false;
        List<PvmTransition> nextTransitionList = currActivity.getOutgoingTransitions();
        // 判断出口线上是否存在condtion表达式
        if (nextTransitionList != null && !nextTransitionList.isEmpty()) {
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
        List<NodeInfo> qualifiedNode = new ArrayList<NodeInfo>();
        List<PvmActivity> results = new ArrayList<PvmActivity>();
        PvmNodeInfo pvmNodeInfo = checkFuHeConditon(currActivity,v);
        initPvmActivityList(pvmNodeInfo,results);
        // 前端需要的数据
        if (!results.isEmpty()) {
            for (PvmActivity tempActivity : results) {
                NodeInfo tempNodeInfo = new NodeInfo();
                if (includeNodeIds != null  && !includeNodeIds.isEmpty()) {
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

    private List<String> initIncludeNodeIds(List<String> includeNodeIds,String actTaskId,Map<String, Object> v) throws NoSuchMethodException, SecurityException{

        //检查是否包含的节点中是否有网关，有则进行替换
        List<String> includeNodeIdsNew = new ArrayList<String>();
        if(includeNodeIds!=null && !includeNodeIds.isEmpty()){
            includeNodeIdsNew.addAll(includeNodeIds);
            // 取得当前任务
            HistoricTaskInstance currTask = historyService
                    .createHistoricTaskInstanceQuery().taskId(actTaskId)
                    .singleResult();
            ProcessDefinitionEntity definition = (ProcessDefinitionEntity) ((RepositoryServiceImpl) repositoryService)
                    .getDeployedProcessDefinition(currTask
                            .getProcessDefinitionId());
            if (definition == null) {
                logger.error(ContextUtil.getMessage("10003"));
            }
            for(String includeNodeId:includeNodeIds){
                // 取得当前活动定义节点
                ActivityImpl tempActivity = ((ProcessDefinitionImpl) definition)
                        .findActivity(includeNodeId);
                if(tempActivity!=null && ifGageway(tempActivity)){
                    List<PvmActivity> results = new ArrayList<PvmActivity>();
                    includeNodeIdsNew.remove(includeNodeId);
                    PvmNodeInfo pvmNodeInfo = this.checkFuHeConditon(tempActivity,v);
                    this.initPvmActivityList(pvmNodeInfo,results);
                    if(results !=null){
                        for(PvmActivity p:results){
                            includeNodeIdsNew.add(p.getId());
                        }
                        includeNodeIdsNew =  initIncludeNodeIds(includeNodeIdsNew,actTaskId,v);
                    }
                }
            }
        }
        return includeNodeIdsNew;
    }

    private List<NodeInfo> getNodeInfo(List<String> includeNodeIds,FlowTask flowTask){
        List<NodeInfo>  result = new ArrayList<NodeInfo>();
        if(includeNodeIds!=null && !includeNodeIds.isEmpty()){
            // 取得当前任务
            HistoricTaskInstance currTask = historyService
                    .createHistoricTaskInstanceQuery().taskId(flowTask.getActTaskId())
                    .singleResult();
            ProcessDefinitionEntity definition = (ProcessDefinitionEntity) ((RepositoryServiceImpl) repositoryService)
                    .getDeployedProcessDefinition(currTask
                            .getProcessDefinitionId());
            if (definition == null) {
                logger.error(ContextUtil.getMessage("10003"));
            }
            for(String includeNodeId:includeNodeIds){
                // 取得当前活动定义节点
                ActivityImpl tempActivity = ((ProcessDefinitionImpl) definition)
                        .findActivity(includeNodeId);
                if(tempActivity!=null){
                    NodeInfo tempNodeInfo = new NodeInfo();
//                    tempNodeInfo.setCurrentTaskType(flowTask.);
                    this.convertNodes(flowTask,tempNodeInfo,tempActivity) ;
                    result.add(tempNodeInfo);
                }
            }
        }
        return result;
    }

    public List<NodeInfo> getCallActivityNodeInfo(FlowTask flowTask,String currNodeId, List<NodeInfo> result){
        FlowInstance flowInstance = flowTask.getFlowInstance();
        String defObjStr = flowInstance.getFlowDefVersion().getDefJson();
        JSONObject defObjP = JSONObject.fromObject(defObjStr);
        Definition definitionP = (Definition) JSONObject.toBean(defObjP, Definition.class);
        net.sf.json.JSONObject currentNode = definitionP.getProcess().getNodes().getJSONObject(currNodeId);
        net.sf.json.JSONObject normal = currentNode.getJSONObject("nodeConfig").getJSONObject("normal");
        String currentVersionId = (String)normal.get("currentVersionId");
        FlowDefVersion flowDefVersion = flowDefVersionDao.findOne(currentVersionId);
        if(flowDefVersion!=null && flowDefVersion.getFlowDefinationStatus() == FlowDefinationStatus.Activate){
            String def = flowDefVersion.getDefJson();
            JSONObject defObjSon = JSONObject.fromObject(def);
            Definition definitionSon = (Definition) JSONObject.toBean(defObjSon, Definition.class);
            List<StartEvent> startEventList = definitionSon.getProcess().getStartEvent();
            if (startEventList != null && startEventList.size() == 1) {
                StartEvent startEvent = startEventList.get(0);
                net.sf.json.JSONObject startEventNode = definitionSon.getProcess().getNodes().getJSONObject(startEvent.getId());
                FlowStartVO flowStartVO = new FlowStartVO();
                flowStartVO.setBusinessKey(flowInstance.getBusinessId());
                try {
                    String callActivityDefKey = (String)normal.get("callActivityDefKey");
                    String  businessVName = "/"+definitionP.getProcess().getId()+"/"+ currentNode.get("id");
                    if( StringUtils.isNotEmpty(flowInstance.getCallActivityPath())){
                        businessVName = flowInstance.getCallActivityPath()+businessVName;
                    }
                    result = flowDefinationService.findXunFanNodesInfo(result, flowStartVO, flowDefVersion.getFlowDefination(), definitionSon, startEventNode,businessVName);
                    if(!result.isEmpty()){
                        for(NodeInfo nodeInfo:result){
                            if(StringUtils.isEmpty(nodeInfo.getCallActivityPath())){
                                businessVName+="/"+callActivityDefKey;
                                nodeInfo.setCallActivityPath(businessVName);
                            }
                        }
                    }
                }catch (Exception e){

                }
            }
        }else {
            throw new RuntimeException("找不到子流程");
        }
        return result;
    }

    /**
     * 选择下一步执行的节点信息
     *
     * @param flowTask
     * @return
     * @throws NoSuchMethodException
     */
    public List<NodeInfo> findNextNodesWithCondition(FlowTask flowTask,String approved, List<String> includeNodeIds) throws NoSuchMethodException {
        String actTaskId = flowTask.getActTaskId();
        String businessId = flowTask.getFlowInstance().getBusinessId();
        String actTaskDefKey = flowTask.getActTaskDefKey();

        String actProcessDefinitionId = flowTask.getFlowInstance().getFlowDefVersion().getActDefId();
        ProcessDefinitionEntity definition = (ProcessDefinitionEntity) ((RepositoryServiceImpl) repositoryService)
                .getDeployedProcessDefinition(actProcessDefinitionId);
        PvmActivity currActivity = this.getActivitNode(definition,actTaskDefKey);
        FlowInstance flowInstanceReal = flowTask.getFlowInstance();
        BusinessModel businessModel = flowInstanceReal.getFlowDefVersion().getFlowDefination().getFlowType().getBusinessModel();
        Map<String, Object> v = ExpressionUtil.getPropertiesValuesMap( businessModel, businessId,false);

        List<String> includeNodeIdsNew = initIncludeNodeIds(includeNodeIds,actTaskId,v);

//        if(ifMultiInstance(currActivity)){//如果是多实例任务
        String defJson = flowTask.getTaskJsonDef();
        JSONObject defObj = JSONObject.fromObject(defJson);
        String nodeType = (String) defObj.get("nodeType");
        List<NodeInfo> result = new ArrayList<NodeInfo>();
        if("CounterSign".equalsIgnoreCase(nodeType)){//会签任务
            int counterDecision=100;
            try {
                counterDecision = defObj.getJSONObject("nodeConfig").getJSONObject("normal").getInt("counterDecision");
            }catch (Exception e){
                logger.error(e.getMessage());
            }
            // 取得当前任务
            HistoricTaskInstance currTask = historyService
                    .createHistoricTaskInstanceQuery().taskId(flowTask.getActTaskId())
                    .singleResult();
            String executionId = currTask.getExecutionId();
            Map<String, VariableInstance>  processVariables= runtimeService.getVariableInstances(executionId);
            //完成会签的次数
            Integer completeCounter=(Integer)processVariables.get("nrOfCompletedInstances").getValue();
            //总循环次数
            Integer instanceOfNumbers=(Integer)processVariables.get("nrOfInstances").getValue();
            if(completeCounter+1==instanceOfNumbers){//会签最后一个执行人
                Boolean  approveResult = null;
                //通过票数
                Integer counterSignAgree = 0;
                if(processVariables.get("counterSign_agree"+currTask.getTaskDefinitionKey())!=null) {
                    counterSignAgree = (Integer) processVariables.get("counterSign_agree"+currTask.getTaskDefinitionKey()).getValue();
                }
                Integer value = 0;//默认弃权
                if("true".equalsIgnoreCase(approved)){
                    counterSignAgree++;
                }
                if(counterDecision<=((counterSignAgree/(instanceOfNumbers+0.0))*100)){//获取通过节点
                    approveResult = true;
                    PvmActivity gateWayIn =  currActivity.getOutgoingTransitions().get(0).getDestination();
                    List<PvmTransition> nextTransitionList = gateWayIn.getOutgoingTransitions();
                    if (nextTransitionList != null && !nextTransitionList.isEmpty()) {
                        for (PvmTransition pv : nextTransitionList) {
                            String conditionText = (String) pv.getProperty("conditionText");
                            if("${approveResult == true}".equalsIgnoreCase(conditionText)){
                                PvmActivity currTempActivity = pv.getDestination();
                                String type = currTempActivity.getProperty("type")+"";
                                if(ifGageway(currTempActivity)|| "ManualTask".equalsIgnoreCase(type)){
                                    List<NodeInfo>  temp =  this.selectQualifiedNode(flowTask,currTempActivity,v,null);
                                    result.addAll(temp);
                                }else  if("CallActivity".equalsIgnoreCase(type)){
                                    result =  getCallActivityNodeInfo(flowTask,currTempActivity.getId(),result);
                                }else{
                                    NodeInfo tempNodeInfo = new NodeInfo();
                                    tempNodeInfo = convertNodes(flowTask, tempNodeInfo, currTempActivity);
                                    result.add(tempNodeInfo);
                                }
                            }
                        }
                    }
                }else {//获取不通过节点
                    approveResult = false;
                    PvmActivity gateWayIn =  currActivity.getOutgoingTransitions().get(0).getDestination();
                    List<PvmTransition> nextTransitionList = gateWayIn.getOutgoingTransitions();
                    if (nextTransitionList != null && !nextTransitionList.isEmpty()) {
                        for (PvmTransition pv : nextTransitionList) {
                            String conditionText = (String) pv.getProperty("conditionText");
                            if(StringUtils.isEmpty(conditionText)){
                                PvmActivity currTempActivity = pv.getDestination();
                                String type = currTempActivity.getProperty("type")+"";
                                if(ifGageway(currTempActivity)|| "ManualTask".equalsIgnoreCase(type)){
                                    List<NodeInfo>  temp =  this.selectQualifiedNode(flowTask,currTempActivity,v,null);
                                    result.addAll(temp);
                                }else  if("CallActivity".equalsIgnoreCase(type)){
                                    result =  getCallActivityNodeInfo(flowTask,currTempActivity.getId(),result);
                                }else{
                                    NodeInfo tempNodeInfo = new NodeInfo();
                                    tempNodeInfo = convertNodes(flowTask, tempNodeInfo, currTempActivity);
                                    result.add(tempNodeInfo);
                                }
                            }
                        }
                    }
                }
                return result;
            }else {
                NodeInfo tempNodeInfo = new NodeInfo();
                tempNodeInfo.setType("CounterSignNotEnd");
                tempNodeInfo = convertNodes(flowTask, tempNodeInfo, currActivity);
                result.add(tempNodeInfo);
            }
            return result;
        }
        else if("Approve".equalsIgnoreCase(nodeType)){//审批任务
            // 取得当前任务
            HistoricTaskInstance currTask = historyService
                    .createHistoricTaskInstanceQuery().taskId(flowTask.getActTaskId())
                    .singleResult();
            String executionId = currTask.getExecutionId();
            Map<String, VariableInstance>  processVariables= runtimeService.getVariableInstances(executionId);

            if("true".equalsIgnoreCase(approved)){ //获取通过节点
                PvmActivity gateWayIn =  currActivity.getOutgoingTransitions().get(0).getDestination();
                List<PvmTransition> nextTransitionList = gateWayIn.getOutgoingTransitions();
                if (nextTransitionList != null && !nextTransitionList.isEmpty()) {
                    for (PvmTransition pv : nextTransitionList) {
                        String conditionText = (String) pv.getProperty("conditionText");
                        if("${approveResult == true}".equalsIgnoreCase(conditionText)){
                            PvmActivity currTempActivity = pv.getDestination();
                            String type = currTempActivity.getProperty("type")+"";
                            if(ifGageway(currTempActivity)|| "ManualTask".equalsIgnoreCase(type)){
                                List<NodeInfo>  temp =  this.selectQualifiedNode(flowTask,currTempActivity,v,null);
                                result.addAll(temp);
                            }else  if("CallActivity".equalsIgnoreCase(type)){
                                result =  getCallActivityNodeInfo(flowTask,currTempActivity.getId(),result);
                            }else{
                                NodeInfo tempNodeInfo = new NodeInfo();
                                tempNodeInfo = convertNodes(flowTask, tempNodeInfo, currTempActivity);
                                result.add(tempNodeInfo);
                            }
                        }
                    }
                }
            }else {//获取不通过节点
                PvmActivity gateWayIn =  currActivity.getOutgoingTransitions().get(0).getDestination();
                List<PvmTransition> nextTransitionList = gateWayIn.getOutgoingTransitions();
                if (nextTransitionList != null && !nextTransitionList.isEmpty()) {
                    for (PvmTransition pv : nextTransitionList) {
                        String conditionText = (String) pv.getProperty("conditionText");
                        if(StringUtils.isEmpty(conditionText)){
                            PvmActivity currTempActivity = pv.getDestination();
                            String type = currTempActivity.getProperty("type")+"";
                            if(ifGageway(currTempActivity)|| "ManualTask".equalsIgnoreCase(type)){
                                List<NodeInfo>  temp =  this.selectQualifiedNode(flowTask,currTempActivity,v,null);
                                result.addAll(temp);
                            }else  if("CallActivity".equalsIgnoreCase(type)){
                                result =  getCallActivityNodeInfo(flowTask,currTempActivity.getId(),result);
                            }else{
                                NodeInfo tempNodeInfo = new NodeInfo();
                                tempNodeInfo = convertNodes(flowTask, tempNodeInfo, currTempActivity);
                                result.add(tempNodeInfo);
                            }
                        }
                    }
                }
            }
            return result;
        }
        else if ("ParallelTask".equalsIgnoreCase(nodeType)||"SerialTask".equalsIgnoreCase(nodeType)){
            // 取得当前任务
            HistoricTaskInstance currTask = historyService
                    .createHistoricTaskInstanceQuery().taskId(flowTask.getActTaskId())
                    .singleResult();
            String executionId = currTask.getExecutionId();
            Map<String, VariableInstance>  processVariables= runtimeService.getVariableInstances(executionId);
            //完成的次数
            Integer completeCounter=(Integer)processVariables.get("nrOfCompletedInstances").getValue();
            //总循环次数
            Integer instanceOfNumbers=(Integer)processVariables.get("nrOfInstances").getValue();
            if(completeCounter+1==instanceOfNumbers){//最后一个执行人
            }else{
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
            if(includeNodeIdsNew!=null && !includeNodeIdsNew.isEmpty()){
                result=  getNodeInfo(includeNodeIdsNew,flowTask);
            }else {
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
    public static PvmActivity getActivitNode(ProcessDefinitionEntity definition ,String taskDefinitionKey) {
        if (definition == null) {
            throw new RuntimeException("definition is null!");
        }
        // 取得当前活动定义节点
        ActivityImpl currActivity = ((ProcessDefinitionImpl) definition)
                .findActivity(taskDefinitionKey);
        return currActivity;
    }

    public static boolean checkNextHas( PvmActivity curActivity, PvmActivity destinationActivity ){
        boolean result = false;
        if(curActivity!=null){
            if(curActivity.getId().equals(destinationActivity.getId())){
                return true;
            }else if(FlowTaskTool.ifGageway(curActivity)||"ManualTask".equalsIgnoreCase(curActivity.getProperty("type") + "")){
                List<PvmTransition> pvmTransitionList =  curActivity.getOutgoingTransitions();
                if(pvmTransitionList != null && !pvmTransitionList.isEmpty()){
                    for(PvmTransition pv:pvmTransitionList){
                        result = checkNextHas(pv.getDestination(),destinationActivity);
                        if(result){
                            return result;
                        }
                    }
                }
            }
        }
        return  result;
    }
    public List<Executor> getExecutors(String userType, String ids){
        String[] idsShuZhu = ids.split(",");
        List<String> idList = java.util.Arrays.asList(idsShuZhu);
        List<Executor> employees = null;
        if ("Position".equalsIgnoreCase(userType)) {//调用岗位获取用户接口
            Map<String,Object> params = new HashMap();
            params.put("positionIds",idList);
            String url = com.ecmp.flow.common.util.Constants.BASIC_SERVICE_URL + com.ecmp.flow.common.util.Constants.BASIC_POSITION_GETEXECUTORSBYPOSITIONIDS_URL;
            employees = ApiClient.getEntityViaProxy(url,new GenericType<List<Executor>>() {},params);
        } else if ("PositionType".equalsIgnoreCase(userType)) {//调用岗位类型获取用户接口
            Map<String,Object> params = new HashMap();
            params.put("posCateIds",idList);
            String url = com.ecmp.flow.common.util.Constants.BASIC_SERVICE_URL + com.ecmp.flow.common.util.Constants.BASIC_POSITION_GETEXECUTORSBYPOSCATEIDS_URL;
            employees = ApiClient.getEntityViaProxy(url,new GenericType<List<Executor>>() {},params);
        }  else if ("AnyOne".equalsIgnoreCase(userType)) {//任意执行人不添加用户
        }
        return employees;
    }

}
