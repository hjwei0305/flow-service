package com.ecmp.flow.service;

import com.ecmp.annotation.AppModule;
import com.ecmp.basic.api.IEmployeeService;
import com.ecmp.basic.api.IPositionService;
import com.ecmp.basic.entity.Employee;
import com.ecmp.config.util.ApiClient;
import com.ecmp.context.ContextUtil;
import com.ecmp.core.dao.BaseEntityDao;
import com.ecmp.core.dao.jpa.BaseDao;
import com.ecmp.core.service.BaseEntityService;
import com.ecmp.core.service.BaseService;
import com.ecmp.flow.api.IFlowTaskService;
import com.ecmp.flow.constant.FlowStatus;
import com.ecmp.flow.dao.FlowInstanceDao;
import com.ecmp.flow.dao.FlowVariableDao;
import com.ecmp.flow.entity.*;
import com.ecmp.flow.util.ConditionUtil;
import com.ecmp.flow.util.ExpressionUtil;
import com.ecmp.flow.util.TaskStatus;
import com.ecmp.flow.dao.FlowHistoryDao;
import com.ecmp.flow.dao.FlowTaskDao;
import com.ecmp.flow.vo.ApprovalHeaderVO;
import com.ecmp.flow.vo.FlowTaskCompleteVO;
import com.ecmp.flow.vo.NodeInfo;
import com.ecmp.flow.vo.bpmn.*;
import com.ecmp.vo.OperateResult;
import com.ecmp.vo.OperateResultWithData;
import jodd.util.StringUtil;
import net.sf.json.JSONObject;
import org.activiti.engine.history.*;
import org.activiti.engine.impl.Condition;
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
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import javax.ws.rs.PathParam;
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
     *
     * @param id     任务id
     * @param userId 用户账号
     * @return
     */
    public OperateResult claim(String id, String userId) {
        FlowTask flowTask = flowTaskDao.findOne(id);
        String actTaskId = flowTask.getActTaskId();
        this.claimActiviti(actTaskId, userId);
        flowTask.setActClaimTime(new Date());
        flowTask.setTaskStatus(TaskStatus.CLAIM.toString());
        flowTaskDao.save(flowTask);
        flowTaskDao.deleteNotClaimTask(actTaskId, id);
        OperateResult result = OperateResult.OperationSuccess("core_00003");
        return result;
    }


    public OperateResultWithData complete(FlowTaskCompleteVO flowTaskCompleteVO) {
        String taskId = flowTaskCompleteVO.getTaskId();
        Map<String, Object> variables = flowTaskCompleteVO.getVariables();
        List<String> manualSelectedNodeIds = flowTaskCompleteVO.getManualSelectedNodeIds();
        OperateResultWithData result = null;
        if (manualSelectedNodeIds == null || manualSelectedNodeIds.isEmpty()) {//非人工选择任务的情况
            result = this.complete(taskId,flowTaskCompleteVO.getOpinion(), variables);
        } else {//人工选择任务的情况
            FlowTask flowTask = flowTaskDao.findOne(taskId);
            String actTaskId = flowTask.getActTaskId();
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
            List<PvmTransition> oriPvmTransitionList = new ArrayList<PvmTransition>();
            List<PvmTransition> pvmTransitionList = currActivity.getOutgoingTransitions();
            for (PvmTransition pvmTransition : pvmTransitionList) {
                oriPvmTransitionList.add(pvmTransition);
            }
            pvmTransitionList.clear();
            // 建立新方向
            TransitionImpl newTransition = currActivity.createOutgoingTransition();
            // 定位到人工选择的节点目标
            for (String nodeId : manualSelectedNodeIds) {
                ActivityImpl destinationActivity = ((ProcessDefinitionImpl) definition).findActivity(nodeId);
                newTransition.setDestination(destinationActivity);
            }

            //执行任务
            result = this.complete(taskId,flowTaskCompleteVO.getOpinion(), variables);

            // 恢复方向
            for (String nodeId : manualSelectedNodeIds) {
                ActivityImpl destinationActivity = ((ProcessDefinitionImpl) definition).findActivity(nodeId);
                destinationActivity.getIncomingTransitions().remove(newTransition);
            }
            List<PvmTransition> pvmTList = currActivity.getOutgoingTransitions();
            pvmTList.clear();
            for (PvmTransition pvmTransition : oriPvmTransitionList) {
                pvmTransitionList.add(pvmTransition);
            }

        }
        return result;
    }

    /**
     * 完成任务
     *
     * @param id        任务id
     * @param opinion  审批意见
     * @param variables 参数
     * @return
     */
    private OperateResultWithData complete(String id,String opinion, Map<String, Object> variables) {
        FlowTask flowTask = flowTaskDao.findOne(id);
        flowTask.setDepict(opinion);
        String actTaskId = flowTask.getActTaskId();
        this.completeActiviti(actTaskId, variables);
        this.saveVariables(variables, flowTask);
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
        HistoricTaskInstance historicTaskInstance = historyService.createHistoricTaskInstanceQuery().taskId(actTaskId).singleResult(); // 创建历史任务实例查询

        // 取得流程实例
        ProcessInstance instance = runtimeService
                .createProcessInstanceQuery()
                .processInstanceId(historicTaskInstance.getProcessInstanceId())
                .singleResult();
        if (historicTaskInstance != null) {
            FlowHistory flowHistory = new FlowHistory();
            flowTask.setFlowDefinitionId(flowTask.getFlowDefinitionId());
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
            if (reject != null && reject == 1) {
                flowHistory.setDepict("被驳回");
                flowHistory.setTaskStatus(TaskStatus.REJECT.toString());
            } else {
                flowHistory.setDepict(null);
                flowHistory.setTaskStatus(TaskStatus.COMPLETED.toString());
            }
            flowHistoryDao.save(flowHistory);
            flowTaskDao.delete(flowTask);

            org.springframework.orm.jpa.JpaTransactionManager transactionManager = (org.springframework.orm.jpa.JpaTransactionManager) ContextUtil.getApplicationContext().getBean("transactionManager");
            DefaultTransactionDefinition def = new DefaultTransactionDefinition();
            def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW); // 事物隔离级别，开启新事务，这样会比较安全些。
            TransactionStatus status = transactionManager.getTransaction(def); // 获得事务状态
            try {
                //逻辑代码，可以写上你的逻辑处理代码
                flowTaskDao.deleteNotClaimTask(actTaskId, id);//删除其他候选用户的任务
                transactionManager.commit(status);
            } catch (Exception e) {
                e.printStackTrace();
                transactionManager.rollback(status);
                throw e;
            }


            //初始化新的任务
            PvmActivity currentNode = this.getActivitNode(actTaskId);
            if (currentNode != null) {
                callInitTaskBack(currentNode, instance, flowHistory);
            }

        }

        OperateResultWithData result = OperateResultWithData.OperationSuccess("core_00003");
        if(instance.isEnded()){
            result.setData(FlowStatus.COMPLETED);//任务结束
        }
        return result;
    }


    private void callInitTaskBack(PvmActivity currentNode, ProcessInstance instance, FlowHistory flowHistory) {
        List<PvmTransition> nextNodes = currentNode.getOutgoingTransitions();
        if (nextNodes != null && nextNodes.size() > 0) {
            for (PvmTransition node : nextNodes) {
                PvmActivity nextActivity = node.getDestination();
                if (ifGageway(nextActivity)) {
                    callInitTaskBack(nextActivity, instance, flowHistory);
                }
                String key = nextActivity.getProperty("key") != null ? nextActivity.getProperty("key").toString() : null;
                if (key == null) {
                    key = nextActivity.getId();
                }
                initTask(instance, key, flowHistory);
            }
        }

    }

    /**
     * 撤回到指定任务节点
     *
     * @param id
     * @return
     */
    public OperateResult rollBackTo(String id) {
        OperateResult result = OperateResult.OperationSuccess("core_00003");
        FlowHistory flowHistory = flowHistoryDao.findOne(id);
        result = this.taskRollBack(flowHistory);
        if (result.successful()) {
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
     *
     * @param taskId
     * @param variables
     */
    private void completeActiviti(String taskId, Map<String, Object> variables) {
        taskService.complete(taskId, variables);
    }

    /**
     * 获取活动节点
     *
     * @param taskId
     * @return
     */
    private PvmActivity getActivitNode(String taskId) {
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

    /**
     * 任务驳回
     *
     * @param id        任务id
     * @param variables 参数
     * @return 结果
     */
    public OperateResult taskReject(String id, Map<String, Object> variables) {
        OperateResult result = OperateResult.OperationSuccess("10006");
        FlowTask flowTask = flowTaskDao.findOne(id);
        if (flowTask != null) {
            FlowHistory preFlowTask = flowHistoryDao.findOne(flowTask.getPreId());//上一个任务id
            if (preFlowTask == null) {
                return OperateResult.OperationFailure("10009");
            } else {
                result = this.activitiReject(flowTask, preFlowTask);
            }
        } else {
            return OperateResult.OperationFailure("10009");
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
    private OperateResult activitiReject(FlowTask currentTask, FlowHistory preFlowTask) {
        OperateResult result = OperateResult.OperationSuccess("core_00003");
        // 取得当前任务
        HistoricTaskInstance currTask = historyService.createHistoricTaskInstanceQuery().taskId(currentTask.getActTaskId())
                .singleResult();
        // 取得流程实例
        ProcessInstance instance = runtimeService.createProcessInstanceQuery()
                .processInstanceId(currTask.getProcessInstanceId()).singleResult();
        if (instance == null) {
            OperateResult.OperationFailure("10009");
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
            OperateResult.OperationFailure("10009");
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
            this.complete(currentTask.getId(),"驳回", variables);

            //恢复方向
            preActivity.getIncomingTransitions().remove(newTransition);
            List<PvmTransition> pvmTList = currentActivity
                    .getOutgoingTransitions();
            pvmTList.clear();
            for (PvmTransition pvmTransition : oriPvmTransitionList) {
                pvmTransitionList.add(pvmTransition);
            }
        }
        return result;
    }


    /**
     * 回退任务
     *
     * @return
     */
    public OperateResult taskRollBack(FlowHistory flowHistory) {
        OperateResult result = OperateResult.OperationSuccess("core_00003");
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
                return OperateResult.OperationFailure("10002");//流程实例不存在或者已经结束
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
            List<HistoricVariableInstance> historicVariableInstances = historyService.createHistoricVariableInstanceQuery().executionId(executionId).list();

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
            if (his != null) {
                historicActivityInstance = his.activityId(currTask.getTaskDefinitionKey()).singleResult();
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
                return OperateResult.OperationFailure("10009");//当前任务找不到
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
            initTask(instance, currTask.getTaskDefinitionKey(), flowHistory);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e.getMessage());

            return OperateResult.OperationFailure("10004");//流程取回失败，未知错误
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

                org.springframework.orm.jpa.JpaTransactionManager transactionManager = (org.springframework.orm.jpa.JpaTransactionManager) ContextUtil.getApplicationContext().getBean("transactionManager");
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
     *
     * @param pvmActivity
     * @return
     */
    private boolean ifGageway(PvmActivity pvmActivity) {
        String nextActivtityType = pvmActivity.getProperty("type").toString();
        Boolean result = false;
        if ("exclusiveGateway".equalsIgnoreCase(nextActivtityType) ||  //排他网关
                "inclusiveGateway".equalsIgnoreCase(nextActivtityType)  //包容网关
                || "parallelGateWay".equalsIgnoreCase(nextActivtityType)) { //并行网关
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
            PvmActivity currentInActivity = currentInTransition.getDestination();
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
        if (instance.isEnded()) {//流程结束
            flowInstance.setEnded(true);
            flowInstance.setEndDate(new Date());
            flowInstanceDao.save(flowInstance);
            return;
        }
        List<Task> tasks = new ArrayList<Task>();
        // 根据当流程实例查询任务
        List<Task> taskList = taskService.createTaskQuery().processInstanceId(instance.getId()).taskDefinitionKey(actTaskDefKey).active().list();
        String flowName = instance.getProcessDefinitionName();
        if (flowName == null) {
            flowName = instance.getProcessDefinitionKey();
        }
        if (taskList != null && taskList.size() > 0) {
            for (Task task : taskList) {
                if (task.getAssignee() != null && !"".equals(task.getAssignee())) {
                    List<IdentityLink> identityLinks = taskService.getIdentityLinksForTask(task.getId());
                    for (IdentityLink identityLink : identityLinks) {
                        FlowTask flowTask = new FlowTask();
                        flowTask.setFlowDefinitionId(flowInstance.getFlowDefVersion().getFlowDefination().getId());
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
                        if (preTask != null) {
                            if (TaskStatus.REJECT.toString().equalsIgnoreCase(preTask.getTaskStatus())) {
                                String oldPreId = preTask.getPreId();//前一个任务的前一个任务ID
                                FlowHistory oldPreFlowHistory = flowHistoryDao.findOne(oldPreId);
                                if (oldPreFlowHistory != null) {
                                    flowTask.setPreId(oldPreFlowHistory.getId());
                                } else {
                                    flowTask.setPreId(null);
                                }
                            } else {
                                flowTask.setPreId(preTask.getId());
                            }
                            flowTask.setDepict(preTask.getDepict());
                        }
                        flowTask.setFlowInstance(flowInstance);

                        flowTaskDao.save(flowTask);
                    }
                } else {
                    FlowTask flowTask = new FlowTask();
                    flowTask.setFlowDefinitionId(flowInstance.getFlowDefVersion().getFlowDefination().getId());
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
                    if (preTask != null) {
                        if (TaskStatus.REJECT.toString().equalsIgnoreCase(preTask.getTaskStatus())) {
                            String oldPreId = preTask.getPreId();//前一个任务的前一个任务ID
                            FlowHistory oldPreFlowHistory = flowHistoryDao.findOne(oldPreId);
                            if (oldPreFlowHistory != null) {
                                flowTask.setPreId(oldPreFlowHistory.getId());
                            } else {
                                flowTask.setPreId(null);
                            }
                        } else {
                            flowTask.setPreId(preTask.getId());
                        }
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
     * 通过任务Id检查当前任务的出口节点是否存在条件表达式
     *
     * @param actTaskId 任务实际ID
     * @return
     */
    public boolean checkHasConditon(String actTaskId) {
        PvmActivity currActivity = this.getActivitNode(actTaskId);
        return this.checkHasConditon(currActivity);
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
//       		 if ("exclusiveGateway".equalsIgnoreCase(nextActivtityType)){//排他网关
//
//       		 } else if("inclusiveGateway".equalsIgnoreCase(nextActivtityType)){ //包容网关
//
//       		 } else
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
//                System.out.println(conditon);
//                System.out.println(type);
//                System.out.println(conditionText);
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
    public List<NodeInfo> findNexNodesWithUserSet(String id, String businessId) throws NoSuchMethodException {
        return this.findNexNodesWithUserSet(id,businessId,null);
    }

    /**
     * 获取出口节点信息（带初始化流程设计器配置用户）
     *
     * @param id
     * @param businessId
     * @return
     * @throws NoSuchMethodException
     */
    public List<NodeInfo> findNexNodesWithUserSet(String id, String businessId,List<String> includeNodeIds) throws NoSuchMethodException {
        List<NodeInfo> nodeInfoList = this.findNextNodes(id, businessId,includeNodeIds);

        if (nodeInfoList != null && !nodeInfoList.isEmpty()) {
            FlowTask flowTask = flowTaskDao.findOne(id);
            String flowDefJson = flowTask.getFlowInstance().getFlowDefVersion().getDefJson();
            JSONObject defObj = JSONObject.fromObject(flowDefJson);
            Definition definition = (Definition) JSONObject.toBean(defObj, Definition.class);

            for (NodeInfo nodeInfo : nodeInfoList) {
                net.sf.json.JSONObject currentNode = definition.getProcess().getNodes().getJSONObject(nodeInfo.getId());
                net.sf.json.JSONObject executor = currentNode.getJSONObject("nodeConfig").getJSONObject("executor");

                UserTask userTaskTemp = (UserTask) JSONObject.toBean(currentNode, UserTask.class);
                if("Normal".equalsIgnoreCase(userTaskTemp.getNodeType())){
                    nodeInfo.setUserVarName(userTaskTemp.getId()+"_Normal");
                }else if("SingleSign".equalsIgnoreCase(userTaskTemp.getNodeType())){
                    nodeInfo.setUserVarName(userTaskTemp.getId()+"_SingleSign");
                }
                else if("CounterSign".equalsIgnoreCase(userTaskTemp.getNodeType())){
                    nodeInfo.setUserVarName(userTaskTemp.getId()+"_List_CounterSign");
//                    MultiInstanceConfig multiInstanceConfig = new MultiInstanceConfig();
//                    multiInstanceConfig.setUserIds("${"+userTaskTemp.getId()+"_List_CounterSign}");
//                    multiInstanceConfig.setVariable("${"+userTaskTemp.getId()+"_CounterSign}");
                }

                if (executor != null) {
                    String userType = (String) executor.get("userType");
                    String ids = (String) executor.get("ids");
                    Set<Employee> employeeSet = new HashSet<Employee>();
                    List<Employee> employees = null;
                    if ("StartUser".equalsIgnoreCase(userType)) {//获取流程实例启动者
                        ProcessInstance instance = runtimeService.createProcessInstanceQuery()
                                .processInstanceId(flowTask.getFlowInstance().getActInstanceId()).singleResult();
                        HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery().processInstanceId(flowTask.getFlowInstance().getActInstanceId()).singleResult();
//                            Map<String,Object> v = instance.getProcessVariables();
                        String startUserId = historicProcessInstance.getStartUserId();
                        IEmployeeService iEmployeeService = ApiClient.createProxy(IEmployeeService.class);
                        employees = iEmployeeService.findByIds(java.util.Arrays.asList(startUserId));
//                            if(v != null){
//                                startUserId = (String) v.get("startUserId");
//                            }
//                            if(StringUtils.isEmpty(startUserId)){
//                                startUserId = flowTask.getFlowInstance().getCreatedBy();
//                            }

                    } else {
                        if (!StringUtils.isEmpty(ids)) {
                            nodeInfo.setUiUserType(userType);
                            String[] idsShuZhu = ids.split(",");
                            List<String> idList = java.util.Arrays.asList(idsShuZhu);
                            //StartUser、Position、PositionType、SelfDefinition、AnyOne
                            if ("Position".equalsIgnoreCase(userType)) {//调用岗位获取用户接口
                                IPositionService iPositionService = ApiClient.createProxy(IPositionService.class);
                                employees = iPositionService.getAssignedEmployeesByPositionIds(idList);
                            } else if ("PositionType".equalsIgnoreCase(userType)) {//调用岗位类型获取用户接口
                                IPositionService iPositionService = ApiClient.createProxy(IPositionService.class);
                                employees = iPositionService.getAssignedEmployeesByPosCateIds(idList);
                            } else if ("SelfDefinition".equalsIgnoreCase(userType)) {//通过业务ID获取自定义用户
                                IEmployeeService iEmployeeService = ApiClient.createProxy(IEmployeeService.class);
                                employees = iEmployeeService.findByIds(idList);
                            } else if ("AnyOne".equalsIgnoreCase(userType)) {//任意执行人不添加用户
                            }
                            if (employees != null && !employees.isEmpty()) {
                                employeeSet.addAll(employees);
                                nodeInfo.setEmployeeSet(employeeSet);
                            }

                        }
                    }
                }
            }
        }
        return nodeInfoList;
    }


    public List<NodeInfo> findNextNodes(String id) throws NoSuchMethodException{
        FlowTask flowTask = flowTaskDao.findOne(id);
        String businessId = flowTask.getFlowInstance().getBusinessId();
        return this.findNextNodes(id,businessId);
    }
    /**
     * 选择下一步执行的节点信息
     *
     * @param id
     * @return
     * @throws NoSuchMethodException
     */
    public List<NodeInfo> findNextNodes(String id, String businessId) throws NoSuchMethodException {
          return this.findNextNodes(id,businessId,null);
    }
    /**
     * 选择下一步执行的节点信息
     *
     * @param id
     * @return
     * @throws NoSuchMethodException
     */
    public List<NodeInfo> findNextNodes(String id, String businessId,List<String> includeNodeIds) throws NoSuchMethodException {
        FlowTask flowTask = flowTaskDao.findOne(id);
        String actTaskId = flowTask.getActTaskId();
        if (checkHasConditon(actTaskId)) {//判断出口任务节点中是否包含有条件表达式
            if (StringUtils.isEmpty(businessId)) {
                throw new RuntimeException("任务出口节点包含条件表达式，请指定业务ID");
            }
//            String defJson = flowTask.getFlowInstance().getFlowDefVersion().getDefJson();
//            JSONObject defObj = JSONObject.fromObject(defJson);
            BusinessModel businessModel = flowTask.getFlowInstance().getFlowDefVersion().getFlowDefination().getFlowType().getBusinessModel();
            String businessModelId = businessModel.getId();
            String appModuleId = businessModel.getAppModuleId();
            com.ecmp.basic.api.IAppModuleService proxy = ApiClient.createProxy(com.ecmp.basic.api.IAppModuleService.class);
            com.ecmp.basic.entity.AppModule appModule = proxy.findOne(appModuleId);
            String clientApiBaseUrl = appModule.getApiBaseAddress();
          //  clientApiBaseUrl =    ContextUtil.getAppModule().getApiBaseAddress();
           // clientApiBaseUrl = "http://localhost:8080/";//测试地址，上线后去掉
            Map<String, Object> v = ExpressionUtil.getConditonPojoValueMap(clientApiBaseUrl, businessModelId, businessId);
            return this.selectQualifiedNode(actTaskId, v, includeNodeIds);
        } else {
            return this.selectNextAllNodes(actTaskId, includeNodeIds);
        }
    }


    /**
     * 获取所有出口节点信息
     *
     * @param actTaskId
     * @return
     */
    private List<NodeInfo> selectNextAllNodes(String actTaskId,List<String> includeNodeIds) {
        PvmActivity currActivity = this.getActivitNode(actTaskId);
        List<PvmActivity> nextNodes = new ArrayList<PvmActivity>();
        initNextNodes(currActivity, nextNodes);
        //前端需要的数据出口任务数据
        List<NodeInfo> nodeInfoList = new ArrayList<NodeInfo>();
        if (!nextNodes.isEmpty()) {
            //判断网关
            PvmActivity firstActivity = nextNodes.get(0);
            Boolean isSizeBigTwo = nextNodes.size() > 1 ? true : false;
            String nextActivtityType = firstActivity.getProperty("type").toString();
            if ("exclusiveGateway".equalsIgnoreCase(nextActivtityType)) {// 排他网关，radiobox,有且只能选择一个
                if (isSizeBigTwo) {
                    for (int i = 1; i < nextNodes.size(); i++) {
                        PvmActivity tempActivity = nextNodes.get(i);
                        if(includeNodeIds != null){
                            if(!includeNodeIds.contains(tempActivity.getId())){
                                continue;
                            }
                        }
                        NodeInfo tempNodeInfo = new NodeInfo();
                        tempNodeInfo = convertNodes(tempNodeInfo, tempActivity);
                        tempNodeInfo.setUiType("radiobox");
                        nodeInfoList.add(tempNodeInfo);
                    }
                }

            } else if ("inclusiveGateway".equalsIgnoreCase(nextActivtityType)) { // 包容网关,checkbox,至少选择一个
                if (isSizeBigTwo) {
                    for (int i = 1; i < nextNodes.size(); i++) {
                        PvmActivity tempActivity = nextNodes.get(i);
                        if(includeNodeIds != null){
                            if(!includeNodeIds.contains(tempActivity.getId())){
                                continue;
                            }
                        }
                        NodeInfo tempNodeInfo = new NodeInfo();
                        tempNodeInfo = convertNodes(tempNodeInfo, tempActivity);
                        tempNodeInfo.setUiType("checkbox");
                        nodeInfoList.add(tempNodeInfo);
                    }
                }

            } else if ("parallelGateWay".equalsIgnoreCase(nextActivtityType)) { // 并行网关,checkbox,默认全部选中显示不能修改
                if (isSizeBigTwo) {
                    for (int i = 1; i < nextNodes.size(); i++) {
                        PvmActivity tempActivity = nextNodes.get(i);
                        if(includeNodeIds != null){
                            if(!includeNodeIds.contains(tempActivity.getId())){
                                continue;
                            }
                        }
                        NodeInfo tempNodeInfo = new NodeInfo();
                        tempNodeInfo = convertNodes(tempNodeInfo, tempActivity);
                        tempNodeInfo.setUiType("readOnly");
                        nodeInfoList.add(tempNodeInfo);
                    }
                }

            } else {
                if (isSizeBigTwo) {//当下步节点大于一个时，按照并行网关处理。checkbox,默认全部选中显示不能修改
                    for (int i = 1; i < nextNodes.size(); i++) {
                        PvmActivity tempActivity = nextNodes.get(i);
                        if(includeNodeIds != null){
                            if(!includeNodeIds.contains(tempActivity.getId())){
                                continue;
                            }
                        }
                        NodeInfo tempNodeInfo = new NodeInfo();
                        tempNodeInfo = convertNodes(tempNodeInfo, tempActivity);
                        tempNodeInfo.setUiType("readOnly");
                        nodeInfoList.add(tempNodeInfo);
                    }
                } else {//按照惟一分支任务处理，显示一个，只读
                    PvmActivity tempActivity = nextNodes.get(0);
                    if(includeNodeIds != null){
                        if(!includeNodeIds.contains(tempActivity.getId())){
                            throw new RuntimeException("惟一分支未选中");
                        }
                    }
                    NodeInfo tempNodeInfo = new NodeInfo();
                    tempNodeInfo = convertNodes(tempNodeInfo, tempActivity);
                    tempNodeInfo.setUiType("readOnly");
                    nodeInfoList.add(tempNodeInfo);
                }
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
    private void initNextNodes(PvmActivity currActivity, List<PvmActivity> nextNodes) {
        List<PvmTransition> nextTransitionList = currActivity.getOutgoingTransitions();
        if (nextTransitionList != null && !nextTransitionList.isEmpty()) {
            for (PvmTransition pv : nextTransitionList) {
                PvmActivity currTempActivity = pv.getDestination();
                Boolean ifGateWay = ifGageway(currTempActivity);
                if (ifGateWay) {//如果是网关，其他直绑节点自行忽略
                    nextNodes.clear();
                    nextNodes.add(currTempActivity);//把网关放入第一个节点
                    initNextNodes(currTempActivity, nextNodes);
                    break;
                } else {
                    nextNodes.add(currTempActivity);
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
    private NodeInfo convertNodes(NodeInfo tempNodeInfo, PvmActivity tempActivity) {
        tempNodeInfo.setName(tempActivity.getProperty("name").toString());
        tempNodeInfo.setType(tempActivity.getProperty("type").toString());
        tempNodeInfo.setId(tempActivity.getId());
        String assignee = tempActivity.getProperty("activiti:assignee") + "";
        String candidateUsers = tempActivity.getProperty("activiti:candidateUsers") + "";
        if (ifMultiInstance(tempActivity)) {//会签任务
            tempNodeInfo.setUiType("checkbox");
            tempNodeInfo.setFlowTaskType("countersign");
        } else if (!StringUtil.isEmpty(assignee)) {//普通任务
            tempNodeInfo.setUiType("radiobox");
            tempNodeInfo.setFlowTaskType("common");
        } else if (StringUtil.isEmpty(candidateUsers)) {//单签任务
            tempNodeInfo.setFlowTaskType("singleSign");
            tempNodeInfo.setUiType("checkbox");
        } else {
            throw new RuntimeException("流程任务节点配置有错误");
        }
        return tempNodeInfo;
    }


    /**
     * 注入符合条件的下一步节点
     *
     * @param currActivity
     * @param v
     * @param results
     * @throws NoSuchMethodException
     * @throws SecurityException
     */
    private void checkFuHeConditon(PvmActivity currActivity, Map<String, Object> v, List<PvmActivity> results)
            throws NoSuchMethodException, SecurityException {
        boolean result = false;
        List<PvmTransition> nextTransitionList = currActivity.getOutgoingTransitions();

        if (nextTransitionList != null && !nextTransitionList.isEmpty()) {
            for (PvmTransition pv : nextTransitionList) {
                PvmActivity currTempActivity = pv.getDestination();
                String nextActivtityType = currTempActivity.getProperty("type").toString();
                // if
                // ("exclusiveGateway".equalsIgnoreCase(nextActivtityType)){//排他网关
                //
                // } else
                // if("inclusiveGateway".equalsIgnoreCase(nextActivtityType)){
                // //包容网关
                //
                // } else
                if ("parallelGateWay".equalsIgnoreCase(nextActivtityType)) { // 并行网关
                    throw new RuntimeException("存在并行网关非法检查条件表达式异常！");
                }
                Boolean ifGateWay = ifGageway(currTempActivity);// 这里改一下，只要排他网关与包容网关，并行网关自动忽略条件表达式
                if (ifGateWay) {// 一个节点的出口，暂时只允许拥有一个网关节点
                    results.clear();
                    checkFuHeConditon(currTempActivity, v, results);
                    break;
                }

                String conditionText = (String) pv.getProperty("conditionText");
//                Condition conditon = (Condition) pv.getProperty("condition");
                if (conditionText != null) {
                    if (conditionText.startsWith("#{")) {// #{开头代表自定义的groovy表达式
                        String conditonFinal = conditionText.substring(conditionText.indexOf("#{") + 2,
                                conditionText.lastIndexOf("}"));
                        if (ConditionUtil.groovyTest(conditonFinal, v)) {
                            results.add(currTempActivity);
                        }
                    } else {//其他的用UEL表达式验证
                        Object tempResult = ConditionUtil.uelResult(conditionText, v);
                        if (tempResult instanceof Boolean) {
                            Boolean resultB = (Boolean) tempResult;
                            if (resultB == true) {
                                results.add(currTempActivity);
                            }
                        }
                    }
                }
            }
        }

    }

    /**
     * 选择符合条件的节点
     *
     * @param actTaskId 流程引擎实际任务ID
     * @param v
     * @return
     * @throws SecurityException
     * @throws NoSuchMethodException
     */
    private List<NodeInfo> selectQualifiedNode(String actTaskId, Map<String, Object> v,List<String> includeNodeIds)
            throws NoSuchMethodException, SecurityException {
        List<NodeInfo> qualifiedNode = new ArrayList<NodeInfo>();
        PvmActivity currActivity = this.getActivitNode(actTaskId);
        List<PvmActivity> results = new ArrayList<PvmActivity>();
        checkFuHeConditon(currActivity, v, results);
        // 前端需要的数据
        if (!results.isEmpty()) {
            for (PvmActivity tempActivity : results) {
                NodeInfo tempNodeInfo = new NodeInfo();
                if(includeNodeIds != null){
                    if(!includeNodeIds.contains(tempActivity.getId())){
                        continue;
                    }
                }
                tempNodeInfo = convertNodes(tempNodeInfo, tempActivity);
                qualifiedNode.add(tempNodeInfo);
            }
        }
        return qualifiedNode;
    }

    public ApprovalHeaderVO getApprovalHeaderVO(String id){
        FlowTask flowTask = flowTaskDao.findOne(id);
        String preId = flowTask.getPreId();
        FlowHistory preFlowTask = null;
        ApprovalHeaderVO result= new ApprovalHeaderVO();
        result.setBusinessId(flowTask.getFlowInstance().getBusinessId());
        result.setCreateUser(flowTask.getFlowInstance().getCreatedBy());
        result.setCreateTime(flowTask.getFlowInstance().getCreatedDate());
        if(!StringUtils.isEmpty(preId)){
             preFlowTask = flowHistoryDao.findOne(flowTask.getPreId());//上一个任务id
        }
        if(preFlowTask == null){//如果没有上一步任务信息,默认上一步为开始节点
            result.setPrUser(flowTask.getFlowInstance().getCreatedBy());
            result.setPreCreateTime(flowTask.getFlowInstance().getCreatedDate());
            result.setPrOpinion("流程启动");
        }else{
            result.setPrUser(preFlowTask.getExecutorAccount()+"["+preFlowTask.getExecutorName()+"]");
            result.setPreCreateTime(preFlowTask.getCreatedDate());
            result.setPrOpinion(preFlowTask.getDepict());
        }
        return result;
    }

    public List<NodeInfo> findNexNodesWithUserSet(String id) throws NoSuchMethodException{
        FlowTask flowTask = flowTaskDao.findOne(id);
        String businessId = flowTask.getFlowInstance().getBusinessId();
        return this.findNexNodesWithUserSet(id,businessId);
    }

    public List<NodeInfo> findNexNodesWithUserSet(String id,List<String> includeNodeIds) throws NoSuchMethodException{
        FlowTask flowTask = flowTaskDao.findOne(id);
        String businessId = flowTask.getFlowInstance().getBusinessId();
        List<NodeInfo> result = this.findNexNodesWithUserSet(id,businessId,includeNodeIds);
        return result;
    }
}
