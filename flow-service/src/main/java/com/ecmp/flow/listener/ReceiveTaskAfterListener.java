package com.ecmp.flow.listener;

import com.ecmp.config.util.ApiClient;
import com.ecmp.context.ContextUtil;
import com.ecmp.flow.basic.vo.Executor;
import com.ecmp.flow.common.util.Auth2ApiClient;
import com.ecmp.flow.common.util.Constants;
import com.ecmp.flow.dao.*;
import com.ecmp.flow.entity.FlowDefVersion;
import com.ecmp.flow.entity.FlowHistory;
import com.ecmp.flow.entity.FlowInstance;
import com.ecmp.flow.entity.FlowTask;
import com.ecmp.flow.service.FlowDefinationService;
import com.ecmp.flow.service.FlowInstanceService;
import com.ecmp.flow.service.FlowTaskService;
import com.ecmp.flow.util.TaskStatus;
import com.ecmp.flow.vo.NodeInfo;
import com.ecmp.flow.vo.bpmn.Definition;
import net.sf.json.JSONObject;
import org.activiti.engine.HistoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricActivityInstanceQuery;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.HistoricActivityInstanceEntity;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.IdentityLink;
import org.activiti.engine.task.Task;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.ws.rs.core.GenericType;
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

    @Autowired
    FlowDefinationService flowDefinationService;

    @Autowired
    private HistoryService historyService;

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
                        com.ecmp.flow.api.IAppModuleService proxy = ApiClient.createProxy(com.ecmp.flow.api.IAppModuleService.class);
                        com.ecmp.flow.entity.AppModule appModule = proxy.findOne(appModuleId);
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
                List<NodeInfo> nodeInfoList = flowTaskService.findNexNodesWithUserSet(flowTask);
                List<NodeInfo> results = null;
                results = nodeInfoList;
                FlowInstance parentFlowInstance = flowTask.getFlowInstance().getParent();
                FlowTask flowTaskTempSrc = new FlowTask();
                org.springframework.beans.BeanUtils.copyProperties(flowTask,flowTaskTempSrc);
                //针对子流程结束，循环向上查找父任务下一步的节点执行人信息
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
                        String flowDefJsonP = parentFlowInstance.getFlowDefVersion().getDefJson();
                        JSONObject defObjP = JSONObject.fromObject(flowDefJsonP);
                        Definition definitionP = (Definition) JSONObject.toBean(defObjP, Definition.class);
                        net.sf.json.JSONObject currentNodeP = definitionP.getProcess().getNodes().getJSONObject(he.getActivityId());
                        flowTaskTemp.setTaskJsonDef(currentNodeP.toString());
                        results = flowTaskService.findNexNodesWithUserSet( flowTaskTemp);
                    }
                    parentFlowInstance=parentFlowInstance.getParent();
                    nodeInfoList=results;
                    flowTaskTempSrc =flowTaskTemp;
                }



                    List<NodeInfo> nextNodes = new ArrayList<NodeInfo>();
                    if(results !=null &&  !results.isEmpty()){
                        Map<String,Object> userVarNameMap = new HashMap<>();
                        List<String> userVarNameList = null;
                        for(NodeInfo nodeInfo:results){
                            if ("EndEvent".equalsIgnoreCase(nodeInfo.getType())) {
                                nodeInfo.setType("EndEvent");
                                continue;
                            }else if("ServiceTask".equalsIgnoreCase(nodeInfo.getType())){//服务任务也不做处理
                                continue;
                            }
                            nextNodes.add(nodeInfo);
                           String taskType = nodeInfo.getFlowTaskType();
                            String uiUserType = nodeInfo.getUiUserType();
                            String callActivityPath = nodeInfo.getCallActivityPath();
                            String varUserName = nodeInfo.getUserVarName();

                            if (StringUtils.isNotEmpty(callActivityPath)) {
                                userVarNameList = (List<String>) userVarNameMap.get(callActivityPath+"_sonProcessSelectNodeUserV");
                                if(userVarNameList==null){
                                    userVarNameList = new ArrayList<String>();
                                    userVarNameMap.put(callActivityPath+"_sonProcessSelectNodeUserV",userVarNameList);//选择的变量名,子流程存在选择了多个的情况
                                }
                                userVarNameList.add(varUserName);

                            }
                            if("AnyOne".equalsIgnoreCase(uiUserType)){//任意执行人默认规则为当前执行人
//                                IEmployeeService proxy = ApiClient.createProxy(IEmployeeService.class);
                                String currentUserId = ContextUtil.getUserId();
                                List<String> usrIdList = new ArrayList<String>(1);
                                usrIdList.add(currentUserId);
//                                List<Executor> employees = proxy.getExecutorsByEmployeeIds(usrIdList);
                                Map<String,Object> params = new HashedMap();
                                params.put("employeeIds",java.util.Arrays.asList(usrIdList));
//                                List<Executor>   employees = ( List<Executor>) new Auth2ApiClient().call(com.ecmp.flow.common.util.Constants.BASIC_SERVICE_URL, Constants.BASIC_EMPLOYEE_GETEXECUTORSBYEMPLOYEEIDS_URL, new GenericType< List<Executor>>() {
//                                }, params,null);
                                Auth2ApiClient auth2ApiClient= new Auth2ApiClient(com.ecmp.flow.common.util.Constants.BASIC_SERVICE_URL, Constants.BASIC_EMPLOYEE_GETEXECUTORSBYEMPLOYEEIDS_URL);
                                List<Executor>   employees = auth2ApiClient.getEntityViaProxy(new GenericType<List<Executor>>() {},params);
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
                                    if (StringUtils.isNotEmpty(callActivityPath)) {
                                        userVarNameMap.put(callActivityPath+"/"+varUserName, userIdArray);
                                    }else {
                                        userVarNameMap.put(varUserName, userIdArray);
                                    }

                                   // runtimeService.setVariable(delegateTask.getProcessInstanceId(),nodeInfo.getUserVarName(), userIdArray);
                                }
                            }else {
                                Set<Executor> executorSet = nodeInfo.getExecutorSet();
                                if(executorSet != null && !executorSet.isEmpty()){
                                    String userId = ((Executor)executorSet.toArray()[0]).getId();

                                    if (StringUtils.isNotEmpty(callActivityPath)) {
                                        userVarNameMap.put(callActivityPath+"/"+varUserName, userId);
                                    }else {
                                        userVarNameMap.put(varUserName, userId);
                                    }
                                   // runtimeService.setVariable(delegateTask.getProcessInstanceId(),nodeInfo.getUserVarName(), userId);
                                }
                            }
                        }
                        runtimeService.setVariables(delegateTask.getProcessInstanceId(),userVarNameMap);
                        runtimeService.setVariable(delegateTask.getProcessInstanceId(),actTaskDefKey+"_nextNodeIds", nextNodes);
                    }
                if(!nextNodes.isEmpty()){
                    ExecutionEntity parent = taskEntity.getSuperExecution();
                    if(parent!=null){//针对作为子任务的情况
                        ExecutionEntity parentTemp = parent;
                        ProcessInstance parentProcessInstance = null;
                        ExecutionEntity zhuzhongEntity = parentTemp;
                         while (parentTemp!=null){
                             parentProcessInstance = parentTemp.getProcessInstance();
                             zhuzhongEntity =parentTemp;
                             parentTemp = ((ExecutionEntity)parentProcessInstance).getSuperExecution();
                         }
                         FlowInstance flowInstance = flowInstanceDao.findByActInstanceId(zhuzhongEntity.getProcessInstanceId());
                        new Thread(new Runnable() {//异步
                            @Override
                            public void run() {
                                initNextAllTask(flowInstance,flowHistory);//初始化相关联的所有待办任务
                            }
                        }).start();
                    }else{
                        FlowInstance flowInstance =  flowTask.getFlowInstance();
                        new Thread(new Runnable() {//异步
                            @Override
                            public void run() {
                                initNextAllTask(flowInstance,flowHistory);
                            }
                        }).start();
                    }
                }
            }
    }
    private void initNextAllTask(FlowInstance flowInstance,FlowHistory flowHistory){
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
                try {
                    flowDefinationService.initTask(flowInstance, flowHistory);
                }catch (Exception e){
                    logger.error(e.getMessage());
                }
            }
        };

        // 第二个参数为首次执行的延时时间，第三个参数为定时执行的间隔时间
        service.scheduleWithFixedDelay(runnable, 1, 10,TimeUnit.SECONDS);
    }
//    //因为当前节点是异步激活，所以需要进行异步生成任务的操作
//   private void initNextTask(String proceeInstanceId,List<NodeInfo> nextNodes){//主要针对开始任务后紧接 接收任务的情况
//       Calendar startTreadTime =  Calendar.getInstance();
//       ScheduledExecutorService service = Executors
//               .newSingleThreadScheduledExecutor();
//       Runnable runnable = new Runnable() {
//           public void run() {
//               Calendar nowTime = Calendar.getInstance();
//               nowTime.add(Calendar.MINUTE, -2);//不能超过2分钟
//               if(nowTime.after(startTreadTime)){
//                   service.shutdown();
//               }
//              Boolean result = initTask(proceeInstanceId,nextNodes);
//               if(result){
//                   service.shutdown();
//               }
//           }
//       };
//
//       // 第二个参数为首次执行的延时时间，第三个参数为定时执行的间隔时间
//       service.scheduleWithFixedDelay(runnable, 1, 1,TimeUnit.SECONDS);
//   }
    private Boolean initTask(String proceeInstanceId,List<NodeInfo> nextNodes) throws Exception{
        Boolean result = false;
        int indexSuccess = 0;
        for(NodeInfo  nextNode:nextNodes){
           Boolean tempResult = initTask(proceeInstanceId,nextNode);
           if(tempResult){
               indexSuccess++;
           }
        }
        if(indexSuccess == nextNodes.size()){
            result = true;
        }
        return  result;
    }

   private Boolean initTask(String proceeInstanceId,NodeInfo nextNode) throws Exception{
       Boolean result = false;
       if("ServiceTask".equalsIgnoreCase(nextNode.getType())){//服务任务也不做处理
           return true;
       }
       String actTaskDefKey = nextNode.getId();

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
//                       IEmployeeService iEmployeeService = ApiClient.createProxy(IEmployeeService.class);
//                       List<Executor> employees = iEmployeeService.getExecutorsByEmployeeIds(java.util.Arrays.asList(userId));
                       Map<String,Object> params = new HashedMap();
                       params.put("employeeIds",java.util.Arrays.asList(userId));
//                       List<Executor>  employees = ( List<Executor>) new Auth2ApiClient().call(com.ecmp.flow.common.util.Constants.BASIC_SERVICE_URL, Constants.BASIC_EMPLOYEE_GETEXECUTORSBYEMPLOYEEIDS_URL, new GenericType< List<Executor>>() {
//                       }, params,null);
                       Auth2ApiClient auth2ApiClient= new Auth2ApiClient(com.ecmp.flow.common.util.Constants.BASIC_SERVICE_URL, Constants.BASIC_EMPLOYEE_GETEXECUTORSBYEMPLOYEEIDS_URL);
                       List<Executor>   employees = auth2ApiClient.getEntityViaProxy(new GenericType<List<Executor>>() {},params);
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
//                       IEmployeeService iEmployeeService = ApiClient.createProxy(IEmployeeService.class);
//                       List<Executor> employees = iEmployeeService.getExecutorsByEmployeeIds(java.util.Arrays.asList(identityLink.getUserId()));
                       Map<String,Object> params = new HashedMap();
                       params.put("employeeIds",java.util.Arrays.asList(identityLink.getUserId()));
//                       List<Executor>  employees = ( List<Executor>) new Auth2ApiClient().call(com.ecmp.flow.common.util.Constants.BASIC_SERVICE_URL, Constants.BASIC_EMPLOYEE_GETEXECUTORSBYEMPLOYEEIDS_URL, new GenericType< List<Executor>>() {
//                       }, params,null);
                       Auth2ApiClient auth2ApiClient= new Auth2ApiClient(com.ecmp.flow.common.util.Constants.BASIC_SERVICE_URL, Constants.BASIC_EMPLOYEE_GETEXECUTORSBYEMPLOYEEIDS_URL);
                       List<Executor>   employees = auth2ApiClient.getEntityViaProxy(new GenericType<List<Executor>>() {},params);
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
