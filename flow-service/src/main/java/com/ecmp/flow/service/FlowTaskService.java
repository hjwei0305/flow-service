package com.ecmp.flow.service;

import com.ecmp.basic.api.IEmployeeService;
import com.ecmp.basic.api.IPositionService;
import com.ecmp.basic.entity.vo.Executor;
import com.ecmp.config.util.ApiClient;
import com.ecmp.context.ContextUtil;
import com.ecmp.core.dao.BaseEntityDao;
import com.ecmp.core.search.PageResult;
import com.ecmp.core.search.Search;
import com.ecmp.core.service.BaseEntityService;
import com.ecmp.flow.activiti.ext.PvmNodeInfo;
import com.ecmp.flow.api.IFlowTaskService;
import com.ecmp.flow.constant.FlowStatus;
import com.ecmp.flow.dao.*;
import com.ecmp.flow.entity.*;
import com.ecmp.flow.util.ConditionUtil;
import com.ecmp.flow.util.ExpressionUtil;
import com.ecmp.flow.util.TaskStatus;
import com.ecmp.flow.vo.ApprovalHeaderVO;
import com.ecmp.flow.vo.FlowTaskCompleteVO;
import com.ecmp.flow.vo.NodeInfo;
import com.ecmp.flow.vo.TodoBusinessSummaryVO;
import com.ecmp.flow.vo.bpmn.Definition;
import com.ecmp.flow.vo.bpmn.UserTask;
import com.ecmp.vo.OperateResult;
import com.ecmp.vo.OperateResultWithData;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.activiti.engine.*;
import org.activiti.engine.history.*;
import org.activiti.engine.impl.Condition;
import org.activiti.engine.impl.RepositoryServiceImpl;
import org.activiti.engine.impl.el.UelExpressionCondition;
import org.activiti.engine.impl.persistence.entity.*;
import org.activiti.engine.impl.pvm.PvmActivity;
import org.activiti.engine.impl.pvm.PvmTransition;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.impl.pvm.process.ProcessDefinitionImpl;
import org.activiti.engine.impl.pvm.process.ProcessElementImpl;
import org.activiti.engine.impl.pvm.process.TransitionImpl;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.IdentityLink;
import org.activiti.engine.task.Task;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import javax.ws.rs.core.GenericType;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

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
    private HistoryService historyService;

    @Autowired
    private ProcessEngine processEngine;

    @Autowired
    private FlowInstanceService flowInstanceService;

    private final Logger logger = LoggerFactory.getLogger(FlowDefinationService.class);

    private  Lock lock = new ReentrantLock();

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
        List<String> manualSelectedNodeIds = flowTaskCompleteVO.getManualSelectedNodeIds();
        OperateResultWithData<FlowStatus> result = null;
        if (manualSelectedNodeIds == null || manualSelectedNodeIds.isEmpty()) {//非人工选择任务的情况
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
                    for (String nodeId : manualSelectedNodeIds) {
                        ActivityImpl destinationActivity = ((ProcessDefinitionImpl) definition).findActivity(nodeId);
                        if(destinationActivity!=null && checkNextHas(pvmTransition.getDestination(),destinationActivity)){
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


                Map<PvmTransition,String> oriPvmTransitionMap = new LinkedHashMap<PvmTransition,String>();
                List<PvmTransition> pvmTransitionList = currActivity.getOutgoingTransitions();
                for (PvmTransition pvmTransition : pvmTransitionList) {
                    UelExpressionCondition uel= (UelExpressionCondition)pvmTransition.getProperty("condition");
                    String uelText = (String) pvmTransition.getProperty("conditionText");
                    for (String nodeId : manualSelectedNodeIds) {
                        ActivityImpl destinationActivity = ((ProcessDefinitionImpl) definition).findActivity(nodeId);
                        if(destinationActivity!=null && checkNextHas(pvmTransition.getDestination(),destinationActivity)){
                            oriPvmTransitionMap.put(pvmTransition,uelText);
                            String proName = destinationActivity.getId()+"_approveResult";
                            uelText = "${"+proName+" == true}";
                            uel = new UelExpressionCondition(uelText);
                            ((ProcessElementImpl)pvmTransition).setProperty("condition",uel);
                            ((ProcessElementImpl)pvmTransition).setProperty("conditionText",uelText);
                            variables.put(proName,true);
                            break;
                        }
                    }
                }
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
//                List<PvmTransition> oriPvmTransitionList = new ArrayList<PvmTransition>();
//                List<PvmTransition> pvmTransitionList = currActivity.getOutgoingTransitions();
//                for (PvmTransition pvmTransition : pvmTransitionList) {
//                    oriPvmTransitionList.add(pvmTransition);
//                }
//                pvmTransitionList.clear();
//
//                // 定位到人工选择的节点目标
//                List<TransitionImpl> newTransitions = new ArrayList<TransitionImpl>();
//                for (String nodeId : manualSelectedNodeIds) {
//                    // 建立新方向
//                    TransitionImpl newTransition = currActivity.createOutgoingTransition();
//                    ActivityImpl destinationActivity = ((ProcessDefinitionImpl) definition).findActivity(nodeId);
//                    newTransition.setDestination(destinationActivity);
//                    newTransitions.add(newTransition);
//                }
//
//                //执行任务
//                result = this.complete(taskId, flowTaskCompleteVO.getOpinion(), variables);
//
//                // 恢复方向
//                int index =0;
//                for (String nodeId : manualSelectedNodeIds) {
//                    ActivityImpl destinationActivity = ((ProcessDefinitionImpl) definition).findActivity(nodeId);
//                    destinationActivity.getIncomingTransitions().remove(newTransitions.get(index++));
//                }
//                List<PvmTransition> pvmTList = currActivity.getOutgoingTransitions();
//                pvmTList.clear();
//                for (PvmTransition pvmTransition : oriPvmTransitionList) {
//                    pvmTransitionList.add(pvmTransition);
//                }
            }
        }
        return result;
    }
    private boolean checkNextHas( PvmActivity curActivity, PvmActivity destinationActivity ){
        boolean result = false;
        if(curActivity!=null){
            if(curActivity.getId().equals(destinationActivity.getId())){
                return true;
            }else if(ifGageway(curActivity)||"ManualTask".equalsIgnoreCase(curActivity.getProperty("type") + "")){
                List<PvmTransition> pvmTransitionList =  curActivity.getOutgoingTransitions();
                if(pvmTransitionList != null && !pvmTransitionList.isEmpty()){
                   for(PvmTransition pv:pvmTransitionList){
                       result = this.checkNextHas(pv.getDestination(),destinationActivity);
                       if(result){
                           return result;
                       }
                   }
                }
            }
        }
        return  result;
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
        //  flowTaskDao.save(flowTask);

        String actTaskId = flowTask.getActTaskId();

        //获取当前业务实体表单的条件表达式信息，（目前是任务执行时就注入，后期根据条件来优化)
        String businessId = flowInstance.getBusinessId();
        BusinessModel businessModel = flowTask.getFlowInstance().getFlowDefVersion().getFlowDefination().getFlowType().getBusinessModel();
        String businessModelId = businessModel.getId();
        String appModuleId = businessModel.getAppModuleId();
        com.ecmp.basic.api.IAppModuleService proxy = ApiClient.createProxy(com.ecmp.basic.api.IAppModuleService.class);
        com.ecmp.basic.entity.AppModule appModule = proxy.findOne(appModuleId);
        String clientApiBaseUrl = ContextUtil.getAppModule(appModule.getCode()).getApiBaseAddress();
        Map<String, Object> v = ExpressionUtil.getConditonPojoValueMap(clientApiBaseUrl, businessModelId, businessId);
        if (v != null && !v.isEmpty()) {
            if (variables == null) {
                variables = new HashMap<String, Object>();
            }
            variables.putAll(v);
        }
        flowTask.getFlowInstance().setBusinessModelRemark(v.get("workCaption") + "");
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
        }else if ("ParallelTask".equalsIgnoreCase(nodeType)||"SerialTask".equalsIgnoreCase(nodeType)){
            String executionId = currTask.getExecutionId();
            Map<String, VariableInstance>      processVariables= runtimeService.getVariableInstances(executionId);
            //完成会签的次数
            Integer completeCounter=(Integer)processVariables.get("nrOfCompletedInstances").getValue();
            //总循环次数
            Integer instanceOfNumbers=(Integer)processVariables.get("nrOfInstances").getValue();
//            runtimeService.setVariable(executionId,"nrOfCompletedInstances", completeCounter+1);
            if(completeCounter+1==instanceOfNumbers){//最后一个任务
                counterSignLastTask=true;}
            this.completeActiviti(actTaskId, variables);
        }
        else {
            this.completeActiviti(actTaskId, variables);
        }
//        this.completeActiviti(actTaskId, variables);
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
//            flowHistory.setBusinessModelRemark();
            flowHistory.setCanCancel(canCancel);
            flowHistory.setFlowName(flowTask.getFlowName());
            flowHistory.setDepict(flowTask.getDepict());
            flowHistory.setActClaimTime(flowTask.getActClaimTime());
            flowHistory.setFlowTaskName(flowTask.getTaskName());
//            flowHistory.setFlowInstanceId(flowTask.getFlowInstanceId());
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
            FlowInstance flowInstanceP = flowInstance.getParent();
            String actTaskDefKey = flowTask.getActTaskDefKey();
            String actProcessDefinitionId = flowTask.getFlowInstance().getFlowDefVersion().getActDefId();
            ProcessDefinitionEntity definition = null;
            PvmActivity currentNode = null;
            if(flowInstance.isEnded() && (flowInstanceP != null && !flowInstanceP.isEnded())){//子流程结束，主流程未结束
                actProcessDefinitionId = flowInstanceP.getFlowDefVersion().getActDefId();
                definition = (ProcessDefinitionEntity) ((RepositoryServiceImpl) repositoryService)
                        .getDeployedProcessDefinition(actProcessDefinitionId);
                String superExecutionId = null;
                superExecutionId = (String) runtimeService.getVariable(flowInstanceP.getActInstanceId(),flowInstance.getActInstanceId()+"_superExecutionId");
                HistoricActivityInstance historicActivityInstance = null;
                HistoricActivityInstanceQuery his = historyService.createHistoricActivityInstanceQuery()
                        .executionId(superExecutionId).activityType("callActivity");
                if (his != null) {
                    historicActivityInstance = his.singleResult();
                    HistoricActivityInstanceEntity he = (HistoricActivityInstanceEntity) historicActivityInstance;
                    actTaskDefKey = he.getActivityId();
                    currentNode = this.getActivitNode(definition,actTaskDefKey);
                    // 取得流程实例
                    instance = runtimeService
                            .createProcessInstanceQuery()
                            .processInstanceId(flowInstanceP.getActInstanceId())
                            .singleResult();
                    callInitTaskBack(currentNode, instance, flowHistory,counterSignLastTask);
                }
            }else{
                definition = (ProcessDefinitionEntity) ((RepositoryServiceImpl) repositoryService)
                        .getDeployedProcessDefinition(actProcessDefinitionId);
                currentNode = this.getActivitNode(definition,actTaskDefKey);
                if (instance != null && currentNode != null && (!"endEvent".equalsIgnoreCase(currentNode.getProperty("type") + ""))) {
                    callInitTaskBack(currentNode, instance, flowHistory,counterSignLastTask);
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


    private void callInitTaskBack(PvmActivity currentNode, ProcessInstance instance, FlowHistory flowHistory,boolean counterSignLastTask) {
        if(!counterSignLastTask && ifMultiInstance(currentNode)){
           String sequential= currentNode.getProperty("multiInstance")+"";
           if("sequential".equalsIgnoreCase(sequential)){//会签当中串行任务,非最后一个任务
               String key = currentNode.getProperty("key") != null ? currentNode.getProperty("key").toString() : null;
               if (key == null) {
                   key = currentNode.getId();
               }
               initTask(instance, key, flowHistory);
               return;
           }
        }
        List<PvmTransition> nextNodes = currentNode.getOutgoingTransitions();
        if (nextNodes != null && nextNodes.size() > 0) {
            for (PvmTransition node : nextNodes) {
                PvmActivity nextActivity = node.getDestination();
                if (ifGageway(nextActivity)||"ManualTask".equalsIgnoreCase(nextActivity.getProperty("type") + "")) {
                    callInitTaskBack(nextActivity, instance, flowHistory,counterSignLastTask);
                    continue;
                }
                String key = nextActivity.getProperty("key") != null ? nextActivity.getProperty("key").toString() : null;
                if (key == null) {
                    key = nextActivity.getId();
                }
                if("serviceTask".equalsIgnoreCase(nextActivity.getProperty("type") + "")){
//                    String executionId = currTask.getExecutionId();
                    Map<String, VariableInstance>  processVariables= runtimeService.getVariableInstances(instance.getId());
                    List<String> nextNodeIds = (List<String>)runtimeService.getVariable(instance.getId(),key+"_nextNodeIds");
                    if(nextNodeIds!=null && !nextNodeIds.isEmpty()){
                        for(String nextNodeId:nextNodeIds){
                            initTask(instance, nextNodeId, flowHistory);
                        }
                    }
                }else {
                    initTask(instance, key, flowHistory);
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
        result = this.taskRollBack(flowHistory, opinion);
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
     * 获取活动节点
     *
     * @param definition
     * @param taskDefinitionKey
     * @return
     */
    private PvmActivity getActivitNode(ProcessDefinitionEntity definition ,String taskDefinitionKey) {
        if (definition == null) {
            logger.error(ContextUtil.getMessage("10003"));
        }
        // 取得当前活动定义节点
        ActivityImpl currActivity = ((ProcessDefinitionImpl) definition)
                .findActivity(taskDefinitionKey);
        return currActivity;

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
        if (this.checkCanReject(currentActivity, preActivity, instance,
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
     * 回退任务
     *
     * @return
     */
    @Transactional(propagation = Propagation.REQUIRED)
    private OperateResult taskRollBack(FlowHistory flowHistory, String opinion) {
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
            initTask(instance, currTask.getTaskDefinitionKey(), flowHistory);
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
    private void callBackRunIdentityLinkEntity(String taskId) {
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
    private Boolean deleteOtherNode(PvmActivity currActivity, ProcessInstance instance,
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
    private Boolean checkNextNodeNotCompleted(PvmActivity currActivity, ProcessInstance instance,
                                              ProcessDefinitionEntity definition, HistoricTaskInstance destnetionTask) {
        List<PvmTransition> nextTransitionList = currActivity.getOutgoingTransitions();
        boolean result = true;

        for (PvmTransition nextTransition : nextTransitionList) {
            PvmActivity nextActivity = nextTransition.getDestination();
            Boolean ifGateWay = ifGageway(nextActivity);
            String type = nextActivity.getProperty("type")+"";
            if("ServiceTask".equalsIgnoreCase(type) || "ReceiveTask".equalsIgnoreCase(type)){//服务任务/接收任务不允许撤回
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
    private boolean ifGageway(PvmActivity pvmActivity) {
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
    private boolean ifExclusiveGateway(PvmActivity pvmActivity) {
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
    private boolean ifMultiInstance(PvmActivity pvmActivity) {
        Object nextActivtityType = pvmActivity.getProperty("multiInstance");
        Boolean result = false;
        if (nextActivtityType != null && !"".equals(nextActivtityType)) { //多实例任务
            result = true;
        }
        return result;
    }

    private Boolean checkCanReject(PvmActivity currActivity, PvmActivity preActivity, ProcessInstance instance,
                                   ProcessDefinitionEntity definition) {
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

    /**
     * 将新的流程任务初始化
     *
     * @param instance
     * @param actTaskDefKey
     */
    private void initTask(ProcessInstance instance, String actTaskDefKey, FlowHistory preTask) {
        FlowInstance flowInstance = flowInstanceDao.findByActInstanceId(instance.getId());
        List<Task> tasks = new ArrayList<Task>();
        // 根据当流程实例查询任务
        List<Task> taskList = taskService.createTaskQuery().processInstanceId(instance.getId()).taskDefinitionKey(actTaskDefKey).active().list();
        if (taskList != null && taskList.size() > 0) {
            String flowName = null;
            String flowDefJson = flowInstance.getFlowDefVersion().getDefJson();
            JSONObject defObj = JSONObject.fromObject(flowDefJson);
            Definition definition = (Definition) JSONObject.toBean(defObj, Definition.class);
            net.sf.json.JSONObject currentNode = definition.getProcess().getNodes().getJSONObject(actTaskDefKey);
            net.sf.json.JSONObject normalInfo = currentNode.getJSONObject("nodeConfig").getJSONObject("normal");
            Integer executeTime = null;
            Boolean canReject = null;
            Boolean canSuspension = null;
            if(normalInfo!=null && !normalInfo.isEmpty() ){
                executeTime = normalInfo.get("executeTime")!=null?(Integer)normalInfo.get("executeTime"):null;
                canReject = normalInfo.get("allowReject")!=null?(Boolean)normalInfo.get("allowReject"):null;
                canSuspension =normalInfo.get("allowTerminate")!=null?(Boolean) normalInfo.get("allowTerminate"):null;
            }

            flowName = definition.getProcess().getName();
            for (Task task : taskList) {
                List<IdentityLink> identityLinks = taskService.getIdentityLinksForTask(task.getId());
                if(identityLinks==null || identityLinks.isEmpty()){//多实例任务为null
                    /** 获取流程变量 **/
                    String executionId = task.getExecutionId();
                    String variableName = "" + actTaskDefKey + "_CounterSign";
                    String  userId = runtimeService.getVariable(executionId,variableName)+"";//使用执行对象Id和流程变量名称，获取值
                    if(StringUtils.isNotEmpty(userId)){
                        IEmployeeService iEmployeeService = ApiClient.createProxy(IEmployeeService.class);
                        List<Executor> employees = iEmployeeService.getExecutorsByEmployeeIds(java.util.Arrays.asList(userId));
                        if(employees!=null && !employees.isEmpty()){
                            Executor executor = employees.get(0);
                            FlowTask flowTask = new FlowTask();
                            flowTask.setCanReject(canReject);
                            flowTask.setCanSuspension(canSuspension);
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
                            flowTaskDao.save(flowTask);
                        }
                    }
                }else{
                    for (IdentityLink identityLink : identityLinks) {
                        IEmployeeService iEmployeeService = ApiClient.createProxy(IEmployeeService.class);
                        List<Executor> employees = iEmployeeService.getExecutorsByEmployeeIds(java.util.Arrays.asList(identityLink.getUserId()));
                        if (employees != null && !employees.isEmpty()) {
                            Executor executor = employees.get(0);
                            FlowTask flowTask = new FlowTask();
                            flowTask.setCanReject(canReject);
                            flowTask.setCanSuspension(canSuspension);
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
//                                flowTask.setExecutorAccount(identityLink.getUserId());
                            flowTask.setActType(identityLink.getType());
                            flowTask.setDepict(task.getDescription());
                            flowTask.setTaskStatus(TaskStatus.INIT.toString());
                            if (preTask != null) {
                                if (TaskStatus.REJECT.toString().equalsIgnoreCase(preTask.getTaskStatus())) {
                                    flowTask.setTaskStatus(TaskStatus.REJECT.toString());
                                } else {
                                    flowTask.setPreId(preTask.getId());
                                }
                                flowTask.setPreId(preTask.getId());
                                flowTask.setDepict(preTask.getDepict());
                            }
                            flowTask.setFlowInstance(flowInstance);

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
    private void saveVariables(Map<String, Object> variables, FlowTask flowTask) {
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
    private boolean checkHasConditon(PvmActivity currActivity) {
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
     * 获取出口节点信息（带初始化流程设计器配置用户）
     *
     * @param id
     * @param businessId
     * @return
     * @throws NoSuchMethodException
     */
    public List<NodeInfo> findNexNodesWithUserSet(String id, String businessId,String approved) throws NoSuchMethodException {
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
        List<NodeInfo> nodeInfoList = this.findNextNodesWithCondition( flowTask,approved, includeNodeIds);

        if (nodeInfoList != null && !nodeInfoList.isEmpty()) {
            String flowDefJson = flowTask.getFlowInstance().getFlowDefVersion().getDefJson();
            JSONObject defObj = JSONObject.fromObject(flowDefJson);
            Definition definition = (Definition) JSONObject.toBean(defObj, Definition.class);

            String flowTaskDefJson = flowTask.getTaskJsonDef();
            JSONObject flowTaskDefObj = JSONObject.fromObject(flowTaskDefJson);
            String currentNodeType = flowTaskDefObj.get("nodeType") + "";

            for (NodeInfo nodeInfo : nodeInfoList) {
                nodeInfo.setCurrentTaskType(currentNodeType);
                if ("CounterSignNotEnd".equalsIgnoreCase(nodeInfo.getType())) {
                    continue;
                }else if("serviceTask".equalsIgnoreCase(nodeInfo.getType())){
                    nodeInfo.setUserVarName(nodeInfo.getId() + "_ServiceTask");
                    nodeInfo.setUiType("radiobox");
                    nodeInfo.setFlowTaskType("serviceTask");
                    IEmployeeService iEmployeeService = ApiClient.createProxy(IEmployeeService.class);
                    String  startUserId =  ContextUtil.getSessionUser().getUserId();
                    List<Executor> employees = iEmployeeService.getExecutorsByEmployeeIds(java.util.Arrays.asList(startUserId));
                    if (employees != null && !employees.isEmpty()) {//服务任务默认选择流程启动人
                        Set<Executor> employeeSet = new HashSet<Executor>();
                        employeeSet.addAll(employees);
                        nodeInfo.setExecutorSet(employeeSet);
                    }
                }else if("receiveTask".equalsIgnoreCase(nodeInfo.getType())){
                    nodeInfo.setUserVarName(nodeInfo.getId() + "_ReceiveTask");
                    nodeInfo.setUiType("radiobox");
                    nodeInfo.setFlowTaskType("receiveTask");
                    IEmployeeService iEmployeeService = ApiClient.createProxy(IEmployeeService.class);
                    String  startUserId =  ContextUtil.getSessionUser().getUserId();
                    List<Executor> employees = iEmployeeService.getExecutorsByEmployeeIds(java.util.Arrays.asList(startUserId));
                    if (employees != null && !employees.isEmpty()) {//服务任务默认选择流程启动人
                        Set<Executor> employeeSet = new HashSet<Executor>();
                        employeeSet.addAll(employees);
                        nodeInfo.setExecutorSet(employeeSet);
                    }
                }else {
                    net.sf.json.JSONObject currentNode = definition.getProcess().getNodes().getJSONObject(nodeInfo.getId());
                    net.sf.json.JSONObject executor = currentNode.getJSONObject("nodeConfig").getJSONObject("executor");

                    UserTask userTaskTemp = (UserTask) JSONObject.toBean(currentNode, UserTask.class);
                    if ("EndEvent".equalsIgnoreCase(userTaskTemp.getType())) {
                        nodeInfo.setType("EndEvent");
                        continue;
                    }

                    if ("Normal".equalsIgnoreCase(userTaskTemp.getNodeType())) {
                        nodeInfo.setUserVarName(userTaskTemp.getId() + "_Normal");
                    } else if ("SingleSign".equalsIgnoreCase(userTaskTemp.getNodeType())) {
                        nodeInfo.setUserVarName(userTaskTemp.getId() + "_SingleSign");
                    } else if ("Approve".equalsIgnoreCase(userTaskTemp.getNodeType())) {
                        nodeInfo.setUserVarName(userTaskTemp.getId() + "_Approve");
                    } else if ("CounterSign".equalsIgnoreCase(userTaskTemp.getNodeType())||"ParallelTask".equalsIgnoreCase(userTaskTemp.getNodeType())||"SerialTask".equalsIgnoreCase(userTaskTemp.getNodeType())) {
                        nodeInfo.setUserVarName(userTaskTemp.getId() + "_List_CounterSign");
                    }

                    if (executor != null && !executor.isEmpty()) {
                        String userType = (String) executor.get("userType");
                        String ids = (String) executor.get("ids");
                        Set<Executor> employeeSet = new HashSet<Executor>();
                        List<Executor> employees = null;
                        nodeInfo.setUiUserType(userType);
                        if ("StartUser".equalsIgnoreCase(userType)) {//获取流程实例启动者
                            ProcessInstance instance = runtimeService.createProcessInstanceQuery()
                                    .processInstanceId(flowTask.getFlowInstance().getActInstanceId()).singleResult();
                            HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery().processInstanceId(flowTask.getFlowInstance().getActInstanceId()).singleResult();
                            String startUserId = historicProcessInstance.getStartUserId();
                            IEmployeeService iEmployeeService = ApiClient.createProxy(IEmployeeService.class);
                            employees = iEmployeeService.getExecutorsByEmployeeIds(java.util.Arrays.asList(startUserId));
                        } else {
                            String selfDefId = (String)executor.get("selfDefId");
                            if (StringUtils.isNotEmpty(ids)||StringUtils.isNotEmpty(selfDefId)) {
                                if ("SelfDefinition".equalsIgnoreCase(userType)) {//通过业务ID获取自定义用户
                                    FlowExecutorConfig flowExecutorConfig = flowExecutorConfigDao.findOne(selfDefId);
                                    String path = flowExecutorConfig.getUrl();
                                    String appModuleId =  flowExecutorConfig.getBusinessModel().getAppModuleId();
                                    com.ecmp.basic.api.IAppModuleService proxy = ApiClient.createProxy(com.ecmp.basic.api.IAppModuleService.class);
                                    com.ecmp.basic.entity.AppModule appModule = proxy.findOne(appModuleId);
                                    String clientApiBaseUrl = ContextUtil.getAppModule(appModule.getCode()).getApiBaseAddress();
                                    String appModuleCode = appModule.getCode();
                                    Map<String, String>  params = new HashMap<String,String>();;
                                    String param = flowExecutorConfig.getParam();
                                    String businessId = flowTask.getFlowInstance().getBusinessId();
                                    params.put("businessId",businessId);
                                    params.put("paramJson",param);
                                    employees =  ApiClient.postViaProxyReturnResult(appModuleCode,  path,new GenericType<List<Executor>>() {}, params);
                                }else{
                                    String[] idsShuZhu = ids.split(",");
                                    List<String> idList = java.util.Arrays.asList(idsShuZhu);
                                    //StartUser、Position、PositionType、SelfDefinition、AnyOne
                                    if ("Position".equalsIgnoreCase(userType)) {//调用岗位获取用户接口
                                        IPositionService iPositionService = ApiClient.createProxy(IPositionService.class);
                                        employees = iPositionService.getExecutorsByPositionIds(idList);
                                    } else if ("PositionType".equalsIgnoreCase(userType)) {//调用岗位类型获取用户接口
                                        IPositionService iPositionService = ApiClient.createProxy(IPositionService.class);
                                        employees = iPositionService.getExecutorsByPosCateIds(idList);
                                    }  else if ("AnyOne".equalsIgnoreCase(userType)) {//任意执行人不添加用户

                                    }
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

    /**
     * 选择下一步执行的节点信息
     *
     * @param flowTask
     * @return
     * @throws NoSuchMethodException
     */
    private List<NodeInfo> findNextNodesWithCondition(FlowTask flowTask,String approved, List<String> includeNodeIds) throws NoSuchMethodException {
        String actTaskId = flowTask.getActTaskId();
        String businessId = flowTask.getFlowInstance().getBusinessId();
        String actTaskDefKey = flowTask.getActTaskDefKey();

        String actProcessDefinitionId = flowTask.getFlowInstance().getFlowDefVersion().getActDefId();
        ProcessDefinitionEntity definition = (ProcessDefinitionEntity) ((RepositoryServiceImpl) repositoryService)
                .getDeployedProcessDefinition(actProcessDefinitionId);
        PvmActivity currActivity = this.getActivitNode(definition,actTaskDefKey);

        BusinessModel businessModel = flowTask.getFlowInstance().getFlowDefVersion().getFlowDefination().getFlowType().getBusinessModel();
        String businessModelId = businessModel.getId();
        String appModuleId = businessModel.getAppModuleId();
        com.ecmp.basic.api.IAppModuleService proxy = ApiClient.createProxy(com.ecmp.basic.api.IAppModuleService.class);
        com.ecmp.basic.entity.AppModule appModule = proxy.findOne(appModuleId);
        String clientApiBaseUrl = ContextUtil.getAppModule(appModule.getCode()).getApiBaseAddress();
        Map<String, Object> v = ExpressionUtil.getConditonPojoValueMap(clientApiBaseUrl, businessModelId, businessId);


      List<String> includeNodeIdsNew = initIncludeNodeIds(includeNodeIds,actTaskId,v);

//        if(ifMultiInstance(currActivity)){//如果是多实例任务
            String defJson = flowTask.getTaskJsonDef();
            JSONObject defObj = JSONObject.fromObject(defJson);
            String nodeType = defObj.get("nodeType") + "";
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
     * 选择下一步执行的节点信息
     *
     * @param id
     * @return
     * @throws NoSuchMethodException
     */
    public List<NodeInfo> findNextNodes(String id, String businessId, List<String> includeNodeIds) throws NoSuchMethodException {
        FlowTask flowTask = flowTaskDao.findOne(id);
        return this.selectNextAllNodes(flowTask, includeNodeIds);
    }

    /**
     * 检查是否下一节点存在网关
     *
     * @param flowTask
     * @return
     */
    private boolean checkGateway(FlowTask flowTask) {
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
    private boolean checkManualExclusiveGateway(FlowTask flowTask) {
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
            if ("ManualExclusiveGateway".equalsIgnoreCase(nextNode.getString("busType"))) {
                result = true;
                break;
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
    private boolean checkSystemExclusiveGateway(FlowTask flowTask) {
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
    private boolean checkManualExclusiveGateway(FlowTask flowTask, String manualExclusiveGatewayId) {
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
    private List<NodeInfo> selectNextAllNodesWithGateWay(FlowTask flowTask, PvmActivity currActivity, Map<String, Object> v, List<String> includeNodeIds) throws NoSuchMethodException, SecurityException {


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
                }
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

            } else if ("inclusiveGateway".equalsIgnoreCase(nextActivtityType)) { // 包容网关,checkbox,至少选择一个
                if (isSizeBigTwo) {
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
    private List<NodeInfo> selectNextAllNodes(FlowTask flowTask, List<String> includeNodeIds) {
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
            } else if ("exclusiveGateway".equalsIgnoreCase(nextActivtityType)) {// 排他网关，radiobox,有且只能选择一个
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
    private void initNextNodes(PvmActivity currActivity, Map<PvmActivity, List> nextNodes, int index,String nodeType, List lineInfo) {
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
//                    nextNodes.clear();

                    if(ifGateWay && index < 1){
                        nextNodes.put(currTempActivity, value);//把网关放入第一个节点
                        index++;
                        initNextNodes(currTempActivity, nextNodes, index,nodeType,null);
                    }else {
                        index++;
                        initNextNodes(currTempActivity, nextNodes, index,nodeType,value);
//                        if(index>1){
//
//                        }else {
//                            initNextNodes(currTempActivity, nextNodes, index++,nodeType,null);
//                        }
                    }
//                    if(!"Approve".equalsIgnoreCase(nodeType)){//非审批任务


//                    }

                    // break;
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
    private NodeInfo convertNodes(FlowTask flowTask, NodeInfo tempNodeInfo, PvmActivity tempActivity) {
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
        }else {
            throw new RuntimeException("流程任务节点配置有错误");
        }
        return tempNodeInfo;
    }




    private PvmNodeInfo pvmNodeInfoGateWayInit(Boolean ifGateWay,PvmNodeInfo pvmNodeInfo,PvmActivity nextTempActivity,Map<String,Object> v)
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
    private PvmNodeInfo checkFuHeConditon(PvmActivity currActivity, Map<String, Object> v)
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
    private  List<PvmActivity> initPvmActivityList(PvmNodeInfo pvmNodeInfo,List<PvmActivity> results){
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
     * 选择符合条件的节点
     *
     * @param v
     * @return
     * @throws SecurityException
     * @throws NoSuchMethodException
     */
    private List<NodeInfo> selectQualifiedNode(FlowTask flowTask, PvmActivity currActivity, Map<String, Object> v, List<String> includeNodeIds)
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
        if (nodeInfoList != null && !nodeInfoList.isEmpty()) {
            if(nodeInfoList.size()==1&&"EndEvent".equalsIgnoreCase(nodeInfoList.get(0).getType()) && parentFlowInstance != null){//针对子流程结束节点
                FlowTask flowTaskTemp = new FlowTask();
                org.springframework.beans.BeanUtils.copyProperties(flowTask,flowTaskTemp);
                flowTaskTemp.setFlowInstance(parentFlowInstance);
                // 取得流程实例
                ProcessInstance instanceSon = runtimeService
                        .createProcessInstanceQuery()
                        .processInstanceId(flowTask.getFlowInstance().getActInstanceId())
                        .singleResult();
               String superExecutionId = instanceSon.getSuperExecutionId();
                ProcessInstance instanceParent = runtimeService
                        .createProcessInstanceQuery()
                        .processInstanceId(parentFlowInstance.getActInstanceId())
                        .singleResult();
//                String actProcessDefinitionId = flowTask.getFlowInstance().getFlowDefVersion().getActDefId();
//                ProcessDefinitionEntity definition = (ProcessDefinitionEntity) ((RepositoryServiceImpl) repositoryService)
//                        .getDeployedProcessDefinition(actProcessDefinitionId);
//                ((ProcessDefinitionImpl) definition)
//                        .
//                PvmActivity currActivity = this.getActivitNode(definition,actTaskDefKey);

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
                }
            }
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
        return flowTaskDao.findByPageByBusinessModelId(businessModelId, userId, searchConfig);
    }
}
