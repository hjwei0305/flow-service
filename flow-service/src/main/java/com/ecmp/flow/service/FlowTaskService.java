package com.ecmp.flow.service;
import com.ecmp.core.service.BaseService;
import com.ecmp.flow.api.IFlowTaskService;
import com.ecmp.flow.com.ecmp.flow.util.Constants;
import com.ecmp.flow.dao.FlowHistoryDao;
import com.ecmp.flow.dao.FlowTaskDao;
import com.ecmp.flow.entity.FlowHistory;
import com.ecmp.flow.entity.FlowTask;
import com.ecmp.vo.OperateResult;
import org.activiti.engine.task.Task;
import org.activiti.engine.*;
import org.activiti.engine.history.HistoricTaskInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
        flowTask.setTaskStatus(Constants.TASK_STATUS_CLAIM);
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
            flowHistory.setActTaskKey(historicTaskInstance.getTaskDefinitionKey());

            flowHistoryDao.save(flowHistory);
            flowTaskDao.delete(flowTask);
            flowTaskDao.deleteNotClaimTask(actTaskId,id);//删除其他候选用户的任务
        }
        OperateResult result =  OperateResult.OperationSuccess("core_00003");
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
     * 通过流程实例ID,查找对应用户的最近一次流程执行任务ID
     * @param processInstanceId
     * @param userId
     * @return 最近一次流程执行任务ID
     */
    public String findActivitiParentTaksId(String processInstanceId,String userId){

        List<Task> tasks = taskService.createTaskQuery().processInstanceId(processInstanceId).list();

        String taskName = "";

        if(tasks != null && tasks.size()>0){

            List<HistoricTaskInstance> htis = historyService.createHistoricTaskInstanceQuery().processInstanceId(processInstanceId).list();

            for(int i = htis.size()-1;i>0;i--){

                if(tasks.get(0).getName().equals(htis.get(i).getName()) && i != 1){//当前任务

                    continue;

                }else{

                    if(taskName!=null && !"".equals(taskName)){

                        taskName = htis.get(i).getName();

                        if(userId.equals(htis.get(i).getAssignee())){

                            return htis.get(i).getId();

                        }

                    }else{

                        if(taskName.equals(htis.get(i).getName())){

                            if(userId.equals(htis.get(i).getAssignee())){

                                return htis.get(i).getId();

                            }

                        }

                    }

                }

            }

        }

        return null;

    }
}
