package com.ecmp.flow.activiti.ext;

import com.ecmp.config.util.ApiClient;
import com.ecmp.context.ContextUtil;
import com.ecmp.flow.basic.vo.Executor;
import com.ecmp.flow.common.util.Constants;
import com.ecmp.flow.dao.*;
import com.ecmp.flow.entity.FlowDefVersion;
import com.ecmp.flow.entity.FlowHistory;
import com.ecmp.flow.entity.FlowInstance;
import com.ecmp.flow.entity.FlowTask;
import com.ecmp.flow.service.FlowDefinationService;
import com.ecmp.flow.service.FlowTaskService;
import com.ecmp.flow.util.FlowException;
import com.ecmp.flow.util.FlowTaskTool;
import com.ecmp.flow.util.ServiceCallUtil;
import com.ecmp.flow.util.TaskStatus;
import com.ecmp.flow.vo.FlowInvokeParams;
import com.ecmp.flow.vo.FlowOperateResult;
import com.ecmp.flow.vo.NodeInfo;
import com.ecmp.flow.vo.bpmn.Definition;
import com.ecmp.util.JsonUtils;
import net.sf.json.JSONObject;
import org.activiti.engine.HistoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.runtime.ProcessInstance;
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
 * 1.0.00      2017/8/2 9:40      谭军(tanjun)                    新建
 * <p/>
 * *************************************************************************************************
 */
//@Component(value="serviceTaskDelegate")
public class ServiceTaskDelegate implements org.activiti.engine.delegate.JavaDelegate {

    public ServiceTaskDelegate(){}

    private final Logger logger = LoggerFactory.getLogger(ServiceTaskDelegate.class);

    @Autowired
    private FlowTaskTool flowTaskTool;

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

            FlowDefVersion flowDefVersion = flowDefVersionDao.findByActDefId(actProcessDefinitionId);
            String flowDefJson = flowDefVersion.getDefJson();
            JSONObject defObj = JSONObject.fromObject(flowDefJson);
            Definition definition = (Definition) JSONObject.toBean(defObj, Definition.class);
            net.sf.json.JSONObject currentNode = definition.getProcess().getNodes().getJSONObject(actTaskDefKey);
            net.sf.json.JSONObject normal = currentNode.getJSONObject("nodeConfig").getJSONObject("normal");
            if (normal != null) {
                String serviceTaskId = (String) normal.get("serviceTaskId");
                String flowTaskName = (String) normal.get("name");
                if (!StringUtils.isEmpty(serviceTaskId)) {
                    Map<String,Object> tempV = delegateTask.getVariables();
                    Map<String,Object> serviceVariables = new HashedMap();

                    FlowHistory flowHistory = new FlowHistory();
                    flowHistory.setTaskJsonDef(currentNode.toString());
                    flowHistory.setFlowName(definition.getProcess().getName());
                    flowHistory.setDepict("服务任务【自动执行】");
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
                    flowHistory.setActHistoryId(null);
                    flowHistory.setActTaskDefKey(actTaskDefKey);
                    flowHistory.setPreId(null);

                    FlowTask flowTask = new FlowTask();
                    BeanUtils.copyProperties(flowHistory,flowTask);
                    flowTask.setTaskStatus(TaskStatus.INIT.toString());
//                    //选择下一步执行人，默认选择第一个，会签、串、并行选择全部
                    ApplicationContext applicationContext = ContextUtil.getApplicationContext();
                    FlowTaskService flowTaskService = (FlowTaskService)applicationContext.getBean("flowTaskService");
                    List<NodeInfo> nodeInfoList = flowTaskService.findNexNodesWithUserSet(flowTask);
                    List<String> paths = new ArrayList<String>();
                    if(nodeInfoList!=null && !nodeInfoList.isEmpty()){
                        for(NodeInfo nodeInfo :nodeInfoList){
                            if(StringUtils.isNotEmpty(nodeInfo.getCallActivityPath())){
                                paths.add(nodeInfo.getCallActivityPath());
                            }
                        }
                    }
                    if(!paths.isEmpty()){
                        tempV.put("callActivtiySonPaths",paths);//提供给调用服务，子流程的绝对路径，用于存入单据id
                    }
                    String param = JsonUtils.toJson(tempV);
                    FlowOperateResult serviceCallResult =(FlowOperateResult)ServiceCallUtil.callService(serviceTaskId, businessId, param);
                    if(!serviceCallResult.isSuccess()){
                        String message = serviceCallResult.getMessage();
                        message="serviceTaskId="+serviceTaskId+",businessId"+businessId+";调用返回失败！"+message;
                        logger.error(message);
                        throw new FlowException(message);
                    }
//                    if(serviceCallResultStr!=null && StringUtils.isNotEmpty(serviceCallResultStr)){
//                        Map serviceCallResult = JsonUtils.fromJson(serviceCallResultStr,Map.class);
//                        serviceVariables.putAll(serviceCallResult);
//                    }
                    flowHistory.setActEndTime(new Date());
                    flowHistory.setTaskStatus(TaskStatus.COMPLETED.toString());
                    if(flowHistory.getActDurationInMillis() == null){
                        Long actDurationInMillis = flowHistory.getActEndTime().getTime()-flowHistory.getActStartTime().getTime();
                        flowHistory.setActDurationInMillis(actDurationInMillis);
                    }
                    flowHistoryDao.save(flowHistory);
                    List<NodeInfo> results = null;
                    results = nodeInfoList;
                    FlowInstance parentFlowInstance = flowTask.getFlowInstance().getParent();
                    FlowTask flowTaskTempSrc = new FlowTask();
                    org.springframework.beans.BeanUtils.copyProperties(flowTask,flowTaskTempSrc);
                    //针对子流程结束，循环向上查找父任务下一步的节点执行人信息
                    ProcessInstance instanceSon = ((ExecutionEntity) delegateTask).getProcessInstance();
                    while (instanceSon!=null && parentFlowInstance != null&&nodeInfoList != null && !nodeInfoList.isEmpty()&& nodeInfoList.size()==1&&"EndEvent".equalsIgnoreCase(nodeInfoList.get(0).getType()) ){//针对子流程结束节点
                        FlowTask flowTaskTemp = new FlowTask();
                        org.springframework.beans.BeanUtils.copyProperties(flowTaskTempSrc,flowTaskTemp);
                        flowTaskTemp.setFlowInstance(parentFlowInstance);
                        // 取得父流程实例
                        ExecutionEntity superExecution =instanceSon.getSuperExecution();
                        if (superExecution != null) {
                            String activityId = superExecution.getActivityId();
                            flowTaskTemp.setActTaskKey(activityId);
                            flowTaskTemp.setActTaskDefKey(activityId);
                            String flowDefJsonP = parentFlowInstance.getFlowDefVersion().getDefJson();
                            JSONObject defObjP = JSONObject.fromObject(flowDefJsonP);
                            Definition definitionP = (Definition) JSONObject.toBean(defObjP, Definition.class);
                            net.sf.json.JSONObject currentNodeP = definitionP.getProcess().getNodes().getJSONObject(activityId);
                            flowTaskTemp.setTaskJsonDef(currentNodeP.toString());
                            results = flowTaskService.findNexNodesWithUserSet( flowTaskTemp);
                        }
                        parentFlowInstance=parentFlowInstance.getParent();
                        nodeInfoList=results;
                        flowTaskTempSrc =flowTaskTemp;
                        instanceSon=superExecution.getProcessInstance();
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
                                Map<String,Object> params = new HashMap();
                                params.put("employeeIds",usrIdList);
                                String url = Constants.BASIC_SERVICE_URL+ Constants.BASIC_EMPLOYEE_GETEXECUTORSBYEMPLOYEEIDS_URL;
                                List<Executor> employees=ApiClient.getEntityViaProxy(url,new GenericType<List<Executor>>() {},params);
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
                                }
                            }
                        }
                        serviceVariables.putAll(userVarNameMap);
                        runtimeService.setVariable(delegateTask.getProcessInstanceId(),actTaskDefKey+"_nextNodeIds",  nextNodes);
                    }
                    runtimeService.setVariables(delegateTask.getProcessInstanceId(),serviceVariables);
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
                            new Thread(new Runnable() {//异步
                                @Override
                                public void run() {
                                    initNextAllTask(flowInstance,flowHistory);
                                }
                            }).start();
                        }
                    }
                }else{
                    throw new FlowException("服务地址不能为空！");
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
                flowTaskTool.initTask(flowInstance,flowHistory,null);
            }
        };
        // 第二个参数为首次执行的延时时间，第三个参数为定时执行的间隔时间
        service.scheduleWithFixedDelay(runnable, 1, 10, TimeUnit.SECONDS);
    }
}
