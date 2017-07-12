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

    private List<StartEvent> startEvent;
    private List<EndEvent> endEvent;
    private List<UserTask> userTask;
    private List<MailTask> mailTask;
    private List<ManualTask> manualTask;
    private List<ScriptTask> scriptTask;
    private List<ServiceTask> serviceTask;
    private List<ExclusiveGateway> exclusiveGateway;
    private List<InclusiveGateway> inclusiveGateway;
    private List<ParallelGateway> parallelGateway;
    private List<EventGateway> eventGateway;
    private List<SequenceFlow> sequenceFlow;

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
            exclusiveGateway = new ArrayList<ExclusiveGateway>();
            inclusiveGateway = new ArrayList<InclusiveGateway>();
            parallelGateway = new ArrayList<ParallelGateway>();
            eventGateway = new ArrayList<EventGateway>();
            sequenceFlow = new ArrayList<SequenceFlow>();
            lineCount = 0;
            Iterator iterator = nodes.keys();
            while (iterator.hasNext()) {
                String key = (String) iterator.next();
                JSONObject node = nodes.getJSONObject(key);
                JSONArray targets = node.getJSONArray("target");
                BaseFlowNode baseFlowNodeTemp = null;
                switch (node.getString("type")) {
                    case "StartEvent":
                        StartEvent startEventTemp = (StartEvent) JSONObject.toBean(node, StartEvent.class);
                        startEventTemp.setInitiator("startUserId");
                        startEvent.add(startEventTemp);
                        baseFlowNodeTemp  = startEventTemp;
                        break;
                    case "EndEvent":
                        EndEvent endEventTemp = (EndEvent) JSONObject.toBean(node, EndEvent.class);
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
                            userTaskTemp.setConfig(multiInstanceConfig);

                            ExtensionElement extensionElement = new ExtensionElement();
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

                        }
                        //添加自定义用户任务事件监听（用于用户任务事前、事后）
                        try {
                            net.sf.json.JSONObject event = node.getJSONObject("nodeConfig").getJSONObject("event");
                            if (event != null && !event.isEmpty()) {
                                String beforeExcuteServiceId =  (String)event.get("beforeExcuteServiceId");
                                ExtensionElement extensionElement  = userTaskTemp.getExtensionElement();
                                if(extensionElement == null){
                                    extensionElement = new ExtensionElement();
                                }
                                if(!StringUtils.isEmpty(beforeExcuteServiceId)){
                                    //添加默认任务创建监听器
                                    TaskListener taskListener = new TaskListener();
                                    taskListener.setEvent("create");
                                    taskListener.setDelegateExpression("${commonUserTaskCreateListener}");
                                    List<TaskListener> taskListeners = extensionElement.getTaskListener();
                                    if(taskListeners == null){
                                        taskListeners = new ArrayList<TaskListener>();
                                    }
                                    taskListeners.add(taskListener);
                                    extensionElement.setTaskListener(taskListeners);
                                    userTaskTemp.setExtensionElement(extensionElement);
                                }
                                String afterExcuteServiceId =  (String)event.get("afterExcuteServiceId");
                                if(!StringUtils.isEmpty(afterExcuteServiceId)){
                                    //添加默认任务完成监听器
                                    TaskListener taskListener = new TaskListener();
                                    taskListener.setEvent("complete");
                                    taskListener.setDelegateExpression("${commonUserTaskCompleteListener}");
                                    List<TaskListener> taskListeners = extensionElement.getTaskListener();
                                    if(taskListeners == null){
                                        taskListeners = new ArrayList<TaskListener>();
                                    }
                                    taskListeners.add(taskListener);
                                    extensionElement.setTaskListener(taskListeners);
                                    userTaskTemp.setExtensionElement(extensionElement);
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
//                            logger.error(e.getMessage());
                        }


                        //添加任务执行监听器，（目前只用于邮件发送）
                        try {
                            String[] notifyType={"notifyExecutor","notifyStarter","notifyPosition"};
                            net.sf.json.JSONObject notify = node.getJSONObject("nodeConfig").getJSONObject("notify");
                            if (notify != null && !notify.isEmpty()) {
                                JSONObject beforeNotify =  notify.getJSONObject("before");
                                ExtensionElement extensionElement  = userTaskTemp.getExtensionElement();
                                if(extensionElement == null){
                                    extensionElement = new ExtensionElement();
                                }
                                if(beforeNotify != null ){
                                    for(int i=0;i<notifyType.length;i++){
                                        JSONArray selectType = beforeNotify.getJSONObject(notifyType[i]).getJSONArray("type");
                                        if(selectType !=null && !selectType.isEmpty() && selectType.size()>0){
                                            //添加执行前事件监听器
                                            ExecutionListener executionListener = new ExecutionListener();
                                            executionListener.setEvent("start");
                                            executionListener.setDelegateExpression("${messageBeforeListener}");
                                            List<ExecutionListener> executionListeners = extensionElement.getExecutionListener();
                                            if(executionListeners == null){
                                                executionListeners = new ArrayList<ExecutionListener>();
                                            }
                                            executionListeners.add(executionListener);
                                            extensionElement.setExecutionListener(executionListeners);
                                            userTaskTemp.setExtensionElement(extensionElement);
                                            break;
                                        }
                                    }
                                }
                                JSONObject afterNotify =  notify.getJSONObject("after");
                                if(afterNotify != null ){
                                    for(int i=0;i<notifyType.length;i++){
                                        JSONArray selectType = beforeNotify.getJSONObject(notifyType[i]).getJSONArray("type");
                                        if(selectType !=null && !selectType.isEmpty() && selectType.size()>0){
                                            //添加执行前事件监听器
                                            ExecutionListener executionListener = new ExecutionListener();
                                            executionListener.setEvent("end");
                                            executionListener.setDelegateExpression("${messageAfterListener}");
                                            List<ExecutionListener> executionListeners = extensionElement.getExecutionListener();
                                            if(executionListeners == null){
                                                executionListeners = new ArrayList<ExecutionListener>();
                                            }
                                            executionListeners.add(executionListener);
                                            extensionElement.setExecutionListener(executionListeners);
                                            userTaskTemp.setExtensionElement(extensionElement);
                                            break;
                                        }
                                    }
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
//                            logger.error(e.getMessage());
                        }



                        userTask.add(userTaskTemp);
                        baseFlowNodeTemp  = userTaskTemp;
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
                        serviceTask.add(serviceTaskTemp);
                        baseFlowNodeTemp  = serviceTaskTemp;
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
                        endEvent.add(terminateEndEventTemp);
                        baseFlowNodeTemp  = terminateEndEventTemp;
                        break;
                    default:
                        break;
                }
                addSequenceFlow(baseFlowNodeTemp, targets);
            }
        }
    }
//
//    private void initMessageListener( UserTask userTaskTemp,ExtensionElement extensionElement ,JSONObject beforeNotify){
//
//
//    }



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
                    if("true".equalsIgnoreCase(isDefault)&&currentNode.getDefaultSequence()==null){
                        currentNode.setDefaultSequence(id);
                    }
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
}
