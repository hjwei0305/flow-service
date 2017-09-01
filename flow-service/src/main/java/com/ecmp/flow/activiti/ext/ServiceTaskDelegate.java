package com.ecmp.flow.activiti.ext;

import com.ecmp.basic.api.IEmployeeService;
import com.ecmp.basic.entity.Employee;
import com.ecmp.basic.entity.vo.Executor;
import com.ecmp.config.util.ApiClient;
import com.ecmp.context.ContextUtil;
import com.ecmp.flow.dao.*;
import com.ecmp.flow.entity.FlowDefVersion;
import com.ecmp.flow.entity.FlowHistory;
import com.ecmp.flow.entity.FlowInstance;
import com.ecmp.flow.entity.FlowTask;
import com.ecmp.flow.service.FlowDefinationService;
import com.ecmp.flow.service.FlowTaskService;
import com.ecmp.flow.util.ServiceCallUtil;
import com.ecmp.flow.util.TaskStatus;
import com.ecmp.flow.vo.NodeInfo;
import com.ecmp.flow.vo.bpmn.Definition;
import com.ecmp.util.JsonUtils;
import net.sf.json.JSONObject;
import org.activiti.engine.HistoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricActivityInstanceQuery;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.HistoricActivityInstanceEntity;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.naming.Context;
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
 * 1.0.00      2017/8/2 9:40      谭军(tanjun)                    新建
 * <p/>
 * *************************************************************************************************
 */
@Component(value="serviceTaskDelegate")
public class ServiceTaskDelegate implements org.activiti.engine.delegate.JavaDelegate {

    private final Logger logger = LoggerFactory.getLogger(ServiceTaskDelegate.class);


    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private FlowTaskDao flowTaskDao;

    @Autowired
    private FlowDefVersionDao flowDefVersionDao;

    @Autowired
    private FlowDefinationDao flowDefinationDao;

    @Autowired
    FlowHistoryDao  flowHistoryDao;

    @Autowired
    private HistoryService historyService;

    @Autowired
    private FlowInstanceDao flowInstanceDao;

    @Autowired
    FlowDefinationService flowDefinationService;

    @Override
    public void execute(DelegateExecution delegateTask) throws Exception {

            ExecutionEntity taskEntity = (ExecutionEntity) delegateTask;
            String actTaskDefKey = delegateTask.getCurrentActivityId();
            String actProcessDefinitionId = delegateTask.getProcessDefinitionId();
            String businessId =delegateTask.getProcessBusinessKey();
//            if(StringUtils.isEmpty(businessId)){
//            ExecutionEntity parentExecutionEntity = ((ExecutionEntity) delegateTask).getSuperExecution();
//            if(parentExecutionEntity != null){
//                businessId =  parentExecutionEntity.getProcessInstance().getBusinessKey();
//            }
//            }
            FlowDefVersion flowDefVersion = flowDefVersionDao.findByActDefId(actProcessDefinitionId);
            String flowDefJson = flowDefVersion.getDefJson();
            JSONObject defObj = JSONObject.fromObject(flowDefJson);
            Definition definition = (Definition) JSONObject.toBean(defObj, Definition.class);
//        net.sf.json.JSONObject currentNode = definition.getProcess().getNodes().getJSONObject(currentTaskId);
            net.sf.json.JSONObject currentNode = definition.getProcess().getNodes().getJSONObject(actTaskDefKey);
            //        net.sf.json.JSONObject executor = currentNode.getJSONObject("nodeConfig").getJSONObject("executor");
            net.sf.json.JSONObject normal = currentNode.getJSONObject("nodeConfig").getJSONObject("normal");
            if (normal != null) {
                String serviceTaskId = (String) normal.get("serviceTaskId");
                String flowTaskName = (String) normal.get("name");
                if (!StringUtils.isEmpty(serviceTaskId)) {
                    Map<String,Object> tempV = delegateTask.getVariables();
                    String param = JsonUtils.toJson(tempV);

                    FlowHistory flowHistory = new FlowHistory();
                    flowHistory.setTaskJsonDef(currentNode.toString());
                    flowHistory.setFlowName(definition.getProcess().getName());
                    flowHistory.setDepict("服务任务【自动执行】");
//                    flowHistory.setActClaimTime(flowTask.getActClaimTime());
                    flowHistory.setFlowTaskName(flowTaskName);
                    flowHistory.setFlowDefId(flowDefVersion.getFlowDefination().getId());
                    String actProcessInstanceId = delegateTask.getProcessInstanceId();
                    List<TaskEntity> taskList = taskEntity.getTasks();
                    System.out.println(taskList);
                    FlowInstance flowInstance = flowInstanceDao.findByActInstanceId(actProcessInstanceId);
                    flowHistory.setFlowInstance(flowInstance);

                    flowHistory.setOwnerAccount("admin");
                    flowHistory.setOwnerName("系统自动");
                    flowHistory.setExecutorAccount("admin");
                    flowHistory.setExecutorId("");
                    flowHistory.setExecutorName("系统自动");
                    flowHistory.setCandidateAccount("");
                    flowHistory.setActStartTime(new Date());
                    ServiceCallUtil.callService(serviceTaskId, businessId, param);
                    flowHistory.setActEndTime(new Date());
                    flowHistory.setActHistoryId(null);
                    flowHistory.setActTaskDefKey(actTaskDefKey);
                    flowHistory.setPreId(null);
                    flowHistory.setTaskStatus(TaskStatus.COMPLETED.toString());
                    if(flowHistory.getActDurationInMillis() == null){
                        Long actDurationInMillis = flowHistory.getActEndTime().getTime()-flowHistory.getActStartTime().getTime();
                        flowHistory.setActDurationInMillis(actDurationInMillis);
                    }
                    flowHistoryDao.save(flowHistory);
                    FlowTask flowTask = new FlowTask();
                    BeanUtils.copyProperties(flowHistory,flowTask);
                    flowTask.setTaskStatus(TaskStatus.INIT.toString());
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
//                        ProcessInstance instanceParent = runtimeService
//                                .createProcessInstanceQuery()
//                                .processInstanceId(parentFlowInstance.getActInstanceId())
//                                .singleResult();
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
//                            if ("Normal".equalsIgnoreCase(taskType)) {
//                                nodeInfo.setUserVarName(nodeInfo.getId() + "_Normal");
//                            } else if ("SingleSign".equalsIgnoreCase(taskType)) {
//                                nodeInfo.setUserVarName(nodeInfo.getId() + "_SingleSign");
//                            } else if ("Approve".equalsIgnoreCase(taskType)) {
//                                nodeInfo.setUserVarName(nodeInfo.getId() + "_Approve");
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
//                                    nextUserMap.put(nodeInfo.getUserVarName(),userIdArray);
                                    runtimeService.setVariable(delegateTask.getProcessInstanceId(),nodeInfo.getUserVarName(), userIdArray);
                                }
                            }else {
                                Set<Executor> executorSet = nodeInfo.getExecutorSet();
                                if(executorSet != null && !executorSet.isEmpty()){
                                    String userId = ((Executor)executorSet.toArray()[0]).getId();
//                                    nextUserMap.put(nodeInfo.getUserVarName(),userId);
                                    runtimeService.setVariable(delegateTask.getProcessInstanceId(),nodeInfo.getUserVarName(), userId);
                                }
                            }
                        }
                        runtimeService.setVariable(delegateTask.getProcessInstanceId(),actTaskDefKey+"_nextNodeIds",  nextNodes);
                    //    flowDefinationService.initTask();
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
                            FlowInstance flowInstanceZhu = flowInstanceDao.findByActInstanceId(zhuzhongEntity.getProcessInstanceId());
                            new Thread(new Runnable() {//异步
                                @Override
                                public void run() {
                                    initNextAllTask(flowInstanceZhu,flowHistory);//初始化相关联的所有待办任务
                                }
                            }).start();
                        }else{
                        }
                    }
                }else{
                    throw new RuntimeException("服务地址不能为空！");
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
                flowDefinationService.initTask(flowInstance,flowHistory);
            }
        };
        // 第二个参数为首次执行的延时时间，第三个参数为定时执行的间隔时间
        service.scheduleWithFixedDelay(runnable, 1, 10, TimeUnit.SECONDS);
    }
}
