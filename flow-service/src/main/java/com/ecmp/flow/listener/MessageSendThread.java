package com.ecmp.flow.listener;

import com.ecmp.config.util.ApiClient;
import com.ecmp.context.ContextUtil;
import com.ecmp.flow.basic.vo.Executor;
import com.ecmp.flow.common.util.Constants;
import com.ecmp.flow.dao.FlowDefVersionDao;
import com.ecmp.flow.dao.FlowHistoryDao;
import com.ecmp.flow.dao.FlowTaskDao;
import com.ecmp.flow.dao.util.PageUrlUtil;
import com.ecmp.flow.entity.FlowDefVersion;
import com.ecmp.flow.util.BpmnUtil;
import com.ecmp.flow.util.FlowCommonUtil;
import com.ecmp.flow.vo.bpmn.Definition;
import com.ecmp.log.util.LogUtil;
import com.ecmp.notity.entity.EcmpMessage;
import com.ecmp.notity.entity.NotifyType;
import com.ecmp.util.JsonUtils;
import com.ecmp.vo.ResponseData;
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
import org.springframework.util.CollectionUtils;

import javax.ws.rs.core.GenericType;
import java.util.*;


public class MessageSendThread implements Runnable {


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
        String actTaskDefKey = taskEntity.getActivityId();
        String actProcessDefinitionId = execution.getProcessDefinitionId();
        ProcessInstance instance = taskEntity.getProcessInstance();
        HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery().processInstanceId(instance.getId()).singleResult();
        String startUserId;

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
        String taskName = (String) taskEntity.getActivity().getProperty("name");
        if (notify != null) {
            JSONObject currentNotify = notify.getJSONObject(eventType);
            if (currentNotify != null) {//只有执行前才能发给执行人
                JSONArray selectType = null;
                if ("before".equalsIgnoreCase(eventType)) {
                    JSONObject notifyExecutor = null;
                    try {
                        notifyExecutor = currentNotify.getJSONObject("notifyExecutor"); //发送给执行人
                        selectType = notifyExecutor.getJSONArray("type");
                    } catch (Exception e) {
                        LogUtil.error(e.getMessage(), e);
                    }
                    if (!CollectionUtils.isEmpty(selectType)) {
                        String content = notifyExecutor.getString("content");
                        List<String> receiverIds = getReceiverIds(currentNode, taskEntity);
                        Object[] types = selectType.toArray();
                        for (Object type : types) {
                            NotifyType typeNotify = null;
                            if ("EMAIL".equalsIgnoreCase(type.toString())) { //邮件【执行人】
                                typeNotify = NotifyType.EMAIL;
                            } else if ("DINGDING".equalsIgnoreCase(type.toString())) { //钉钉【执行人】
                                typeNotify = NotifyType.DingTalk;
                            } else if ("MESSAGE".equalsIgnoreCase(type.toString())) { //站内信【执行人】
                                typeNotify = NotifyType.SEI_REMIND;
                            }
                            this.sendMessageByType(flowDefVersion, taskName, typeNotify, receiverIds, content, "执行人");
                        }
                    }
                }


                String multiInstance = (String) taskEntity.getActivity().getProperty("multiInstance");
                Boolean isMmultiInstance = StringUtils.isNotEmpty(multiInstance);
                if (!isMmultiInstance || taskEntity.getTransition() != null) {
                    JSONObject notifyStarter = currentNotify.getJSONObject("notifyStarter"); //发送流程发起人
                    selectType = notifyStarter.getJSONArray("type");
                    if (!CollectionUtils.isEmpty(selectType)) {
                        String content = notifyStarter.getString("content");
                        List<String> receiverIds = new ArrayList<>();
                        receiverIds.add(startUserId);
                        Object[] types = selectType.toArray();
                        for (Object type : types) {
                            NotifyType typeNotify = null;
                            if ("EMAIL".equalsIgnoreCase(type.toString())) { //邮件【发起人】
                                typeNotify = NotifyType.EMAIL;
                            } else if ("DINGDING".equalsIgnoreCase(type.toString())) { //钉钉【发起人】
                                typeNotify = NotifyType.DingTalk;
                            } else if ("MESSAGE".equalsIgnoreCase(type.toString())) { //站内信【发起人】
                                typeNotify = NotifyType.SEI_REMIND;
                            }
                            this.sendMessageByType(flowDefVersion, taskName, typeNotify, receiverIds, content, "发起人");
                        }
                    }


                    JSONObject notifyPosition = currentNotify.getJSONObject("notifyPosition");//发送给选定的岗位
                    selectType = notifyPosition.getJSONArray("type");
                    if (!CollectionUtils.isEmpty(selectType)) {
                        String content = notifyPosition.getString("content");
                        JSONArray notifyPositionJsonArray = notifyPosition.getJSONArray("positionData");
                        List<String> receiverIds = new ArrayList<>();
                        if (!CollectionUtils.isEmpty(notifyPositionJsonArray)) {
                            List<String> idList = new ArrayList<>();
                            for (int i = 0; i < notifyPositionJsonArray.size(); i++) {
                                JSONObject jsonObject = notifyPositionJsonArray.getJSONObject(i);
                                String positionId = jsonObject.getString("id");
                                if (StringUtils.isNotEmpty(positionId)) {
                                    idList.add(positionId);
                                }
                            }
                            //通过岗位ids和单据所属组织机构id来获取执行人
                            String orgId = (String) execution.getVariable(Constants.ORG_ID);
                            List<Executor> employees = flowCommonUtil.getBasicExecutorsByPositionIds(idList, orgId);
                            Set<String> linkedHashSetReceiverIds = new LinkedHashSet<>();
                            if (!CollectionUtils.isEmpty(employees)) {
                                for (Executor executor : employees) {
                                    linkedHashSetReceiverIds.add(executor.getId());
                                }
                            }
                            receiverIds.addAll(linkedHashSetReceiverIds);
                        }

                        Object[] types = selectType.toArray();
                        for (Object type : types) {
                            NotifyType typeNotify = null;
                            if ("EMAIL".equalsIgnoreCase(type.toString())) { //邮件【指定岗位】
                                typeNotify = NotifyType.EMAIL;
                            } else if ("DINGDING".equalsIgnoreCase(type.toString())) {//钉钉【指定岗位】
                                typeNotify = NotifyType.DingTalk;
                            } else if ("MESSAGE".equalsIgnoreCase(type.toString())) {//站内信【指定岗位】
                                typeNotify = NotifyType.SEI_REMIND;
                            }
                            this.sendMessageByType(flowDefVersion, taskName, typeNotify, receiverIds, content, "指定岗位");
                        }
                    }
                }
            }
        }
    }


    /**
     * @param flowDefVersion 流程版本
     * @param taskName       任务名称
     * @param notifyType     发送消息中心类型：邮件、钉钉、站内信
     * @param receiverIds    接收人ID集合
     * @param content        流程定义中设置的通知内容
     * @param sendType       流程定义中的发送类型：发起人、执行人、指定岗位
     */
    private void sendMessageByType(FlowDefVersion flowDefVersion, String taskName,
                                   NotifyType notifyType, List<String> receiverIds,
                                   String content, String sendType) {
        if (notifyType == null) {
            LogUtil.error("【" + flowDefVersion.getName() + ":" + taskName + "】发送【" + sendType + "】通知错误：获取通知类型为空！");
            return;
        }
        if (CollectionUtils.isEmpty(receiverIds)) {
            LogUtil.error("【" + flowDefVersion.getName() + ":" + taskName + "】发送【" + sendType + "】通知错误：获取通知人为空！");
            return;
        }
        EcmpMessage message = new EcmpMessage();
        message.setCanToSender(true); //可以发送给发件人
        message.setSenderId(ContextUtil.getUserId()); //发布人ID
        message.setSubject(flowDefVersion.getName() + ":" + taskName);//消息主题：流程名+任务名
        message.setNotifyTypes(Arrays.asList(notifyType)); //消息类型
        message.setReceiverIds(receiverIds); //接收用户ID清单

        //设置内容模板或消息内容
        if (notifyType.equals(NotifyType.EMAIL) || notifyType.equals(NotifyType.SEI_REMIND)) { //邮件和站内信设置消息模板
            if (StringUtils.isEmpty(contentTemplateCode)) {
                message.setContent(content); //消息内容
            } else {
                Map<String, Object> contentTemplateParams = getParamsMap();
                contentTemplateParams.put("remark", content);//备注说明
                message.setContentTemplateParams(contentTemplateParams); //模板参数
                message.setContentTemplateCode(contentTemplateCode);//模板代码
            }
        } else if (notifyType.equals(NotifyType.DingTalk)) { //钉钉发送只设置流程定义的消息框内容
            message.setContent(content);
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                sendNotifyMessage(message);
            }
        }).start();
    }


    private void sendNotifyMessage(EcmpMessage message) {
        String notifyUrl = Constants.getConfigValueByApi("NOTIFY_API");
        String endNotifyPath = PageUrlUtil.buildUrl(notifyUrl, "/notify/send");
        String messageLog = "开始调用【消息模块】，接口url=" + endNotifyPath + ",参数值" + JsonUtils.toJson(message);
        try {
            ResponseData result = ApiClient.postViaProxyReturnResult(endNotifyPath, new GenericType<ResponseData>() {
            }, message);
            if (!result.successful()) {
                LogUtil.error(messageLog + "-调用报错,返回错误信息【" + JsonUtils.toJson(result) + "】");
            }
        } catch (Exception e) {
            LogUtil.error(messageLog + "内部报错!", e);
        }
    }


    private Map<String, Object> getParamsMap() {
        Map<String, Object> contentTemplateParams = new HashMap<>();
        String workCaption = (String) execution.getVariable("workCaption");
        String businessName = (String) execution.getVariable("name");
        String businessCode = (String) execution.getVariable("businessCode");
        String opinion = (String) execution.getVariable("opinion");
        if ("after".equalsIgnoreCase(eventType)) {//当前任务执行意见
            String eventName = execution.getEventName();
            if (!"end".equalsIgnoreCase(eventName)) {
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
        contentTemplateParams.put("ECMP_BASIC_WEB_HOST", Constants.getBaseWeb());//web基地址
        return contentTemplateParams;
    }

    private List<String> getReceiverIds(net.sf.json.JSONObject currentNode, ExecutionEntity taskEntity) {
        List<String> receiverIds = new ArrayList<>();
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
