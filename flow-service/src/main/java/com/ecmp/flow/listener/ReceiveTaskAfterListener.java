package com.ecmp.flow.listener;

import com.ecmp.basic.api.IEmployeeService;
import com.ecmp.basic.entity.vo.Executor;
import com.ecmp.config.util.ApiClient;
import com.ecmp.context.ContextUtil;
import com.ecmp.flow.dao.*;
import com.ecmp.flow.entity.FlowDefVersion;
import com.ecmp.flow.entity.FlowHistory;
import com.ecmp.flow.entity.FlowInstance;
import com.ecmp.flow.entity.FlowTask;
import com.ecmp.flow.service.FlowInstanceService;
import com.ecmp.flow.service.FlowTaskService;
import com.ecmp.flow.util.ServiceCallUtil;
import com.ecmp.flow.util.TaskStatus;
import com.ecmp.flow.vo.NodeInfo;
import com.ecmp.flow.vo.bpmn.Definition;
import com.ecmp.util.JsonUtils;
import net.sf.json.JSONObject;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.task.IdentityLink;
import org.activiti.engine.task.Task;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * *************************************************************************************************
 * <p/>
 * 实现功能：
 * <p>
 * ------------------------------------------------------------------------------------------------
 * 版本          变更时间             变更人                     变更原因
 * ------------------------------------------------------------------------------------------------
 * 1.0.00      2017/8/14 9:40      谭军(tanjun)                    新建
 * <p/>
 * *************************************************************************************************
 */
@Component(value="receiveTaskAfterListener")
public class ReceiveTaskAfterListener implements org.activiti.engine.delegate.JavaDelegate {

    private final Logger logger = LoggerFactory.getLogger(ReceiveTaskAfterListener.class);


    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private FlowTaskDao flowTaskDao;

    @Autowired
    private FlowDefVersionDao flowDefVersionDao;

    @Autowired
    private FlowDefinationDao flowDefinationDao;

    @Autowired
    FlowHistoryDao  flowHistoryDao;

    @Autowired
    private FlowInstanceDao flowInstanceDao;

    @Autowired
    private FlowInstanceService flowInstanceService;

    @Override
    public void execute(DelegateExecution delegateTask) throws Exception {
            String actProcessInstanceId = delegateTask.getProcessInstanceId();
            ExecutionEntity taskEntity = (ExecutionEntity) delegateTask;
            String actTaskDefKey = delegateTask.getCurrentActivityId();
            String actProcessDefinitionId = delegateTask.getProcessDefinitionId();
            String businessId =delegateTask.getProcessBusinessKey();
            FlowDefVersion flowDefVersion = flowDefVersionDao.findByActDefId(actProcessDefinitionId);
            String flowDefJson = flowDefVersion.getDefJson();
            JSONObject defObj = JSONObject.fromObject(flowDefJson);
            Definition definition = (Definition) JSONObject.toBean(defObj, Definition.class);
            JSONObject currentNode = definition.getProcess().getNodes().getJSONObject(actTaskDefKey);
            JSONObject normal = currentNode.getJSONObject("nodeConfig").getJSONObject("normal");
            if (normal != null) {
                    String flowTaskName = (String) normal.get("name");
                    FlowTask flowTask = null;
                    FlowHistory flowHistory = new FlowHistory();
                    List<FlowTask> flowTaskList = flowTaskDao.findByActTaskDefKeyAndActInstanceId(actTaskDefKey,actProcessInstanceId);
                    if(flowTaskList!=null && !flowTaskList.isEmpty()){
                        flowTask = flowTaskList.get(0);
                        flowTaskDao.delete(flowTask);
                    }
                    if(flowTask!=null){
                        BeanUtils.copyProperties(flowTask,flowHistory);
                        flowHistory.setId(null);
                        flowHistory.setActStartTime(flowTask.getActDueDate());
                    }else{
                        flowTask = new FlowTask();
                        flowHistory.setTaskJsonDef(currentNode.toString());
                        flowHistory.setFlowName(definition.getProcess().getName());
                        flowHistory.setFlowTaskName(flowTaskName);


                        FlowInstance flowInstance = flowInstanceDao.findByActInstanceId(actProcessInstanceId);
                        flowHistory.setFlowInstance(flowInstance);
                        String ownerName = flowDefVersion.getFlowDefination().getFlowType().getBusinessModel().getName();
                        String appModuleId = flowDefVersion.getFlowDefination().getFlowType().getBusinessModel().getAppModuleId();
                        com.ecmp.basic.api.IAppModuleService proxy = ApiClient.createProxy(com.ecmp.basic.api.IAppModuleService.class);
                        com.ecmp.basic.entity.AppModule appModule = proxy.findOne(appModuleId);
                        if(appModule!=null && StringUtils.isNotEmpty(appModule.getName())){
                            ownerName = appModule.getName();
                        }
                        flowHistory.setOwnerAccount("admin");
                        flowHistory.setOwnerName(ownerName);
                        flowHistory.setExecutorAccount("admin");
                        flowHistory.setExecutorId("");
                        flowHistory.setExecutorName(ownerName);
                        flowHistory.setCandidateAccount("");
                        flowHistory.setActStartTime(new Date());

                        flowHistory.setActHistoryId(null);
                        flowHistory.setActTaskDefKey(actTaskDefKey);
                        flowHistory.setPreId(null);

                        BeanUtils.copyProperties(flowHistory,flowTask);
                        flowTask.setTaskStatus(TaskStatus.INIT.toString());
                    }
                     flowHistory.setFlowName(definition.getProcess().getName());
                     flowHistory.setFlowTaskName(flowTaskName);
                     flowHistory.setDepict("接收任务【执行完成】");
                     flowHistory.setTaskStatus(TaskStatus.COMPLETED.toString());
                     flowHistory.setActEndTime(new Date());
                     flowHistory.setFlowDefId(flowDefVersion.getFlowDefination().getId());

                    if(flowHistory.getActDurationInMillis() == null){
                        Long actDurationInMillis = flowHistory.getActEndTime().getTime()-flowHistory.getActStartTime().getTime();
                        flowHistory.setActDurationInMillis(actDurationInMillis);
                    }
                    flowHistoryDao.save(flowHistory);

//                    //选择下一步执行人，默认选择第一个，会签、串、并行选择全部
                    ApplicationContext applicationContext = ContextUtil.getApplicationContext();
                    FlowTaskService flowTaskService = (FlowTaskService)applicationContext.getBean("flowTaskService");
                    List<NodeInfo> results = flowTaskService.findNexNodesWithUserSet(flowTask);
                    List<String> nextNodeIds = new ArrayList<String>();
                    if(results !=null &&  !results.isEmpty()){
                        for(NodeInfo nodeInfo:results){
                            if ("EndEvent".equalsIgnoreCase(nodeInfo.getType())) {
                                nodeInfo.setType("EndEvent");
                                continue;
                            }
                            nextNodeIds.add(nodeInfo.getId());
                           String taskType = nodeInfo.getFlowTaskType();
                            String uiUserType = nodeInfo.getUiUserType();
                            if("AnyOne".equalsIgnoreCase(uiUserType)){//任意执行人默认规则为当前执行人
                                IEmployeeService proxy = ApiClient.createProxy(IEmployeeService.class);
                                String currentUserId = ContextUtil.getUserId();
                                List<String> usrIdList = new ArrayList<String>(1);
                                usrIdList.add(currentUserId);
                                List<Executor> employees = proxy.getExecutorsByEmployeeIds(usrIdList);
                                Set<Executor> employeeSet = new HashSet<Executor>();
                                employeeSet.addAll(employees);
                                nodeInfo.setExecutorSet(employeeSet);
                            }
                            if ("SingleSign".equalsIgnoreCase(taskType) || "CounterSign".equalsIgnoreCase(taskType)||"ParallelTask".equalsIgnoreCase(taskType)||"SerialTask".equalsIgnoreCase(taskType)) {
                                Set<Executor> executorSet = nodeInfo.getExecutorSet();
                                if(executorSet != null && !executorSet.isEmpty()){
                                    List<String> userIdArray = new ArrayList<String>();
                                    for(Executor executor:executorSet){
                                        userIdArray.add(executor.getId());
                                    }
                                    runtimeService.setVariable(delegateTask.getProcessInstanceId(),nodeInfo.getUserVarName(), userIdArray);
                                }
                            }else {
                                Set<Executor> executorSet = nodeInfo.getExecutorSet();
                                if(executorSet != null && !executorSet.isEmpty()){
                                    String userId = ((Executor)executorSet.toArray()[0]).getId();
                                    runtimeService.setVariable(delegateTask.getProcessInstanceId(),nodeInfo.getUserVarName(), userId);
                                }
                            }
                        }
                        runtimeService.setVariable(delegateTask.getProcessInstanceId(),actTaskDefKey+"_nextNodeIds", nextNodeIds);
                    }
                if(!nextNodeIds.isEmpty()){
                    new Thread(new Runnable() {//异步
                        @Override
                        public void run() {
                            initNextTask(actProcessInstanceId,nextNodeIds);
                        }
                    }).start();
                }
            }
    }

    //因为当前节点是异步激活，所以需要进行异步生成任务的操作
   private void initNextTask(String proceeInstanceId,List<String> nextNodeIds){//主要针对开始任务后紧接 接收任务的情况
       Calendar startTreadTime =  Calendar.getInstance();
       ScheduledExecutorService service = Executors
               .newSingleThreadScheduledExecutor();
       Runnable runnable = new Runnable() {
           public void run() {
               Calendar nowTime = Calendar.getInstance();
               nowTime.add(Calendar.MINUTE, -2);//不能超过2分钟
               if(nowTime.after(startTreadTime)){
                   service.shutdown();
               }
              Boolean result = initTask(proceeInstanceId,nextNodeIds);
               if(result){
                   service.shutdown();
               }
           }
       };

       // 第二个参数为首次执行的延时时间，第三个参数为定时执行的间隔时间
       service.scheduleWithFixedDelay(runnable, 1, 1,TimeUnit.SECONDS);
   }
    private Boolean initTask(String proceeInstanceId,List<String> nextNodeIds){
        Boolean result = false;
        int indexSuccess = 0;
        for(String  nextNodeId:nextNodeIds){
           Boolean tempResult = initTask(proceeInstanceId,nextNodeId);
           if(tempResult){
               indexSuccess++;
           }
        }
        if(indexSuccess == nextNodeIds.size()){
            result = true;
        }
        return  result;
    }

   private Boolean initTask(String proceeInstanceId,String actTaskDefKey){
       Boolean result = false;
       FlowInstance flowInstance = flowInstanceDao.findByActInstanceId(proceeInstanceId);

       Integer flowTaskCount =  flowTaskDao.findCountByActTaskDefKeyAndActInstanceId(actTaskDefKey,proceeInstanceId);
     //  Integer flowHistoryCount =  flowHistoryDao.findCountByActTaskDefKeyAndActInstanceId(actTaskDefKey,proceeInstanceId);
       // 根据当流程实例查询任务
       if(flowTaskCount>0){
           return true;
       }
       List<Task> taskList = taskService.createTaskQuery().processInstanceId(proceeInstanceId).taskDefinitionKey(actTaskDefKey).active().list();

       String flowName = null;
       if (taskList != null && taskList.size() > 0) {
           for (Task task : taskList) {
               String flowDefJson = flowInstance.getFlowDefVersion().getDefJson();
               JSONObject defObj = JSONObject.fromObject(flowDefJson);
               Definition definition = (Definition) JSONObject.toBean(defObj, Definition.class);
               net.sf.json.JSONObject currentNode = definition.getProcess().getNodes().getJSONObject(actTaskDefKey);
               flowName = definition.getProcess().getName();
               net.sf.json.JSONObject normalInfo = currentNode.getJSONObject("nodeConfig").getJSONObject("normal");
               Integer executeTime = null;
               Boolean canReject = null;
               Boolean canSuspension = null;
               if(normalInfo.get("executeTime")!=null){
                   executeTime=normalInfo.getInt("executeTime");
               }
               if(normalInfo.get("allowReject")!=null){
                   canReject = normalInfo.getBoolean("allowReject");
               }
               if(normalInfo.get("allowTerminate")!=null){
                   canSuspension = normalInfo.getBoolean("allowTerminate");
               }
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
               }else {
                   for (IdentityLink identityLink : identityLinks) {
                       IEmployeeService iEmployeeService = ApiClient.createProxy(IEmployeeService.class);
                       List<Executor> employees = iEmployeeService.getExecutorsByEmployeeIds(java.util.Arrays.asList(identityLink.getUserId()));
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
                           flowTask.setActType(identityLink.getType());
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
               }
           }
           flowInstanceService.checkCanEnd(flowInstance.getId());
       }
       return result;
   }
}
