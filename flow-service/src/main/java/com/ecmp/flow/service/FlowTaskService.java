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
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
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

    @Autowired
    private FlowVariableDao flowVariableDao;

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
    public OperateResultWithData<FlowStatus> complete(FlowTaskCompleteVO flowTaskCompleteVO) {
        String taskId = flowTaskCompleteVO.getTaskId();
        Map<String, Object> variables = flowTaskCompleteVO.getVariables();
        Map<String,String> manualSelectedNodes = flowTaskCompleteVO.getManualSelectedNode();
        OperateResultWithData<FlowStatus> result = null;
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
    private OperateResultWithData<FlowStatus> complete(String id, String opinion, Map<String, Object> variables) {
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
        } else {
            flowTask.setTaskStatus(TaskStatus.COMPLETED.toString());
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
            flowHistory.setOwnerName(flowTask.getOwnerName());
            flowHistory.setExecutorAccount(flowTask.getExecutorAccount());
            flowHistory.setExecutorId(flowTask.getExecutorId());
            flowHistory.setExecutorName(flowTask.getExecutorName());
            flowHistory.setCandidateAccount(flowTask.getCandidateAccount());

            flowHistory.setActDurationInMillis(historicTaskInstance.getDurationInMillis());
            flowHistory.setActWorkTimeInMillis(historicTaskInstance.getWorkTimeInMillis());
            flowHistory.setActStartTime(historicTaskInstance.getStartTime());
            flowHistory.setActEndTime(historicTaskInstance.getEndTime());
            flowHistory.setActHistoryId(historicTaskInstance.getId());
            flowHistory.setActTaskDefKey(historicTaskInstance.getTaskDefinitionKey());
            flowHistory.setPreId(flowTask.getPreId());
            flowHistory.setDepict(flowTask.getDepict());
            flowHistory.setTaskStatus(flowTask.getTaskStatus());

            if(flowHistory.getActEndTime() == null){
                flowHistory.setActEndTime(new Date());
            }
            if(flowHistory.getActDurationInMillis() == null){
                Long actDurationInMillis = flowHistory.getActEndTime().getTime()-flowHistory.getActStartTime().getTime();
                flowHistory.setActDurationInMillis(actDurationInMillis);
            }
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
    private void completeActiviti(String taskId, Map<String, Object> variables) {
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
    public OperateResult taskReject(String id, String opinion, Map<String, Object> variables) {
        OperateResult result = OperateResult.operationSuccess("10006");
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
    private OperateResult activitiReject(FlowTask currentTask, FlowHistory preFlowTask) {
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
                    params.put("employeeIds",java.util.Arrays.asList(java.util.Arrays.asList(startUserId)));
                    String url = Constants.BASIC_SERVICE_URL+ Constants.BASIC_EMPLOYEE_GETEXECUTORSBYEMPLOYEEIDS_URL;
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
                    params.put("employeeIds",java.util.Arrays.asList(java.util.Arrays.asList(startUserId)));
                    String url = Constants.BASIC_SERVICE_URL+ Constants.BASIC_EMPLOYEE_GETEXECUTORSBYEMPLOYEEIDS_URL;
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
                    net.sf.json.JSONObject executor = currentNode.getJSONObject("nodeConfig").getJSONObject("executor");

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
                            String url = Constants.BASIC_SERVICE_URL+ Constants.BASIC_EMPLOYEE_GETEXECUTORSBYEMPLOYEEIDS_URL;
                            employees=ApiClient.getEntityViaProxy(url,new GenericType<List<Executor>>() {},params);

                        } else {
                            String selfDefId = (String)executor.get("selfDefId");
                            if (StringUtils.isNotEmpty(ids)||StringUtils.isNotEmpty(selfDefId)) {
                                if ("SelfDefinition".equalsIgnoreCase(userType)) {//通过业务ID获取自定义用户
                                    FlowExecutorConfig flowExecutorConfig = flowExecutorConfigDao.findOne(selfDefId);
                                    String path = flowExecutorConfig.getUrl();
                                    String appModuleId =  flowExecutorConfig.getBusinessModel().getAppModuleId();
                                    com.ecmp.flow.api.IAppModuleService proxy = ApiClient.createProxy(com.ecmp.flow.api.IAppModuleService.class);
                                    com.ecmp.flow.entity.AppModule appModule = proxy.findOne(appModuleId);
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
        List<TodoBusinessSummaryVO> voList = null;
        String userID = ContextUtil.getUserId();
        List groupResultList = flowTaskDao.findByExecutorIdGroup(userID);
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
                todoBusinessSummaryVO.setBusinessModelName(map.getKey().getName());
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
        org.springframework.beans.BeanUtils.copyProperties(resultVO,pageResult);
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
    public OperateResultWithData<FlowStatus> completeBatchApproval(List<FlowTaskCompleteVO> flowTaskCompleteVOList){
        for(FlowTaskCompleteVO flowTaskCompleteVO:flowTaskCompleteVOList){
            OperateResultWithData<FlowStatus>   tempResult = this.complete(flowTaskCompleteVO);
            if(!tempResult.successful()){
                throw new RuntimeException("batch approval is failure! ");
            }
        }
       return OperateResultWithData.operationSuccess("10017");
    }

}
