package com.ecmp.flow.service;

import com.ecmp.annotation.AppModule;
import com.ecmp.basic.api.IEmployeeService;
import com.ecmp.basic.api.IPositionService;
import com.ecmp.basic.entity.Employee;
import com.ecmp.basic.entity.vo.Executor;
import com.ecmp.config.util.ApiClient;
import com.ecmp.config.util.GlobalParam;
import com.ecmp.context.ContextUtil;
import com.ecmp.core.dao.BaseEntityDao;
import com.ecmp.core.dao.jpa.BaseDao;
import com.ecmp.core.search.PageResult;
import com.ecmp.core.search.Search;
import com.ecmp.core.service.BaseEntityService;
import com.ecmp.core.service.BaseService;
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
import com.ecmp.flow.vo.bpmn.*;
import com.ecmp.flow.vo.bpmn.UserTask;
import com.ecmp.vo.OperateResult;
import com.ecmp.vo.OperateResultWithData;
import jodd.util.StringUtil;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.activiti.bpmn.model.*;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.history.*;
import org.activiti.engine.impl.Condition;
import org.activiti.engine.impl.RepositoryServiceImpl;
import org.activiti.engine.impl.persistence.entity.*;
import org.activiti.engine.impl.pvm.PvmActivity;
import org.activiti.engine.impl.pvm.PvmTransition;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.impl.pvm.process.ProcessDefinitionImpl;
import org.activiti.engine.impl.pvm.process.TransitionImpl;
import org.activiti.engine.impl.task.TaskDefinition;
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
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import javax.ws.rs.PathParam;
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
        OperateResult result = OperateResult.OperationSuccess("10012");
        return result;
    }

    @Transactional
    public OperateResultWithData<FlowStatus> complete(FlowTaskCompleteVO flowTaskCompleteVO) {
        String taskId = flowTaskCompleteVO.getTaskId();
        Map<String, Object> variables = flowTaskCompleteVO.getVariables();
        List<String> manualSelectedNodeIds = flowTaskCompleteVO.getManualSelectedNodeIds();
        OperateResultWithData<FlowStatus> result = null;
        if (manualSelectedNodeIds == null || manualSelectedNodeIds.isEmpty()) {//非人工选择任务的情况
            result = this.complete(taskId, flowTaskCompleteVO.getOpinion(), variables);
        } else {//人工选择任务的情况
            FlowTask flowTask = flowTaskDao.findOne(taskId);
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
            List<PvmTransition> oriPvmTransitionList = new ArrayList<PvmTransition>();
            List<PvmTransition> pvmTransitionList = currActivity.getOutgoingTransitions();
            for (PvmTransition pvmTransition : pvmTransitionList) {
                oriPvmTransitionList.add(pvmTransition);
            }
            pvmTransitionList.clear();

            // 定位到人工选择的节点目标
            List<TransitionImpl> newTransitions = new ArrayList<TransitionImpl>();
            for (String nodeId : manualSelectedNodeIds) {
                // 建立新方向
                TransitionImpl newTransition = currActivity.createOutgoingTransition();
                ActivityImpl destinationActivity = ((ProcessDefinitionImpl) definition).findActivity(nodeId);
                newTransition.setDestination(destinationActivity);
                newTransitions.add(newTransition);
            }

            //执行任务
            result = this.complete(taskId, flowTaskCompleteVO.getOpinion(), variables);

            // 恢复方向
            int index =0;
            for (String nodeId : manualSelectedNodeIds) {
                ActivityImpl destinationActivity = ((ProcessDefinitionImpl) definition).findActivity(nodeId);
                destinationActivity.getIncomingTransitions().remove(newTransitions.get(index++));
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
     * @param opinion   审批意见
     * @param variables 参数
     * @return
     */
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
        if("CounterSign".equalsIgnoreCase(nodeType)){//会签任务做处理判断
            // 取得当前任务
            HistoricTaskInstance currTask = historyService
                    .createHistoricTaskInstanceQuery().taskId(flowTask.getActTaskId())
                    .singleResult();
            String executionId = currTask.getExecutionId();

            Map<String, VariableInstance>      processVariables= runtimeService.getVariableInstances(executionId);

           //通过票数
            Integer counterSignAgree = 0;
            if(processVariables.get("counterSign_agree")!=null){
                counterSignAgree =  (Integer)processVariables.get("counterSign_agree").getValue();
            }

            //完成会签的次数
         //   Integer completeCounter=(Integer)processVariables.get("nrOfCompletedInstances").getValue();
            //总循环次数
            Integer instanceOfNumbers=(Integer)processVariables.get("nrOfInstances").getValue();
            //当前处于激活状态的任务实例
//            Integer nrOfActiveInstances=(Integer)processVariables.get("nrOfActiveInstances").getValue();
            Integer nrOfActiveInstances = flowTaskDao.findCountByActTaskDefKeyAndActInstanceId(flowTask.getActTaskDefKey(),flowInstance.getActInstanceId());
            if(nrOfActiveInstances==1){//会签最后一个任务
//              Boolean  approveResult = null;
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
                // 取得当前活动定义节点
                ActivityImpl currActivity = ((ProcessDefinitionImpl) definition)
                        .findActivity(currTask.getTaskDefinitionKey());

                PvmActivity destinationActivity = null;
                if(counterDecision<=((counterSignAgree/instanceOfNumbers)*100)){//获取通过节点
                    List<PvmTransition> nextTransitionList = currActivity.getOutgoingTransitions();
                    if (nextTransitionList != null && !nextTransitionList.isEmpty()) {
                        for (PvmTransition pv : nextTransitionList) {
                            String conditionText = (String) pv.getProperty("conditionText");
                            if("${approveResult == true}".equalsIgnoreCase(conditionText)){
                                destinationActivity = pv.getDestination();
                                 break;
                            }
                        }
                    }
                }
                else{
                    List<PvmTransition> nextTransitionList = currActivity.getOutgoingTransitions();
                    if (nextTransitionList != null && !nextTransitionList.isEmpty()) {
                        for (PvmTransition pv : nextTransitionList) {
                            String conditionText = (String) pv.getProperty("conditionText");
                            if("${approveResult == false}".equalsIgnoreCase(conditionText)){
                                destinationActivity = pv.getDestination();
                                break;
                            }
                        }
                    }
//                    approveResult=false;
                }
//                variables.put("approveResult",approveResult);
                List<PvmTransition> oriPvmTransitionList = new ArrayList<PvmTransition>();
                List<PvmTransition> pvmTransitionList = currActivity.getOutgoingTransitions();
                for (PvmTransition pvmTransition : pvmTransitionList) {
                    oriPvmTransitionList.add(pvmTransition);
                }
                pvmTransitionList.clear();

                // 定位到人工选择的节点目标
                TransitionImpl newTransition = currActivity.createOutgoingTransition();
                newTransition.setDestination((ActivityImpl)destinationActivity);
                //执行任务
                this.completeActiviti(actTaskId, variables);
                // 恢复方向
                destinationActivity.getIncomingTransitions().remove(newTransition);
                List<PvmTransition> pvmTList = currActivity.getOutgoingTransitions();
                pvmTList.clear();
                for (PvmTransition pvmTransition : oriPvmTransitionList) {
                    pvmTransitionList.add(pvmTransition);
                }
            }
//            else {
//                try {
//                    lock.lock(); // 加锁
//                    this.completeActiviti(actTaskId, variables);
//                } finally {
//                    lock.unlock(); // 释放锁
//                }
//            }

        }else {
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
//            flowHistory.setBusinessModelRemark();
            flowHistory.setCanCancel(canCancel);
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
                Long actDurationInMillis = flowHistory.getActStartTime().getTime()-flowHistory.getActEndTime().getTime();
                flowHistory.setActDurationInMillis(actDurationInMillis);
            }
//            if (reject != null && reject == 1) {
//                flowHistory.setDepict("【被驳回】"+flowHistory.getDepict());
//                flowHistory.setTaskStatus(TaskStatus.REJECT.toString());
//            } else {
//                flowHistory.setTaskStatus(TaskStatus.COMPLETED.toString());
//            }
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
            if (instance != null && currentNode != null && (!"endEvent".equalsIgnoreCase(currentNode.getProperty("type") + ""))) {

                callInitTaskBack(currentNode, instance, flowHistory);
            }

        }

        OperateResultWithData<FlowStatus> result = OperateResultWithData.OperationSuccess("10017");
        if (instance == null || instance.isEnded()) {
            result.setData(FlowStatus.COMPLETED);//任务结束
            flowInstance.setEnded(true);
            flowInstance.setEndDate(new Date());
            flowInstanceDao.save(flowTask.getFlowInstance());
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
     * 撤回到指定任务节点,加撤销意见
     *
     * @param id
     * @return
     */
    public OperateResult rollBackTo(String id, String opinion) throws CloneNotSupportedException {
        OperateResult result = OperateResult.OperationSuccess("core_00003");
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
    @Transactional(propagation = Propagation.REQUIRES_NEW)
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
    public OperateResult taskReject(String id, String opinion, Map<String, Object> variables) {
        OperateResult result = OperateResult.OperationSuccess("10006");
        FlowTask flowTask = flowTaskDao.findOne(id);
        if (flowTask == null) {
            return OperateResult.OperationFailure("10009");
        }
        flowTask.setDepict(opinion);
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
        OperateResult result = OperateResult.OperationSuccess("10015");
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
            result = OperateResult.OperationFailure("10016");
        }
        return result;
    }


    /**
     * 回退任务
     *
     * @return
     */
    private OperateResult taskRollBack(FlowHistory flowHistory, String opinion) {
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
            if (execution == null) {
                return OperateResult.OperationFailure("10014");//当前任务不允许撤回
            }
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
//            newTask.setDescription("【被撤销】"+opinion);
            // newTnewTaskask.setExecution((DelegateExecution) execution);
            // newTask.setProcessInstance(instance);
            // newTask.setTaskDefinition(currActivity.gett);
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
        if (instance.isEnded()) {//流程结束
            flowInstance.setEnded(true);
            flowInstance.setEndDate(new Date());
            flowInstanceDao.save(flowInstance);
            return;
        }
        List<Task> tasks = new ArrayList<Task>();
        // 根据当流程实例查询任务
        List<Task> taskList = taskService.createTaskQuery().processInstanceId(instance.getId()).taskDefinitionKey(actTaskDefKey).active().list();
////        List<Task> unsignedTasks = taskService.createTaskQuery().taskCandidateUser(assignee).active().list();
//        List<Task> testTask = taskService.createTaskQuery().processInstanceId(instance.getId()).taskDefinitionKey(actTaskDefKey).list();
//System.out.println(testTask);
//        List<Task> unsignedTasks = taskService.createTaskQuery().processInstanceId(instance.getId()).taskCandidateUser(ContextUtil.getUserId()).active().list();
//        System.out.println(unsignedTasks);
//        HistoricTaskInstance unsignedTasks2 =  historyService
//                .createHistoricTaskInstanceQuery().processInstanceId(instance.getId()).taskDefinitionKey(actTaskDefKey).unfinished().singleResult();
//        System.out.println(unsignedTasks2);
//        // 合并
////        list.addAll(unsignedTasks);
        String flowName = null;
        String flowDefJson = flowInstance.getFlowDefVersion().getDefJson();
        JSONObject defObj = JSONObject.fromObject(flowDefJson);
        Definition definition = (Definition) JSONObject.toBean(defObj, Definition.class);
        net.sf.json.JSONObject currentNode = definition.getProcess().getNodes().getJSONObject(actTaskDefKey);
        net.sf.json.JSONObject normalInfo = currentNode.getJSONObject("nodeConfig").getJSONObject("normal");
        Integer executeTime = (Integer)normalInfo.get("executeTime");
        Boolean canReject = (Boolean)normalInfo.get("allowReject");
        Boolean canSuspension =(Boolean) normalInfo.get("allowTerminate");
        flowName = definition.getProcess().getName();
        if (taskList != null && taskList.size() > 0) {
            for (Task task : taskList) {
//                if (task.getAssignee() != null && !"".equals(task.getAssignee())) {
                List<IdentityLink> identityLinks = taskService.getIdentityLinksForTask(task.getId());
                if(identityLinks==null || identityLinks.isEmpty()){//多实例任务为null
                    /** 获取流程变量 **/
                    String executionId = task.getExecutionId();
                    // Map<String, Object> tempV = task.getProcessVariables();
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
//                                flowTask.setExecutorAccount(identityLink.getUserId());
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
                    // runtimeService.getVariables(executionId);使用执行对象Id，获取所有的流程变量，返回Map集合
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
//                                String oldPreId = preTask.getPreId();//前一个任务的前一个任务ID
//                                if(!StringUtils.isEmpty(oldPreId)){
//                                    FlowHistory oldPreFlowHistory = flowHistoryDao.findOne(oldPreId);
//                                    if (oldPreFlowHistory != null) {
//                                        flowTask.setPreId(oldPreFlowHistory.getId());
//                                    } else {
//                                        flowTask.setPreId(null);
//                                    }
//                                }
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
//                flowTask.setCandidateAccount(instance.get);

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
    public List<NodeInfo> findNexNodesWithUserSet(String id, String businessId,String approved) throws NoSuchMethodException {
        return this.findNexNodesWithUserSet(id, businessId, approved,null);
    }

    /**
     * 获取出口节点信息（带初始化流程设计器配置用户）
     *
     * @param id
     * @param businessId
     * @return
     * @throws NoSuchMethodException
     */
    public List<NodeInfo> findNexNodesWithUserSet(String id, String businessId,String approved, List<String> includeNodeIds) throws NoSuchMethodException {
        List<NodeInfo> nodeInfoList = this.findNextNodesWithCondition(id, businessId,approved, includeNodeIds);

        if (nodeInfoList != null && !nodeInfoList.isEmpty()) {
            FlowTask flowTask = flowTaskDao.findOne(id);
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
                }
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
                } else if ("CounterSign".equalsIgnoreCase(userTaskTemp.getNodeType())) {
                    nodeInfo.setUserVarName(userTaskTemp.getId() + "_List_CounterSign");
//                    MultiInstanceConfig multiInstanceConfig = new MultiInstanceConfig();
//                    multiInstanceConfig.setUserIds("${"+userTaskTemp.getId()+"_List_CounterSign}");
//                    multiInstanceConfig.setVariable("${"+userTaskTemp.getId()+"_CounterSign}");
                }

                if (executor != null) {
                    String userType = (String) executor.get("userType");
                    String ids = (String) executor.get("ids");
                    Set<Executor> employeeSet = new HashSet<Executor>();
                    List<Executor> employees = null;
                    nodeInfo.setUiUserType(userType);
                    if ("StartUser".equalsIgnoreCase(userType)) {//获取流程实例启动者
                        ProcessInstance instance = runtimeService.createProcessInstanceQuery()
                                .processInstanceId(flowTask.getFlowInstance().getActInstanceId()).singleResult();
                        HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery().processInstanceId(flowTask.getFlowInstance().getActInstanceId()).singleResult();
//                            Map<String,Object> v = instance.getProcessVariables();
                        String startUserId = historicProcessInstance.getStartUserId();
                        IEmployeeService iEmployeeService = ApiClient.createProxy(IEmployeeService.class);
                        employees = iEmployeeService.getExecutorsByEmployeeIds(java.util.Arrays.asList(startUserId));
//                            if(v != null){
//                                startUserId = (String) v.get("startUserId");
//                            }
//                            if(StringUtils.isEmpty(startUserId)){
//                                startUserId = flowTask.getFlowInstance().getCreatedBy();
//                            }

                    } else {
                        String selfDefId = (String)executor.get("selfDefId");
                        if (StringUtils.isNotEmpty(ids)||StringUtils.isNotEmpty(selfDefId)) {
                             if ("SelfDefinition".equalsIgnoreCase(userType)) {//通过业务ID获取自定义用户
//                                IEmployeeService iEmployeeService = ApiClient.createProxy(IEmployeeService.class);
//                                employees = iEmployeeService.getExecutorsByEmployeeIds(idList);

                                FlowExecutorConfig flowExecutorConfig = flowExecutorConfigDao.findOne(selfDefId);
                                String path = flowExecutorConfig.getUrl();
                                String appModuleId =  flowExecutorConfig.getBusinessModel().getAppModuleId();
                                com.ecmp.basic.api.IAppModuleService proxy = ApiClient.createProxy(com.ecmp.basic.api.IAppModuleService.class);
                                com.ecmp.basic.entity.AppModule appModule = proxy.findOne(appModuleId);
                                String clientApiBaseUrl = ContextUtil.getAppModule(appModule.getCode()).getApiBaseAddress();
                                String appModuleCode = appModule.getCode();
                                Map<String, String>  params = new HashMap<String,String>();;
                                String param = flowExecutorConfig.getParam();
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
    public List<NodeInfo> findNextNodesWithCondition(String id, String businessId,String approved, List<String> includeNodeIds) throws NoSuchMethodException {
        FlowTask flowTask = flowTaskDao.findOne(id);
        String actTaskId = flowTask.getActTaskId();
        PvmActivity currActivity = this.getActivitNode(actTaskId);

        BusinessModel businessModel = flowTask.getFlowInstance().getFlowDefVersion().getFlowDefination().getFlowType().getBusinessModel();
        String businessModelId = businessModel.getId();
        String appModuleId = businessModel.getAppModuleId();
        com.ecmp.basic.api.IAppModuleService proxy = ApiClient.createProxy(com.ecmp.basic.api.IAppModuleService.class);
        com.ecmp.basic.entity.AppModule appModule = proxy.findOne(appModuleId);
        String clientApiBaseUrl = ContextUtil.getAppModule(appModule.getCode()).getApiBaseAddress();
        Map<String, Object> v = ExpressionUtil.getConditonPojoValueMap(clientApiBaseUrl, businessModelId, businessId);
//        if(ifMultiInstance(currActivity)){//如果是多实例任务
            String defJson = flowTask.getTaskJsonDef();
            JSONObject defObj = JSONObject.fromObject(defJson);
            String nodeType = defObj.get("nodeType") + "";
            if("CounterSign".equalsIgnoreCase(nodeType)){//会签任务
                List<NodeInfo> result = new ArrayList<NodeInfo>();
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

                Map<String, VariableInstance>      processVariables= runtimeService.getVariableInstances(executionId);
//                //完成会签的次数
//                Integer completeCounter=(Integer)processVariables.get("nrOfCompletedInstances").getValue();
                //总循环次数
                Integer instanceOfNumbers=(Integer)processVariables.get("nrOfInstances").getValue();
                //当前处于激活状态的任务实例
                Integer nrOfActiveInstances = flowTaskDao.findCountByActTaskDefKeyAndActInstanceId(flowTask.getActTaskDefKey(),flowTask.getFlowInstance().getActInstanceId());

                if(nrOfActiveInstances==1){//会签最后一个执行人
                    Boolean  approveResult = null;
                    Integer counterSignAgree = (Integer) processVariables.get("counterSign_agree").getValue();
                    if(counterSignAgree==null) {
                        counterSignAgree = 0;
                    }
                    Integer value = 0;//默认弃权
                    if("true".equalsIgnoreCase(approved)){
                        counterSignAgree++;
                    }

                    if(counterDecision<=((counterSignAgree/instanceOfNumbers)*100)){//获取通过节点
                        approveResult = true;
                        List<PvmTransition> nextTransitionList = currActivity.getOutgoingTransitions();
                        if (nextTransitionList != null && !nextTransitionList.isEmpty()) {
                            for (PvmTransition pv : nextTransitionList) {
                                String conditionText = (String) pv.getProperty("conditionText");
                                if("${approveResult == true}".equalsIgnoreCase(conditionText)){
                                    PvmActivity currTempActivity = pv.getDestination();
                                    NodeInfo tempNodeInfo = new NodeInfo();
                                     tempNodeInfo = convertNodes(flowTask, tempNodeInfo, currTempActivity);
                                    result.add(tempNodeInfo);

                                }
                                }
                        }
                    }else {//获取不通过节点
                        approveResult = false;
                        List<PvmTransition> nextTransitionList = currActivity.getOutgoingTransitions();
                        if (nextTransitionList != null && !nextTransitionList.isEmpty()) {
                            for (PvmTransition pv : nextTransitionList) {
                                String conditionText = (String) pv.getProperty("conditionText");
                                if("${approveResult == false}".equalsIgnoreCase(conditionText)){
                                    PvmActivity currTempActivity = pv.getDestination();
                                    NodeInfo tempNodeInfo = new NodeInfo();
                                    tempNodeInfo = convertNodes(flowTask, tempNodeInfo, currTempActivity);
                                    result.add(tempNodeInfo);
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
//        }
        if (this.checkSystemExclusiveGateway(flowTask)) {//判断是否存在系统排他网关、系统包容网关
            if (StringUtils.isEmpty(businessId)) {
                throw new RuntimeException("任务出口节点包含条件表达式，请指定业务ID");
            }

            List<NodeInfo> result = this.selectQualifiedNode(flowTask, currActivity, v, includeNodeIds);
            if (result == null || result.isEmpty()) {//如果找不到就选择全部
                result = this.selectNextAllNodesWithGateWay(flowTask, currActivity, v, includeNodeIds);
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
        String actTaskId = flowTask.getActTaskId();
        return this.selectNextAllNodes(flowTask, includeNodeIds);
//        this.checkManualExclusiveGateway(flowTask);
//        if (checkHasConditon(actTaskId)) {//判断出口任务节点中是否包含有条件表达式
//            if (StringUtils.isEmpty(businessId)) {
//                throw new RuntimeException("任务出口节点包含条件表达式，请指定业务ID");
//            }
////            String defJson = flowTask.getFlowInstance().getFlowDefVersion().getDefJson();
////            JSONObject defObj = JSONObject.fromObject(defJson);
//            BusinessModel businessModel = flowTask.getFlowInstance().getFlowDefVersion().getFlowDefination().getFlowType().getBusinessModel();
//            String businessModelId = businessModel.getId();
//            String appModuleId = businessModel.getAppModuleId();
//            com.ecmp.basic.api.IAppModuleService proxy = ApiClient.createProxy(com.ecmp.basic.api.IAppModuleService.class);
//            com.ecmp.basic.entity.AppModule appModule = proxy.findOne(appModuleId);
//           System.out.println( ContextUtil.getAppModule().getApiBaseAddress());
////            System.out.println(ContextUtil.getAppModule(appModule.getCode()).getApiBaseAddress());
////            String clientApiBaseUrl =  GlobalParam.environmentFormat(appModule.getApiBaseAddress());
//
//            String clientApiBaseUrl = ContextUtil.getAppModule(appModule.getCode()).getApiBaseAddress();
////            clientApiBaseUrl =    ContextUtil.getHost();
////            clientApiBaseUrl = "http://localhost:8080/";//测试地址，上线后去掉
//            Map<String, Object> v = ExpressionUtil.getConditonPojoValueMap(clientApiBaseUrl, businessModelId, businessId);
//            return this.selectQualifiedNode(actTaskId, v, includeNodeIds);
//        } else {
//            return this.selectNextAllNodes(actTaskId, includeNodeIds);
//        }
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

        Map<PvmActivity, String> nextNodes = new LinkedHashMap<PvmActivity, String>();
        initNextNodes(currActivity, nextNodes, 0,nodeType);
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
                    if (includeNodeIds != null) {
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
                    tempNodeInfo.setPreLineName(nextNodes.get(tempActivity));
                    nodeInfoList.add(tempNodeInfo);


                }
            }
            if ("Approve".equalsIgnoreCase(nodeType)) {//如果是审批结点
                uiType = "radiobox";
                for (int i = 0; i < nextNodes.size(); i++) {
                    PvmActivity tempActivity = (PvmActivity) nextNodesKeyArray[i];
                    if (includeNodeIds != null) {
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
                    tempNodeInfo.setPreLineName(nextNodes.get(tempActivity));
                    nodeInfoList.add(tempNodeInfo);
                }
            } else if ("exclusiveGateway".equalsIgnoreCase(nextActivtityType)) {// 排他网关，radiobox,有且只能选择一个
                if (this.checkManualExclusiveGateway(flowTask, firstActivity.getId())) {//如果人工网关
                    uiType = "radiobox";
                }
                if (isSizeBigTwo) {
                    for (int i = 1; i < nextNodes.size(); i++) {
                        PvmActivity tempActivity = (PvmActivity) nextNodesKeyArray[i];
                        if (includeNodeIds != null) {
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
                        tempNodeInfo.setPreLineName(nextNodes.get(tempActivity));
                        nodeInfoList.add(tempNodeInfo);
                    }
                }

            } else if ("inclusiveGateway".equalsIgnoreCase(nextActivtityType)) { // 包容网关,checkbox,至少选择一个
                if (isSizeBigTwo) {
                    for (int i = 1; i < nextNodes.size(); i++) {
                        PvmActivity tempActivity = (PvmActivity) nextNodesKeyArray[i];
                        if (includeNodeIds != null) {
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
                        tempNodeInfo.setPreLineName(nextNodes.get(tempActivity));
                        nodeInfoList.add(tempNodeInfo);
                    }
                }

            } else if ("parallelGateWay".equalsIgnoreCase(nextActivtityType)) { // 并行网关,checkbox,默认全部选中显示不能修改
                if (isSizeBigTwo) {
                    for (int i = 1; i < nextNodes.size(); i++) {
                        PvmActivity tempActivity = (PvmActivity) nextNodesKeyArray[i];
                        if (includeNodeIds != null) {
                            if (!includeNodeIds.contains(tempActivity.getId())) {
                                continue;
                            }
                        }
                        NodeInfo tempNodeInfo = new NodeInfo();
                        tempNodeInfo = convertNodes(flowTask, tempNodeInfo, tempActivity);
                        tempNodeInfo.setUiType(uiType);
                        tempNodeInfo.setPreLineName(nextNodes.get(tempActivity));
                        nodeInfoList.add(tempNodeInfo);
                    }
                }

            } else {
                if (isSizeBigTwo) {//当下步节点大于一个时，按照并行网关处理。checkbox,默认全部选中显示不能修改
                    for (int i = 0; i < nextNodes.size(); i++) {
                        PvmActivity tempActivity = (PvmActivity) nextNodesKeyArray[i];
//                        if(includeNodeIds != null){
//                            if(!includeNodeIds.contains(tempActivity.getId())){
//                                continue;
//                            }
//                        }
                        NodeInfo tempNodeInfo = new NodeInfo();
                        tempNodeInfo = convertNodes(flowTask, tempNodeInfo, tempActivity);
                        tempNodeInfo.setUiType(uiType);
                        tempNodeInfo.setPreLineName(nextNodes.get(tempActivity));
                        nodeInfoList.add(tempNodeInfo);
                    }
                } else {//按照惟一分支任务处理，显示一个，只读
                    PvmActivity tempActivity = (PvmActivity) nextNodesKeyArray[0];
                    if (includeNodeIds != null) {
                        if (!includeNodeIds.contains(tempActivity.getId())) {
                            throw new RuntimeException("惟一分支未选中");
                        }
                    }
                    NodeInfo tempNodeInfo = new NodeInfo();
                    tempNodeInfo = convertNodes(flowTask, tempNodeInfo, tempActivity);
                    tempNodeInfo.setUiType(uiType);
                    tempNodeInfo.setPreLineName(nextNodes.get(tempActivity));
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



        String actTaskId = flowTask.getActTaskId();
        PvmActivity currActivity = this.getActivitNode(actTaskId);
        //前端需要的数据出口任务数据
        List<NodeInfo> nodeInfoList = new ArrayList<NodeInfo>();
        String uiType = "readOnly";
        if("CounterSign".equalsIgnoreCase(nodeType)){//会签节点，直接返回当前会签节点信息
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
            //当前处于激活状态的任务实例
//            Integer nrOfActiveInstances=(Integer)processVariables.get("nrOfActiveInstances").getValue();
            Integer nrOfActiveInstances = flowTaskDao.findCountByActTaskDefKeyAndActInstanceId(flowTask.getActTaskDefKey(),flowTask.getFlowInstance().getActInstanceId());
            if(nrOfActiveInstances==1){//会签最后一个执行人
                tempNodeInfo.setCounterSignLastTask(true);
            }
            nodeInfoList.add(tempNodeInfo);
            return nodeInfoList;
        }
        Map<PvmActivity, String> nextNodes = new LinkedHashMap<PvmActivity, String>();
        initNextNodes(currActivity, nextNodes, 0,nodeType);
        if (!nextNodes.isEmpty()) {
            //判断网关
            Object[] nextNodesKeyArray = nextNodes.keySet().toArray();
            PvmActivity firstActivity = (PvmActivity) nextNodesKeyArray[0];
            Boolean isSizeBigTwo = nextNodes.size() > 1 ? true : false;
            String nextActivtityType = firstActivity.getProperty("type").toString();
            if ("Approve".equalsIgnoreCase(nodeType)) {//如果是审批结点
                uiType = "radiobox";
                for (int i = 0; i < nextNodes.size(); i++) {
                    PvmActivity tempActivity = (PvmActivity) nextNodesKeyArray[i];
                    boolean markInclude=false;
                    for(int j = 0; j < nextNodes.size(); j++){
                        PvmActivity tempActivityJ = (PvmActivity) nextNodesKeyArray[j];
                        if(tempActivityJ!=tempActivity){
                            List<PvmTransition>  nextPvmTransitions =   tempActivity.getIncomingTransitions();
                            if(nextPvmTransitions!=null  && !nextPvmTransitions.isEmpty()){
                                for(PvmTransition v:nextPvmTransitions){
                                    PvmTransition findResult =   tempActivityJ.findOutgoingTransition(v.getId());
                                    if(findResult!=null){
                                        markInclude = true;
                                        break;
                                    }
                                }
                                if(markInclude){
                                    break;
                                }
                            }
                        }
                    }
                    if(markInclude){
                        continue;
                    }

                }
            } else if ("exclusiveGateway".equalsIgnoreCase(nextActivtityType)) {// 排他网关，radiobox,有且只能选择一个
                if (this.checkManualExclusiveGateway(flowTask, firstActivity.getId())) {//如果人工网关
                    uiType = "radiobox";
                }
                if (isSizeBigTwo) {
                    for (int i = 1; i < nextNodes.size(); i++) {
                        PvmActivity tempActivity = (PvmActivity) nextNodesKeyArray[i];
                        if (includeNodeIds != null) {
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
                        tempNodeInfo.setPreLineName(nextNodes.get(tempActivity));
                        nodeInfoList.add(tempNodeInfo);
                    }
                }

            } else if ("inclusiveGateway".equalsIgnoreCase(nextActivtityType)) { // 包容网关,checkbox,至少选择一个
                if (isSizeBigTwo) {
                    for (int i = 1; i < nextNodes.size(); i++) {
                        PvmActivity tempActivity = (PvmActivity) nextNodesKeyArray[i];
                        if (includeNodeIds != null) {
                            if (!includeNodeIds.contains(tempActivity.getId())) {
                                continue;
                            }
                        }
                        NodeInfo tempNodeInfo = new NodeInfo();
                        tempNodeInfo.setCurrentTaskType(nodeType);
                        tempNodeInfo = convertNodes(flowTask, tempNodeInfo, tempActivity);
                        tempNodeInfo.setUiType(uiType);
                        tempNodeInfo.setPreLineName(nextNodes.get(tempActivity));
                        nodeInfoList.add(tempNodeInfo);
                    }
                }

            } else if ("parallelGateWay".equalsIgnoreCase(nextActivtityType)) { // 并行网关,checkbox,默认全部选中显示不能修改
                if (isSizeBigTwo) {
                    for (int i = 1; i < nextNodes.size(); i++) {
                        PvmActivity tempActivity = (PvmActivity) nextNodesKeyArray[i];
                        if (includeNodeIds != null) {
                            if (!includeNodeIds.contains(tempActivity.getId())) {
                                continue;
                            }
                        }
                        NodeInfo tempNodeInfo = new NodeInfo();
                        tempNodeInfo.setCurrentTaskType(nodeType);
                        tempNodeInfo = convertNodes(flowTask, tempNodeInfo, tempActivity);
                        tempNodeInfo.setUiType(uiType);
                        tempNodeInfo.setPreLineName(nextNodes.get(tempActivity));
                        nodeInfoList.add(tempNodeInfo);
                    }
                }

            } else {
                if (isSizeBigTwo) {//当下步节点大于一个时，按照并行网关处理。checkbox,默认全部选中显示不能修改
                    for (int i = 0; i < nextNodes.size(); i++) {
                        PvmActivity tempActivity = (PvmActivity) nextNodesKeyArray[i];
                        if (includeNodeIds != null) {
                            if (!includeNodeIds.contains(tempActivity.getId())) {
                                continue;
                            }
                        }
                        NodeInfo tempNodeInfo = new NodeInfo();
                        tempNodeInfo.setCurrentTaskType(nodeType);
                        tempNodeInfo = convertNodes(flowTask, tempNodeInfo, tempActivity);
                        tempNodeInfo.setUiType(uiType);
                        tempNodeInfo.setPreLineName(nextNodes.get(tempActivity));
                        nodeInfoList.add(tempNodeInfo);
                    }
                } else {//按照惟一分支任务处理，显示一个，只读
                    PvmActivity tempActivity = (PvmActivity) nextNodesKeyArray[0];
                    if (includeNodeIds != null) {
                        if (!includeNodeIds.contains(tempActivity.getId())) {
                            throw new RuntimeException("惟一分支未选中");
                        }
                    }
                    NodeInfo tempNodeInfo = new NodeInfo();
                    tempNodeInfo.setCurrentTaskType(nodeType);
                    tempNodeInfo = convertNodes(flowTask, tempNodeInfo, tempActivity);
                    tempNodeInfo.setUiType(uiType);
                    tempNodeInfo.setPreLineName(nextNodes.get(tempActivity));
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
    private void initNextNodes(PvmActivity currActivity, Map<PvmActivity, String> nextNodes, int index,String nodeType) {
        List<PvmTransition> nextTransitionList = currActivity.getOutgoingTransitions();
        if (nextTransitionList != null && !nextTransitionList.isEmpty()) {
            for (PvmTransition pv : nextTransitionList) {
                PvmActivity currTempActivity = pv.getDestination();
                String lineName = pv.getProperty("name") + "";//线的名称
                Boolean ifGateWay = ifGageway(currTempActivity);
                if (ifGateWay && index < 1) {//如果是网关，其他直绑节点自行忽略
                 //   nextNodes.clear();
                    nextNodes.put(currTempActivity, lineName);//把网关放入第一个节点
//                    if(!"Approve".equalsIgnoreCase(nodeType)){//非审批任务
                        initNextNodes(currTempActivity, nextNodes, 1,nodeType);
//                    }

                    // break;
                } else {
                    nextNodes.put(currTempActivity, lineName);
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
            tempNodeInfo.setFlowTaskType("CounterSign");
        } else if ("Normal".equalsIgnoreCase(nodeType)) {//普通任务
            tempNodeInfo.setUiType("radiobox");
            tempNodeInfo.setFlowTaskType("common");
        } else if ("SingleSign".equalsIgnoreCase(nodeType)) {//单签任务
            tempNodeInfo.setFlowTaskType("singleSign");
            tempNodeInfo.setUiType("checkbox");
        } else if ("Approve".equalsIgnoreCase(nodeType)) {//审批任务
            tempNodeInfo.setUiType("radiobox");
            tempNodeInfo.setFlowTaskType("approve");
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
                String conditionText = (String) pv.getProperty("conditionText");
                PvmActivity nextTempActivity = pv.getDestination();
                String nextActivtityType = nextTempActivity.getProperty("type").toString();
                String currentActivtityType = currActivity.getProperty("type").toString();
                Boolean ifGateWay = ifGageway(nextTempActivity);//当前节点的子节点是否为网关

                if ("ExclusiveGateway".equalsIgnoreCase(currentActivtityType) || "inclusiveGateway".equalsIgnoreCase(currentActivtityType)) {
                    if (conditionText != null) {
                        if (conditionText.startsWith("#{")) {// #{开头代表自定义的groovy表达式
                            String conditonFinal = conditionText.substring(conditionText.indexOf("#{") + 2,
                                    conditionText.lastIndexOf("}"));
                            if (ConditionUtil.groovyTest(conditonFinal, v)) {
                                if (ifGateWay) {
                                    gateWayCheckFuHeConditon(nextActivtityType, nextTempActivity, results, v);
                                } else {
                                    results.add(nextTempActivity);
                                }

                            }
                        } else {//其他的用UEL表达式验证
                            Object tempResult = ConditionUtil.uelResult(conditionText, v);
                            if (tempResult instanceof Boolean) {
                                Boolean resultB = (Boolean) tempResult;
                                if (resultB == true) {
                                    if (ifGateWay) {
                                        gateWayCheckFuHeConditon(nextActivtityType, nextTempActivity, results, v);
                                    } else {
                                        results.add(nextTempActivity);
                                    }
                                }
                            }
                        }
                    }
                } else {
                    if (ifGateWay) {
                        gateWayCheckFuHeConditon(nextActivtityType, nextTempActivity, results, v);
                    } else {
                        results.add(nextTempActivity);
                    }
                }
            }
        }

    }

    private void gateWayCheckFuHeConditon(String nextActivtityType, PvmActivity nextTempActivity, List<PvmActivity> results, Map<String, Object> v) throws NoSuchMethodException, SecurityException {
        List<PvmActivity> resultsTemp = new ArrayList<PvmActivity>();
        if ("parallelGateWay".equalsIgnoreCase(nextActivtityType)) { // 并行网关
            checkFuHeConditon(nextTempActivity, v, resultsTemp);
            results.addAll(resultsTemp);
        } else if ("exclusiveGateway".equalsIgnoreCase(nextActivtityType)) {//排他网关
            checkFuHeConditon(nextTempActivity, v, resultsTemp);
            if (resultsTemp.isEmpty()) {//如果为空，查找节点的default路径节点
                String defaultSequenceId = nextTempActivity.getProperty("default") + "";
                if (StringUtils.isNotEmpty(defaultSequenceId)) {
                    PvmTransition pvmTransition = nextTempActivity.findOutgoingTransition(defaultSequenceId);
                    if (pvmTransition != null) {
                        resultsTemp.add(pvmTransition.getDestination());
                    }
                }
            } else if (resultsTemp.size() > 1 && "ExclusiveGateway".equalsIgnoreCase(nextActivtityType)) {
                PvmActivity reTemp = resultsTemp.get(0);
                resultsTemp.clear();
                resultsTemp.add(reTemp);
            }
            results.addAll(resultsTemp);
        } else if ("inclusiveGateway".equalsIgnoreCase(nextActivtityType)) {//包容网关
            checkFuHeConditon(nextTempActivity, v, resultsTemp);
            if (resultsTemp.isEmpty()) {//如果为空，查找节点的default路径节点
                String defaultSequenceId = nextTempActivity.getProperty("default") + "";
                if (StringUtils.isNotEmpty(defaultSequenceId)) {
                    PvmTransition pvmTransition = nextTempActivity.findOutgoingTransition(defaultSequenceId);
                    if (pvmTransition != null) {
                        resultsTemp.add(pvmTransition.getDestination());
                    }
                }
            }
            results.addAll(resultsTemp);
        }
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
        String nextActivtityType = currActivity.getProperty("type").toString();
        Boolean ifGateWay = ifGageway(currActivity);
        if(ifGateWay){
            gateWayCheckFuHeConditon( nextActivtityType,currActivity, results, v);
        }else {
            checkFuHeConditon(currActivity, v, results);
        }

        // 前端需要的数据
        if (!results.isEmpty()) {
            for (PvmActivity tempActivity : results) {
                NodeInfo tempNodeInfo = new NodeInfo();
                if (includeNodeIds != null) {
                    if (!includeNodeIds.contains(tempActivity.getId())) {
                        continue;
                    }
                }
                tempNodeInfo = convertNodes(flowTask, tempNodeInfo, tempActivity);
//                tempNodeInfo.setUiType("readOnly");
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
        String businessId = flowTask.getFlowInstance().getBusinessId();
        return this.findNexNodesWithUserSet(id, businessId,null,null);
    }

    public List<NodeInfo> findNexNodesWithUserSet(String id,String approved, List<String> includeNodeIds) throws NoSuchMethodException {
        FlowTask flowTask = flowTaskDao.findOne(id);
        String businessId = flowTask.getFlowInstance().getBusinessId();
        List<NodeInfo> result = this.findNexNodesWithUserSet(id, businessId,approved, includeNodeIds);
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
