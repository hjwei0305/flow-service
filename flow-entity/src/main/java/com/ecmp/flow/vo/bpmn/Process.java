package com.ecmp.flow.vo.bpmn;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.annotation.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * *************************************************************************************************
 * <p/>
 * 实现功能：流程类
 * <p>
 * ------------------------------------------------------------------------------------------------
 * 版本          变更时间             变更人                     变更原因
 * ------------------------------------------------------------------------------------------------
 * 1.0.00      2017/4/10 9:52      陈飞(fly)                  新建
 * <p/>
 * *************************************************************************************************
 */
@XmlType(name = "process")
@XmlAccessorType(XmlAccessType.FIELD)
public class Process extends BaseNode implements Serializable {
    private static final long serialVersionUID = 1L;
//    private final Logger logger = LoggerFactory.getLogger(Process.class);

    /**
     * 流程名
     */
    @XmlAttribute
    private String name;
    /**
     * 是否可执行
     */
    @XmlAttribute(name = "isExecutable")
    private boolean executable = true;

    /**
     * 启动条件
     */
    @XmlTransient
    private JSONObject startUEL;

    /**
     * 流程定义版本ID
     */
    @XmlTransient
    private String flowDefVersionId;

    @XmlTransient
    private JSONObject nodes;

    @XmlTransient
    private String beforeStartServiceId;//启动时调用服务id

    @XmlTransient
    private String beforeStartServiceName;//启动时调用服务名称

    @XmlTransient
    private String afterStartServiceId;//启动完成时调用服务id

    @XmlTransient
    private String afterStartServiceName;//启动完成时调用服务名称

    @XmlTransient
    private Boolean afterStartServiceAync;//启动完成时调用服务是否异步

    @XmlTransient
    private String afterEndServiceId;//结束时调用服务id

    @XmlTransient
    private String afterEndServiceName;//结束时调用服务名称

    @XmlTransient
    private String beforeEndServiceId;//结束前调用服务id

    @XmlTransient
    private String beforeEndServiceName;//结束前调用服务名称

    private List<StartEvent> startEvent;
    private List<EndEvent> endEvent;
    private List<UserTask> userTask;
    private List<MailTask> mailTask;
    private List<ManualTask> manualTask;
    private List<ScriptTask> scriptTask;
    private List<ServiceTask> serviceTask;
    private List<ReceiveTask> receiveTask;
    private List<ExclusiveGateway> exclusiveGateway;
    private List<InclusiveGateway> inclusiveGateway;
    private List<ParallelGateway> parallelGateway;
    private List<EventGateway> eventGateway;
    private List<SequenceFlow> sequenceFlow;
    private List<CallActivity> callActivity;

    @XmlTransient
    private int lineCount = 0;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public JSONObject getStartUEL() {
        return startUEL;
    }

    public void setStartUEL(JSONObject startUEL) {
        this.startUEL = startUEL;
    }

    public JSONObject getNodes() {
        return nodes;
    }

    public boolean isExecutable() {
        return executable;
    }

    public void setExecutable(boolean executable) {
        this.executable = executable;
    }

    public void setNodes(JSONObject nodes) {
        this.nodes = nodes;
        if (nodes != null) {
            startEvent = new ArrayList<StartEvent>();
            endEvent = new ArrayList<EndEvent>();
            userTask = new ArrayList<UserTask>();
            mailTask = new ArrayList<MailTask>();
            manualTask = new ArrayList<ManualTask>();
            scriptTask = new ArrayList<ScriptTask>();
            serviceTask = new ArrayList<ServiceTask>();
            receiveTask = new ArrayList<ReceiveTask>();
            callActivity = new ArrayList<CallActivity>();
            exclusiveGateway = new ArrayList<ExclusiveGateway>();
            inclusiveGateway = new ArrayList<InclusiveGateway>();
            parallelGateway = new ArrayList<ParallelGateway>();
            eventGateway = new ArrayList<EventGateway>();
            sequenceFlow = new ArrayList<SequenceFlow>();
            lineCount = 0;
            Iterator iterator = nodes.keys();
            int indexCount=0;//计数器
            while (iterator.hasNext()) {
                String key = (String) iterator.next();
                JSONObject node = nodes.getJSONObject(key);
                JSONArray targets = node.getJSONArray("target");
                BaseFlowNode baseFlowNodeTemp = null;
                ExtensionElement extensionElement = null;
                ExecutionListener executionListener = null;
                List<ExecutionListener> executionListeners = null;
                ExclusiveGateway exclusiveGatewayIn = null;
                String exclusiveGatewayInId = null;
                switch (node.getString("type")) {
                    case "StartEvent":
                        StartEvent startEventTemp = (StartEvent) JSONObject.toBean(node, StartEvent.class);
                        startEventTemp.setInitiator("startUserId");
                         extensionElement  = startEventTemp.getExtensionElement();
                        if(extensionElement == null){
                            extensionElement = new ExtensionElement();
                        }
                            //添加默认启动完成监听器
                         executionListener = new ExecutionListener();
                        executionListener.setEvent("end");
                        executionListener.setDelegateExpression("${startEventCompleteListener}");
                        executionListeners = extensionElement.getExecutionListener();
                        if(executionListeners == null){
                                executionListeners = new ArrayList<ExecutionListener>();
                         }
                        executionListeners.add(executionListener);
                        extensionElement.setExecutionListener(executionListeners);
                        startEventTemp.setExtensionElement(extensionElement);
                        startEvent.add(startEventTemp);
                        baseFlowNodeTemp  = startEventTemp;
                        break;
                    case "EndEvent":
                        EndEvent endEventTemp = (EndEvent) JSONObject.toBean(node, EndEvent.class);
                        extensionElement  = endEventTemp.getExtensionElement();
                        if(extensionElement == null){
                            extensionElement = new ExtensionElement();
                        }
                        //添加默认启动完成监听器
                        executionListener = new ExecutionListener();
                        executionListener.setEvent("end");
                        executionListener.setDelegateExpression("${endEventCompleteListener}");
                        executionListeners = extensionElement.getExecutionListener();
                        if(executionListeners == null){
                            executionListeners = new ArrayList<ExecutionListener>();
                        }
                        executionListeners.add(executionListener);
                        extensionElement.setExecutionListener(executionListeners);
                        endEventTemp.setExtensionElement(extensionElement);
                        endEvent.add(endEventTemp);
                        baseFlowNodeTemp  = endEventTemp;
                        break;
                    case "UserTask": {
                        UserTask userTaskTemp = (UserTask) JSONObject.toBean(node, UserTask.class);
                        if ("Normal".equalsIgnoreCase(userTaskTemp.getNodeType())) {
                            userTaskTemp.setAssignee("${" + userTaskTemp.getId() + "_Normal}");
                        } else if ("SingleSign".equalsIgnoreCase(userTaskTemp.getNodeType())) {
                            userTaskTemp.setCandidateUsers("${" + userTaskTemp.getId() + "_SingleSign}");
                        } else if ("Approve".equalsIgnoreCase(userTaskTemp.getNodeType())) {
                            userTaskTemp.setAssignee("${" + userTaskTemp.getId() + "_Approve}");
                        }  else if ("CounterSign".equalsIgnoreCase(userTaskTemp.getNodeType())) {
                            MultiInstanceConfig multiInstanceConfig = new MultiInstanceConfig();
                            multiInstanceConfig.setUserIds("" + userTaskTemp.getId() + "_List_CounterSign");
                            multiInstanceConfig.setCandidateUsers("${" + userTaskTemp.getId() + "_List_CounterSign}");
                            multiInstanceConfig.setVariable("" + userTaskTemp.getId() + "_CounterSign");
                            multiInstanceConfig.setAssignee("${" + userTaskTemp.getId() + "_CounterSign}");
                            String isSequential = node.getJSONObject("nodeConfig").getJSONObject("normal").get("isSequential")+"";
                            if("true".equalsIgnoreCase(isSequential)){
                                multiInstanceConfig.setSequential(true);
                            }
                            userTaskTemp.setConfig(multiInstanceConfig);

                            extensionElement = new ExtensionElement();
                            //添加默认任务监听器
                            TaskListener taskListener = new TaskListener();//在结点处理逻辑执行完成时
                            taskListener.setEvent("complete");
                            taskListener.setDelegateExpression("${commonCounterSignCompleteListener}");
                            List<TaskListener> taskListeners = new ArrayList<TaskListener>();
                            taskListeners.add(taskListener);

//                            TaskListener  taskListenerA = new TaskListener();//在结点处理逻辑被指派时
//                            taskListenerA.setEvent("assignment");
//                            taskListenerA.setDelegateExpression("${commonCounterSignAssignmentListener}");
//                            taskListeners.add(taskListenerA);

//                            ExecutionListener executionListener = new ExecutionListener();
//                            executionListener.setEvent("start");
//                            executionListener.setDelegateExpression("${commonCounterSignBeforeListener}");
//                            List<ExecutionListener> executionListeners = new ArrayList<ExecutionListener>();
//                            executionListeners.add(executionListener);
//                            extensionElement.setExecutionListener(executionListeners);

                            extensionElement.setTaskListener(taskListeners);
                            userTaskTemp.setExtensionElement(extensionElement);
                        }else if("ParallelTask".equalsIgnoreCase(userTaskTemp.getNodeType())){//并行任务
                            MultiInstanceConfig multiInstanceConfig = new MultiInstanceConfig();
                            multiInstanceConfig.setUserIds("" + userTaskTemp.getId() + "_List_CounterSign");
                            multiInstanceConfig.setCandidateUsers("${" + userTaskTemp.getId() + "_List_CounterSign}");
                            multiInstanceConfig.setVariable("" + userTaskTemp.getId() + "_CounterSign");
                            multiInstanceConfig.setAssignee("${" + userTaskTemp.getId() + "_CounterSign}");
                            multiInstanceConfig.setSequential(false);
                            userTaskTemp.setConfig(multiInstanceConfig);
                        }else if("SerialTask".equalsIgnoreCase(userTaskTemp.getNodeType())){//串行任务
                            MultiInstanceConfig multiInstanceConfig = new MultiInstanceConfig();
                            multiInstanceConfig.setUserIds("" + userTaskTemp.getId() + "_List_CounterSign");
                            multiInstanceConfig.setCandidateUsers("${" + userTaskTemp.getId() + "_List_CounterSign}");
                            multiInstanceConfig.setVariable("" + userTaskTemp.getId() + "_CounterSign");
                            multiInstanceConfig.setAssignee("${" + userTaskTemp.getId() + "_CounterSign}");
                            multiInstanceConfig.setSequential(true);
                            userTaskTemp.setConfig(multiInstanceConfig);
                        }
                        //添加自定义用户任务事件监听（用于用户任务事前、事后）
                        initEventListener(userTaskTemp,node);
                        initMessageListener(userTaskTemp,node);

                        userTask.add(userTaskTemp);
                        baseFlowNodeTemp  = userTaskTemp;
                        if ("CounterSign".equalsIgnoreCase(userTaskTemp.getNodeType())||"Approve".equalsIgnoreCase(userTaskTemp.getNodeType())) {
                            exclusiveGatewayIn = new ExclusiveGateway();
                            exclusiveGateway.add(exclusiveGatewayIn);
                            exclusiveGatewayInId = (indexCount++)+"ExclusiveGateway_In_"+System.currentTimeMillis();
                            exclusiveGatewayIn.setId(exclusiveGatewayInId);
                            String id = "flow" + (++lineCount);
                            SequenceFlow sf = new SequenceFlow(id, userTaskTemp.getId(), exclusiveGatewayInId, null);
                            sequenceFlow.add(sf);
                            baseFlowNodeTemp = exclusiveGatewayIn;
                        }
                        break;
                    }
                    case "MailTask":
                        MailTask mailTaskTemp = (MailTask) JSONObject.toBean(node, MailTask.class);
                        mailTask.add(mailTaskTemp);
                        baseFlowNodeTemp  = mailTaskTemp;
                        break;
                    case "ManualTask":
                        ManualTask manualTaskTemp = (ManualTask) JSONObject.toBean(node, ManualTask.class);
                        manualTask.add(manualTaskTemp);
                        baseFlowNodeTemp  = manualTaskTemp;
                        break;
                    case "ScriptTask":
                        ScriptTask scriptTaskTemp =  (ScriptTask) JSONObject.toBean(node, ScriptTask.class);
                        scriptTask.add(scriptTaskTemp);
                        baseFlowNodeTemp  = scriptTaskTemp;
                        break;
                    case "ServiceTask":
                        ServiceTask serviceTaskTemp = (ServiceTask) JSONObject.toBean(node, ServiceTask.class);
                        serviceTaskTemp.setDelegateExpression("${serviceTaskDelegate}");
                        initEventListener(serviceTaskTemp,node);
                        initMessageListener(serviceTaskTemp,node);
                        serviceTask.add(serviceTaskTemp);
                        baseFlowNodeTemp  = serviceTaskTemp;
                        break;
                    case "ReceiveTask":
                        ReceiveTask receiveTaskTemp = (ReceiveTask) JSONObject.toBean(node, ReceiveTask.class);
                        extensionElement  = receiveTaskTemp.getExtensionElement();
                        if(extensionElement == null){
                            extensionElement = new ExtensionElement();
                        }
                        //添加默认任务到达监听器
                        executionListener = new ExecutionListener();
                        executionListener.setEvent("start");
                        executionListener.setDelegateExpression("${receiveTaskBeforeListener}");
                        executionListeners = extensionElement.getExecutionListener();
                        if(executionListeners == null){
                            executionListeners = new ArrayList<ExecutionListener>();
                        }
                        executionListeners.add(executionListener);

                        //添加默认任务执行后监听器
                        executionListener = new ExecutionListener();
                        executionListener.setEvent("end");
                        executionListener.setDelegateExpression("${receiveTaskAfterListener}");
                        executionListeners.add(executionListener);

                        extensionElement.setExecutionListener(executionListeners);
                        receiveTaskTemp.setExtensionElement(extensionElement);
                        initEventListener(receiveTaskTemp,node);
                        initMessageListener(receiveTaskTemp,node);
                        receiveTask.add(receiveTaskTemp);
                        baseFlowNodeTemp  = receiveTaskTemp;
                        break;
                    case "ExclusiveGateway":
                        ExclusiveGateway exclusiveGatewayTemp = (ExclusiveGateway) JSONObject.toBean(node, ExclusiveGateway.class);
                        exclusiveGateway.add(exclusiveGatewayTemp);
                        baseFlowNodeTemp  = exclusiveGatewayTemp;
                        break;
                    case "InclusiveGateway":
                        InclusiveGateway inclusiveGatewayTemp = (InclusiveGateway) JSONObject.toBean(node, InclusiveGateway.class);
                        inclusiveGateway.add(inclusiveGatewayTemp);
                        baseFlowNodeTemp  = inclusiveGatewayTemp;
                        break;
                    case "ParallelGateway":
                        ParallelGateway parallelGatewayTemp = (ParallelGateway) JSONObject.toBean(node, ParallelGateway.class);
                        parallelGateway.add(parallelGatewayTemp);
                        baseFlowNodeTemp  = parallelGatewayTemp;
                        break;
                    case "EventGateway":
                        EventGateway eventGatewayTemp = (EventGateway) JSONObject.toBean(node, EventGateway.class);
                        eventGateway.add(eventGatewayTemp);
                        baseFlowNodeTemp  = eventGatewayTemp;
                        break;
                    case "TerminateEndEvent":
                        EndEvent terminateEndEventTemp = (EndEvent) JSONObject.toBean(node, EndEvent.class);
                        terminateEndEventTemp.setTerminateEventDefinition("");

                        extensionElement  = terminateEndEventTemp.getExtensionElement();
                        if(extensionElement == null){
                            extensionElement = new ExtensionElement();
                        }
                        //添加默认启动完成监听器
                        executionListener = new ExecutionListener();
                        executionListener.setEvent("end");
                        executionListener.setDelegateExpression("${endEventCompleteListener}");
                        executionListeners = extensionElement.getExecutionListener();
                        if(executionListeners == null){
                            executionListeners = new ArrayList<ExecutionListener>();
                        }
                        executionListeners.add(executionListener);
                        extensionElement.setExecutionListener(executionListeners);
                        terminateEndEventTemp.setExtensionElement(extensionElement);
                        endEvent.add(terminateEndEventTemp);
                        baseFlowNodeTemp  = terminateEndEventTemp;
                        break;
                    case "CallActivity":
                        CallActivity callActivityTemp = (CallActivity) JSONObject.toBean(node, CallActivity.class);
                        net.sf.json.JSONObject normal = node.getJSONObject("nodeConfig").getJSONObject("normal");
                        String callActivityDefKey = (String)normal.get("callActivityDefKey");
                        callActivityTemp.setCalledElement(callActivityDefKey);
                        callActivity.add(callActivityTemp);
                        baseFlowNodeTemp  = callActivityTemp;
                        break;
                    default:
                        break;
                }
                addSequenceFlow(baseFlowNodeTemp, targets);
            }
        }
    }

    //添加自定义事件监听（用于用户任务事前、事后）
    private void initEventListener( BaseFlowNode currentFlowTemp ,JSONObject node){
        ExtensionElement extensionElement = null;
        ExecutionListener executionListener = null;
        List<ExecutionListener> executionListeners = null;
        try {
            net.sf.json.JSONObject event = node.getJSONObject("nodeConfig").getJSONObject("event");
            if (event != null && !event.isEmpty()) {
                String beforeExcuteServiceId =  (String)event.get("beforeExcuteServiceId");
                extensionElement  = currentFlowTemp.getExtensionElement();
                if(extensionElement == null){
                    extensionElement = new ExtensionElement();
                }
                if(!StringUtils.isEmpty(beforeExcuteServiceId)){
                    //添加默认任务创建监听器
                    executionListener = new ExecutionListener();
                    executionListener.setEvent("start");
                    executionListener.setDelegateExpression("${commonUserTaskCreateListener}");
                    executionListeners = extensionElement.getExecutionListener();
                    if(executionListeners == null){
                        executionListeners = new ArrayList<ExecutionListener>();
                    }
                    executionListeners.add(executionListener);
                    extensionElement.setExecutionListener(executionListeners);
                    currentFlowTemp.setExtensionElement(extensionElement);
                }
                String afterExcuteServiceId =  (String)event.get("afterExcuteServiceId");
                if(!StringUtils.isEmpty(afterExcuteServiceId)){
                    //添加默认任务完成监听器
                    executionListener = new ExecutionListener();
                    executionListener.setEvent("end");
                    executionListener.setDelegateExpression("${commonUserTaskCompleteListener}");
                    executionListeners = extensionElement.getExecutionListener();
                    if(executionListeners == null){
                        executionListeners = new ArrayList<ExecutionListener>();
                    }
                    executionListeners.add(executionListener);
                    extensionElement.setExecutionListener(executionListeners);
                    currentFlowTemp.setExtensionElement(extensionElement);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

    }

    private void initMessageListener( BaseFlowNode currentFlowTemp ,JSONObject node){
        //添加自定义事件监听（用于用户任务事前、事后）
        ExtensionElement extensionElement = null;
        ExecutionListener executionListener = null;
        List<ExecutionListener> executionListeners = null;
        //添加任务执行监听器，（目前只用于邮件发送）
        try {
            String[] notifyType={"notifyExecutor","notifyStarter","notifyPosition"};
            net.sf.json.JSONObject notify = node.getJSONObject("nodeConfig").getJSONObject("notify");
            if (notify != null && !notify.isEmpty()) {
                JSONObject beforeNotify =  notify.getJSONObject("before");
                extensionElement  = currentFlowTemp.getExtensionElement();
                if(extensionElement == null){
                    extensionElement = new ExtensionElement();
                }
                if(beforeNotify != null ){
                    for(int i=0;i<notifyType.length;i++){
                        JSONObject notifyTypeJsonObject = beforeNotify.getJSONObject(notifyType[i]);
                        if(notifyTypeJsonObject==null || notifyTypeJsonObject.isEmpty()){
                            continue;
                        }
                        JSONArray selectType = notifyTypeJsonObject
                                .getJSONArray("type");
                        if(selectType !=null && !selectType.isEmpty() && selectType.size()>0){
                            //添加执行前事件监听器
                            executionListener = new ExecutionListener();
                            executionListener.setEvent("start");
                            executionListener.setDelegateExpression("${messageBeforeListener}");
                            executionListeners = extensionElement.getExecutionListener();
                            if(executionListeners == null){
                                executionListeners = new ArrayList<ExecutionListener>();
                            }
                            executionListeners.add(executionListener);
                            extensionElement.setExecutionListener(executionListeners);
                            currentFlowTemp.setExtensionElement(extensionElement);
                            break;
                        }
                    }
                }
                JSONObject afterNotify =  notify.getJSONObject("after");
                if(afterNotify != null ){
                    for(int i=1;i<notifyType.length;i++){
                        JSONObject notifyTypeJsonObject = afterNotify.getJSONObject(notifyType[i]);
                        if(notifyTypeJsonObject==null || notifyTypeJsonObject.isEmpty()){
                            continue;
                        }
                        JSONArray selectType = notifyTypeJsonObject.getJSONArray("type");
                        if(selectType !=null && !selectType.isEmpty() && selectType.size()>0){
                            //添加执行前事件监听器
                            executionListener = new ExecutionListener();
                            executionListener.setEvent("end");
                            executionListener.setDelegateExpression("${messageAfterListener}");
                            executionListeners = extensionElement.getExecutionListener();
                            if(executionListeners == null){
                                executionListeners = new ArrayList<ExecutionListener>();
                            }
                            executionListeners.add(executionListener);
                            extensionElement.setExecutionListener(executionListeners);
                            currentFlowTemp.setExtensionElement(extensionElement);
                            break;
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

    }



    private void addSequenceFlow(BaseFlowNode currentNode, JSONArray targets) {
        String sourceId =  currentNode.getId();
        for (int i = 0; i < targets.size(); i++) {
            JSONObject target = targets.getJSONObject(i);
            String id = "flow" + (++lineCount);
            String targetId = target.getString("targetId");
            String uel = target.getString("uel");
            if(StringUtils.isNotEmpty(uel)){
                try{
                JSONObject uelObject = target.getJSONObject("uel");
                if(uelObject!=null){
                    String isDefault = uelObject.get("isDefault")+"";
                    if("true".equalsIgnoreCase(isDefault) && currentNode.getDefaultSequence()==null){
                        currentNode.setDefaultSequence(id);
                    }
//                    Boolean agree = (Boolean)uelObject.get("agree");//针对扩展的审批、会签节点
//                    if(agree == false){
//                       currentNode.setDefaultSequence(id);
//                    }
                }}catch (Exception e){
                    e.printStackTrace();
                }
            }
            SequenceFlow sf = new SequenceFlow(id, sourceId, targetId, uel);
            sequenceFlow.add(sf);
        }
    }




    public List<StartEvent> getStartEvent() {
        return startEvent;
    }

    public void setStartEvent(List<StartEvent> startEvent) {
        this.startEvent = startEvent;
    }

    public List<EndEvent> getEndEvent() {
        return endEvent;
    }

    public void setEndEvent(List<EndEvent> endEvent) {
        this.endEvent = endEvent;
    }

    public List<UserTask> getUserTask() {
        return userTask;
    }

    public void setUserTask(List<UserTask> userTask) {
        this.userTask = userTask;
    }

    public List<MailTask> getMailTask() {
        return mailTask;
    }

    public void setMailTask(List<MailTask> mailTask) {
        this.mailTask = mailTask;
    }

    public List<ManualTask> getManualTask() {
        return manualTask;
    }

    public void setManualTask(List<ManualTask> manualTask) {
        this.manualTask = manualTask;
    }

    public List<ScriptTask> getScriptTask() {
        return scriptTask;
    }

    public void setScriptTask(List<ScriptTask> scriptTask) {
        this.scriptTask = scriptTask;
    }

    public List<ServiceTask> getServiceTask() {
        return serviceTask;
    }

    public void setServiceTask(List<ServiceTask> serviceTask) {
        this.serviceTask = serviceTask;
    }

    public List<ExclusiveGateway> getExclusiveGateway() {
        return exclusiveGateway;
    }

    public void setExclusiveGateway(List<ExclusiveGateway> exclusiveGateway) {
        this.exclusiveGateway = exclusiveGateway;
    }

    public List<InclusiveGateway> getInclusiveGateway() {
        return inclusiveGateway;
    }

    public void setInclusiveGateway(List<InclusiveGateway> inclusiveGateway) {
        this.inclusiveGateway = inclusiveGateway;
    }

    public List<ParallelGateway> getParallelGateway() {
        return parallelGateway;
    }

    public void setParallelGateway(List<ParallelGateway> parallelGateway) {
        this.parallelGateway = parallelGateway;
    }

    public List<EventGateway> getEventGateway() {
        return eventGateway;
    }

    public void setEventGateway(List<EventGateway> eventGateway) {
        this.eventGateway = eventGateway;
    }

    public List<SequenceFlow> getSequenceFlow() {
        return sequenceFlow;
    }

    public void setSequenceFlow(List<SequenceFlow> sequenceFlow) {
        this.sequenceFlow = sequenceFlow;
    }

    public String getFlowDefVersionId() {
        return flowDefVersionId;
    }

    public void setFlowDefVersionId(String flowDefVersionId) {
        this.flowDefVersionId = flowDefVersionId;
    }

    public List<ReceiveTask> getReceiveTask() {
        return receiveTask;
    }

    public void setReceiveTask(List<ReceiveTask> receiveTask) {
        this.receiveTask = receiveTask;
    }

    public String getBeforeStartServiceId() {
        return beforeStartServiceId;
    }

    public void setBeforeStartServiceId(String beforeStartServiceId) {
        this.beforeStartServiceId = beforeStartServiceId;
    }

    public String getBeforeStartServiceName() {
        return beforeStartServiceName;
    }

    public void setBeforeStartServiceName(String beforeStartServiceName) {
        this.beforeStartServiceName = beforeStartServiceName;
    }

    public String getAfterEndServiceId() {
        return afterEndServiceId;
    }

    public void setAfterEndServiceId(String afterEndServiceId) {
        this.afterEndServiceId = afterEndServiceId;
    }

    public String getAfterEndServiceName() {
        return afterEndServiceName;
    }

    public void setAfterEndServiceName(String afterEndServiceName) {
        this.afterEndServiceName = afterEndServiceName;
    }

    public String getAfterStartServiceId() {
        return afterStartServiceId;
    }

    public void setAfterStartServiceId(String afterStartServiceId) {
        this.afterStartServiceId = afterStartServiceId;
    }

    public String getAfterStartServiceName() {
        return afterStartServiceName;
    }

    public void setAfterStartServiceName(String afterStartServiceName) {
        this.afterStartServiceName = afterStartServiceName;
    }

    public Boolean getAfterStartServiceAync() {
        return afterStartServiceAync;
    }

    public void setAfterStartServiceAync(Boolean afterStartServiceAync) {
        this.afterStartServiceAync = afterStartServiceAync;
    }

    public String getBeforeEndServiceId() {
        return beforeEndServiceId;
    }

    public void setBeforeEndServiceId(String beforeEndServiceId) {
        this.beforeEndServiceId = beforeEndServiceId;
    }

    public String getBeforeEndServiceName() {
        return beforeEndServiceName;
    }

    public void setBeforeEndServiceName(String beforeEndServiceName) {
        this.beforeEndServiceName = beforeEndServiceName;
    }
}
