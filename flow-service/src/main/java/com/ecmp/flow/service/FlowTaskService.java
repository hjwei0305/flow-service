package com.ecmp.flow.service;
import com.ecmp.context.ContextUtil;
import com.ecmp.core.service.BaseService;
import com.ecmp.flow.api.IFlowTaskService;
import com.ecmp.flow.util.TaskStatus;
import com.ecmp.flow.dao.FlowHistoryDao;
import com.ecmp.flow.dao.FlowTaskDao;
import com.ecmp.flow.entity.FlowHistory;
import com.ecmp.flow.entity.FlowTask;
import com.ecmp.vo.OperateResult;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.impl.RepositoryServiceImpl;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.pvm.PvmActivity;
import org.activiti.engine.impl.pvm.PvmTransition;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.impl.pvm.process.ProcessDefinitionImpl;
import org.activiti.engine.impl.pvm.process.TransitionImpl;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.IdentityLink;
import org.activiti.engine.task.Task;
import org.activiti.engine.*;
import org.activiti.engine.history.HistoricTaskInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
    private FlowHistoryDao flowHistoryDao;

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
        HistoricTaskInstance historicTaskInstance=historyService.createHistoricTaskInstanceQuery().taskId(actTaskId).singleResult(); // 创建历史任务实例查询

        if(historicTaskInstance!=null){
            FlowHistory flowHistory = new FlowHistory();
            flowHistory.setActType(flowTask.getActType());
            flowHistory.setFlowName(flowTask.getFlowName());
            flowHistory.setDepict(flowTask.getDepict());
            flowHistory.setActClaimTime(flowTask.getActClaimTime());
            flowHistory.setFlowTaskName(flowTask.getTaskName());
            flowHistory.setFlowDefId(flowTask.getFlowDefinitionId());
            flowHistory.setFlowInstanceId(flowTask.getFlowInstanceId());
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

            flowHistoryDao.save(flowHistory);
            flowTaskDao.delete(flowTask);
            flowTaskDao.deleteNotClaimTask(actTaskId,id);//删除其他候选用户的任务
        }
        OperateResult result =  OperateResult.OperationSuccess("core_00003");
        return result;
    }

    /**
     * 撤回到指定任务节点
     * @param id
     * @return
     */
    public  OperateResult rollBackTo(String id){
        OperateResult result =  OperateResult.OperationSuccess("core_00003");
        FlowTask flowTask =  flowTaskDao.findOne(id);
        result = this.taskRollBack(flowTask.getActTaskId());
        if(result.successful()){
            flowTask.setTaskStatus(TaskStatus.CANCLE.toString());
            flowTaskDao.save(flowTask);
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



    /**
     * 任务撤销、回退
     * @param taskId 要回退的任务ID
     * @return
     */
    private OperateResult taskRollBack(String taskId){
        OperateResult result =  OperateResult.OperationSuccess("core_00003");
        try {
            Map<String, Object> variables;
            // 取得当前任务
            HistoricTaskInstance currTask = historyService
                    .createHistoricTaskInstanceQuery().taskId(taskId)
                    .singleResult();
            // 取得流程实例
            ProcessInstance instance = runtimeService
                    .createProcessInstanceQuery()
                    .processInstanceId(currTask.getProcessInstanceId())
                    .singleResult();
            if (instance == null) {
                return  result.fail("10002");//流程实例不存在或者已经结束
            }
            variables=instance.getProcessVariables();
            // 取得流程定义
            ProcessDefinitionEntity definition = (ProcessDefinitionEntity) ((RepositoryServiceImpl) repositoryService)
                    .getDeployedProcessDefinition(currTask
                            .getProcessDefinitionId());
            if (definition == null) {
                logger.error(ContextUtil.getMessage("10003"));
                return  result.fail("10003");//流程定义未找到
            }
            // 取得回退目标的活动定义节点
            ActivityImpl currActivity = ((ProcessDefinitionImpl) definition)
                    .findActivity(currTask.getTaskDefinitionKey());
            if(ifMultiInstance(currActivity)){
                 return  result.fail("10006");//当前是会签任务，不允许撤回
            }
            //通过回调函数，实现出口活动定义节点的遍历执行
            result = activitiCallBack( currActivity, instance,definition,currTask);
            if(result.notSuccessful()){
                return result;
            }
            //清除节点历史记录
            historyService.deleteHistoricActivityInstancesByTaskId(currTask.getId());
            //清除任务历史记录
            historyService.deleteHistoricTaskInstance(currTask.getId());
//	            	historyService.deleteHistoricIdentityLinksByTaskId(currTask.getId());

            //初始化本地对象

//            flowTaskDao.deleteByActTaskId(currTask.getId());
            initTask(instance,currTask.getTaskDefinitionKey());

            return result;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e.getMessage());
            return result.fail("10004");//流程取回失败，未知错误
        }
    }

    private OperateResult activitiCallBack(PvmActivity currActivity, ProcessInstance instance, ProcessDefinitionEntity definition, HistoricTaskInstance destnetionTask  ){
        List<PvmTransition> nextTransitionList = currActivity
                .getOutgoingTransitions();
        OperateResult result =  OperateResult.OperationSuccess("core_00003");
        for (PvmTransition nextTransition : nextTransitionList) {
            PvmActivity nextActivity = nextTransition.getDestination();

            Boolean ifGateWay=this.ifGageway(nextActivity);
            if(ifGateWay){//如果是网关节点，直接寻找网关出口的可执行节点
                 result = activitiCallBack( nextActivity, instance,definition,destnetionTask);
//                if(result.notSuccessful()){
                return result;
//                }
            }

            List<HistoricTaskInstance> completeTasks = historyService
                    .createHistoricTaskInstanceQuery()
                    .processInstanceId(instance.getId())
                    .taskDefinitionKey(nextActivity.getId()).finished()
                    .list();
            for(HistoricTaskInstance h:completeTasks){ //校验关联的出口节点任务执行情况，如果没有，则可回退
                if(h.getEndTime().after(destnetionTask.getEndTime())){
                     result =  OperateResult.OperationFailure("10005");//下一任务正在执行或者已经执行完成，退回失败
                }
            }
            Map<String, Object> variables;
            variables=instance.getProcessVariables();
            List<Task> nextTasks = taskService.createTaskQuery().processInstanceId(instance.getId())
                    .taskDefinitionKey(nextActivity.getId()).list();
            for (Task nextTask : nextTasks) {
                //取活动，清除活动方向
                List<PvmTransition> oriPvmTransitionList = new ArrayList<PvmTransition>();
                List<PvmTransition> pvmTransitionList = nextActivity
                        .getOutgoingTransitions();
                for (PvmTransition pvmTransition : pvmTransitionList) {
                    oriPvmTransitionList.add(pvmTransition);
                }
                pvmTransitionList.clear();
                //建立新方向
                ActivityImpl nextActivityImpl = ((ProcessDefinitionImpl) definition)
                        .findActivity(nextTask.getTaskDefinitionKey());
                TransitionImpl newTransition = nextActivityImpl
                        .createOutgoingTransition();
                // 取得转向的目标，这里需要指定用需要回退到的任务节点
                ActivityImpl destination = ((ProcessDefinitionImpl) definition)
                        .findActivity(destnetionTask.getTaskDefinitionKey());

//                 newTransition.setDestination((ActivityImpl) currActivity);
                newTransition.setDestination(destination);
                //完成任务
                taskService.complete(nextTask.getId(), variables);
                historyService.deleteHistoricActivityInstancesByTaskId(nextTask.getId());

                historyService.deleteHistoricTaskInstance(nextTask.getId());

                if(ifGageway(currActivity)){
                    HistoricActivityInstance gateWayActivity = historyService.createHistoricActivityInstanceQuery().processInstanceId(destnetionTask.getProcessInstanceId()).activityId(currActivity.getId()).singleResult();
                    if(gateWayActivity!=null){
                        historyService.deleteHistoricActivityInstanceById(gateWayActivity.getId());
                    }
                }

//                 historyService.deleteHistoricIdentityLinksByTaskId(nextTask.getId());
                //恢复方向
                destination.getIncomingTransitions().remove(newTransition);
                List<PvmTransition> pvmTList = nextActivity
                        .getOutgoingTransitions();
                pvmTList.clear();
                for (PvmTransition pvmTransition : oriPvmTransitionList) {
                    pvmTransitionList.add(pvmTransition);
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
    private void initTask(ProcessInstance instance,String actTaskDefKey){
        List<Task> tasks = new ArrayList<Task>();
        // 根据当流程实例查询任务
        List<Task> taskList = taskService.createTaskQuery().processInstanceId(instance.getId()).taskDefinitionKey(actTaskDefKey).active().list();
        if(taskList!=null && taskList.size()>0){
            for(Task task:taskList){
                if(task.getAssignee()!=null && !"".equals(task.getAssignee())){
                    List<IdentityLink> identityLinks = taskService.getIdentityLinksForTask(task.getId());
                    for(IdentityLink identityLink:identityLinks){
                        FlowTask  flowTask = new FlowTask();
                        flowTask.setFlowName(task.getName());
                        flowTask.setActTaskId(task.getId());
                        flowTask.setOwnerAccount(task.getOwner());
                        flowTask.setPriority(task.getPriority());
                        flowTask.setExecutorAccount(identityLink.getUserId());
                        flowTask.setActType(identityLink.getType());
                        flowTask.setDepict(task.getDescription());
                        flowTask.setTaskStatus(TaskStatus.INIT.toString());
                        flowTaskDao.save(flowTask);
                    }
                }else{
                    FlowTask  flowTask = new FlowTask();
                    flowTask.setFlowName(task.getName());
                    flowTask.setActTaskId(task.getId());
                    flowTask.setOwnerAccount(task.getOwner());
                    flowTask.setPriority(task.getPriority());
                    flowTask.setExecutorAccount(task.getAssignee());
                    flowTask.setDepict(task.getDescription());
                    flowTask.setActType("assignee");
                    flowTask.setTaskStatus(TaskStatus.INIT.toString());
                    flowTaskDao.save(flowTask);
                }
//                flowTask.setCandidateAccount(instance.get);

            }
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
