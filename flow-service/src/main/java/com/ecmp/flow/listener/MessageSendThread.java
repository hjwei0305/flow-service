package com.ecmp.flow.listener;

import com.ecmp.config.util.ApiClient;
import com.ecmp.context.ContextUtil;
import com.ecmp.flow.basic.vo.Executor;
import com.ecmp.flow.dao.FlowDefVersionDao;
import com.ecmp.flow.dao.FlowHistoryDao;
import com.ecmp.flow.dao.FlowTaskDao;
import com.ecmp.flow.entity.FlowDefVersion;
import com.ecmp.flow.util.BpmnUtil;
import com.ecmp.flow.util.FlowCommonUtil;
import com.ecmp.flow.vo.bpmn.Definition;
import com.ecmp.notify.api.INotifyService;
import com.ecmp.notity.entity.EcmpMessage;
import com.ecmp.notity.entity.NotifyType;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.activiti.engine.HistoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.runtime.ProcessInstance;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * *************************************************************************************************
 * <p/>
 * 实现功能：
 * <p>
 * ------------------------------------------------------------------------------------------------
 * 版本          变更时间             变更人                     变更原因
 * ------------------------------------------------------------------------------------------------
 * 1.0.00      2017/6/27 12:22      谭军(tanjun)                    新建
 * <p/>
 * *************************************************************************************************
 */
public class MessageSendThread implements Runnable {

    private final Logger logger = LoggerFactory.getLogger(MessageBeforeListener.class);

    private String eventType;
    private DelegateExecution execution;
    private String contentTemplateCode;

    private FlowTaskDao flowTaskDao;

    private FlowDefVersionDao flowDefVersionDao;

    private HistoryService historyService;

    private FlowHistoryDao flowHistoryDao;

    private RuntimeService runtimeService;

    private TaskService taskService;

    private FlowCommonUtil flowCommonUtil;

    public MessageSendThread() {
    }

    public MessageSendThread(String eventType, DelegateExecution execution, String contentTemplateCode) {
        this.eventType = eventType;
        this.execution = execution;
        this.contentTemplateCode = contentTemplateCode;
    }

    @Override
    public void run() {
        ExecutionEntity taskEntity = (ExecutionEntity) execution;
        boolean canToSender = true;
        String actTaskDefKey = taskEntity.getActivityId();
        String actProcessDefinitionId = execution.getProcessDefinitionId();
        ProcessInstance instance = taskEntity.getProcessInstance();
        HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery().processInstanceId(instance.getId()).singleResult();
        String startUserId = null;

        if (historicProcessInstance != null) {
            startUserId = historicProcessInstance.getStartUserId();
        } else {
            startUserId = ContextUtil.getUserId();
        }
        FlowDefVersion flowDefVersion = flowDefVersionDao.findByActDefId(actProcessDefinitionId);
        String flowDefJson = flowDefVersion.getDefJson();
        JSONObject defObj = JSONObject.fromObject(flowDefJson);
        Definition definition = (Definition) JSONObject.toBean(defObj, Definition.class);
        net.sf.json.JSONObject currentNode = definition.getProcess().getNodes().getJSONObject(actTaskDefKey);
        net.sf.json.JSONObject notify = currentNode.getJSONObject("nodeConfig").getJSONObject("notify");
        if (notify != null) {
            JSONObject currentNotify = notify.getJSONObject(eventType);
//            String[] notifyType = {"notifyExecutor", "notifyStarter", "notifyPosition"};
            if (currentNotify != null) {//只有执行前才能发给执行人
                JSONArray selectType = null;
                //发送给流程的实际执行人
                if ("before".equalsIgnoreCase(eventType)) {
                    JSONObject notifyExecutor = null;
                    try {
                        notifyExecutor = currentNotify.getJSONObject("notifyExecutor");
                        selectType = notifyExecutor.getJSONArray("type");
                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);
                    }
                    if (selectType != null && !selectType.isEmpty() && selectType.size() > 0) {
                        Object[] types = selectType.toArray();
                        for (Object type : types) {
                            if ("EMAIL".equalsIgnoreCase(type.toString())) {
                                EcmpMessage message = new EcmpMessage();
                                String senderId = ContextUtil.getUserId();
                                message.setSenderId(senderId);
                                List<String> receiverIds = getReceiverIds(currentNode, taskEntity);
                                if (receiverIds == null || receiverIds.isEmpty()) {
                                    continue;
                                }
                                message.setReceiverIds(receiverIds);
                                if(StringUtils.isEmpty(contentTemplateCode)){
                                    message.setContent(notifyExecutor.getString("content"));
                                }else{
                                    Map<String, Object> contentTemplateParams = getParamsMap();
                                    contentTemplateParams.put("remark", notifyExecutor.getString("content"));//备注说明
                                    message.setContentTemplateParams(contentTemplateParams);
                                    message.setContentTemplateCode(contentTemplateCode);//模板代码
                                }
                                message.setCanToSender(canToSender);
                                this.emailSend(message, flowDefVersion);
                            } else if ("DINGDING".equalsIgnoreCase(type.toString())) {//钉钉
                                EcmpMessage message = new EcmpMessage();
                                String senderId = ContextUtil.getUserId();
                                message.setSenderId(senderId);
                                List<String> receiverIds = getReceiverIds(currentNode, taskEntity);
                                if (receiverIds == null || receiverIds.isEmpty()) {
                                    continue;
                                }
                                message.setReceiverIds(receiverIds);
                                message.setContent(notifyExecutor.getString("content"));
                                message.setCanToSender(canToSender);
                                this.dingDingSend(message, flowDefVersion);
                            } else if ("SMS".equalsIgnoreCase(type.toString())) {//后期扩展
                            } else if ("APP".equalsIgnoreCase(type.toString())) {//后期扩展
                            }
                        }
                    }
                }
                String multiInstance = (String) taskEntity.getActivity().getProperty("multiInstance");
                Boolean isMmultiInstance = StringUtils.isNotEmpty(multiInstance);
                if ((isMmultiInstance && taskEntity.getTransition() != null) || !isMmultiInstance) {
                    //发送给流程启动人
                    JSONObject notifyStarter = currentNotify.getJSONObject("notifyStarter");
                    selectType = notifyStarter.getJSONArray("type");
                    if (selectType != null && !selectType.isEmpty() && selectType.size() > 0) {
                        Object[] types = selectType.toArray();
                        for (Object type : types) {
                            if ("EMAIL".equalsIgnoreCase(type.toString())) {
                                EcmpMessage message = new EcmpMessage();
                                String senderId = ContextUtil.getUserId();
                                message.setSenderId(senderId);
                                List<String> receiverIds = new ArrayList<String>();
                                receiverIds.add(startUserId);//流程启动人
                                message.setReceiverIds(receiverIds);
                                if(StringUtils.isEmpty(contentTemplateCode)){
                                    message.setContent(notifyStarter.getString("content"));
                                }else{
                                    Map<String, Object> contentTemplateParams = getParamsMap();
                                    contentTemplateParams.put("remark", notifyStarter.getString("content"));//备注说明
                                    message.setContentTemplateParams(contentTemplateParams);
                                    message.setContentTemplateCode(contentTemplateCode);//模板代码
                                }
                                message.setCanToSender(canToSender);
                                this.emailSend(message, flowDefVersion);
                            } else if ("DINGDING".equalsIgnoreCase(type.toString())) {//钉钉
                                EcmpMessage message = new EcmpMessage();
                                String senderId = ContextUtil.getUserId();
                                message.setSenderId(senderId);
                                List<String> receiverIds = new ArrayList<String>();
                                receiverIds.add(startUserId);//流程启动人
                                message.setReceiverIds(receiverIds);
                                message.setContent(notifyStarter.getString("content"));
                                message.setCanToSender(canToSender);
                                this.dingDingSend(message, flowDefVersion);
                            } else if ("SMS".equalsIgnoreCase(type.toString())) {//后期扩展
                            } else if ("APP".equalsIgnoreCase(type.toString())) {//后期扩展
                            }
                        }
                    }

                    //发送给选定的岗位
                    JSONObject notifyPosition = currentNotify.getJSONObject("notifyPosition");
                    selectType = notifyPosition.getJSONArray("type");
                    if (selectType != null && !selectType.isEmpty() && selectType.size() > 0) {
                        Object[] types = selectType.toArray();
                        for (Object type : types) {
                            if ("EMAIL".equalsIgnoreCase(type.toString())) {
                                JSONArray notifyPositionJsonArray = notifyPosition.getJSONArray("positionData");
                                if (notifyPositionJsonArray != null && !notifyPositionJsonArray.isEmpty()) {
                                    List<String> idList = new ArrayList<String>();
                                    for (int i = 0; i < notifyPositionJsonArray.size(); i++) {
                                        JSONObject jsonObject = notifyPositionJsonArray.getJSONObject(i);
                                        String positionId = jsonObject.getString("id");
                                        if (StringUtils.isNotEmpty(positionId)) {
                                            idList.add(positionId);
                                        }
                                    }
                                    List<Executor> employees;
                                    employees = flowCommonUtil.getBasicExecutorsByPositionIds(idList, "");
                                    Set<String> linkedHashSetReceiverIds = new LinkedHashSet<String>();
                                    List<String> receiverIds = new ArrayList<String>();
                                    if (employees != null && !employees.isEmpty()) {
                                        for (Executor executor : employees) {
                                            linkedHashSetReceiverIds.add(executor.getId());
                                        }
                                    } else {
                                        return;
                                    }
                                    receiverIds.addAll(linkedHashSetReceiverIds);
                                    EcmpMessage message = new EcmpMessage();
                                    String senderId = ContextUtil.getUserId();
                                    message.setSenderId(senderId);
                                    message.setReceiverIds(receiverIds);
                                    if(StringUtils.isEmpty(contentTemplateCode)){
                                        message.setContent(notifyPosition.getString("content"));
                                    }else{
                                        Map<String, Object> contentTemplateParams = getParamsMap();
                                        contentTemplateParams.put("remark", notifyPosition.getString("content"));//备注说明
                                        message.setContentTemplateParams(contentTemplateParams);
                                        message.setContentTemplateCode(contentTemplateCode);//模板代码
                                    }
                                    message.setCanToSender(canToSender);
                                    this.emailSend(message, flowDefVersion);
                                }

                            } else if ("DINGDING".equalsIgnoreCase(type.toString())) {//钉钉
                                JSONArray notifyPositionJsonArray = notifyPosition.getJSONArray("positionData");
                                if (notifyPositionJsonArray != null && !notifyPositionJsonArray.isEmpty()) {
                                    List<String> idList = new ArrayList<String>();
                                    for (int i = 0; i < notifyPositionJsonArray.size(); i++) {
                                        JSONObject jsonObject = notifyPositionJsonArray.getJSONObject(i);
                                        String positionId = jsonObject.getString("id");
                                        if (StringUtils.isNotEmpty(positionId)) {
                                            idList.add(positionId);
                                        }
                                    }
                                    List<Executor> employees;
                                    employees = flowCommonUtil.getBasicExecutorsByPositionIds(idList, "");
                                    Set<String> linkedHashSetReceiverIds = new LinkedHashSet<String>();
                                    List<String> receiverIds = new ArrayList<String>();
                                    if (employees != null && !employees.isEmpty()) {
                                        for (Executor executor : employees) {
                                            linkedHashSetReceiverIds.add(executor.getId());
                                        }
                                    } else {
                                        return;
                                    }
                                    receiverIds.addAll(linkedHashSetReceiverIds);
                                    EcmpMessage message = new EcmpMessage();
                                    String senderId = ContextUtil.getUserId();
                                    message.setSenderId(senderId);
                                    message.setReceiverIds(receiverIds);
                                    message.setContent(notifyPosition.getString("content"));
                                    message.setCanToSender(canToSender);
                                    this.dingDingSend(message, flowDefVersion);
                                }
                            } else if ("SMS".equalsIgnoreCase(type.toString())) {//后期扩展
                            } else if ("APP".equalsIgnoreCase(type.toString())) {//后期扩展
                            }
                        }
                    }
                }
            }
        }
    }

    private void dingDingSend(EcmpMessage message, FlowDefVersion flowDefVersion) {
        INotifyService iNotifyService = ApiClient.createProxy(INotifyService.class);
        ExecutionEntity taskEntity = (ExecutionEntity) execution;
        String taskName = (String) taskEntity.getActivity().getProperty("name");
        message.setSubject(flowDefVersion.getName() + ":" + taskName);//流程名+任务名
        List<NotifyType> notifyTypes = new ArrayList<NotifyType>();
        notifyTypes.add(NotifyType.DingTalk);
        message.setNotifyTypes(notifyTypes);
        new Thread(new Runnable() {
            @Override
            public void run() {
                iNotifyService.send(message);
            }
        }).start();
    }

    private void emailSend(EcmpMessage message, FlowDefVersion flowDefVersion) {
        INotifyService iNotifyService = ApiClient.createProxy(INotifyService.class);
        ExecutionEntity taskEntity = (ExecutionEntity) execution;
        String taskName = (String) taskEntity.getActivity().getProperty("name");
        message.setSubject(flowDefVersion.getName() + ":" + taskName);//流程名+任务名
        List<NotifyType> notifyTypes = new ArrayList<NotifyType>();
        notifyTypes.add(NotifyType.Email);
        message.setNotifyTypes(notifyTypes);
        new Thread(new Runnable() {
            @Override
            public void run() {
                iNotifyService.send(message);
            }
        }).start();
    }

    private Map<String, Object> getParamsMap() {
        Map<String, Object> contentTemplateParams = new HashMap<>();
        String workCaption = (String) execution.getVariable("workCaption");
        String businessName = (String) execution.getVariable("name");
        String businessCode = (String) execution.getVariable("businessCode");
        String opinion = (String) execution.getVariable("opinion");
        if ("after".equalsIgnoreCase(eventType)) {//当前任务执行意见
            String eventName = execution.getEventName();
            if ("end".equalsIgnoreCase(eventName)) {
                opinion = ((ExecutionEntity) execution).getDeleteReason();
            }
        }

        contentTemplateParams.put("businessName", businessName);//业务单据名称
        contentTemplateParams.put("businessCode", businessCode);//业务单据代码
        if ("before".equalsIgnoreCase(eventType)) {
            contentTemplateParams.put("preOpinion", opinion);//上一步审批意见
        } else if ("after".equalsIgnoreCase(eventType)) {
            contentTemplateParams.put("opinion", opinion);//审批意见
        }
        contentTemplateParams.put("workCaption", workCaption);//业务单据工作说明
        return contentTemplateParams;
    }

    private List<String> getReceiverIds(net.sf.json.JSONObject currentNode, ExecutionEntity taskEntity) {
        List<String> receiverIds = new ArrayList<String>();
        if ("before".equalsIgnoreCase(eventType)) {
            String nodeParamName = BpmnUtil.getCurrentNodeParamName(currentNode);
            Object userObject = execution.getVariable(nodeParamName);
            if (userObject != null && userObject instanceof List) {//单签的情况
                List userList = (List) userObject;
                for (Object userO : userList) {
                    receiverIds.add((String) userO);
                }
            } else {
                String userIds = (String) execution.getVariable(nodeParamName);
                if (StringUtils.isNotEmpty(userIds)) {
                    String[] userIdArray = userIds.split(",");
                    for (String userId : userIdArray) {
                        receiverIds.add(userId);
                    }
                }
            }
        } else if ("after".equalsIgnoreCase(eventType)) {//不做处理

        }
        return receiverIds;
    }


    public FlowTaskDao getFlowTaskDao() {
        return flowTaskDao;
    }

    public void setFlowTaskDao(FlowTaskDao flowTaskDao) {
        this.flowTaskDao = flowTaskDao;
    }

    public FlowDefVersionDao getFlowDefVersionDao() {
        return flowDefVersionDao;
    }

    public void setFlowDefVersionDao(FlowDefVersionDao flowDefVersionDao) {
        this.flowDefVersionDao = flowDefVersionDao;
    }

    public HistoryService getHistoryService() {
        return historyService;
    }

    public void setHistoryService(HistoryService historyService) {
        this.historyService = historyService;
    }

    public FlowHistoryDao getFlowHistoryDao() {
        return flowHistoryDao;
    }

    public void setFlowHistoryDao(FlowHistoryDao flowHistoryDao) {
        this.flowHistoryDao = flowHistoryDao;
    }

    public RuntimeService getRuntimeService() {
        return runtimeService;
    }

    public void setRuntimeService(RuntimeService runtimeService) {
        this.runtimeService = runtimeService;
    }

    public TaskService getTaskService() {
        return taskService;
    }

    public void setTaskService(TaskService taskService) {
        this.taskService = taskService;
    }

    public FlowCommonUtil getFlowCommonUtil() {
        return flowCommonUtil;
    }

    public void setFlowCommonUtil(FlowCommonUtil flowCommonUtil) {
        this.flowCommonUtil = flowCommonUtil;
    }
}
