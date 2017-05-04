package com.ecmp.flow.service;
import com.ecmp.context.ContextUtil;
import com.ecmp.core.service.BaseService;
import com.ecmp.flow.api.IFlowTaskService;
import com.ecmp.flow.dao.FlowInstanceDao;
import com.ecmp.flow.dao.FlowVariableDao;
import com.ecmp.flow.entity.FlowInstance;
import com.ecmp.flow.entity.FlowVariable;
import com.ecmp.flow.util.TaskStatus;
import com.ecmp.flow.dao.FlowHistoryDao;
import com.ecmp.flow.dao.FlowTaskDao;
import com.ecmp.flow.entity.FlowHistory;
import com.ecmp.flow.entity.FlowTask;
import com.ecmp.vo.OperateResult;
import org.activiti.engine.history.*;
import org.activiti.engine.impl.RepositoryServiceImpl;
import org.activiti.engine.impl.persistence.entity.*;
import org.activiti.engine.impl.pvm.PvmActivity;
import org.activiti.engine.impl.pvm.PvmTransition;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.impl.pvm.process.ProcessDefinitionImpl;
import org.activiti.engine.impl.pvm.process.TransitionImpl;
import org.activiti.engine.repository.DeploymentBuilder;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.IdentityLink;
import org.activiti.engine.task.Task;
import org.activiti.engine.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

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
public class FlowTaskService extends BaseService<FlowTask, String> implements IFlowTaskService {

    @Autowired
    private FlowTaskDao flowTaskDao;

    @Autowired
    private FlowInstanceDao flowInstanceDao;

    @Autowired
    private FlowHistoryDao flowHistoryDao;

    @Autowired
    private FlowVariableDao flowVariableDao;

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private IdentityService identityService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private HistoryService historyService;

    @Autowired
    private ProcessEngine processEngine;

    private final Logger logger = LoggerFactory.getLogger(FlowDefinationService.class);

    /**
     * 任务签收
     * @param id 任务id
     * @param userId 用户账号
     * @return
     */
    public OperateResult claim(String id, String userId){
        FlowTask flowTask = flowTaskDao.findOne(id);
        String actTaskId = flowTask.getActTaskId();
        this.claimActiviti(actTaskId, userId);
        flowTask.setActClaimTime(new Date());
        flowTask.setTaskStatus(TaskStatus.CLAIM.toString());
        flowTaskDao.save(flowTask);
        flowTaskDao.deleteNotClaimTask(actTaskId,id);
        OperateResult result =  OperateResult.OperationSuccess("core_00003");
        return  result;
    }

    /**
     * 完成任务
     * @param id 任务id
     * @param variables 参数
     * @return
     */
    public OperateResult complete(String id, Map<String, Object> variables){
        FlowTask flowTask = flowTaskDao.findOne(id);
        String actTaskId = flowTask.getActTaskId();
        this.completeActiviti( actTaskId,variables);
        this.saveVariables(variables,flowTask);
        Integer reject = null;
        if(variables!=null){
            Object rejectO = variables.get("reject");
            if(rejectO != null){
               try {
                   reject = Integer.parseInt(rejectO.toString());
               }catch (Exception e){
                   logger.error(e.getMessage());
               }
            }
        }
        HistoricTaskInstance historicTaskInstance=historyService.createHistoricTaskInstanceQuery().taskId(actTaskId).singleResult(); // 创建历史任务实例查询

        // 取得流程实例
        ProcessInstance instance = runtimeService
                .createProcessInstanceQuery()
                .processInstanceId(historicTaskInstance.getProcessInstanceId())
                .singleResult();
        if(historicTaskInstance!=null){
            FlowHistory flowHistory = new FlowHistory();
            flowHistory.setActType(flowTask.getActType());
            flowHistory.setFlowName(flowTask.getFlowName());
            flowHistory.setDepict(flowTask.getDepict());
            flowHistory.setActClaimTime(flowTask.getActClaimTime());
            flowHistory.setFlowTaskName(flowTask.getTaskName());
            flowHistory.setFlowDefId(flowTask.getFlowDefinitionId());
//            flowHistory.setFlowInstanceId(flowTask.getFlowInstanceId());
            flowHistory.setFlowInstance(flowTask.getFlowInstance());
            flowHistory.setOwnerAccount(flowTask.getOwnerAccount());
            flowHistory.setOwnerName(flowTask.getOwnerName());
            flowHistory.setExecutorAccount(flowTask.getExecutorAccount());
            flowHistory.setExecutorName(flowTask.getExecutorName());
            flowHistory.setCandidateAccount(flowTask.getCandidateAccount());

            flowHistory.setActDurationInMillis(historicTaskInstance.getDurationInMillis());
            flowHistory.setActWorkTimeInMillis(historicTaskInstance.getWorkTimeInMillis());
            flowHistory.setActStartTime(historicTaskInstance.getStartTime());
            flowHistory.setActEndTime(historicTaskInstance.getEndTime());
            flowHistory.setActHistoryId(historicTaskInstance.getId());
            flowHistory.setActTaskDefKey(historicTaskInstance.getTaskDefinitionKey());
            flowHistory.setTaskStatus(TaskStatus.COMPLETED.toString());
            if(reject!=null && reject == 1){
                flowHistory.setDepict("被驳回");
            }else {
                flowHistory.setDepict(null);
            }
            flowHistoryDao.save(flowHistory);
            flowTaskDao.delete(flowTask);

            org.springframework.orm.jpa.JpaTransactionManager  transactionManager =(org.springframework.orm.jpa.JpaTransactionManager) ContextUtil.getApplicationContext().getBean("transactionManager");
            DefaultTransactionDefinition def = new DefaultTransactionDefinition();
            def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW); // 事物隔离级别，开启新事务，这样会比较安全些。
            TransactionStatus status = transactionManager.getTransaction(def); // 获得事务状态
            try {
                //逻辑代码，可以写上你的逻辑处理代码
                flowTaskDao.deleteNotClaimTask(actTaskId,id);//删除其他候选用户的任务
                transactionManager.commit(status);
            } catch (Exception e) {
                e.printStackTrace();
                transactionManager.rollback(status);
                throw e;
            }


            //初始化新的任务
            PvmActivity  currentNode = this.getActivitNode(actTaskId);
            if(currentNode!=null){
                callInitTaskBack(currentNode,instance, flowHistory);
            }

        }
        OperateResult result =  OperateResult.OperationSuccess("core_00003");
        return result;
    }

    private void callInitTaskBack( PvmActivity  currentNode,ProcessInstance instance,FlowHistory flowHistory ){
        List<PvmTransition>   nextNodes = currentNode.getOutgoingTransitions();
        if(nextNodes!=null && nextNodes.size()>0){
            for(PvmTransition node:nextNodes){
                PvmActivity nextActivity = node.getDestination();
                if( ifGageway( nextActivity)){
                    callInitTaskBack(nextActivity,instance,flowHistory);
                }
                String key = nextActivity.getProperty("key")!=null?nextActivity.getProperty("key").toString():null;
                if(key==null){
                    key = nextActivity.getId();
                }
                initTask(instance,key,flowHistory);
            }
        }

    }

    /**
     * 撤回到指定任务节点
     * @param id
     * @return
     */
    public  OperateResult rollBackTo(String id){
        OperateResult result =  OperateResult.OperationSuccess("core_00003");
        FlowHistory flowHistory =  flowHistoryDao.findOne(id);
        result = this.taskRollBack(flowHistory);
        if(result.successful()){
            flowHistory.setTaskStatus(TaskStatus.CANCLE.toString());
            flowHistoryDao.save(flowHistory);
        }
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
     * @param taskId
     * @param variables
     */
    private void completeActiviti(String taskId, Map<String, Object> variables) {
        taskService.complete(taskId, variables);
    }

   private PvmActivity getActivitNode(String taskId){
       // 取得当前任务
       HistoricTaskInstance currTask = historyService
               .createHistoricTaskInstanceQuery().taskId(taskId)
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

//       List<PvmTransition> nextTransitionList = currActivity
//               .getOutgoingTransitions();
       return currActivity;

   }
//
//
//    private OperateResult activitiCallBack(PvmActivity currActivity, ProcessInstance instance, ProcessDefinitionEntity definition, HistoricTaskInstance destnetionTask  ){
//        List<PvmTransition> nextTransitionList = currActivity
//                .getOutgoingTransitions();
//        OperateResult result =  OperateResult.OperationSuccess("core_00003");
//        for (PvmTransition nextTransition : nextTransitionList) {
//            PvmActivity nextActivity = nextTransition.getDestination();
//
//            Boolean ifGateWay=this.ifGageway(nextActivity);
//            if(ifGateWay){//如果是网关节点，直接寻找网关出口的可执行节点
//                 result = activitiCallBack( nextActivity, instance,definition,destnetionTask);
////                if(result.notSuccessful()){
//                return result;
////                }
//            }
//
//            List<HistoricTaskInstance> completeTasks = historyService
//                    .createHistoricTaskInstanceQuery()
//                    .processInstanceId(instance.getId())
//                    .taskDefinitionKey(nextActivity.getId()).finished()
//                    .list();
//            for(HistoricTaskInstance h:completeTasks){ //校验关联的出口节点任务执行情况，如果没有，则可回退
//                if(h.getEndTime().after(destnetionTask.getEndTime())){
//                     result =  OperateResult.OperationFailure("10005");//下一任务正在执行或者已经执行完成，退回失败
//                }
//            }
//            Map<String, Object> variables;
//            variables=instance.getProcessVariables();
//            List<Task> nextTasks = taskService.createTaskQuery().processInstanceId(instance.getId())
//                    .taskDefinitionKey(nextActivity.getId()).list();
//            for (Task nextTask : nextTasks) {
//                //取活动，清除活动方向
//                List<PvmTransition> oriPvmTransitionList = new ArrayList<PvmTransition>();
//                List<PvmTransition> pvmTransitionList = nextActivity
//                        .getOutgoingTransitions();
//                for (PvmTransition pvmTransition : pvmTransitionList) {
//                    oriPvmTransitionList.add(pvmTransition);
//                }
//                pvmTransitionList.clear();
//                //建立新方向
//                ActivityImpl nextActivityImpl = ((ProcessDefinitionImpl) definition)
//                        .findActivity(nextTask.getTaskDefinitionKey());
//                TransitionImpl newTransition = nextActivityImpl
//                        .createOutgoingTransition();
//                // 取得转向的目标，这里需要指定用需要回退到的任务节点
//                ActivityImpl destination = ((ProcessDefinitionImpl) definition)
//                        .findActivity(destnetionTask.getTaskDefinitionKey());
//
////                 newTransition.setDestination((ActivityImpl) currActivity);
//                newTransition.setDestination(destination);
//                //完成任务
//                taskService.complete(nextTask.getId(), variables);
//                historyService.deleteHistoricActivityInstancesByTaskId(nextTask.getId());
//
//                historyService.deleteHistoricTaskInstance(nextTask.getId());
//
//                if(ifGageway(currActivity)){
//                    HistoricActivityInstance gateWayActivity = historyService.createHistoricActivityInstanceQuery().processInstanceId(destnetionTask.getProcessInstanceId()).activityId(currActivity.getId()).singleResult();
//                    if(gateWayActivity!=null){
//                        historyService.deleteHistoricActivityInstanceById(gateWayActivity.getId());
//                    }
//                }
//
////                 historyService.deleteHistoricIdentityLinksByTaskId(nextTask.getId());
//                //恢复方向
//                destination.getIncomingTransitions().remove(newTransition);
//                List<PvmTransition> pvmTList = nextActivity
//                        .getOutgoingTransitions();
//                pvmTList.clear();
//                for (PvmTransition pvmTransition : oriPvmTransitionList) {
//                    pvmTransitionList.add(pvmTransition);
//                }
//
//
//            }
//
//        }
//        return result;
//    }


    /**
     * 回退任务
     *
     * @return
     */
    public OperateResult taskRollBack(FlowHistory flowHistory) {
        OperateResult result =  OperateResult.OperationSuccess("core_00003");
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
                return  OperateResult.OperationFailure("10002");//流程实例不存在或者已经结束
            }
            variables = instance.getProcessVariables();
            Map variablesTask = currTask.getTaskLocalVariables();
            // 取得流程定义
            ProcessDefinitionEntity definition = (ProcessDefinitionEntity) ((RepositoryServiceImpl) repositoryService)
                    .getDeployedProcessDefinition(currTask.getProcessDefinitionId());
            if (definition == null) {
                logger.error(ContextUtil.getMessage("10003"));
                return OperateResult.OperationFailure("10003");//流程定义未找到找到");
            }

            String executionId = currTask.getExecutionId();
            Execution execution = runtimeService.createExecutionQuery().executionId(executionId).singleResult();
            List<HistoricVariableInstance> historicVariableInstances=	historyService.createHistoricVariableInstanceQuery().executionId(executionId).list();

//            for(HistoricVariableInstance h: historicVariableInstances){
//
//            }
            // 取得下一步活动
            ActivityImpl currActivity = ((ProcessDefinitionImpl) definition)
                    .findActivity(currTask.getTaskDefinitionKey());
            if (currTask.getEndTime() == null) {// 当前任务可能已经被还原
                logger.error(ContextUtil.getMessage("10008"));
                return OperateResult.OperationFailure("10008");//当前任务可能已经被还原
            }

            Boolean resultCheck = checkNextNodeNotCompleted(currActivity, instance, definition, currTask);
            if (!resultCheck) {
                logger.info(ContextUtil.getMessage("10005"));
                return OperateResult.OperationFailure("10005");//下一任务正在执行或者已经执行完成，退回失败
            }

            HistoricActivityInstance historicActivityInstance = null;
            HistoricActivityInstanceQuery his = historyService.createHistoricActivityInstanceQuery()
                    .executionId(execution.getId());
            if(his!=null){
                historicActivityInstance = his.activityId(currTask.getTaskDefinitionKey()).singleResult();
                if(historicActivityInstance ==null ){
                    his = historyService.createHistoricActivityInstanceQuery().processInstanceId(instance.getId())
                            .taskAssignee(currTask.getAssignee());
                    if(his!=null){
                        historicActivityInstance = his.activityId(currTask.getTaskDefinitionKey()).singleResult();
                    }
                }
            }

            if(historicActivityInstance == null){
                logger.error(ContextUtil.getMessage("10009"));
                return  OperateResult.OperationFailure("10009");//当前任务找不到
            }
            if (!currTask.getTaskDefinitionKey().equalsIgnoreCase(execution.getActivityId())) {
                if(execution.getActivityId()!=null){
                    List<HistoricActivityInstance> historicActivityInstanceList = historyService
                            .createHistoricActivityInstanceQuery().executionId(execution.getId())
                            .activityId(execution.getActivityId()).list();
                    if (historicActivityInstanceList != null) {
                        for(HistoricActivityInstance hTemp:historicActivityInstanceList){
                            if(hTemp.getEndTime()==null){
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
            // newTask.setDeleted(false);
            // newTask.setDueDate(currTask.getDueDate());
            // newTask.setEventName(currTask.gete);
            newTask.setId(currTask.getId());
            newTask.setExecutionId(currTask.getExecutionId());
            newTask.setProcessDefinitionId(currTask.getProcessDefinitionId());
            newTask.setProcessInstanceId(currTask.getProcessInstanceId());
            newTask.setVariables(currTask.getProcessVariables());
            // newTnewTaskask.setExecution((DelegateExecution) execution);
            // newTask.setProcessInstance(instance);
            // newTask.setTaskDefinition(currActivity.gett);
            newTask.setTaskDefinitionKey(currTask.getTaskDefinitionKey());

            taskService.callBackTask(newTask, execution);

            callBackRunIdentityLinkEntity(currTask.getId());//还原候选人等信
            // 删除其他到达的节点
            deleteOtherNode(currActivity, instance, definition, currTask);

            //初始化回退后的新任务
            flowHistory.setDepict(null);
            initTask(instance,currTask.getTaskDefinitionKey(),flowHistory);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e.getMessage());

            return OperateResult.OperationFailure("10004");//流程取回失败，未知错误
        }
    }

    /**
     * 还原执行人、候选人
     * @param taskId
     */
    private void callBackRunIdentityLinkEntity(String taskId){
        List<HistoricIdentityLink> historicIdentityLinks = historyService.getHistoricIdentityLinksForTask(taskId);

        for(HistoricIdentityLink hlink : historicIdentityLinks){
            HistoricIdentityLinkEntity historicIdentityLinkEntity = (HistoricIdentityLinkEntity)hlink;
            if(historicIdentityLinkEntity.getId()==null){
                continue;
            }
            IdentityLinkEntity identityLinkEntity = new IdentityLinkEntity();
            identityLinkEntity.setGroupId(historicIdentityLinkEntity.getGroupId());
            identityLinkEntity.setId(historicIdentityLinkEntity.getId());
            identityLinkEntity.setProcessInstanceId(historicIdentityLinkEntity.getProcessInstanceId());
            identityLinkEntity.setTaskId(historicIdentityLinkEntity.getTaskId());
            identityLinkEntity.setType(historicIdentityLinkEntity.getType());
            identityLinkEntity.setUserId(historicIdentityLinkEntity.getUserId());
            try{
                identityService.save(identityLinkEntity);
            }catch(Exception e){
                e.printStackTrace();
                throw e;
            }

        }
    }

    /**
     * 删除其他到达的节点
     */
    private Boolean deleteOtherNode(PvmActivity currActivity, ProcessInstance instance,
                                   ProcessDefinitionEntity definition, HistoricTaskInstance destnetionTask) {
        List<PvmTransition> nextTransitionList = currActivity.getOutgoingTransitions();
        Boolean result = true;
        for (PvmTransition nextTransition : nextTransitionList) {
            PvmActivity nextActivity = nextTransition.getDestination();
            Boolean ifGateWay = ifGageway(nextActivity);
            boolean ifMultiInstance = ifMultiInstance(nextActivity);
            if (ifGateWay) {
                result= deleteOtherNode(nextActivity, instance, definition, destnetionTask);
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

                org.springframework.orm.jpa.JpaTransactionManager  transactionManager =(org.springframework.orm.jpa.JpaTransactionManager) ContextUtil.getApplicationContext().getBean("transactionManager");
                DefaultTransactionDefinition def = new DefaultTransactionDefinition();
                def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW); // 事物隔离级别，开启新事务，这样会比较安全些。
                TransactionStatus status = transactionManager.getTransaction(def); // 获得事务状态
                try {
                    //逻辑代码，可以写上你的逻辑处理代码
                    flowTaskDao.deleteByActTaskId(nextTask.getId());//删除关联的流程新任务
                    transactionManager.commit(status);
                } catch (Exception e) {
                    e.printStackTrace();
                    transactionManager.rollback(status);
                    throw e;
                }


            }

            if ((nextTasks!=null) && (!nextTasks.isEmpty()) && (ifGageway(currActivity))) {

                HistoricActivityInstance gateWayActivity = historyService.createHistoricActivityInstanceQuery()
                        .processInstanceId(destnetionTask.getProcessInstanceId()).activityId(currActivity.getId())
                        .singleResult();
                if (gateWayActivity != null) {
                    historyService.deleteHistoricActivityInstanceById(gateWayActivity.getId());
                }

            }
            if(ifMultiInstance){//多实例任务，清除父执行分支
                ExecutionEntity pExecution=(ExecutionEntity) runtimeService.createExecutionQuery().processInstanceId(instance.getId()).activityIdNoActive(nextActivity.getId()).singleResult();
                if(pExecution != null){
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
            if (ifGateWay) {
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
     * @param pvmActivity
     * @return
     */
    private boolean ifGageway( PvmActivity pvmActivity ){
        String nextActivtityType = pvmActivity.getProperty("type").toString();
        Boolean result=false;
        if("exclusiveGateway".equalsIgnoreCase(nextActivtityType)||  //排他网关
                "inclusiveGateway".equalsIgnoreCase(nextActivtityType)  //包容网关
                || "parallelGateWay".equalsIgnoreCase(nextActivtityType)){ //并行网关
            result=true;
        }
        return result;
    }
    /**
     * 判断是否是多实例任务（会签）
     * @param pvmActivity
     * @return
     */
    private boolean ifMultiInstance( PvmActivity pvmActivity ){
        Object nextActivtityType = pvmActivity.getProperty("multiInstance");
        Boolean result=false;
        if(nextActivtityType!=null && !"".equals(nextActivtityType)){ //多实例任务
            result=true;
        }
        return result;
    }

    /**
     * 将新的流程任务初始化
     * @param instance
     * @param actTaskDefKey
     */
    private void initTask(ProcessInstance instance,String actTaskDefKey,FlowHistory preTask){
        FlowInstance flowInstance = flowInstanceDao.findByActInstanceId(instance.getId());
        if( instance.isEnded()){//流程结束
            flowInstance.setEnded(true);
            flowInstance.setEndDate(new Date());
            flowInstanceDao.save(flowInstance);
            return;
        }
        List<Task> tasks = new ArrayList<Task>();
        // 根据当流程实例查询任务
        List<Task> taskList = taskService.createTaskQuery().processInstanceId(instance.getId()).taskDefinitionKey(actTaskDefKey).active().list();
        String flowName = instance.getProcessDefinitionName();
        if(flowName == null){
            flowName = instance.getProcessDefinitionKey();
        }
        if(taskList!=null && taskList.size()>0){
            for(Task task:taskList){
                if(task.getAssignee()!=null && !"".equals(task.getAssignee())){
                    List<IdentityLink> identityLinks = taskService.getIdentityLinksForTask(task.getId());
                    for(IdentityLink identityLink:identityLinks){
                        FlowTask  flowTask = new FlowTask();
                        flowTask.setActTaskDefKey(actTaskDefKey);
                        flowTask.setFlowName(flowName);
                        flowTask.setTaskName(task.getName());
                        flowTask.setActTaskId(task.getId());
                        flowTask.setOwnerAccount(task.getOwner());
                        flowTask.setPriority(task.getPriority());
                        flowTask.setExecutorAccount(identityLink.getUserId());
                        flowTask.setActType(identityLink.getType());
                        flowTask.setDepict(task.getDescription());
                        flowTask.setTaskStatus(TaskStatus.INIT.toString());
                        if(preTask!=null){
                            flowTask.setPreId(preTask.getId());
                            flowTask.setDepict(preTask.getDepict());
                        }
                        flowTask.setFlowInstance(flowInstance);

                        flowTaskDao.save(flowTask);
                    }
                }else{
                    FlowTask  flowTask = new FlowTask();
                    flowTask.setActTaskDefKey(actTaskDefKey);
                    flowTask.setFlowName(flowName);
                    flowTask.setTaskName(task.getName());
                    flowTask.setActTaskId(task.getId());
                    flowTask.setOwnerAccount(task.getOwner());
                    flowTask.setPriority(task.getPriority());
                    flowTask.setExecutorAccount(task.getAssignee());
                    flowTask.setDepict(task.getDescription());
                    flowTask.setActType("assignee");
                    flowTask.setTaskStatus(TaskStatus.INIT.toString());
                    flowTask.setFlowInstance(flowInstance);
                    if(preTask!=null){
                        flowTask.setPreId(preTask.getId());
                        flowTask.setDepict(preTask.getDepict());
                    }
                    flowTaskDao.save(flowTask);
                }
//                flowTask.setCandidateAccount(instance.get);

            }
        }

    }

    /**
     * 记录任务执行过程中传入的参数
     * @param variables  参数map
     * @param flowTask   关联的工作任务
     */
    private void saveVariables( Map<String, Object> variables, FlowTask flowTask){
        if((variables!=null) && (!variables.isEmpty()) && (flowTask!=null)){
            FlowVariable flowVariable = new FlowVariable();
                for(Map.Entry<String,Object> vs:variables.entrySet()){
                   String key= vs.getKey();
                   Object value = vs.getValue();
                    Long longV = null;
                    Double doubleV = null;
                    String strV =null;
                    flowVariable.setName(key);
                    flowVariable.setFlowTask(flowTask);
                    try{
                        longV =  Long.parseLong(value.toString());
                        flowVariable.setType(Long.class.getName());
                        flowVariable.setVLong(longV);
                    }catch(RuntimeException e1){
                        try{
                            doubleV = Double.parseDouble(value.toString());
                            flowVariable.setType(Double.class.getName());
                            flowVariable.setVDouble(doubleV);
                        }catch(RuntimeException e2){
                            strV = value.toString();
                        }
                    }
                    flowVariable.setVText(strV);
            }
            flowVariableDao.save(flowVariable);
        }
    }




//    /**
//     * 通过流程实例ID,查找对应用户的最近一次流程执行任务ID
//     * @param processInstanceId
//     * @param userId
//     * @return 最近一次流程执行任务ID
//     */
//    public String findActivitiParentTaksId(String processInstanceId,String userId){
//
//        List<Task> tasks = taskService.createTaskQuery().processInstanceId(processInstanceId).list();
//
//        String taskName = "";
//
//        if(tasks != null && tasks.size()>0){
//
//            List<HistoricTaskInstance> htis = historyService.createHistoricTaskInstanceQuery().processInstanceId(processInstanceId).list();
//
//            for(int i = htis.size()-1;i>0;i--){
//
//                if(tasks.get(0).getName().equals(htis.get(i).getName()) && i != 1){//当前任务
//
//                    continue;
//
//                }else{
//
//                    if(taskName!=null && !"".equals(taskName)){
//
//                        taskName = htis.get(i).getName();
//
//                        if(userId.equals(htis.get(i).getAssignee())){
//
//                            return htis.get(i).getId();
//
//                        }
//
//                    }else{
//
//                        if(taskName.equals(htis.get(i).getName())){
//
//                            if(userId.equals(htis.get(i).getAssignee())){
//
//                                return htis.get(i).getId();
//
//                            }
//
//                        }
//
//                    }
//
//                }
//
//            }
//
//        }
//
//        return null;
//
//    }
}
