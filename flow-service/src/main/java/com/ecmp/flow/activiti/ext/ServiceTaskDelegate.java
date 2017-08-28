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
import com.ecmp.flow.service.FlowTaskService;
import com.ecmp.flow.util.ServiceCallUtil;
import com.ecmp.flow.util.TaskStatus;
import com.ecmp.flow.vo.NodeInfo;
import com.ecmp.flow.vo.bpmn.Definition;
import com.ecmp.util.JsonUtils;
import net.sf.json.JSONObject;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
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
    private FlowInstanceDao flowInstanceDao;

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
                        runtimeService.setVariable(delegateTask.getProcessInstanceId(),actTaskDefKey+"_nextNodeIds", nextNodeIds);
                    }
                }else{
                    throw new RuntimeException("服务地址不能为空！");
                }
            }

    }
}
