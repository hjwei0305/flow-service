package com.ecmp.flow.service;

import com.ecmp.config.util.ApiClient;
import com.ecmp.context.ContextUtil;
import com.ecmp.core.dao.BaseEntityDao;
import com.ecmp.core.search.PageResult;
import com.ecmp.core.search.Search;
import com.ecmp.core.service.BaseEntityService;
import com.ecmp.flow.api.IFlowTaskService;
import com.ecmp.flow.basic.vo.Executor;
import com.ecmp.flow.common.util.Constants;
import com.ecmp.flow.constant.FlowStatus;
import com.ecmp.flow.dao.*;
import com.ecmp.flow.entity.*;
import com.ecmp.flow.util.ExpressionUtil;
import com.ecmp.flow.util.FlowException;
import com.ecmp.flow.util.FlowTaskTool;
import com.ecmp.flow.util.TaskStatus;
import com.ecmp.flow.vo.*;
import com.ecmp.flow.vo.bpmn.Definition;
import com.ecmp.flow.vo.bpmn.UserTask;
import com.ecmp.vo.OperateResult;
import com.ecmp.vo.OperateResultWithData;
import net.sf.json.JSONObject;
import org.activiti.engine.*;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricActivityInstanceQuery;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.impl.RepositoryServiceImpl;
import org.activiti.engine.impl.el.UelExpressionCondition;
import org.activiti.engine.impl.persistence.entity.HistoricActivityInstanceEntity;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.persistence.entity.VariableInstance;
import org.activiti.engine.impl.pvm.PvmActivity;
import org.activiti.engine.impl.pvm.PvmTransition;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.impl.pvm.process.ProcessDefinitionImpl;
import org.activiti.engine.impl.pvm.process.ProcessElementImpl;
import org.activiti.engine.impl.pvm.process.TransitionImpl;
import org.activiti.engine.runtime.ProcessInstance;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import javax.ws.rs.core.GenericType;
import java.util.*;

/**
 * *************************************************************************************************
 * <p/>
 * 实现功能：流程任务管理
 * <p>
 * ------------------------------------------------------------------------------------------------
 * 版本          变更时间             变更人                     变更原因
 * ------------------------------------------------------------------------------------------------
 * 1.0.00      2017/3/23 22:39      谭军(tanjun)               新建
 * <p/>
 * *************************************************************************************************
 */
@Service
public class FlowTaskService extends BaseEntityService<FlowTask> implements IFlowTaskService {

    @Autowired
    private FlowTaskTool flowTaskTool;

    @Autowired
    private FlowTaskDao flowTaskDao;

    protected BaseEntityDao<FlowTask> getDao() {
        return this.flowTaskDao;
    }

    @Autowired
    private FlowInstanceDao flowInstanceDao;

    @Autowired
    private FlowHistoryDao flowHistoryDao;

//    @Autowired
//    private FlowVariableDao flowVariableDao;

    @Autowired
    private FlowExecutorConfigDao  flowExecutorConfigDao;

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

    private final Logger logger = LoggerFactory.getLogger(FlowDefinationService.class);

    /**
     * 任务签收
     *
     * @param id     任务id
     * @param userId 用户账号
     * @return
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public OperateResult claim(String id, String userId) {
        FlowTask flowTask = flowTaskDao.findOne(id);
        String actTaskId = flowTask.getActTaskId();
        this.claimActiviti(actTaskId, userId);
        flowTask.setActClaimTime(new Date());
        flowTask.setTaskStatus(TaskStatus.CLAIM.toString());
        flowTaskDao.save(flowTask);
        flowTaskDao.deleteNotClaimTask(actTaskId, id);
        OperateResult result = OperateResult.operationSuccess("10012");
        return result;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public OperateResultWithData<FlowStatus> complete(FlowTaskCompleteVO flowTaskCompleteVO) throws Exception {
        String taskId = flowTaskCompleteVO.getTaskId();
        Map<String, Object> variables = flowTaskCompleteVO.getVariables();
        Map<String,String> manualSelectedNodes = flowTaskCompleteVO.getManualSelectedNode();
        OperateResultWithData<FlowStatus> result = null;
        try{
        if (manualSelectedNodes == null || manualSelectedNodes.isEmpty()) {//非人工选择任务的情况
            result = this.complete(taskId, flowTaskCompleteVO.getOpinion(), variables);
        } else {//人工选择任务的情况
            FlowTask flowTask = flowTaskDao.findOne(taskId);
            String taskJsonDef = flowTask.getTaskJsonDef();
            JSONObject taskJsonDefObj = JSONObject.fromObject(taskJsonDef);
            String nodeType = taskJsonDefObj.get("nodeType")+"";//针对审批网关的情况
            String actTaskId = flowTask.getActTaskId();
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
            // 取得当前活动定义节点
            ActivityImpl currActivity = ((ProcessDefinitionImpl) definition)
                    .findActivity(currTask.getTaskDefinitionKey());
            if("Approve".equalsIgnoreCase(nodeType)){//针对审批任务的情况
                currActivity = (ActivityImpl)currActivity.getOutgoingTransitions().get(0).getDestination();
                String defaultSequenId = (String)currActivity.getProperty("default");
                Map<PvmTransition,String> oriPvmTransitionMap = new LinkedHashMap<PvmTransition,String>();
                List<PvmTransition> pvmTransitionList = currActivity.getOutgoingTransitions();
                for (PvmTransition pvmTransition : pvmTransitionList) {
                    UelExpressionCondition uel= (UelExpressionCondition)pvmTransition.getProperty("condition");
                    String uelText = (String) pvmTransition.getProperty("conditionText");
                    if(pvmTransition.getId().equals(defaultSequenId)){
                        continue;
                    }
                    for (Map.Entry<String,String> entry : manualSelectedNodes.entrySet()) {
                        String nodeId = entry.getValue();
                        ActivityImpl destinationActivity = ((ProcessDefinitionImpl) definition).findActivity(nodeId);
                        if(destinationActivity!=null && FlowTaskTool.checkNextHas(pvmTransition.getDestination(),destinationActivity)){
                            oriPvmTransitionMap.put(pvmTransition,uelText);
                            String proName = destinationActivity.getId()+"_approveResult";
                            uelText = "${"+proName+" == true}";
                            uel = new UelExpressionCondition(uelText);
                            ((ProcessElementImpl)pvmTransition).setProperty("condition",uel);
                            ((ProcessElementImpl)pvmTransition).setProperty("conditionText",uelText);
                            variables.put(proName,true);
                        }
                    }
                }
                variables.put("approveResult",null);
                //执行任务
                result = this.complete(taskId, flowTaskCompleteVO.getOpinion(), variables);
                if(!oriPvmTransitionMap.isEmpty()){
                    for(Map.Entry<PvmTransition,String> entry:oriPvmTransitionMap.entrySet()){
                        PvmTransition pvmTransition =  entry.getKey();
                        String uelText = entry.getValue();
                        UelExpressionCondition uel = new UelExpressionCondition(uelText);
                        ((ProcessElementImpl)pvmTransition).setProperty("condition",uel);
                        ((ProcessElementImpl)pvmTransition).setProperty("conditionText",uelText);
                    }
                }
            }else {//针对人工网关的情况
                ActivityImpl  currActivityTemp = (ActivityImpl)currActivity.getOutgoingTransitions().get(0).getDestination();
                boolean gateWay = FlowTaskTool.ifExclusiveGateway(currActivityTemp);
                if(gateWay){
                    currActivity = currActivityTemp;
                }
                Map<PvmTransition,String> oriPvmTransitionMap = new LinkedHashMap<PvmTransition,String>();
                List<PvmTransition> pvmTransitionList = currActivity.getOutgoingTransitions();
                for (PvmTransition pvmTransition : pvmTransitionList) {
                    UelExpressionCondition uel= (UelExpressionCondition)pvmTransition.getProperty("condition");
                    PvmActivity nextNode = pvmTransition.getDestination();
                    String uelText = (String) pvmTransition.getProperty("conditionText");
                    boolean isSet = false;
                    for (Map.Entry<String,String> entry : manualSelectedNodes.entrySet()) {
                        String nodeId = entry.getValue();
                        if(!nodeId.equals(entry.getKey())){//存在子流程的情况
                            String path = entry.getKey();
                            String[] resultArray = path.split("/");
                            nodeId = resultArray[2];
                        }
                        ActivityImpl destinationActivity = ((ProcessDefinitionImpl) definition).findActivity(nodeId);
                        if(destinationActivity!=null && FlowTaskTool.checkNextHas(pvmTransition.getDestination(),destinationActivity)){
                            oriPvmTransitionMap.put(pvmTransition,uelText);
                            String proName = destinationActivity.getId()+"_approveResult";
                            uelText = "${"+proName+" == true}";
                            uel = new UelExpressionCondition(uelText);
                            ((ProcessElementImpl)pvmTransition).setProperty("condition",uel);
                            ((ProcessElementImpl)pvmTransition).setProperty("conditionText",uelText);
                            variables.put(proName,true);
                            isSet=true;
                            break;
                        }
                    }
                    if(gateWay && !isSet && (uel == null || StringUtils.isEmpty(uelText))){
                        oriPvmTransitionMap.put(pvmTransition,uelText);
                        uelText = "${0>1}";
                        uel = new UelExpressionCondition(uelText);
                        ((ProcessElementImpl)pvmTransition).setProperty("condition",uel);
                        ((ProcessElementImpl)pvmTransition).setProperty("conditionText",uelText);
                    }
                }
                //执行任务
                result = this.complete(taskId, flowTaskCompleteVO.getOpinion(), variables);
                if(!oriPvmTransitionMap.isEmpty()){
                    for(Map.Entry<PvmTransition,String> entry:oriPvmTransitionMap.entrySet()){
                        PvmTransition pvmTransition =  entry.getKey();
                        String uelText = entry.getValue();
                        if(StringUtils.isNotEmpty(uelText)){
                            UelExpressionCondition uel = new UelExpressionCondition(uelText);
                            ((ProcessElementImpl)pvmTransition).setProperty("condition",uel);
                            ((ProcessElementImpl)pvmTransition).setProperty("conditionText",uelText);
                        }else {
                            ((ProcessElementImpl)pvmTransition).setProperty("condition",null);
                            ((ProcessElementImpl)pvmTransition).setProperty("conditionText",null);
                        }
                    }
                }
            }
          }
        }catch (FlowException e){
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            result =   OperateResultWithData.operationFailure(e.getMessage());
        }
        return result;
    }


    /**
     * 完成任务
     *
     * @param id        任务id
     * @param opinion   审批意见
     * @param variables 参数
     * @return
     */
    @Transactional(propagation = Propagation.REQUIRED)
    private OperateResultWithData<FlowStatus> complete(String id, String opinion, Map<String, Object> variables) throws Exception{
        FlowTask flowTask = flowTaskDao.findOne(id);
        FlowInstance flowInstance = flowTask.getFlowInstance();
        flowTask.setDepict(opinion);
        Integer reject = null;
        if (variables != null) {
            Object rejectO = variables.get("reject");
            if (rejectO != null) {
                try {
                    reject = Integer.parseInt(rejectO.toString());
                } catch (Exception e) {
                    logger.error(e.getMessage());
                }
            }
        }
        if (reject != null && reject == 1) {
            flowTask.setDepict("【被驳回】" + flowTask.getDepict());
            flowTask.setTaskStatus(TaskStatus.REJECT.toString());
            flowTask.setPriority(1);
        } else {
            flowTask.setTaskStatus(TaskStatus.COMPLETED.toString());
        }
        //匿名用户执行的情况，直接使用当前上下文用户
        if(Constants.ANONYMOUS.equalsIgnoreCase(flowTask.getOwnerId())&& !Constants.ANONYMOUS.equalsIgnoreCase(ContextUtil.getUserId())){
            flowTask.setOwnerId(ContextUtil.getUserId());
            flowTask.setOwnerAccount(ContextUtil.getUserAccount());
            flowTask.setOwnerName(ContextUtil.getUserName());
            flowTask.setExecutorId(ContextUtil.getUserId());
            flowTask.setExecutorAccount(ContextUtil.getUserAccount());
            flowTask.setExecutorName(ContextUtil.getUserName());
        }
        variables.put("opinion", flowTask.getDepict());
        String actTaskId = flowTask.getActTaskId();

        //获取当前业务实体表单的条件表达式信息，（目前是任务执行时就注入，后期根据条件来优化)
        String businessId = flowInstance.getBusinessId();
        BusinessModel businessModel = flowTask.getFlowInstance().getFlowDefVersion().getFlowDefination().getFlowType().getBusinessModel();
        Map<String, Object> v = ExpressionUtil.getPropertiesValuesMap( businessModel,businessId,true);
        if (v != null && !v.isEmpty()) {
            if (variables == null) {
                variables = new HashMap<String, Object>();
            }
            variables.putAll(v);
        }
        flowInstance.setBusinessModelRemark(v.get("workCaption") + "");
        String taskJsonDef = flowTask.getTaskJsonDef();
        JSONObject taskJsonDefObj = JSONObject.fromObject(taskJsonDef);
        String nodeType = taskJsonDefObj.get("nodeType")+"";//会签
        Boolean counterSignLastTask = false;
        // 取得当前任务
        HistoricTaskInstance currTask = historyService
                .createHistoricTaskInstanceQuery().taskId(flowTask.getActTaskId())
                .singleResult();
        if("CounterSign".equalsIgnoreCase(nodeType)){//会签任务做处理判断
            String executionId = currTask.getExecutionId();
            Map<String, VariableInstance>      processVariables= runtimeService.getVariableInstances(executionId);
            //完成会签的次数
            Integer completeCounter=(Integer)processVariables.get("nrOfCompletedInstances").getValue();
            //总循环次数
            Integer instanceOfNumbers=(Integer)processVariables.get("nrOfInstances").getValue();
            if(completeCounter+1==instanceOfNumbers){//会签最后一个任务
                counterSignLastTask=true;
                //通过票数
                Integer counterSignAgree = 0;
                if(processVariables.get("counterSign_agree"+currTask.getTaskDefinitionKey())!=null) {
                    counterSignAgree = (Integer) processVariables.get("counterSign_agree"+currTask.getTaskDefinitionKey()).getValue();
                }
                int counterDecision=100;
                try {
                    counterDecision = taskJsonDefObj.getJSONObject("nodeConfig").getJSONObject("normal").getInt("counterDecision");
                }catch (Exception e){
                    logger.error(e.getMessage());
                }
                String approved = variables.get("approved")+"";
                Integer value = 0;//默认弃权
                if("true".equalsIgnoreCase(approved)){
                    counterSignAgree++;
                }
                ProcessDefinitionEntity definition = (ProcessDefinitionEntity) ((RepositoryServiceImpl) repositoryService)
                        .getDeployedProcessDefinition(currTask
                                .getProcessDefinitionId());
                if (definition == null) {
                    logger.error(ContextUtil.getMessage("10003"));
                }
                //取得当前活动定义节点
                ActivityImpl currActivity = ((ProcessDefinitionImpl) definition)
                        .findActivity(currTask.getTaskDefinitionKey());

                PvmActivity destinationActivity = null;
                if(counterDecision<=((counterSignAgree/(instanceOfNumbers+0.0))*100)){//获取通过节点
                    variables.put("approveResult",true);
                }
                else{
                    variables.put("approveResult",false);
                }
                //执行任务
                this.completeActiviti(actTaskId, variables);
            }else {
                this.completeActiviti(actTaskId, variables);
            }

        }else if("Approve".equalsIgnoreCase(nodeType)){
            String approved = variables.get("approved")+"";
            if("true".equalsIgnoreCase(approved)){
                variables.put("approveResult",true);
            }else {
                variables.put("approveResult",false);
            }
            this.completeActiviti(actTaskId, variables);
            counterSignLastTask = true;
        }else if ("ParallelTask".equalsIgnoreCase(nodeType)||"SerialTask".equalsIgnoreCase(nodeType)){
            String executionId = currTask.getExecutionId();
            Map<String, VariableInstance>      processVariables= runtimeService.getVariableInstances(executionId);
            //完成会签的次数
            Integer completeCounter=(Integer)processVariables.get("nrOfCompletedInstances").getValue();
            //总循环次数
            Integer instanceOfNumbers=(Integer)processVariables.get("nrOfInstances").getValue();
            if(completeCounter+1==instanceOfNumbers){//最后一个任务
                counterSignLastTask=true;}
            this.completeActiviti(actTaskId, variables);
        }
        else {
            this.completeActiviti(actTaskId, variables);
            counterSignLastTask = true;
        }
//        this.saveVariables(variables, flowTask);先不做保存
        HistoricTaskInstance historicTaskInstance = historyService.createHistoricTaskInstanceQuery().taskId(actTaskId).singleResult(); // 创建历史任务实例查询

        // 取得流程实例
        ProcessInstance instance = runtimeService
                .createProcessInstanceQuery()
                .processInstanceId(historicTaskInstance.getProcessInstanceId())
                .singleResult();
        if (historicTaskInstance != null) {
            String defJson = flowTask.getTaskJsonDef();
            JSONObject defObj = JSONObject.fromObject(defJson);
            net.sf.json.JSONObject normalInfo = defObj.getJSONObject("nodeConfig").getJSONObject("normal");

            Boolean canCancel = null;
            if( normalInfo.get("allowPreUndo")!=null){
                canCancel =  normalInfo.getBoolean("allowPreUndo");
            }
            FlowHistory flowHistory = flowTaskTool.initFlowHistory(flowTask,historicTaskInstance,canCancel, variables);

            flowHistoryDao.save(flowHistory);
            flowTaskDao.delete(flowTask);
            if("SingleSign".equalsIgnoreCase(nodeType)) {//单签任务，清除其他待办
                flowTaskDao.deleteNotClaimTask(actTaskId, id);//删除其他候选用户的任务
            }
            //初始化新的任务
            String actTaskDefKey = flowTask.getActTaskDefKey();
            String actProcessDefinitionId = flowTask.getFlowInstance().getFlowDefVersion().getActDefId();
            ProcessDefinitionEntity definition = null;
            PvmActivity currentNode = null;
            FlowInstance flowInstanceTemp = flowInstance;
            FlowInstance flowInstanceP = flowInstanceTemp.getParent();
            boolean sonEndButParnetNotEnd = false;
            while(flowInstanceTemp.isEnded() && (flowInstanceP != null )){//子流程结束，主流程未结束
                if(!flowInstanceP.isEnded()){
                    actProcessDefinitionId = flowInstanceP.getFlowDefVersion().getActDefId();
                    definition = (ProcessDefinitionEntity) ((RepositoryServiceImpl) repositoryService)
                            .getDeployedProcessDefinition(actProcessDefinitionId);
                    String superExecutionId = null;
                    superExecutionId = (String) runtimeService.getVariable(flowInstanceP.getActInstanceId(),flowInstanceTemp.getActInstanceId()+"_superExecutionId");
                    HistoricActivityInstance historicActivityInstance = null;
                    HistoricActivityInstanceQuery his = historyService.createHistoricActivityInstanceQuery()
                            .executionId(superExecutionId).activityType("callActivity");
                    if (his != null) {
                        historicActivityInstance = his.singleResult();
                        HistoricActivityInstanceEntity he = (HistoricActivityInstanceEntity) historicActivityInstance;
                        actTaskDefKey = he.getActivityId();
                        currentNode = FlowTaskTool.getActivitNode(definition,actTaskDefKey);
                        callInitTaskBack(currentNode, flowInstanceP, flowHistory,counterSignLastTask);
                    }
                }
                sonEndButParnetNotEnd =true;
                flowInstanceTemp = flowInstanceP;
                flowInstanceP = flowInstanceTemp.getParent();
            }
            if(!sonEndButParnetNotEnd){
                    definition = (ProcessDefinitionEntity) ((RepositoryServiceImpl) repositoryService)
                            .getDeployedProcessDefinition(actProcessDefinitionId);
                    currentNode = FlowTaskTool.getActivitNode(definition,actTaskDefKey);
                    if (instance != null && currentNode != null && (!"endEvent".equalsIgnoreCase(currentNode.getProperty("type") + ""))) {
                        callInitTaskBack(currentNode, flowInstance, flowHistory,counterSignLastTask);
                    }
             }
        }
        OperateResultWithData<FlowStatus> result = OperateResultWithData.operationSuccess("10017");
        if (instance == null || instance.isEnded()) {
            result.setData(FlowStatus.COMPLETED);//任务结束
            flowTaskDao.deleteByFlowInstanceId(flowInstance.getId());//针对终止结束时，删除所有待办
        }
        return result;
    }


    private void callInitTaskBack(PvmActivity currentNode,  FlowInstance flowInstance, FlowHistory flowHistory,boolean counterSignLastTask) {
        if(!counterSignLastTask && FlowTaskTool.ifMultiInstance(currentNode)){
           String sequential= currentNode.getProperty("multiInstance")+"";
           if("sequential".equalsIgnoreCase(sequential)){//会签当中串行任务,非最后一个任务
               String key = currentNode.getProperty("key") != null ? currentNode.getProperty("key").toString() : null;
               if (key == null) {
                   key = currentNode.getId();
               }
               flowTaskTool.initTask(flowInstance, flowHistory,key);
               return;
           }
        }
        List<PvmTransition> nextNodes = currentNode.getOutgoingTransitions();
        if (nextNodes != null && nextNodes.size() > 0) {
            for (PvmTransition node : nextNodes) {
                PvmActivity nextActivity = node.getDestination();
                if (FlowTaskTool.ifGageway(nextActivity)||"ManualTask".equalsIgnoreCase(nextActivity.getProperty("type") + "")) {
                    callInitTaskBack(nextActivity, flowInstance, flowHistory,counterSignLastTask);
                    continue;
                }
                String key = nextActivity.getProperty("key") != null ? nextActivity.getProperty("key").toString() : null;
                if (key == null) {
                    key = nextActivity.getId();
                }
                if("serviceTask".equalsIgnoreCase(nextActivity.getProperty("type") + "")){
                }else if("CallActivity".equalsIgnoreCase(nextActivity.getProperty("type") + "") && counterSignLastTask){
                    flowTaskTool.initTask(flowInstance,flowHistory,null);
                }else {
                    flowTaskTool.initTask(flowInstance, flowHistory,key);
                }
            }
        }
    }

    /**
     * 撤回到指定任务节点,加撤销意见
     *
     * @param id
     * @return
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public OperateResult rollBackTo(String id, String opinion) throws CloneNotSupportedException {
        OperateResult result = OperateResult.operationSuccess("core_00003");
        FlowHistory flowHistory = flowHistoryDao.findOne(id);
        result = flowTaskTool.taskRollBack(flowHistory, opinion);
        return result;
    }

    /**
     * 签收任务
     *
     * @param taskId
     * @param userId
     */
    private void claimActiviti(String taskId, String userId) {
        taskService.claim(taskId, userId);
    }

    /**
     * 完成任务
     *
     * @param taskId
     * @param variables
     */
    @Transactional(propagation = Propagation.REQUIRED)
    private void completeActiviti(String taskId, Map<String, Object> variables) throws Exception{
            taskService.complete(taskId, variables);
    }

    /**
     * 任务驳回
     *
     * @param id        任务id
     * @param variables 参数
     * @return 结果
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public OperateResult taskReject(String id, String opinion, Map<String, Object> variables) throws Exception{
        OperateResult result = OperateResult.operationSuccess("10006");
        try{
        FlowTask flowTask = flowTaskDao.findOne(id);
        if (flowTask == null) {
            return OperateResult.operationFailure("10009");
        }
        flowTask.setDepict(opinion);
        if (flowTask != null && StringUtils.isNotEmpty(flowTask.getPreId())) {
            FlowHistory preFlowTask = flowHistoryDao.findOne(flowTask.getPreId());//上一个任务id
            if (preFlowTask == null) {
                return OperateResult.operationFailure("10016");
            } else {
                result = this.activitiReject(flowTask, preFlowTask);
            }
        } else {
            return OperateResult.operationFailure("10023");
        }}catch(FlowException flowE){
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return OperateResult.operationFailure(flowE.getMessage());
        }
        return result;
    }


    /**
     * 驳回前一个任务
     *
     * @param currentTask 当前任务
     * @param preFlowTask 上一个任务
     * @return 结果
     */
    @Transactional(propagation = Propagation.REQUIRED)
    private OperateResult activitiReject(FlowTask currentTask, FlowHistory preFlowTask) throws Exception{
        OperateResult result = OperateResult.operationSuccess("10015");
        // 取得当前任务
        HistoricTaskInstance currTask = historyService.createHistoricTaskInstanceQuery().taskId(currentTask.getActTaskId())
                .singleResult();
        // 取得流程实例
        ProcessInstance instance = runtimeService.createProcessInstanceQuery()
                .processInstanceId(currTask.getProcessInstanceId()).singleResult();
        if (instance == null) {
            OperateResult.operationFailure("10009");
        }
        Map variables = new HashMap();
        Map variablesProcess = instance.getProcessVariables();
        Map variablesTask = currTask.getTaskLocalVariables();
        if ((variablesProcess != null) && (!variablesProcess.isEmpty())) {
            variables.putAll(variablesProcess);
        }
        if ((variablesTask != null) && (!variablesTask.isEmpty())) {
            variables.putAll(variablesTask);
        }

        // 取得流程定义
        ProcessDefinitionEntity definition = (ProcessDefinitionEntity) ((RepositoryServiceImpl) repositoryService)
                .getDeployedProcessDefinition(currTask.getProcessDefinitionId());
        if (definition == null) {
            OperateResult.operationFailure("10009");
        }

        // 取得当前任务标节点的活动
        ActivityImpl currentActivity = ((ProcessDefinitionImpl) definition)
                .findActivity(currentTask.getActTaskDefKey());
        // 取得驳回目标节点的活动
        while("cancel".equalsIgnoreCase(preFlowTask.getTaskStatus())||"reject".equalsIgnoreCase(preFlowTask.getTaskStatus())||preFlowTask.getActTaskDefKey().equals(currentTask.getActTaskDefKey())){//如果前一任务为撤回或者驳回任务，则依次向上迭代
            String preFlowTaskId = preFlowTask.getPreId();
            if(StringUtils.isNotEmpty(preFlowTaskId)){
                preFlowTask =  flowHistoryDao.findOne(preFlowTaskId);
            }else {
                return   OperateResult.operationFailure("10016");
            }
        }
        ActivityImpl preActivity = ((ProcessDefinitionImpl) definition)
                .findActivity(preFlowTask.getActTaskDefKey());
        if (FlowTaskTool.checkCanReject(currentActivity, preActivity, instance,
                definition)) {
            //取活动，清除活动方向
            List<PvmTransition> oriPvmTransitionList = new ArrayList<PvmTransition>();
            List<PvmTransition> pvmTransitionList = currentActivity
                    .getOutgoingTransitions();
            for (PvmTransition pvmTransition : pvmTransitionList) {
                oriPvmTransitionList.add(pvmTransition);
            }
            pvmTransitionList.clear();
            //建立新方向
            TransitionImpl newTransition = currentActivity
                    .createOutgoingTransition();
            // 取得转向的目标，这里需要指定用需要回退到的任务节点
            newTransition.setDestination(preActivity);

            //完成任务
            variables.put("reject", 1);
            this.complete(currentTask.getId(), currentTask.getDepict(), variables);

            //恢复方向
            preActivity.getIncomingTransitions().remove(newTransition);
            List<PvmTransition> pvmTList = currentActivity
                    .getOutgoingTransitions();
            pvmTList.clear();
            for (PvmTransition pvmTransition : oriPvmTransitionList) {
                pvmTransitionList.add(pvmTransition);
            }
          //  variablesProcess.put("reject", 0);//将状态重置
            runtimeService.removeVariable(instance.getProcessInstanceId(),"reject");//将状态重置
        } else {
            result = OperateResult.operationFailure("10016");
        }
        return result;
    }

    /**
     * 获取出口节点信息（带初始化流程设计器配置用户）
     *
     * @param id
     * @return
     * @throws NoSuchMethodException
     */
    public List<NodeInfo> findNexNodesWithUserSet(String id,String approved) throws NoSuchMethodException {
        FlowTask flowTask = flowTaskDao.findOne(id);
        if (flowTask == null) {
            return null;
        }
        return this.findNexNodesWithUserSet(flowTask, approved,null);
    }

    /**
     * 获取出口节点信息（带初始化流程设计器配置用户）
     *
     * @param flowTask
     * @param approved
     * @return
     * @throws NoSuchMethodException
     */
    public List<NodeInfo> findNexNodesWithUserSet(FlowTask flowTask ,String approved, List<String> includeNodeIds) throws NoSuchMethodException {
        List<NodeInfo> nodeInfoList = flowTaskTool.findNextNodesWithCondition( flowTask,approved, includeNodeIds);

        if (nodeInfoList != null && !nodeInfoList.isEmpty()) {
            String flowDefJson = flowTask.getFlowInstance().getFlowDefVersion().getDefJson();
            JSONObject defObj = JSONObject.fromObject(flowDefJson);
            Definition definition = (Definition) JSONObject.toBean(defObj, Definition.class);

            String flowTaskDefJson = flowTask.getTaskJsonDef();
            JSONObject flowTaskDefObj = JSONObject.fromObject(flowTaskDefJson);
            String currentNodeType = flowTaskDefObj.get("nodeType") + "";
            Map<NodeInfo,List<NodeInfo>> nodeInfoSonMap = new LinkedHashMap();
            for (NodeInfo nodeInfo : nodeInfoList) {
                nodeInfo.setCurrentTaskType(currentNodeType);
                if ("CounterSignNotEnd".equalsIgnoreCase(nodeInfo.getType())) {
                    continue;
                }else if("serviceTask".equalsIgnoreCase(nodeInfo.getType())){
                    nodeInfo.setUserVarName(nodeInfo.getId() + "_ServiceTask");
                    nodeInfo.setUiType("radiobox");
                    nodeInfo.setFlowTaskType("serviceTask");
                    String  startUserId =  ContextUtil.getSessionUser().getUserId();
                    Map<String,Object> params = new HashMap();
                    params.put("employeeIds",java.util.Arrays.asList(startUserId));
                    String url = Constants.getBasicEmployeeGetexecutorsbyemployeeidsUrl();
                    List<Executor> employees=ApiClient.getEntityViaProxy(url,new GenericType<List<Executor>>() {},params);
                    if (employees != null && !employees.isEmpty()) {//服务任务默认选择流程启动人
                        Set<Executor> employeeSet = new HashSet<Executor>();
                        employeeSet.addAll(employees);
                        nodeInfo.setExecutorSet(employeeSet);
                    }
                }else if("receiveTask".equalsIgnoreCase(nodeInfo.getType())){
                    nodeInfo.setUserVarName(nodeInfo.getId() + "_ReceiveTask");
                    nodeInfo.setUiType("radiobox");
                    nodeInfo.setFlowTaskType("receiveTask");
                    String  startUserId =  ContextUtil.getSessionUser().getUserId();
                    Map<String,Object> params = new HashMap();
                    params.put("employeeIds",java.util.Arrays.asList(startUserId));
                    String url = Constants.getBasicEmployeeGetexecutorsbyemployeeidsUrl();
                    List<Executor> employees=ApiClient.getEntityViaProxy(url,new GenericType<List<Executor>>() {},params);
                    if (employees != null && !employees.isEmpty()) {//服务任务默认选择流程启动人
                        Set<Executor> employeeSet = new HashSet<Executor>();
                        employeeSet.addAll(employees);
                        nodeInfo.setExecutorSet(employeeSet);
                    }
                }else if("callActivity".equalsIgnoreCase(nodeInfo.getType())){
                    List<NodeInfo> nodeInfoListSons = new ArrayList<NodeInfo>();
                    nodeInfoListSons =  flowTaskTool.getCallActivityNodeInfo(flowTask,nodeInfo.getId(),nodeInfoListSons);
                    nodeInfoSonMap.put(nodeInfo,nodeInfoListSons);
                }else {
                    Set<Executor> executorSet = nodeInfo.getExecutorSet();
                    if(executorSet!=null && !executorSet.isEmpty()){
                        continue;
                    }
                    net.sf.json.JSONObject currentNode = definition.getProcess().getNodes().getJSONObject(nodeInfo.getId());
                    net.sf.json.JSONObject executor = null;
                    net.sf.json.JSONArray executorList=null;//针对两个条件以上的情况
                    if(currentNode.getJSONObject(Constants.NODE_CONFIG).has(Constants.EXECUTOR)){
                        try {
                            executor = currentNode.getJSONObject(Constants.NODE_CONFIG).getJSONObject(Constants.EXECUTOR);
                        }catch (Exception e){
                            if(executor == null){
                                try {
                                    executorList = currentNode.getJSONObject(Constants.NODE_CONFIG).getJSONArray(Constants.EXECUTOR);
                                }catch (Exception e2){
                                    e2.printStackTrace();
                                }
                                if(executorList!=null && executorList.size()==1){
                                    executor = executorList.getJSONObject(0);
                                }
                            }
                        }
                    }

                    UserTask userTaskTemp = (UserTask) JSONObject.toBean(currentNode, UserTask.class);
                    if ("EndEvent".equalsIgnoreCase(userTaskTemp.getType())) {
                        nodeInfo.setType("EndEvent");
                        continue;
                    }
                    if(StringUtils.isEmpty(nodeInfo.getUserVarName())){
                        if ("Normal".equalsIgnoreCase(userTaskTemp.getNodeType())) {
                            nodeInfo.setUserVarName(userTaskTemp.getId() + "_Normal");
                        } else if ("SingleSign".equalsIgnoreCase(userTaskTemp.getNodeType())) {
                            nodeInfo.setUserVarName(userTaskTemp.getId() + "_SingleSign");
                            nodeInfo.setUiType("checkbox");
                        } else if ("Approve".equalsIgnoreCase(userTaskTemp.getNodeType())) {
                            nodeInfo.setUserVarName(userTaskTemp.getId() + "_Approve");
                        } else if ("CounterSign".equalsIgnoreCase(userTaskTemp.getNodeType())||"ParallelTask".equalsIgnoreCase(userTaskTemp.getNodeType())||"SerialTask".equalsIgnoreCase(userTaskTemp.getNodeType())) {
                            nodeInfo.setUserVarName(userTaskTemp.getId() + "_List_CounterSign");
                            nodeInfo.setUiType("checkbox");
                        }
                    }

                    if (executor != null && !executor.isEmpty()) {
                        String userType = (String) executor.get("userType");
                        String ids = (String) executor.get("ids");
                        Set<Executor> employeeSet = new HashSet<Executor>();
                        List<Executor> employees = null;
                        nodeInfo.setUiUserType(userType);
                        if ("StartUser".equalsIgnoreCase(userType)) {//获取流程实例启动者
                            FlowInstance flowInstance = flowTask.getFlowInstance();
                            while(flowInstance.getParent() != null){ //以父流程的启动人为准
                                flowInstance = flowInstance.getParent();
                            }
                            String startUserId = null;
                            HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery().processInstanceId(flowInstance.getActInstanceId()).singleResult();
                            if(historicProcessInstance==null){//当第一个任务为服务任务的时候存在为空的情况发生
                                startUserId = ContextUtil.getUserId();
                            }else{
                                 startUserId = historicProcessInstance.getStartUserId();
                            }
                            Map<String,Object> params = new HashMap();
                            params.put("employeeIds",java.util.Arrays.asList(startUserId));
                            String url = Constants.getBasicEmployeeGetexecutorsbyemployeeidsUrl();
                            employees=ApiClient.getEntityViaProxy(url,new GenericType<List<Executor>>() {},params);

                        } else {
                            String selfDefId = (String)executor.get("selfDefId");
                            if (StringUtils.isNotEmpty(ids)||StringUtils.isNotEmpty(selfDefId)) {
                                if ("SelfDefinition".equalsIgnoreCase(userType)) {//通过业务ID获取自定义用户
                                    FlowExecutorConfig flowExecutorConfig = flowExecutorConfigDao.findOne(selfDefId);
                                    String path = flowExecutorConfig.getUrl();
                                    AppModule appModule= flowExecutorConfig.getBusinessModel().getAppModule();
                                    String appModuleCode = appModule.getCode();
                                    Map<String, String>  params = new HashMap<String,String>();;
                                    String param = flowExecutorConfig.getParam();
                                    String businessId = flowTask.getFlowInstance().getBusinessId();
                                    params.put("businessId",businessId);
                                    params.put("paramJson",param);
                                    employees =  ApiClient.postViaProxyReturnResult(appModuleCode,  path,new GenericType<List<Executor>>() {}, params);
                                }else{
                                    employees=flowTaskTool.getExecutors(userType, ids);
                                }
                            }
                        }
                        if (employees != null && !employees.isEmpty()) {
                            employeeSet.addAll(employees);
                            nodeInfo.setExecutorSet(employeeSet);
                        }
                    }else if(executorList!=null && executorList.size()>1){
                        Set<Executor> employeeSet = new HashSet<Executor>();
                        List<Executor> employees = null;
                        String selfDefId = null;
                        List<String> orgDimensionCodes = null;//组织维度代码集合
                        List<String> positionIds = null;//岗位代码集合
                        for(Object executorObject:executorList.toArray()){
                            JSONObject executorTemp = (JSONObject) executorObject;
                            String userType = executorTemp.get("userType") + "";
                            String ids = executorTemp.get("ids") + "";
//                nodeInfo.setUiUserType(userType);
                            List<String> tempList = null;
                            if(StringUtils.isNotEmpty(ids)){
                                String[] idsShuZhu = ids.split(",");
                                tempList = java.util.Arrays.asList(idsShuZhu);
                            }
                            if ("SelfDefinition".equalsIgnoreCase(userType)) {//通过业务ID获取自定义用户
                                selfDefId = executorTemp.get("selfDefOfOrgAndSelId") + "";
                            }else if("Position".equalsIgnoreCase(userType)){
                                positionIds = tempList;
                            }else if("OrganizationDimension".equalsIgnoreCase(userType)){
                                orgDimensionCodes = tempList;
                            }
                        }
                        // 取得当前任务
                        HistoricTaskInstance currTask = historyService
                                .createHistoricTaskInstanceQuery().taskId(flowTask.getActTaskId())
                                .singleResult();
                        String executionId = currTask.getExecutionId();
                        Map<String, VariableInstance>      processVariables= runtimeService.getVariableInstances(executionId);
                        String currentOrgId = processVariables.get("orgId").getValue()+"";
                        if(StringUtils.isNotEmpty(selfDefId) && !Constants.NULL_S.equalsIgnoreCase(selfDefId)){
                            FlowExecutorConfig flowExecutorConfig = flowExecutorConfigDao.findOne(selfDefId);
                            String path = flowExecutorConfig.getUrl();
                            AppModule appModule = flowExecutorConfig.getBusinessModel().getAppModule();
                            String appModuleCode = appModule.getApiBaseAddress();
                            String param = flowExecutorConfig.getParam();
                            FlowInvokeParams flowInvokeParams = new FlowInvokeParams();
                            flowInvokeParams.setId(flowTask.getFlowInstance().getBusinessId());

                            flowInvokeParams.setOrgId(currentOrgId);
                            flowInvokeParams.setOrgDimensionCodes(orgDimensionCodes);
                            flowInvokeParams.setPositionIds(positionIds);
                            flowInvokeParams.setJsonParam(param);
                            employees = ApiClient.postViaProxyReturnResult(appModuleCode, path, new GenericType<List<Executor>>() {
                            }, flowInvokeParams);
                        }else{
                            String path =  Constants.getBasicPositionGetExecutorsUrl();
                            Map<String,Object> params = new HashMap();
                            params.put("orgId",currentOrgId);
                            params.put("orgDimIds",orgDimensionCodes);
                            params.put("positionIds",positionIds);
//                params.put("orgId","0");
//                params.put("orgDimIds",java.util.Arrays.asList("1"));
//                params.put("positionIds",java.util.Arrays.asList("1"));
                            employees=ApiClient.getEntityViaProxy(path,new GenericType<List<Executor>>() {},params);
                        }
                        if (employees != null && !employees.isEmpty()) {
                            employeeSet.addAll(employees);
                            nodeInfo.setExecutorSet(employeeSet);
                        }
                    }
                }
            }
            if(nodeInfoSonMap!=null && !nodeInfoSonMap.isEmpty()){
                  for(Map.Entry<NodeInfo,List<NodeInfo>> entry:nodeInfoSonMap.entrySet()){
                      nodeInfoList.remove(entry.getKey());
                      nodeInfoList.addAll(entry.getValue());
                  }
            }
        }
        return nodeInfoList;
    }

    public List<NodeInfo> findNextNodes(String id) throws NoSuchMethodException {
        FlowTask flowTask = flowTaskDao.findOne(id);
        if (flowTask == null) {
            return null;
        }
        String businessId = flowTask.getFlowInstance().getBusinessId();
        return this.findNextNodes(id, businessId);
    }

    /**
     * 选择下一步执行的节点信息
     *
     * @param id
     * @return
     * @throws NoSuchMethodException
     */
    public List<NodeInfo> findNextNodes(String id, String businessId) throws NoSuchMethodException {
        return this.findNextNodes(id, businessId, null);
    }


    /**
     * 选择下一步执行的节点信息
     *
     * @param id
     * @return
     * @throws NoSuchMethodException
     */
    public List<NodeInfo> findNextNodes(String id, String businessId, List<String> includeNodeIds) throws NoSuchMethodException {
        FlowTask flowTask = flowTaskDao.findOne(id);
        return flowTaskTool.selectNextAllNodes(flowTask, includeNodeIds);
    }


    public ApprovalHeaderVO getApprovalHeaderVO(String id) {
        FlowTask flowTask = flowTaskDao.findOne(id);
        if (flowTask == null) {
            return null;
        }
        String preId = flowTask.getPreId();
        FlowHistory preFlowTask = null;
        ApprovalHeaderVO result = new ApprovalHeaderVO();
        result.setBusinessId(flowTask.getFlowInstance().getBusinessId());
        result.setBusinessCode(flowTask.getFlowInstance().getBusinessCode());
        result.setCreateUser(flowTask.getFlowInstance().getCreatorName());
        result.setCreateTime(flowTask.getFlowInstance().getCreatedDate());

        String defJson = flowTask.getTaskJsonDef();
        JSONObject defObj = JSONObject.fromObject(defJson);
        net.sf.json.JSONObject normalInfo = defObj.getJSONObject("nodeConfig").getJSONObject("normal");
        if(normalInfo.has("defaultOpinion")){
            result.setCurrentNodeDefaultOpinion(normalInfo.getString("defaultOpinion"));
        }

        if (!StringUtils.isEmpty(preId)) {
            preFlowTask = flowHistoryDao.findOne(flowTask.getPreId());//上一个任务id
        }
        if (preFlowTask == null) {//如果没有上一步任务信息,默认上一步为开始节点
            result.setPrUser(flowTask.getFlowInstance().getCreatorName());
            result.setPreCreateTime(flowTask.getFlowInstance().getCreatedDate());
            result.setPrOpinion("流程启动");
        } else {
            result.setPrUser(preFlowTask.getExecutorAccount() + "[" + preFlowTask.getExecutorName() + "]");
            result.setPreCreateTime(preFlowTask.getCreatedDate());
            result.setPrOpinion(preFlowTask.getDepict());
        }
        return result;
    }

    public List<NodeInfo> findNexNodesWithUserSet(String id) throws NoSuchMethodException {
        FlowTask flowTask = flowTaskDao.findOne(id);
        if (flowTask == null) {
            return null;
        }
        return this.findNexNodesWithUserSet(flowTask,null,null);
    }

    public List<NodeInfo> findNexNodesWithUserSet(FlowTask flowTask) throws NoSuchMethodException {
        if (flowTask == null) {
            return null;
        }
        return this.findNexNodesWithUserSet(flowTask,null,null);
    }

    public List<NodeInfo> findNexNodesWithUserSet(String id,String approved, List<String> includeNodeIds) throws NoSuchMethodException {
        FlowTask flowTask = flowTaskDao.findOne(id);
        if (flowTask == null) {
            return null;
        }
        List<NodeInfo> result = null;
        List<NodeInfo> nodeInfoList = this.findNexNodesWithUserSet( flowTask ,approved, includeNodeIds);
        result = nodeInfoList;
        FlowInstance parentFlowInstance = flowTask.getFlowInstance().getParent();
        FlowTask flowTaskTempSrc = new FlowTask();
        org.springframework.beans.BeanUtils.copyProperties(flowTask,flowTaskTempSrc);

        while (parentFlowInstance != null&&nodeInfoList != null && !nodeInfoList.isEmpty()&& nodeInfoList.size()==1&&"EndEvent".equalsIgnoreCase(nodeInfoList.get(0).getType()) ){//针对子流程结束节点
            FlowTask flowTaskTemp = new FlowTask();
            org.springframework.beans.BeanUtils.copyProperties(flowTaskTempSrc,flowTaskTemp);

            ProcessInstance instanceSon = runtimeService
                    .createProcessInstanceQuery()
                    .processInstanceId(flowTaskTemp.getFlowInstance().getActInstanceId())
                    .singleResult();
                flowTaskTemp.setFlowInstance(parentFlowInstance);
                // 取得流程实例
               String superExecutionId = instanceSon.getSuperExecutionId();
                HistoricActivityInstance historicActivityInstance = null;
                HistoricActivityInstanceQuery his = historyService.createHistoricActivityInstanceQuery()
                        .executionId(superExecutionId).activityType("callActivity").unfinished();
                if (his != null) {
                    historicActivityInstance = his.singleResult();
                    HistoricActivityInstanceEntity he = (HistoricActivityInstanceEntity) historicActivityInstance;
                    flowTaskTemp.setActTaskKey(he.getActivityId());
                    flowTaskTemp.setActTaskDefKey(he.getActivityId());
                    String flowDefJson = parentFlowInstance.getFlowDefVersion().getDefJson();
                    JSONObject defObj = JSONObject.fromObject(flowDefJson);
                    Definition definition = (Definition) JSONObject.toBean(defObj, Definition.class);
                    net.sf.json.JSONObject currentNode = definition.getProcess().getNodes().getJSONObject(he.getActivityId());
                    flowTaskTemp.setTaskJsonDef(currentNode.toString());
                    result = this.findNexNodesWithUserSet( flowTaskTemp ,approved, includeNodeIds);
                    flowTaskTempSrc =flowTaskTemp;
                }
            parentFlowInstance=parentFlowInstance.getParent();
            nodeInfoList=result;
        }
        return result;
    }


    /**
     * 根据流程实例Id获取待办
     *
     * @param instanceId
     * @return
     */
    @Override
    public List<FlowTask> findByInstanceId(String instanceId) {
        return flowTaskDao.findByInstanceId(instanceId);
    }


    /**
     * 查询当前用户待办业务单据汇总信息
     *
     * @return
     */
    public List<TodoBusinessSummaryVO> findTaskSumHeader() {
        return this.findCommonTaskSumHeader(false);
    }

    /**
     * 查询当前用户待办业务单据汇总信息,只有批量审批
     *
     * @return
     */
    public List<TodoBusinessSummaryVO> findCommonTaskSumHeader(Boolean batchApproval) {
        List<TodoBusinessSummaryVO> voList = null;
        String userID = ContextUtil.getUserId();
        List groupResultList = null;
        if(batchApproval==true){
            groupResultList = flowTaskDao.findByExecutorIdGroupCanBatchApproval(userID);
        }else {
            groupResultList = flowTaskDao.findByExecutorIdGroup(userID);
        }

        Map<BusinessModel, Integer> businessModelCountMap = new HashMap<BusinessModel, Integer>();
        if (groupResultList != null && !groupResultList.isEmpty()) {
            Iterator it = groupResultList.iterator();
            while (it.hasNext()) {
                Object[] res = (Object[]) it.next();
                int count = ((Number) res[0]).intValue();
                String flowDefinationId = res[1] + "";
                FlowDefination flowDefination = flowDefinationDao.findOne(flowDefinationId);
                BusinessModel businessModel = flowDefination.getFlowType().getBusinessModel();
                Integer oldCount = businessModelCountMap.get(businessModel);
                if (oldCount == null) {
                    oldCount = 0;
                }
                businessModelCountMap.put(businessModel, oldCount + count);
            }
        }
        if (businessModelCountMap != null && !businessModelCountMap.isEmpty()) {
            voList = new ArrayList<TodoBusinessSummaryVO>();
            for (Map.Entry<BusinessModel, Integer> map : businessModelCountMap.entrySet()) {
                TodoBusinessSummaryVO todoBusinessSummaryVO = new TodoBusinessSummaryVO();
                todoBusinessSummaryVO.setBusinessModelCode(map.getKey().getClassName());
                todoBusinessSummaryVO.setBusinessModeId(map.getKey().getId());
                todoBusinessSummaryVO.setCount(map.getValue());
                todoBusinessSummaryVO.setBusinessModelName(map.getKey().getName()+"("+map.getValue()+")");
                voList.add(todoBusinessSummaryVO);
            }
        }
        return voList;
    }

    public PageResult<FlowTask> findByBusinessModelId(String businessModelId, Search searchConfig) {
        String userId = ContextUtil.getUserId();
        if(StringUtils.isNotEmpty(businessModelId)){
            return flowTaskDao.findByPageByBusinessModelId(businessModelId, userId, searchConfig);
        }else{
            return flowTaskDao.findByPage(userId, searchConfig);
        }
    }

    public PageResult<FlowTask> findByPageCanBatchApproval(Search searchConfig) {
        String userId = ContextUtil.getUserId();
        PageResult<FlowTask> flowTaskPageResult = flowTaskDao.findByPageCanBatchApproval(userId, searchConfig);
        FlowTaskTool.changeTaskStatue(flowTaskPageResult);
        return flowTaskPageResult;
    }

    public PageResult<FlowTask> findByPageCanBatchApprovalByBusinessModelId(String businessModelId,Search searchConfig){
        String userId = ContextUtil.getUserId();
        PageResult<FlowTask> flowTaskPageResult = null;
        if(StringUtils.isNotEmpty(businessModelId)){
            flowTaskPageResult = flowTaskDao.findByPageCanBatchApprovalByBusinessModelId(businessModelId, userId, searchConfig);
        }else{
            flowTaskPageResult = flowTaskDao.findByPageCanBatchApproval(userId, searchConfig);
        }
        FlowTaskTool.changeTaskStatue(flowTaskPageResult);
        return flowTaskPageResult;
    }
    public FlowTaskPageResultVO<FlowTask> findByBusinessModelIdWithAllCount(String businessModelId, Search searchConfig) {
        String userId = ContextUtil.getUserId();
        Long allCount = flowTaskDao.findCountByExecutorId(userId, searchConfig);
        FlowTaskPageResultVO<FlowTask> resultVO = new FlowTaskPageResultVO<FlowTask>();
        PageResult<FlowTask>  pageResult = null;

        if(StringUtils.isNotEmpty(businessModelId)){
           pageResult = flowTaskDao.findByPageByBusinessModelId(businessModelId, userId, searchConfig);
        }else{
            pageResult = flowTaskDao.findByPage(userId, searchConfig);
        }
        resultVO.setRows(pageResult.getRows());
        resultVO.setRecords(pageResult.getRecords());
        resultVO.setPage(pageResult.getPage());
        resultVO.setTotal(pageResult.getTotal());
        resultVO.setAllTotal(allCount);
        return resultVO;
    }

    public List<BatchApprovalFlowTaskGroupVO> getBatchApprovalFlowTasks(List<String> taskIdArray) throws NoSuchMethodException{
        List<BatchApprovalFlowTaskGroupVO> result = new ArrayList<>();
        List<FlowTask> flowTaskList = this.findByIds(taskIdArray);
        if(flowTaskList!=null  &&  !flowTaskList.isEmpty()){
            for(FlowTask flowTask:flowTaskList){
                List<NodeInfo> nodeInfoList =  this.findNexNodesWithUserSet(flowTask,"true",null);
                BatchApprovalFlowTaskGroupVO batchApprovalFlowTaskGroupVO = new BatchApprovalFlowTaskGroupVO();
                String key = flowTask.getActTaskDefKey()+"@"+flowTask.getFlowInstance().getFlowDefVersion().getVersionCode()+"@"+flowTask.getFlowDefinitionId();
                batchApprovalFlowTaskGroupVO.setKey(key);
                int index = result.indexOf(batchApprovalFlowTaskGroupVO);
                if(index>-1){
                    batchApprovalFlowTaskGroupVO = result.get(index);
                }else{
                    result.add(batchApprovalFlowTaskGroupVO);
                }
                Map<FlowTask, List<NodeInfo>>  flowTaskNextNodesInfoMap= batchApprovalFlowTaskGroupVO.getFlowTaskNextNodesInfo();
                flowTaskNextNodesInfoMap.put(flowTask,nodeInfoList);
            }
        }
        return result;
    }
    public OperateResultWithData<FlowStatus> completeBatchApproval(List<FlowTaskCompleteVO> flowTaskCompleteVOList) throws Exception{
        for(FlowTaskCompleteVO flowTaskCompleteVO:flowTaskCompleteVOList){
            OperateResultWithData<FlowStatus>   tempResult = this.complete(flowTaskCompleteVO);
            if(!tempResult.successful()){
                throw new FlowException("batch approval is failure! ");
            }
        }
       return OperateResultWithData.operationSuccess("10017");
    }

    public OperateResultWithData getSelectedNodesInfo(String taskId,String approved, String includeNodeIdsStr) throws NoSuchMethodException {
        OperateResultWithData operateResultWithData = null;
        List<String> includeNodeIds = null;
        if (StringUtils.isNotEmpty(includeNodeIdsStr)) {
            String[] includeNodeIdsStringArray = includeNodeIdsStr.split(",");
            includeNodeIds = java.util.Arrays.asList(includeNodeIdsStringArray);
        }
        if(StringUtils.isEmpty(approved)){
            approved="true";
        }
        List<NodeInfo> nodeInfoList = this.findNexNodesWithUserSet(taskId,approved, includeNodeIds);
        if (nodeInfoList != null && !nodeInfoList.isEmpty()) {
            operateResultWithData = OperateResultWithData.operationSuccess();
            if(nodeInfoList.size()==1&&"EndEvent".equalsIgnoreCase(nodeInfoList.get(0).getType())){//只存在结束节点
                operateResultWithData.setData("EndEvent");
            }else if(nodeInfoList.size()==1&&"CounterSignNotEnd".equalsIgnoreCase(nodeInfoList.get(0).getType())){
                operateResultWithData.setData("CounterSignNotEnd");
            }else {
                operateResultWithData.setData(nodeInfoList);
            }
        } else {
            operateResultWithData =OperateResultWithData.operationFailure("10033");
        }
        return operateResultWithData;
    }


    public List<NodeInfo> findNexNodesWithUserSetCanBatch(String taskIds)  throws NoSuchMethodException{
        List<NodeInfo> all = new ArrayList<>();
        List<String> taskIdList = null;
        if (StringUtils.isNotEmpty(taskIds)) {
            String[] includeNodeIdsStringArray = taskIds.split(",");
            taskIdList = java.util.Arrays.asList(includeNodeIdsStringArray);
        }
        if(taskIdList != null && !taskIdList.isEmpty()){
            String approved="true";
            for(String taskId:taskIdList){
                List<NodeInfo> nodeInfoList = this.findNexNodesWithUserSet(taskId,approved, null);
                if(nodeInfoList != null && !nodeInfoList.isEmpty()){
                    all.addAll(nodeInfoList);
                }
            }
        }
        return all;
    }
    public List<NodeGroupInfo> findNexNodesGroupWithUserSetCanBatch(String taskIds)  throws NoSuchMethodException{
        List<NodeGroupInfo> all = new ArrayList<NodeGroupInfo>();
        List<String> taskIdList = null;
        if (StringUtils.isNotEmpty(taskIds)) {
            String[] includeNodeIdsStringArray = taskIds.split(",");
            taskIdList = java.util.Arrays.asList(includeNodeIdsStringArray);
        }
        if(taskIdList != null && !taskIdList.isEmpty()){
            String approved="true";
            Map<String,NodeGroupInfo> tempNodeGroupInfoMap = new HashMap<>();
            for(String taskId:taskIdList){
                List<NodeInfo> nodeInfoList = this.findNexNodesWithUserSet(taskId,approved, null);
                if(nodeInfoList != null && !nodeInfoList.isEmpty()){
                    for(NodeInfo nodeInfo:nodeInfoList){
                        String flowDefVersionId = nodeInfo.getFlowDefVersionId();
                        NodeGroupInfo tempNodeGroupInfo = tempNodeGroupInfoMap.get(flowDefVersionId+nodeInfo.getId());
                        if(tempNodeGroupInfo==null){
                            tempNodeGroupInfo = new NodeGroupInfo();
                            tempNodeGroupInfo.setFlowDefVersionId(flowDefVersionId);
                            tempNodeGroupInfo.setNodeId(nodeInfo.getId());
                            tempNodeGroupInfo.setFlowDefVersionName(nodeInfo.getFlowDefVersionName());
                            BeanUtils.copyProperties(nodeInfo,tempNodeGroupInfo);
                            tempNodeGroupInfo.getIds().add(nodeInfo.getFlowTaskId());
                            tempNodeGroupInfo.setExecutorSet(nodeInfo.getExecutorSet());
                            tempNodeGroupInfoMap.put(flowDefVersionId+nodeInfo.getId(),tempNodeGroupInfo);
                        }else {
                            tempNodeGroupInfo.getIds().add(nodeInfo.getFlowTaskId());
                        }
                    }
                }
            }
            all.addAll(tempNodeGroupInfoMap.values());
        }
        return all;
    }

    public List<NodeGroupByFlowVersionInfo> findNexNodesGroupByVersionWithUserSetCanBatch(String taskIds)  throws NoSuchMethodException{
        List<NodeGroupByFlowVersionInfo> all = new ArrayList<NodeGroupByFlowVersionInfo>();
        List<NodeGroupInfo> nodeGroupInfoList = this.findNexNodesGroupWithUserSetCanBatch(taskIds);
        Map<String,NodeGroupByFlowVersionInfo> nodeGroupByFlowVersionInfoMap = new HashMap<String,NodeGroupByFlowVersionInfo>();
        if(nodeGroupInfoList!=null && !nodeGroupInfoList.isEmpty()){
            for(NodeGroupInfo nodeGroupInfo:nodeGroupInfoList){
               String flowDefVersionId = nodeGroupInfo.getFlowDefVersionId();
                NodeGroupByFlowVersionInfo nodeGroupByFlowVersionInfo = nodeGroupByFlowVersionInfoMap.get(flowDefVersionId);
                if(nodeGroupByFlowVersionInfo==null){
                    nodeGroupByFlowVersionInfo = new NodeGroupByFlowVersionInfo();
                    nodeGroupByFlowVersionInfo.setId(flowDefVersionId);
                    nodeGroupByFlowVersionInfo.setName(nodeGroupInfo.getFlowDefVersionName());
                    nodeGroupByFlowVersionInfo.getNodeGroupInfos().add(nodeGroupInfo);
                    nodeGroupByFlowVersionInfoMap.put(flowDefVersionId,nodeGroupByFlowVersionInfo);
                }else {
                    nodeGroupByFlowVersionInfo.getNodeGroupInfos().add(nodeGroupInfo);
                }
            }
            all.addAll(nodeGroupByFlowVersionInfoMap.values());
        }
        return all;
    }
    @Transactional(propagation = Propagation.REQUIRED)
    public OperateResultWithData<Integer>  completeBatch(FlowTaskBatchCompleteVO flowTaskBatchCompleteVO){
       List<String> taskIdList =  flowTaskBatchCompleteVO.getTaskIdList();
       int total = 0;
        OperateResultWithData result =  null;
       if(taskIdList!=null && !taskIdList.isEmpty()){
           for (String taskId:taskIdList){
               FlowTaskCompleteVO flowTaskCompleteVO = new FlowTaskCompleteVO();
               BeanUtils.copyProperties(flowTaskBatchCompleteVO,flowTaskCompleteVO);
               flowTaskCompleteVO.setTaskId(taskId);
               try {
                   this.complete(flowTaskCompleteVO);
                   total++;
               }catch (Exception e){
                   logger.error(e.getMessage());
               }
           }
           if(total>0){
               result =  OperateResultWithData.operationSuccess();
               result.setData(total);
           }else {
               result =  OperateResultWithData.operationFailure("10034");
           }
       }else {
           result =  OperateResultWithData.operationFailure("10034");
       }
       return result;
    }
    public OperateResult taskTurnToDo(String taskId,String userId){
        OperateResult result =  null;
        FlowTask flowTask = flowTaskDao.findOne(taskId);
        if(flowTask!=null){
            HistoricTaskInstance historicTaskInstance = historyService.createHistoricTaskInstanceQuery().taskId(flowTask.getActTaskId()).singleResult(); // 创建历史任务实例查询
            Map<String,Object> params = new HashMap();
            params.put("employeeIds",java.util.Arrays.asList(userId));
//            String url = Constants.BASIC_SERVICE_URL+ Constants.BASIC_EMPLOYEE_GETEXECUTORSBYEMPLOYEEIDS_URL;
            String url = Constants.getBasicEmployeeGetexecutorsbyemployeeidsUrl();
            List<Executor> employees=ApiClient.getEntityViaProxy(url,new GenericType<List<Executor>>() {},params);
            if(employees!=null && !employees.isEmpty()){
                Executor executor = employees.get(0);
                FlowTask newFlowTask = new FlowTask();
                BeanUtils.copyProperties(flowTask,newFlowTask);
                FlowHistory flowHistory = flowTaskTool.initFlowHistory(flowTask,historicTaskInstance,true,null);//转办后先允许撤回
                flowHistory.setDepict("【被转办给：“"+executor.getName()+"”】");

                newFlowTask.setId(null);
                newFlowTask.setExecutorId(executor.getId());
                newFlowTask.setExecutorAccount(executor.getCode());
                newFlowTask.setExecutorName(executor.getName());
                newFlowTask.setOwnerId(executor.getId());
                newFlowTask.setOwnerName(executor.getName());
                newFlowTask.setOwnerAccount(executor.getCode());
                newFlowTask.setPreId(flowHistory.getId());
                newFlowTask.setTrustState(0);
                newFlowTask.setDepict("【由：“"+flowTask.getExecutorName()+"”转办】" + (StringUtils.isNotEmpty(flowTask.getDepict())?flowTask.getDepict():""));
                taskService.setAssignee(flowTask.getActTaskId(), executor.getId());
                flowHistoryDao.save(flowHistory);
                flowTaskDao.delete(flowTask);
                flowTaskDao.save(newFlowTask);
                result = OperateResult.operationSuccess();
            }else{
                result = OperateResult.operationFailure("10038");//执行人查询结果为空
            }
        }else {
            result = OperateResult.operationFailure("10033");//任务不存在，可能已经被处理
        }
        return result;
    }

    public OperateResult taskTrustToDo(String taskId,String userId) throws Exception{
        OperateResult result =  null;
        FlowTask flowTask = flowTaskDao.findOne(taskId);
        if(flowTask!=null){
            HistoricTaskInstance historicTaskInstance = historyService.createHistoricTaskInstanceQuery().taskId(flowTask.getActTaskId()).singleResult(); // 创建历史任务实例查询
            Map<String,Object> params = new HashMap();
            params.put("employeeIds",java.util.Arrays.asList(userId));
//            String url = Constants.BASIC_SERVICE_URL+ Constants.BASIC_EMPLOYEE_GETEXECUTORSBYEMPLOYEEIDS_URL;
            String url = Constants.getBasicEmployeeGetexecutorsbyemployeeidsUrl();
            List<Executor> employees=ApiClient.getEntityViaProxy(url,new GenericType<List<Executor>>() {},params);
            if(employees!=null && !employees.isEmpty()){
                Executor executor = employees.get(0);
                FlowTask newFlowTask = new FlowTask();
                BeanUtils.copyProperties(flowTask,newFlowTask);
                FlowHistory flowHistory = flowTaskTool.initFlowHistory(flowTask,historicTaskInstance,true,null); //委托后先允许撤回
                flowHistory.setDepict("【被委托给："+executor.getName()+"】");

                newFlowTask.setId(null);
                newFlowTask.setExecutorId(executor.getId());
                newFlowTask.setExecutorAccount(executor.getCode());
                newFlowTask.setExecutorName(executor.getName());
                newFlowTask.setPreId(flowHistory.getId());
                newFlowTask.setDepict("【由：“"+flowTask.getExecutorName()+"”委托】" + flowTask.getDepict());
                flowHistoryDao.save(flowHistory);
                flowTask.setTrustState(1);
                newFlowTask.setTrustState(2);
                newFlowTask.setTrustOwnerTaskId(flowTask.getId());
                flowTaskDao.save(flowTask);
                flowTaskDao.save(newFlowTask);
                result = OperateResult.operationSuccess();
            }else{
                result = OperateResult.operationFailure("10038");//执行人查询结果为空
            }
        }else {
            result = OperateResult.operationFailure("10033");//任务不存在，可能已经被处理
        }
        return result;
    }
}
