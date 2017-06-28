package com.ecmp.flow.listener;

import com.ecmp.basic.api.IEmployeeService;
import com.ecmp.basic.entity.vo.Executor;
import com.ecmp.config.util.ApiClient;
import com.ecmp.context.ContextUtil;
import com.ecmp.flow.dao.FlowDefVersionDao;
import com.ecmp.flow.dao.FlowHistoryDao;
import com.ecmp.flow.dao.FlowTaskDao;
import com.ecmp.flow.entity.FlowDefVersion;
import com.ecmp.flow.entity.FlowHistory;
import com.ecmp.flow.entity.FlowTask;
import com.ecmp.flow.util.BpmnUtil;
import com.ecmp.flow.vo.bpmn.Definition;
import com.ecmp.notify.api.INotifyService;
import com.ecmp.notity.entity.EcmpMessage;
import com.ecmp.notity.entity.NotifyType;
import com.ecmp.util.JsonUtils;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.util.JSONUtils;
import org.activiti.engine.HistoryService;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.IdentityLinkEntity;
import org.activiti.engine.impl.pvm.process.TransitionImpl;
import org.activiti.engine.runtime.ProcessInstance;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
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

    private  FlowHistoryDao flowHistoryDao;

    private String dateFormat = "yyyy-MM-dd HH:mm:ss";


    public MessageSendThread() {
    }

    ;

    public MessageSendThread(String eventType, DelegateExecution execution, String contentTemplateCode) {
        this.eventType = eventType;
        this.execution = execution;
        this.contentTemplateCode = contentTemplateCode;
    }

    ;

    @Override
    public void run() {
        ExecutionEntity taskEntity = (ExecutionEntity) execution;

        String actTaskDefKey = taskEntity.getActivityId();
        String actProcessDefinitionId = execution.getProcessDefinitionId();
        ProcessInstance instance = taskEntity.getProcessInstance();
        HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery().processInstanceId(instance.getId()).singleResult();
        String startUserId = null;
        Date startTime = null;
        String startTimeStr = null;
        if (historicProcessInstance != null) {
            startUserId = historicProcessInstance.getStartUserId();
            startTime = historicProcessInstance.getStartTime();
            startTimeStr = new SimpleDateFormat(dateFormat).format(startTime);
        } else {
            startUserId = ContextUtil.getUserId();
            startTime = new Date();
            startTimeStr = new SimpleDateFormat(dateFormat).format(startTime);
        }

        String businessId = instance.getBusinessKey();
        FlowDefVersion flowDefVersion = flowDefVersionDao.findByActDefId(actProcessDefinitionId);
        String flowDefJson = flowDefVersion.getDefJson();
        JSONObject defObj = JSONObject.fromObject(flowDefJson);
        Definition definition = (Definition) JSONObject.toBean(defObj, Definition.class);
        net.sf.json.JSONObject currentNode = definition.getProcess().getNodes().getJSONObject(actTaskDefKey);

        net.sf.json.JSONObject notify = currentNode.getJSONObject("nodeConfig").getJSONObject("notify");
        if (notify != null) {

            JSONObject currentNotify = notify.getJSONObject(eventType);
//            String[] notifyType = {"notifyExecutor", "notifyStarter", "notifyPosition"};
            if (currentNotify != null) {
//发送给流程的实际执行人
                JSONObject notifyExecutor = currentNotify.getJSONObject("notifyExecutor");
                JSONArray selectType = notifyExecutor.getJSONArray("type");
                if (selectType != null && !selectType.isEmpty() && selectType.size() > 0) {
                    Object[] types = selectType.toArray();
                    for (Object type : types) {
                        if ("EMAIL".equalsIgnoreCase(type.toString())) {
                            INotifyService iNotifyService = ApiClient.createProxy(INotifyService.class);
                            EcmpMessage message = new EcmpMessage();
                            message.setSubject(flowDefVersion.getName() + ":" + taskEntity.getCurrentActivityName());//流程名+任务名
                            String senderId = ContextUtil.getUserId();
                            message.setSenderId(senderId);

                            List<String> receiverIds =  getReceiverIds(currentNode,taskEntity);
                            message.setReceiverIds(receiverIds);

                            Map<String, String> contentTemplateParams = new HashMap<String, String>();
                            String workCaption = (String) execution.getVariable("workCaption");
                            String businessName = (String) execution.getVariable("name");
                            String businessCode = (String) execution.getVariable("businessCode");
                            String preOpinion = null;
                            String opinion = null;

                            if ("before".equalsIgnoreCase(eventType)) {//上一步执行意见
                                actTaskDefKey =  taskEntity.getTransition().getSource().getId();
//                                FlowHistory flowHistory = flowHistoryDao.findByActTaskDefKeyAndActInstanceId(actTaskDefKey,instance.getId());
                                FlowTask flowTask = flowTaskDao.findByActTaskDefKeyAndActInstanceId(actTaskDefKey,instance.getId());
                                if (flowTask == null ) {
                                    preOpinion = "流程启动";
                                } else {
                                    preOpinion = flowTask.getDepict();
                                }
                            }else if ("after".equalsIgnoreCase(eventType)) {//当前任务执行意见
                                FlowTask flowTask = flowTaskDao.findByActTaskDefKeyAndActInstanceId(actTaskDefKey,instance.getId());
                                opinion = flowTask.getDepict();
                            }

                            contentTemplateParams.put("businessName", businessName);//业务单据名称
                            contentTemplateParams.put("businessCode", businessCode);//业务单据代码
//                            contentTemplateParams.put("businessId", businessId);//业务单据Id
                            if ("before".equalsIgnoreCase(eventType)) {
                                contentTemplateParams.put("preOpinion", preOpinion);//上一步审批意见
                            } else if ("after".equalsIgnoreCase(eventType)) {
                                contentTemplateParams.put("opinion", opinion);//审批意见
                            }
                            contentTemplateParams.put("workCaption", workCaption);//业务单据工作说明
//                            contentTemplateParams.put("startTime",startTimeStr );//流程启动时间
                            contentTemplateParams.put("remark", notifyExecutor.getString("content"));//备注说明
                            message.setContentTemplateParams(contentTemplateParams);
                            message.setContentTemplateCode(contentTemplateCode);//模板代码

                            List<NotifyType> notifyTypes = new ArrayList<NotifyType>();
                            notifyTypes.add(NotifyType.Email);
                            message.setNotifyTypes(notifyTypes);
//                            System.out.println(JsonUtils.toJson(message));
                            message.setCanToSender(true);
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    iNotifyService.send(message);
                                }
                            }).start();
                        } else if ("SMS".equalsIgnoreCase(type.toString())) {

                        } else if ("APP".equalsIgnoreCase(type.toString())) {

                        }
                    }
                }
//发送给流程启动人
                JSONObject notifyStarter = currentNotify.getJSONObject("notifyStarter");
                selectType = notifyStarter.getJSONArray("type");
                if (selectType != null && !selectType.isEmpty() && selectType.size() > 0) {

                    Object[] types = selectType.toArray();
                    for (Object type : types) {
                        if ("EMAIL".equalsIgnoreCase(type.toString())) {
                            INotifyService iNotifyService = ApiClient.createProxy(INotifyService.class);
                            EcmpMessage message = new EcmpMessage();
                            message.setSubject(flowDefVersion.getName() + ":" + taskEntity.getCurrentActivityName());//流程名+任务名
                            String senderId = ContextUtil.getUserId();
                            message.setSenderId(senderId);

                            List<String> receiverIds = new ArrayList<String>();
                            receiverIds.add(startUserId);//流程启动人
                            message.setReceiverIds(receiverIds);

                            Map<String, String> contentTemplateParams = new HashMap<String, String>();
                            String workCaption = (String) execution.getVariable("workCaption");
                            String businessName = (String) execution.getVariable("name");
                            String businessCode = (String) execution.getVariable("businessCode");
                            String preOpinion = null;
                            String opinion = null;

                            if ("before".equalsIgnoreCase(eventType)) {//上一步执行意见
                                actTaskDefKey =  taskEntity.getTransition().getSource().getId();
                                FlowTask flowTask = flowTaskDao.findByActTaskDefKeyAndActInstanceId(actTaskDefKey,instance.getId());
                                if (flowTask == null ) {
                                    preOpinion = "";
                                } else {
                                    preOpinion = flowTask.getDepict();
                                }
                            }else if ("after".equalsIgnoreCase(eventType)) {//当前任务执行意见
                                FlowTask flowTask = flowTaskDao.findByActTaskDefKeyAndActInstanceId(actTaskDefKey,instance.getId());
                                opinion = flowTask.getDepict();
                            }

                            contentTemplateParams.put("businessName", businessName);//业务单据名称
                            contentTemplateParams.put("businessCode", businessCode);//业务单据代码
//                            contentTemplateParams.put("businessId", businessId);//业务单据Id
                            contentTemplateParams.put("preOpinion", preOpinion);//上一步审批意见
                            contentTemplateParams.put("workCaption", workCaption);//业务单据工作说明
//                            contentTemplateParams.put("startTime",startTimeStr );//流程启动时间
                            contentTemplateParams.put("remark", notifyStarter.getString("content"));//备注说明

                            message.setContentTemplateParams(contentTemplateParams);
                            message.setContentTemplateCode(contentTemplateCode);//模板代码

                            List<NotifyType> notifyTypes = new ArrayList<NotifyType>();
                            notifyTypes.add(NotifyType.Email);
                            message.setNotifyTypes(notifyTypes);
                            message.setCanToSender(true);
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    iNotifyService.send(message);
                                }
                            }).start();

                        } else if ("SMS".equalsIgnoreCase(type.toString())) {
                        } else if ("APP".equalsIgnoreCase(type.toString())) {
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
                        } else if ("SMS".equalsIgnoreCase(type.toString())) {
                        } else if ("APP".equalsIgnoreCase(type.toString())) {
                        }
                    }
                }

            }
        }
    }



    private List<String> getReceiverIds(net.sf.json.JSONObject currentNode ,ExecutionEntity taskEntity ){
        List<String> receiverIds = new ArrayList<String>();

        if ("before".equalsIgnoreCase(eventType)) {
            String nodeParamName = BpmnUtil.getCurrentNodeParamName(currentNode);
            String userIds = execution.getVariable(nodeParamName) + "";
            if (StringUtils.isNotEmpty(userIds)) {
                String[] userIdArray = userIds.split(",");
                for (String userId : userIdArray) {
                    receiverIds.add(userId);
                }
            } else {
                throw new RuntimeException("下一步的用户id为空");
            }
        } else if ("after".equalsIgnoreCase(eventType)) {
            List<IdentityLinkEntity> identityLinks = taskEntity.getIdentityLinks();
            if (identityLinks == null || identityLinks.isEmpty()) {
                logger.error("找不到当前任务执行人");
                return receiverIds;
            }
            IEmployeeService iEmployeeService = ApiClient.createProxy(IEmployeeService.class);
            for (IdentityLinkEntity identityLink : identityLinks) {
                receiverIds.add(identityLink.getUserId());
                List<Executor> employees = iEmployeeService.getExecutorsByEmployeeIds(java.util.Arrays.asList(identityLink.getUserId()));
                if (employees != null && !employees.isEmpty()) {
                    Executor executor = employees.get(0);
                    receiverIds.add(executor.getId());
                }
            }
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
}
