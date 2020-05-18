package com.ecmp.flow.service;

import com.ecmp.config.util.ApiClient;
import com.ecmp.context.ContextUtil;
import com.ecmp.core.dao.BaseEntityDao;
import com.ecmp.core.search.*;
import com.ecmp.core.service.BaseEntityService;
import com.ecmp.enums.UserAuthorityPolicy;
import com.ecmp.flow.api.IFlowTaskService;
import com.ecmp.flow.basic.vo.Executor;
import com.ecmp.flow.basic.vo.UserEmailAlert;
import com.ecmp.flow.common.util.Constants;
import com.ecmp.flow.constant.FlowExecuteStatus;
import com.ecmp.flow.constant.FlowStatus;
import com.ecmp.flow.dao.*;
import com.ecmp.flow.dao.util.PageUrlUtil;
import com.ecmp.flow.dto.FlowTaskExecutorIdAndCount;
import com.ecmp.flow.entity.*;
import com.ecmp.flow.util.*;
import com.ecmp.flow.vo.*;
import com.ecmp.flow.vo.bpmn.Definition;
import com.ecmp.flow.vo.bpmn.UserTask;
import com.ecmp.flow.vo.phone.FlowTaskBatchPhoneVO;
import com.ecmp.flow.vo.phone.FlowTaskPhoneVo;
import com.ecmp.log.util.LogUtil;
import com.ecmp.notify.api.INotifyService;
import com.ecmp.notity.entity.EcmpMessage;
import com.ecmp.notity.entity.NotifyType;
import com.ecmp.util.JsonUtils;
import com.ecmp.vo.OperateResult;
import com.ecmp.vo.OperateResultWithData;
import com.ecmp.vo.ResponseData;
import com.ecmp.vo.SessionUser;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.activiti.engine.HistoryService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricActivityInstanceQuery;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.impl.RepositoryServiceImpl;
import org.activiti.engine.impl.el.UelExpressionCondition;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.HistoricActivityInstanceEntity;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.persistence.entity.VariableInstance;
import org.activiti.engine.impl.pvm.PvmActivity;
import org.activiti.engine.impl.pvm.PvmTransition;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.impl.pvm.process.ProcessDefinitionImpl;
import org.activiti.engine.impl.pvm.process.ProcessElementImpl;
import org.activiti.engine.impl.pvm.process.TransitionImpl;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.ProcessInstance;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.util.CollectionUtils;

import javax.ws.rs.core.GenericType;
import java.util.*;
import java.util.stream.Collectors;

/**
 * *************************************************************************************************
 * <p/>
 * 实现功能：流程任务管理
 * <p>
 * ------------------------------------------------------------------------------------------------
 * 版本          变更时间             变更人                     变更原因
 * ------------------------------------------------------------------------------------------------
 * 1.0.00      2017/3/23 22:39      谭军(tanjun)               新建
 * <p/>
 * *************************************************************************************************
 */
@Service
public class FlowTaskService extends BaseEntityService<FlowTask> implements IFlowTaskService {

    @Autowired
    private FlowTaskTool flowTaskTool;

    @Autowired
    private FlowTaskDao flowTaskDao;

    @Autowired
    private AppModuleDao appModuleDao;

    protected BaseEntityDao<FlowTask> getDao() {
        return this.flowTaskDao;
    }

    @Autowired
    private FlowInstanceDao flowInstanceDao;

    @Autowired
    private FlowHistoryDao flowHistoryDao;

    @Autowired
    private FlowCommonUtil flowCommonUtil;

    @Autowired
    private FlowExecutorConfigDao flowExecutorConfigDao;

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private FlowDefinationDao flowDefinationDao;

    @Autowired
    private BusinessModelDao businessModelDao;

    @Autowired
    private FlowDefVersionDao flowDefVersionDao;

    @Autowired
    private HistoryService historyService;

    @Autowired
    private FlowSolidifyExecutorDao flowSolidifyExecutorDao;

    @Autowired
    private FlowSolidifyExecutorService flowSolidifyExecutorService;

    @Autowired
    private AppModuleService appModuleService;

    @Autowired
    private FlowTaskPushControlService flowTaskPushControlService;

    @Autowired
    private FlowHistoryService flowHistoryService;

    @Autowired
    private TaskMakeOverPowerService taskMakeOverPowerService;

    @Autowired
    private DefaultFlowBaseService defaultFlowBaseService;


    /**
     * 保存推送信息
     *
     * @param type     推送类型
     * @param status   推送状态
     * @param url      推送url
     * @param taskList 推送的任务集合
     */
    public void savePushAndControlInfo(String type, String status, String url, Boolean success, List<FlowTask> taskList) {
        if (taskList != null && taskList.size() > 0) {
            try {
                //判断进来的任务是不是属于同一个实例
                String oneFlowInstanceId = taskList.get(0).getFlowInstance().getId();
                String oneTaskId = taskList.get(0).getId();
                FlowTask bean = taskList.stream().filter(a -> (!oneFlowInstanceId.equals(a.getFlowInstance().getId()) && !oneTaskId.equals(a.getId()))).findFirst().orElse(null);
                //taskList属于不同流程实例
                if (bean != null) {
                    for (FlowTask flowtask : taskList) {
                        List<FlowTask> needList = new ArrayList<>();
                        needList.add(flowtask);
                        List<FlowTaskPushControl> list = flowTaskPushControlService.getByInstanceAndNodeAndTypeAndStatus(flowtask.getFlowInstance().getId(), flowtask.getActTaskDefKey(), type, status);
                        if (list == null || list.size() == 0) {
                            //新建推送任务父表和任务列表
                            flowTaskPushControlService.saveNewControlInfo(type, status, url, success, needList);
                        } else {
                            //更新推送任务父表和任务列表
                            flowTaskPushControlService.updateOldControlInfo(type, status, url, success, needList, list);
                        }
                    }
                } else { //taskList属于同实例
                    List<FlowTaskPushControl> list = flowTaskPushControlService.getByInstanceAndNodeAndTypeAndStatus(taskList.get(0).getFlowInstance().getId(), taskList.get(0).getActTaskDefKey(), type, status);
                    if (list == null || list.size() == 0) {
                        //新建推送任务父表和任务列表
                        flowTaskPushControlService.saveNewControlInfo(type, status, url, success, taskList);
                    } else {
                        //更新推送任务父表和任务列表
                        flowTaskPushControlService.updateOldControlInfo(type, status, url, success, taskList, list);
                    }
                }
            } catch (Exception e) {
                LogUtil.error("保存推送信息失败！", e);
            }
        }
    }


    /**
     * 查看是否需要推送任务信息到basic模块
     *
     * @return true或者false，true为需要推送
     */
    public boolean getBooleanPushTaskToBasic() {
        Boolean pushBasic = false;
        String pushBasicStr = Constants.getConfigKeyValueProperties("FLOW_PUSH_TASK_BASIC");
        if (StringUtils.isNotEmpty(pushBasicStr)) {
            if ("true".equalsIgnoreCase(pushBasicStr.trim())) {
                pushBasic = true;
            }
        }
        return pushBasic;
    }

    /**
     * @param newList 待办
     * @param oldList 待办转已办
     * @param delList 删除待办
     * @param endTask 归档
     */
    public void pushToBasic(List<FlowTask> newList, List<FlowTask> oldList, List<FlowTask> delList, FlowTask endTask) {
        if (delList != null && delList.size() > 0) { //删除待办
            pushDelTaskToBasic(delList);
        }
        if (oldList != null && oldList.size() > 0) { //待办转已办
            pushOldTaskToBasic(oldList);
        }
        if (newList != null && newList.size() > 0) {  //新增待办
            pushNewTaskToBasic(newList);
        }
        if (endTask != null) { //归档（终止）
            pushEndTaskToBasic(endTask);
        }
    }

    /**
     * 推送待办到basic模块
     *
     * @param taskList 需要推送的待办
     */
    @Override
    public void pushNewTaskToBasic(List<FlowTask> taskList) {
        if (taskList != null && taskList.size() > 0) {
            List<String> idList = new ArrayList<String>();
            taskList.forEach(a -> idList.add("【id=" + a.getId() + "】"));
            this.initFlowTasks(taskList); //添加待办处理地址等
            String url = Constants.getBasicPushNewTaskUrl(); //推送待办接口
            String messageLog = "开始调用‘推送待办到basic’接口，接口url=" + url + ",参数值ID集合:" + JsonUtils.toJson(idList);
            Boolean success = false;
            try {
                ApiClient.postViaProxyReturnResult(url, Void.class, taskList);
                success = true;
            } catch (Exception e) {
                messageLog += "-推送待办异常：" + e.getMessage();
                LogUtil.error(messageLog, e);
            } finally {
                this.savePushAndControlInfo(Constants.TYPE_BASIC, Constants.STATUS_BASIC_NEW, url, success, taskList);
            }
        }
    }


    /**
     * 推送新的已办到basic模块
     *
     * @param taskList 需要推送的已办（刚执行完成的）
     */
    @Override
    public void pushOldTaskToBasic(List<FlowTask> taskList) {
        if (taskList != null && taskList.size() > 0) {
            List<String> idList = new ArrayList<String>();
            taskList.forEach(a -> idList.add("【id=" + a.getId() + "】"));
            String url = Constants.getBasicPushOldTaskUrl(); //推送已办接口
            String messageLog = "开始调用‘推送已办到basic’接口，接口url=" + url + ",参数值ID集合:" + JsonUtils.toJson(idList);
            Boolean success = false;
            try {
                ApiClient.postViaProxyReturnResult(url, Void.class, taskList);
                success = true;
            } catch (Exception e) {
                messageLog += "-推送已办异常：" + e.getMessage();
                LogUtil.error(messageLog, e);
            } finally {
                this.savePushAndControlInfo(Constants.TYPE_BASIC, Constants.STATUS_BASIC_OLD, url, success, taskList);
            }
        }
    }

    /**
     * 推送需要删除的待办到basic模块
     *
     * @param taskList 需要删除的待办
     */
    @Override
    public void pushDelTaskToBasic(List<FlowTask> taskList) {
        if (taskList != null && taskList.size() > 0) {
            List<String> idList = new ArrayList<String>();
            taskList.forEach(a -> idList.add("【id=" + a.getId() + "】"));
            String url = Constants.getBasicPushDelTaskUrl(); //推送需要删除待办接口
            String messageLog = "开始调用‘推送删除待办到basic’接口，接口url=" + url + ",参数值ID集合:" + JsonUtils.toJson(idList);
            Boolean success = false;
            try {
                ApiClient.postViaProxyReturnResult(url, Void.class, taskList);
                success = true;
            } catch (Exception e) {
                messageLog += "-推送删除待办异常：" + e.getMessage();
                LogUtil.error(messageLog, e);
            } finally {
                this.savePushAndControlInfo(Constants.TYPE_BASIC, Constants.STATUS_BASIC_DEL, url, success, taskList);
            }
        }
    }

    /**
     * 推送需要归档（终止）的任务到basic模块
     *
     * @param task 需要终止的任务
     */
    @Override
    public void pushEndTaskToBasic(FlowTask task) {
        if (task != null) {
            String url = Constants.getBasicPushEndTaskUrl(); //推送需要归档（终止）的任务到basic模块接口
            String messageLog = "开始调用‘推送归档任务到basic’接口，接口url=" + url + ",参数值ID集合:" + task.getId();
            Boolean success = false;
            try {
                ApiClient.postViaProxyReturnResult(url, Void.class, task);
                success = true;
            } catch (Exception e) {
                messageLog += "-推送归档任务异常：" + e.getMessage();
                LogUtil.error(messageLog, e);
            } finally {
                List<FlowTask> taskList = new ArrayList<FlowTask>();
                taskList.add(task);
                this.savePushAndControlInfo(Constants.TYPE_BASIC, Constants.STATUS_BASIC_END, url, success, taskList);
            }
        }
    }


    /**
     * 查看是否需要推动任务到<业务模块>、<配置的url>
     *
     * @param flowInstance 流程实例
     * @return
     */
    public boolean getBooleanPushModelOrUrl(FlowInstance flowInstance) {
        Boolean pushModelOrUrl = false;
        if (flowInstance != null) {
            BusinessModel businessModel = flowInstance.getFlowDefVersion().getFlowDefination().getFlowType().getBusinessModel();
            if (businessModel != null) {
                String pushMsgUrl = businessModel.getPushMsgUrl();
                if (StringUtils.isNotEmpty(pushMsgUrl)) { //业务实体中是否配置了推送待办的接口地址
                    pushModelOrUrl = true;
                    return pushModelOrUrl;
                }
            }
        }
        //取配置中心统一推送待办地址
        String flowPushTaskUrl = Constants.getConfigKeyValueProperties("FLOW_PUSH_TASK_URL");
        if (StringUtils.isNotEmpty(flowPushTaskUrl)) {
            pushModelOrUrl = true;
        }
        return pushModelOrUrl;
    }

    /**
     * 通过流程实例得到推动到<业务模块>、<配置的url>的地址
     *
     * @param flowInstance 流程实例
     * @return
     */
    public String getPushModelOrUrlStr(FlowInstance flowInstance) {
        String flowPushTaskUrl = "";  //具体推送地址
        if (flowInstance != null) {
            BusinessModel businessModel = flowInstance.getFlowDefVersion().getFlowDefination().getFlowType().getBusinessModel();
            if (businessModel != null) {
                String pushMsgUrl = businessModel.getPushMsgUrl();
                if (StringUtils.isNotEmpty(pushMsgUrl)) { //业务实体中是否配置了推送待办的接口地址
                    String apiBaseAddressConfig = ExpressionUtil.getAppModule(businessModel).getApiBaseAddress();
                    String clientApiBaseUrl = Constants.getConfigValueByApi(apiBaseAddressConfig);
                    if (StringUtils.isEmpty(clientApiBaseUrl)) {
                        LogUtil.error("推送待办-配置中心获取【" + apiBaseAddressConfig + "】参数失败！");
                    } else {
                        flowPushTaskUrl = clientApiBaseUrl + pushMsgUrl;
                        return flowPushTaskUrl;  //如果都配置了，业务模块中的生效
                    }
                }
            }
        }
        //取配置中心统一推送待办地址
        flowPushTaskUrl = Constants.getConfigKeyValueProperties("FLOW_PUSH_TASK_URL");
        return flowPushTaskUrl;
    }

    /**
     * 推送待办、已办到<业务模块>、<配置的url>
     *
     * @param flowInstance 流程实例
     * @param taskList     需要推送的待办
     */
    public void pushTaskToModelOrUrl(FlowInstance flowInstance, List<FlowTask> taskList, TaskStatus taskStatus) {
        if (taskList != null && taskList.size() > 0) {
            //得到具体推送地址
            String flowPushTaskUrl = this.getPushModelOrUrlStr(flowInstance);
            if (StringUtils.isNotEmpty(flowPushTaskUrl)) {
                List<String> idList = new ArrayList<String>();
                taskList.forEach(a -> {
                    a.getFlowInstance().setFlowTasks(null);
                    a.setTaskStatus(taskStatus.toString());
                    idList.add("【id=" + a.getId() + "】");
                });
                String msg = "";
                if (taskStatus.toString().equals(TaskStatus.INIT.toString())) {
                    msg = "推送待办";
                    this.initFlowTasks(taskList); //添加待办处理地址等
                } else if (taskStatus.toString().equals(TaskStatus.DELETE.toString())) {
                    msg = "推送删除待办";
                } else {
                    msg = "推送已办";
                }
                String messageLog = "开始调用[" + msg + "]接口，接口url=" + flowPushTaskUrl + ",参数值:" + JsonUtils.toJson(idList);
                Boolean success = false;
                try {
                    ApiClient.postViaProxyReturnResult(flowPushTaskUrl, new GenericType<String>() {
                    }, taskList);
                    LogUtil.bizLog(messageLog);
                    success = true;
                } catch (Exception e) {
                    messageLog += "-[" + msg + "异常]：" + e.getMessage();
                    LogUtil.error(messageLog, e);
                } finally {
                    if (taskStatus.getValue().equals(Constants.STATUS_BUSINESS_INIT)) { //新增
                        this.savePushAndControlInfo(Constants.TYPE_BUSINESS, Constants.STATUS_BUSINESS_INIT, flowPushTaskUrl, success, taskList);
                    } else if (taskStatus.getValue().equals(Constants.STATUS_BUSINESS_DEDLETE)) { //删除
                        this.savePushAndControlInfo(Constants.TYPE_BUSINESS, Constants.STATUS_BUSINESS_DEDLETE, flowPushTaskUrl, success, taskList);
                    } else {  //已办
                        this.savePushAndControlInfo(Constants.TYPE_BUSINESS, Constants.STATUS_BUSINESS_COMPLETED, flowPushTaskUrl, success, taskList);
                    }
                }
            }
        }
    }


    /**
     * 任务签收
     *
     * @param id     任务id
     * @param userId 用户账号
     * @return
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public OperateResult claim(String id, String userId) {
        FlowTask flowTask = flowTaskDao.findOne(id);
        String actTaskId = flowTask.getActTaskId();
        this.claimActiviti(actTaskId, userId);
        flowTask.setActClaimTime(new Date());
        flowTask.setTaskStatus(TaskStatus.CLAIM.toString());
        flowTaskDao.save(flowTask);
        //是否推送信息到baisc
        Boolean pushBasic = this.getBooleanPushTaskToBasic();
        //是否推送信息到业务模块或者直接配置的url
        Boolean pushModelOrUrl = this.getBooleanPushModelOrUrl(flowTask.getFlowInstance());
        //需要异步推送删除待办信息到basic
        if (pushBasic || pushModelOrUrl) {
            List<FlowTask> alllist = flowTaskDao.findListByProperty("actTaskId", actTaskId);
            List<FlowTask> needDelList = alllist.stream().filter((a) -> !a.getId().equalsIgnoreCase(flowTask.getId())).collect(Collectors.toList());
            if (pushBasic) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        pushToBasic(null, null, needDelList, null);
                    }
                }).start();
            }
            if (pushModelOrUrl) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        pushTaskToModelOrUrl(flowTask.getFlowInstance(), needDelList, TaskStatus.DELETE);
                    }
                }).start();
            }
        }
        flowTaskDao.deleteNotClaimTask(actTaskId, id);
        OperateResult result = OperateResult.operationSuccess("10012");
        return result;
    }


    /**
     * 任务签收
     *
     * @param taskId 任务id
     * @return
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ResponseData claimTaskOfPhone(String taskId) {
        String userId = ContextUtil.getUserId();
        OperateResult result = this.claim(taskId, userId);
        ResponseData responseData = new ResponseData();
        responseData.setSuccess(result.successful());
        responseData.setMessage(result.getMessage());
        return responseData;
    }


    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public OperateResultWithData<FlowStatus> complete(FlowTaskCompleteVO flowTaskCompleteVO) throws Exception {
        String taskId = flowTaskCompleteVO.getTaskId();
        Map<String, Object> variables = flowTaskCompleteVO.getVariables();
        Map<String, String> manualSelectedNodes = flowTaskCompleteVO.getManualSelectedNode();
        OperateResultWithData<FlowStatus> result = null;
        try {
            if (manualSelectedNodes == null || manualSelectedNodes.isEmpty()) {//非人工选择任务的情况
                result = this.complete(taskId, flowTaskCompleteVO.getOpinion(), variables);
            } else {//人工选择任务的情况
                FlowTask flowTask = flowTaskDao.findOne(taskId);
                String taskJsonDef = flowTask.getTaskJsonDef();
                JSONObject taskJsonDefObj = JSONObject.fromObject(taskJsonDef);
                String nodeType = taskJsonDefObj.get("nodeType") + "";//针对审批网关的情况
                String actTaskId = flowTask.getActTaskId();
                // 取得当前任务
                HistoricTaskInstance currTask = historyService
                        .createHistoricTaskInstanceQuery().taskId(actTaskId)
                        .singleResult();
                ProcessDefinitionEntity definition = (ProcessDefinitionEntity) ((RepositoryServiceImpl) repositoryService)
                        .getDeployedProcessDefinition(currTask
                                .getProcessDefinitionId());
                if (definition == null) {
                    LogUtil.error(ContextUtil.getMessage("10003"));
                }
                // 取得当前活动定义节点
                ActivityImpl currActivity = ((ProcessDefinitionImpl) definition)
                        .findActivity(currTask.getTaskDefinitionKey());
                if ("Approve".equalsIgnoreCase(nodeType)) {//针对审批任务的情况
                    currActivity = (ActivityImpl) currActivity.getOutgoingTransitions().get(0).getDestination();
                    String defaultSequenId = (String) currActivity.getProperty("default");
                    Map<PvmTransition, String> oriPvmTransitionMap = new LinkedHashMap<PvmTransition, String>();
                    List<PvmTransition> pvmTransitionList = currActivity.getOutgoingTransitions();
                    for (PvmTransition pvmTransition : pvmTransitionList) {
                        UelExpressionCondition uel = (UelExpressionCondition) pvmTransition.getProperty("condition");
                        String uelText = (String) pvmTransition.getProperty("conditionText");
                        if (pvmTransition.getId().equals(defaultSequenId)) {
                            continue;
                        }
                        for (Map.Entry<String, String> entry : manualSelectedNodes.entrySet()) {
                            String nodeId = entry.getValue();
                            ActivityImpl destinationActivity = ((ProcessDefinitionImpl) definition).findActivity(nodeId);
                            if (destinationActivity != null && FlowTaskTool.checkNextHas(pvmTransition.getDestination(), destinationActivity)) {
                                oriPvmTransitionMap.put(pvmTransition, uelText);
                                String proName = destinationActivity.getId() + "_approveResult";
                                uelText = "${" + proName + " == true}";
                                uel = new UelExpressionCondition(uelText);
                                ((ProcessElementImpl) pvmTransition).setProperty("condition", uel);
                                ((ProcessElementImpl) pvmTransition).setProperty("conditionText", uelText);
                                variables.put(proName, true);
                            }
                        }
                    }
                    variables.put("approveResult", null);
                    //执行任务
                    result = this.complete(taskId, flowTaskCompleteVO.getOpinion(), variables);
                    if (!oriPvmTransitionMap.isEmpty()) {
                        for (Map.Entry<PvmTransition, String> entry : oriPvmTransitionMap.entrySet()) {
                            PvmTransition pvmTransition = entry.getKey();
                            String uelText = entry.getValue();
                            UelExpressionCondition uel = new UelExpressionCondition(uelText);
                            ((ProcessElementImpl) pvmTransition).setProperty("condition", uel);
                            ((ProcessElementImpl) pvmTransition).setProperty("conditionText", uelText);
                        }
                    }
                } else {//针对人工网关的情况
                    ActivityImpl currActivityTemp = (ActivityImpl) currActivity.getOutgoingTransitions().get(0).getDestination();
                    boolean gateWay = FlowTaskTool.ifExclusiveGateway(currActivityTemp);
                    if (gateWay) {
                        currActivity = currActivityTemp;
                    }
                    Map<PvmTransition, String> oriPvmTransitionMap = new LinkedHashMap<PvmTransition, String>();
                    List<PvmTransition> pvmTransitionList = currActivity.getOutgoingTransitions();
                    for (PvmTransition pvmTransition : pvmTransitionList) {
                        ((ProcessElementImpl) pvmTransition).setProperty("condition", null);
                        ((ProcessElementImpl) pvmTransition).setProperty("conditionText", null);
                        UelExpressionCondition uel = (UelExpressionCondition) pvmTransition.getProperty("condition");
                        PvmActivity nextNode = pvmTransition.getDestination();
                        String uelText = (String) pvmTransition.getProperty("conditionText");
                        boolean isSet = false;
                        for (Map.Entry<String, String> entry : manualSelectedNodes.entrySet()) {
                            String nodeId = entry.getValue();
                            if (!nodeId.equals(entry.getKey())) {//存在子流程的情况
                                String path = entry.getKey();
                                String[] resultArray = path.split("/");
                                nodeId = resultArray[2];
                            }
                            ActivityImpl destinationActivity = ((ProcessDefinitionImpl) definition).findActivity(nodeId);
                            if (destinationActivity != null && FlowTaskTool.checkNextHas(pvmTransition.getDestination(), destinationActivity)) {
                                oriPvmTransitionMap.put(pvmTransition, uelText);
                                String proName = destinationActivity.getId() + "_approveResult";
                                uelText = "${" + proName + " == true}";
                                uel = new UelExpressionCondition(uelText);
                                ((ProcessElementImpl) pvmTransition).setProperty("condition", uel);
                                ((ProcessElementImpl) pvmTransition).setProperty("conditionText", uelText);
                                variables.put(proName, true);
                                isSet = true;
                                break;
                            }
                        }
                        if (gateWay && !isSet && (uel == null || StringUtils.isEmpty(uelText))) {
                            oriPvmTransitionMap.put(pvmTransition, uelText);
                            uelText = "${0>1}";
                            uel = new UelExpressionCondition(uelText);
                            ((ProcessElementImpl) pvmTransition).setProperty("condition", uel);
                            ((ProcessElementImpl) pvmTransition).setProperty("conditionText", uelText);
                        }
                    }
                    //执行任务
                    result = this.complete(taskId, flowTaskCompleteVO.getOpinion(), variables);
                    if (!oriPvmTransitionMap.isEmpty()) {
                        for (Map.Entry<PvmTransition, String> entry : oriPvmTransitionMap.entrySet()) {
                            PvmTransition pvmTransition = entry.getKey();
                            String uelText = entry.getValue();
                            if (StringUtils.isNotEmpty(uelText)) {
                                UelExpressionCondition uel = new UelExpressionCondition(uelText);
                                ((ProcessElementImpl) pvmTransition).setProperty("condition", uel);
                                ((ProcessElementImpl) pvmTransition).setProperty("conditionText", uelText);
                            } else {
                                ((ProcessElementImpl) pvmTransition).setProperty("condition", null);
                                ((ProcessElementImpl) pvmTransition).setProperty("conditionText", null);
                            }
                        }
                    }
                }
            }
        } catch (FlowException e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            result = OperateResultWithData.operationFailure(e.getMessage());
            LogUtil.error(e.getMessage(), e);
        }
        return result;
    }


    /**
     * 完成任务
     *
     * @param id        任务id
     * @param opinion   审批意见
     * @param variables 参数
     * @return
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public OperateResultWithData<FlowStatus> complete(String id, String opinion, Map<String, Object> variables) throws Exception {
        FlowTask flowTask = flowTaskDao.findOne(id);
        if (flowTask == null) {
            return OperateResultWithData.operationFailure("任务不存在，可能已经被处理!");
        }
        String userId = ContextUtil.getUserId();
        if (flowTask.getExecutorId() != null && !flowTask.getExecutorId().equals(userId)) {
            //根据被授权人和待办查询符合的转授权信息（通过查看模式）
            TaskMakeOverPower bean = taskMakeOverPowerService.findPowerByPowerUserAndTask(userId, flowTask);
            if (bean != null) {
                //转授权情况替换执行人(共同查看模式-授权人处理)
                flowTask.setExecutorId(ContextUtil.getUserId());
                flowTask.setExecutorAccount(ContextUtil.getUserAccount());
                flowTask.setExecutorName(ContextUtil.getUserName());
                //添加组织机构信息
                flowTask.setExecutorOrgId(bean.getPowerUserOrgId());
                flowTask.setExecutorOrgCode(bean.getPowerUserOrgCode());
                flowTask.setExecutorOrgName(bean.getPowerUserOrgName());
            }
        }
        FlowInstance flowInstance = flowTask.getFlowInstance();
        //如果是转授权转办模式（获取转授权记录信息）
        String OverPowerStr = taskMakeOverPowerService.getOverPowerStrByDepict(flowTask.getDepict());
        flowTask.setDepict(OverPowerStr + opinion);
        Integer reject = null;
        Boolean manageSolidifyFlow = false;
        if (variables != null) {
            Object rejectO = variables.get("reject");
            if (rejectO != null) {
                try {
                    reject = Integer.parseInt(rejectO.toString());
                } catch (Exception e) {
                    LogUtil.error(e.getMessage(), e);
                }
            }
            Object manageSolidifyFlowO = variables.get("manageSolidifyFlow");
            if (manageSolidifyFlowO != null) {
                manageSolidifyFlow = Boolean.parseBoolean(manageSolidifyFlowO.toString());
            }
        }
        if (reject != null && reject == 1) {
            flowTask.setDepict("【被驳回】" + flowTask.getDepict());
            flowTask.setTaskStatus(TaskStatus.REJECT.toString());
            flowTask.setPriority(1);
        } else {
            flowTask.setTaskStatus(TaskStatus.COMPLETED.toString());
        }
        //匿名用户执行的情况，直接使用当前上下文用户
        if (Constants.ANONYMOUS.equalsIgnoreCase(flowTask.getOwnerId()) && !Constants.ANONYMOUS.equalsIgnoreCase(ContextUtil.getUserId())) {
            flowTask.setOwnerId(ContextUtil.getUserId());
            flowTask.setOwnerAccount(ContextUtil.getUserAccount());
            flowTask.setOwnerName(ContextUtil.getUserName());
            flowTask.setExecutorId(ContextUtil.getUserId());
            flowTask.setExecutorAccount(ContextUtil.getUserAccount());
            flowTask.setExecutorName(ContextUtil.getUserName());
            //添加组织机构信息
            Executor executor = flowCommonUtil.getBasicUserExecutor(ContextUtil.getUserId());
            flowTask.setOwnerOrgId(executor.getOrganizationId());
            flowTask.setOwnerOrgCode(executor.getOrganizationCode());
            flowTask.setOwnerOrgName(executor.getOrganizationName());
            flowTask.setExecutorOrgId(executor.getOrganizationId());
            flowTask.setExecutorOrgCode(executor.getOrganizationCode());
            flowTask.setExecutorOrgName(executor.getOrganizationName());
        }
        variables.put("opinion", flowTask.getDepict());
        String actTaskId = flowTask.getActTaskId();

        //获取当前业务实体表单的条件表达式信息，（目前是任务执行时就注入，后期根据条件来优化)
        String businessId = flowInstance.getBusinessId();
        BusinessModel businessModel = flowTask.getFlowInstance().getFlowDefVersion().getFlowDefination().getFlowType().getBusinessModel();
        Map<String, Object> v = ExpressionUtil.getPropertiesValuesMap(businessModel, businessId, true);
        if (v != null && !v.isEmpty()) {
            if (variables == null) {
                variables = new HashMap<String, Object>();
            }
            variables.putAll(v);
        }
//        flowInstance.setBusinessModelRemark(v.get("workCaption") + "");
        String taskJsonDef = flowTask.getTaskJsonDef();
        JSONObject taskJsonDefObj = JSONObject.fromObject(taskJsonDef);
        String nodeType = taskJsonDefObj.get("nodeType") + "";//会签
        Boolean counterSignLastTask = false;
        //得到当前执行节点自定义code
        String currentNodeCode = "";
        try {
            currentNodeCode = taskJsonDefObj.getJSONObject("nodeConfig").getJSONObject("normal").getString("nodeCode");
        } catch (Exception e) {
        }
        variables.put("currentNodeCode", currentNodeCode);

        // 取得当前任务
        HistoricTaskInstance currTask = historyService
                .createHistoricTaskInstanceQuery().taskId(flowTask.getActTaskId())
                .singleResult();
        if ("CounterSign".equalsIgnoreCase(nodeType)) {//会签任务做处理判断
            String executionId = currTask.getExecutionId();
            Map<String, VariableInstance> processVariables = runtimeService.getVariableInstances(executionId);
            //完成会签的次数
            Integer completeCounter = (Integer) processVariables.get("nrOfCompletedInstances").getValue();
            if (completeCounter == 0) {//表示会签第一人审批（初始化赞成、不赞成、弃权参数）
                runtimeService.setVariable(currTask.getProcessInstanceId(), Constants.COUNTER_SIGN_AGREE + currTask.getTaskDefinitionKey(), 0);
                runtimeService.setVariable(currTask.getProcessInstanceId(), Constants.COUNTER_SIGN_OPPOSITION + currTask.getTaskDefinitionKey(), 0);
                runtimeService.setVariable(currTask.getProcessInstanceId(), Constants.COUNTER_SIGN_WAIVER + currTask.getTaskDefinitionKey(), 0);
                processVariables = runtimeService.getVariableInstances(executionId);
            }

            //总循环次数
            Integer instanceOfNumbers = (Integer) processVariables.get("nrOfInstances").getValue();
            //会签决策比
            int counterDecision = 100;
            try {
                counterDecision = taskJsonDefObj.getJSONObject("nodeConfig").getJSONObject("normal").getInt("counterDecision");
            } catch (Exception e) {
                LogUtil.error(e.getMessage(), e);
            }
            //会签结果是否即时生效
            Boolean immediatelyEnd = false;
            try {
                immediatelyEnd = taskJsonDefObj.getJSONObject("nodeConfig").getJSONObject("normal").getBoolean("immediatelyEnd");
            } catch (Exception e) {
                LogUtil.error(e.getMessage(), e);
            }

            if (completeCounter + 1 == instanceOfNumbers) {//会签最后一个任务
                counterSignLastTask = true;
                //通过票数
                Integer counterSignAgree = 0;
                if (processVariables.get(Constants.COUNTER_SIGN_AGREE + currTask.getTaskDefinitionKey()) != null) {
                    counterSignAgree = (Integer) processVariables.get(Constants.COUNTER_SIGN_AGREE + currTask.getTaskDefinitionKey()).getValue();
                }

                String approved = variables.get("approved") + "";
                Integer value = 0;//默认弃权
                if ("true".equalsIgnoreCase(approved)) {
                    counterSignAgree++;
                }
                ProcessDefinitionEntity definition = (ProcessDefinitionEntity) ((RepositoryServiceImpl) repositoryService)
                        .getDeployedProcessDefinition(currTask
                                .getProcessDefinitionId());
                if (definition == null) {
                    LogUtil.error(ContextUtil.getMessage("10003"));
                }
                if (counterDecision <= ((counterSignAgree / (instanceOfNumbers + 0.0)) * 100)) {//获取通过节点
                    variables.put("approveResult", true);
                } else {
                    variables.put("approveResult", false);
                }
                //执行任务
                this.completeActiviti(actTaskId, variables);
            } else if (immediatelyEnd) { //会签结果是否即时生效
                String approved = variables.get("approved") + "";
                //并串行会签（false为并行：并行将已生成的待办转已办，串行在最后人审批中记录）
                Boolean isSequential = taskJsonDefObj.getJSONObject("nodeConfig").getJSONObject("normal").getBoolean("isSequential");

                if ("true".equalsIgnoreCase(approved)) {
                    //通过票数
                    Integer counterSignAgree = 0;
                    if (processVariables.get(Constants.COUNTER_SIGN_AGREE + currTask.getTaskDefinitionKey()) != null) {
                        counterSignAgree = (Integer) processVariables.get(Constants.COUNTER_SIGN_AGREE + currTask.getTaskDefinitionKey()).getValue();
                    }
                    counterSignAgree++;
                    if (counterDecision <= ((counterSignAgree / (instanceOfNumbers + 0.0)) * 100)) {//通过
                        flowTaskTool.counterSignImmediatelyEnd(flowTask, flowInstance, variables, isSequential);//删除其他会签执行人
                        variables.put("approveResult", true);
                        counterSignLastTask = true; //即时结束后，当前任务算最后一个节点
                    }
                } else {
                    //不通过票数
                    Integer counterSignOpposition = 0;
                    if (processVariables.get(Constants.COUNTER_SIGN_OPPOSITION + currTask.getTaskDefinitionKey()) != null && completeCounter != 0) {
                        counterSignOpposition = (Integer) processVariables.get(Constants.COUNTER_SIGN_OPPOSITION + currTask.getTaskDefinitionKey()).getValue();
                    }
                    counterSignOpposition++;
                    if ((100 - counterDecision) < ((counterSignOpposition / (instanceOfNumbers + 0.0)) * 100)) {//不通过
                        flowTaskTool.counterSignImmediatelyEnd(flowTask, flowInstance, variables, isSequential);//删除其他会签执行人
                        variables.put("approveResult", false);
                        counterSignLastTask = true; //即时结束后，当前任务算最后一个节点
                    }
                }
                this.completeActiviti(actTaskId, variables);
            } else {
                this.completeActiviti(actTaskId, variables);
            }

        } else if ("Approve".equalsIgnoreCase(nodeType)) {
            String approved = variables.get("approved") + "";
            if ("true".equalsIgnoreCase(approved)) {
                variables.put("approveResult", true);
            } else {
                variables.put("approveResult", false);
            }
            this.completeActiviti(actTaskId, variables);
            counterSignLastTask = true;
        } else if ("ParallelTask".equalsIgnoreCase(nodeType) || "SerialTask".equalsIgnoreCase(nodeType)) {
            String executionId = currTask.getExecutionId();
            Map<String, VariableInstance> processVariables = runtimeService.getVariableInstances(executionId);
            //完成会签的次数
            Integer completeCounter = (Integer) processVariables.get("nrOfCompletedInstances").getValue();
            //总循环次数
            Integer instanceOfNumbers = (Integer) processVariables.get("nrOfInstances").getValue();
            if (completeCounter + 1 == instanceOfNumbers) {//最后一个任务
                counterSignLastTask = true;
            }
            this.completeActiviti(actTaskId, variables);
        } else {
            this.completeActiviti(actTaskId, variables);
            counterSignLastTask = true;
        }
//        this.saveVariables(variables, flowTask);先不做保存
        HistoricTaskInstance historicTaskInstance = historyService.createHistoricTaskInstanceQuery().taskId(actTaskId).singleResult(); // 创建历史任务实例查询

        // 取得流程实例
        ProcessInstance instance = runtimeService
                .createProcessInstanceQuery()
                .processInstanceId(historicTaskInstance.getProcessInstanceId())
                .singleResult();
        //是否推送信息到baisc
        Boolean pushBasic = this.getBooleanPushTaskToBasic();
        //是否推送信息到业务模块或者直接配置的url
        Boolean pushModelOrUrl = this.getBooleanPushModelOrUrl(flowInstance);

        List<FlowTask> oldList = new ArrayList<FlowTask>();
        List<FlowTask> needDelList = new ArrayList<FlowTask>();

        if (historicTaskInstance != null) {
            String defJson = flowTask.getTaskJsonDef();
            JSONObject defObj = JSONObject.fromObject(defJson);
            JSONObject normalInfo = defObj.getJSONObject("nodeConfig").getJSONObject("normal");

            Boolean canCancel = null;
            if (normalInfo.get("allowPreUndo") != null) {
                canCancel = normalInfo.getBoolean("allowPreUndo");
            }
            FlowHistory flowHistory = flowTaskTool.initFlowHistory(flowTask, historicTaskInstance, canCancel, variables);

            if (manageSolidifyFlow) {  //需要处理流程固化表(添加逻辑执行顺序)
                flowSolidifyExecutorService.manageSolidifyFlowByBusinessIdAndTaskKey(businessId, flowTask);
            }


            //需要异步推送待办转已办信息到basic、<业务模块>、<配置的url>
            if (pushBasic || pushModelOrUrl) {
                oldList.add(flowTask);
                //异步推送已办信息到<业务模块>、<配置的url>
                if (pushModelOrUrl) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            pushTaskToModelOrUrl(flowInstance, oldList, TaskStatus.COMPLETED);
                        }
                    }).start();
                }
            }
            flowHistoryDao.save(flowHistory);
            //单签任务，清除其他待办;工作池任务新增多执行人模式，也需要清楚其他待办
            if ("SingleSign".equalsIgnoreCase(nodeType) || "PoolTask".equalsIgnoreCase(nodeType)) {
                //需要异步推送删除待办信息到basic
                if (pushBasic || pushModelOrUrl) {
                    List<FlowTask> alllist = flowTaskDao.findListByProperty("actTaskId", actTaskId);
                    List<FlowTask> DelList = alllist.stream().filter((a) -> !a.getId().equalsIgnoreCase(id)).collect(Collectors.toList());
                    needDelList.addAll(DelList);
                    if (pushModelOrUrl) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                pushTaskToModelOrUrl(flowInstance, needDelList, TaskStatus.DELETE);
                            }
                        }).start();
                    }
                }
//                flowTaskDao.deleteNotClaimTask(actTaskId, id);//删除其他候选用户的任务
//                flowTaskDao.deleteByFlowInstanceId(flowInstance.getId());
                flowTaskDao.delteTaskByActTaskId(actTaskId);
            } else {
                flowTaskDao.delete(flowTask);
            }
            //初始化新的任务
            String actTaskDefKey = flowTask.getActTaskDefKey();
            String actProcessDefinitionId = flowTask.getFlowInstance().getFlowDefVersion().getActDefId();
            ProcessDefinitionEntity definition = null;
            PvmActivity currentNode = null;
            FlowInstance flowInstanceTemp = flowInstance;
            FlowInstance flowInstanceP = flowInstanceTemp.getParent();
            boolean sonEndButParnetNotEnd = false;
            while (flowInstanceTemp.isEnded() && (flowInstanceP != null)) {//子流程结束，主流程未结束
                if (!flowInstanceP.isEnded()) {
                    actProcessDefinitionId = flowInstanceP.getFlowDefVersion().getActDefId();
                    definition = (ProcessDefinitionEntity) ((RepositoryServiceImpl) repositoryService)
                            .getDeployedProcessDefinition(actProcessDefinitionId);
                    String superExecutionId = null;
                    superExecutionId = (String) runtimeService.getVariable(flowInstanceP.getActInstanceId(), flowInstanceTemp.getActInstanceId() + "_superExecutionId");
                    HistoricActivityInstance historicActivityInstance = null;
                    HistoricActivityInstanceQuery his = historyService.createHistoricActivityInstanceQuery()
                            .executionId(superExecutionId).activityType("callActivity");
                    if (his != null) {
                        historicActivityInstance = his.singleResult();
                        HistoricActivityInstanceEntity he = (HistoricActivityInstanceEntity) historicActivityInstance;
                        actTaskDefKey = he.getActivityId();
                        currentNode = FlowTaskTool.getActivitNode(definition, actTaskDefKey);
                        callInitTaskBack(currentNode, flowInstanceP, flowHistory, counterSignLastTask, variables);
                    }
                }
                sonEndButParnetNotEnd = true;
                flowInstanceTemp = flowInstanceP;
                flowInstanceP = flowInstanceTemp.getParent();
            }
            if (!sonEndButParnetNotEnd) {
                definition = (ProcessDefinitionEntity) ((RepositoryServiceImpl) repositoryService)
                        .getDeployedProcessDefinition(actProcessDefinitionId);
                currentNode = FlowTaskTool.getActivitNode(definition, actTaskDefKey);
                if (instance != null && currentNode != null && (!"endEvent".equalsIgnoreCase(currentNode.getProperty("type") + ""))) {
                    callInitTaskBack(currentNode, flowInstance, flowHistory, counterSignLastTask, variables);
                }
            }
        }
        OperateResultWithData<FlowStatus> result = OperateResultWithData.operationSuccess("10017");
        if (instance == null || instance.isEnded()) {
            result.setData(FlowStatus.COMPLETED);//任务结束
//            flowTaskDao.deleteByFlowInstanceId(flowInstance.getId());//针对终止结束时，删除所有待办
            flowSolidifyExecutorDao.deleteByBusinessId(flowInstance.getBusinessId());//查看是否为固化流程（如果是固化流程删除固化执行人列表）
            //需要异步推送归档信息到basic
            if (pushBasic) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        pushToBasic(null, oldList, needDelList, flowTask);
                    }
                }).start();
            }
        } else {
            if (pushBasic) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        pushToBasic(null, oldList, needDelList, null);
                    }
                }).start();
            }
        }
        return result;
    }


    private void callInitTaskBack(PvmActivity currentNode, FlowInstance flowInstance, FlowHistory flowHistory, boolean counterSignLastTask, Map<String, Object> variables) {
        if (!counterSignLastTask && FlowTaskTool.ifMultiInstance(currentNode)) {
            String sequential = currentNode.getProperty("multiInstance") + "";
            if ("sequential".equalsIgnoreCase(sequential)) {//会签当中串行任务,非最后一个任务
                String key = currentNode.getProperty("key") != null ? currentNode.getProperty("key").toString() : null;
                if (key == null) {
                    key = currentNode.getId();
                }
                flowTaskTool.initTask(flowInstance, flowHistory, key, variables);
                return;
            }
        }
        List<PvmTransition> nextNodes = currentNode.getOutgoingTransitions();
        if (nextNodes != null && nextNodes.size() > 0) {
            for (PvmTransition node : nextNodes) {
                PvmActivity nextActivity = node.getDestination();
                if (FlowTaskTool.ifGageway(nextActivity) || "ManualTask".equalsIgnoreCase(nextActivity.getProperty("type") + "")) {
                    callInitTaskBack(nextActivity, flowInstance, flowHistory, counterSignLastTask, variables);
                    continue;
                }
                String key = nextActivity.getProperty("key") != null ? nextActivity.getProperty("key").toString() : null;
                if (key == null) {
                    key = nextActivity.getId();
                }
                if ("serviceTask".equalsIgnoreCase(nextActivity.getProperty("type") + "")) {
                } else if ("CallActivity".equalsIgnoreCase(nextActivity.getProperty("type") + "") && counterSignLastTask) {
                    flowTaskTool.initTask(flowInstance, flowHistory, null, variables);
                } else {
                    flowTaskTool.initTask(flowInstance, flowHistory, key, variables);
                }
            }
        }
    }

    /**
     * 撤回到指定任务节点,加撤销意见
     *
     * @param id
     * @return
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public OperateResult rollBackTo(String id, String opinion) throws CloneNotSupportedException {
        FlowHistory flowHistory = flowHistoryDao.findOne(id);
        String businessId = flowHistory.getFlowInstance().getBusinessId();
        OperateResult result = flowTaskTool.taskRollBack(flowHistory, opinion);
        if (result.successful()) {
            new Thread(new Runnable() {//检测待办是否自动执行
                @Override
                public void run() {
                    flowSolidifyExecutorService.selfMotionExecuteTask(businessId);
                }
            }).start();
        }
        return result;
    }


    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ResponseData rollBackToOfPhone(String preTaskId, String opinion) throws CloneNotSupportedException {
        OperateResult res = rollBackTo(preTaskId, opinion);
        ResponseData responseData = new ResponseData();
        responseData.setSuccess(res.successful());
        responseData.setMessage(res.getMessage());
        return responseData;
    }

    /**
     * 签收任务
     *
     * @param taskId
     * @param userId
     */
    private void claimActiviti(String taskId, String userId) {
        taskService.claim(taskId, userId);
    }

    /**
     * 完成任务
     *
     * @param taskId
     * @param variables
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public void completeActiviti(String taskId, Map<String, Object> variables) throws Exception {
        taskService.complete(taskId, variables);
    }


    @Transactional(propagation = Propagation.REQUIRED)
    public ResponseData rejectTaskOfPhone(String taskId, String opinion) throws Exception {
        ResponseData responseData = new ResponseData();
        OperateResult result = this.taskReject(taskId, opinion, null);
        responseData.setSuccess(result.successful());
        responseData.setMessage(result.getMessage());
        return responseData;
    }

    /**
     * 任务驳回
     *
     * @param id        任务id
     * @param variables 参数
     * @return 结果
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public OperateResult taskReject(String id, String opinion, Map<String, Object> variables) throws Exception {
        OperateResult result = OperateResult.operationSuccess("10006");
        FlowTask flowTask = flowTaskDao.findOne(id);
        if (flowTask == null) {
            return OperateResult.operationFailure("10009");
        }
        flowTask.setDepict(opinion);
        String businessId = flowTask.getFlowInstance().getBusinessId();
        try {
            if (flowTask != null && StringUtils.isNotEmpty(flowTask.getPreId())) {
                FlowHistory preFlowTask = flowHistoryDao.findOne(flowTask.getPreId());//上一个任务id
                if (preFlowTask == null) {
                    return OperateResult.operationFailure("10016");
                } else {
                    result = this.activitiReject(flowTask, preFlowTask);
                }
            } else {
                return OperateResult.operationFailure("10023");
            }
        } catch (FlowException flowE) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return OperateResult.operationFailure(flowE.getMessage());
        }
        if (result.successful()) {
            new Thread(new Runnable() {//检测待办是否自动执行
                @Override
                public void run() {
                    flowSolidifyExecutorService.selfMotionExecuteTask(businessId);
                }
            }).start();
        }
        return result;
    }


    /**
     * 驳回前一个任务
     *
     * @param currentTask 当前任务
     * @param preFlowTask 上一个任务
     * @return 结果
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public OperateResult activitiReject(FlowTask currentTask, FlowHistory preFlowTask) throws Exception {
        OperateResult result = OperateResult.operationSuccess("10015");
        // 取得当前任务
        HistoricTaskInstance currTask = historyService.createHistoricTaskInstanceQuery().taskId(currentTask.getActTaskId())
                .singleResult();
        // 取得流程实例
        ProcessInstance instance = runtimeService.createProcessInstanceQuery()
                .processInstanceId(currTask.getProcessInstanceId()).singleResult();
        if (instance == null) {
            OperateResult.operationFailure("10009");
        }
        Map variables = new HashMap();
        Map variablesProcess = instance.getProcessVariables();
        Map variablesTask = currTask.getTaskLocalVariables();
        if ((variablesProcess != null) && (!variablesProcess.isEmpty())) {
            variables.putAll(variablesProcess);
        }
        if ((variablesTask != null) && (!variablesTask.isEmpty())) {
            variables.putAll(variablesTask);
        }

        // 取得流程定义
        ProcessDefinitionEntity definition = (ProcessDefinitionEntity) ((RepositoryServiceImpl) repositoryService)
                .getDeployedProcessDefinition(currTask.getProcessDefinitionId());
        if (definition == null) {
            OperateResult.operationFailure("10009");
        }

        // 取得当前任务标节点的活动
        ActivityImpl currentActivity = ((ProcessDefinitionImpl) definition)
                .findActivity(currentTask.getActTaskDefKey());
        // 取得驳回目标节点的活动
        while ("cancel".equalsIgnoreCase(preFlowTask.getTaskStatus()) || "reject".equalsIgnoreCase(preFlowTask.getTaskStatus()) || preFlowTask.getActTaskDefKey().equals(currentTask.getActTaskDefKey())) {//如果前一任务为撤回或者驳回任务，则依次向上迭代
            String preFlowTaskId = preFlowTask.getPreId();
            if (StringUtils.isNotEmpty(preFlowTaskId)) {
                preFlowTask = flowHistoryDao.findOne(preFlowTaskId);
            } else {
                return OperateResult.operationFailure("10016");
            }
        }
        ActivityImpl preActivity = ((ProcessDefinitionImpl) definition)
                .findActivity(preFlowTask.getActTaskDefKey());
        if (FlowTaskTool.checkCanReject(currentActivity, preActivity, instance,
                definition)) {
            //取活动，清除活动方向
            List<PvmTransition> oriPvmTransitionList = new ArrayList<PvmTransition>();
            List<PvmTransition> pvmTransitionList = currentActivity
                    .getOutgoingTransitions();
            for (PvmTransition pvmTransition : pvmTransitionList) {
                oriPvmTransitionList.add(pvmTransition);
            }
            pvmTransitionList.clear();
            //建立新方向
            TransitionImpl newTransition = currentActivity
                    .createOutgoingTransition();
            // 取得转向的目标，这里需要指定用需要回退到的任务节点
            newTransition.setDestination(preActivity);

            //完成任务
            variables.put("reject", 1);
            this.complete(currentTask.getId(), currentTask.getDepict(), variables);

            //恢复方向
            preActivity.getIncomingTransitions().remove(newTransition);
            List<PvmTransition> pvmTList = currentActivity
                    .getOutgoingTransitions();
            pvmTList.clear();
            for (PvmTransition pvmTransition : oriPvmTransitionList) {
                pvmTransitionList.add(pvmTransition);
            }
            //  variablesProcess.put("reject", 0);//将状态重置
            runtimeService.removeVariable(instance.getProcessInstanceId(), "reject");//将状态重置
        } else {
            result = OperateResult.operationFailure("10016");
        }
        return result;
    }

    /**
     * 获取出口节点信息（带初始化流程设计器配置用户）
     *
     * @param id
     * @return
     * @throws NoSuchMethodException
     */
    public List<NodeInfo> findNexNodesWithUserSet(String id, String approved) throws NoSuchMethodException {
        FlowTask flowTask = flowTaskDao.findOne(id);
        if (flowTask == null) {
            return null;
        }
        return this.findNexNodesWithUserSet(flowTask, approved, null);
    }

    /**
     * 获取出口节点信息（带初始化流程设计器配置用户）
     *
     * @param flowTask
     * @param approved
     * @return
     * @throws NoSuchMethodException
     */
    public List<NodeInfo> findNexNodesWithUserSet(FlowTask flowTask, String approved, List<String> includeNodeIds) throws NoSuchMethodException {
        List<NodeInfo> nodeInfoList = flowTaskTool.findNextNodesWithCondition(flowTask, approved, includeNodeIds);

        if (nodeInfoList != null && !nodeInfoList.isEmpty()) {
            String flowDefJson = flowTask.getFlowInstance().getFlowDefVersion().getDefJson();
            JSONObject defObj = JSONObject.fromObject(flowDefJson);
            Definition definition = (Definition) JSONObject.toBean(defObj, Definition.class);

            String flowTaskDefJson = flowTask.getTaskJsonDef();
            JSONObject flowTaskDefObj = JSONObject.fromObject(flowTaskDefJson);
            String currentNodeType = flowTaskDefObj.get("nodeType") + "";
            JSONObject normalInfo = flowTaskDefObj.getJSONObject("nodeConfig").getJSONObject("normal");
            Boolean currentSingleTaskAuto = false;
            if (normalInfo != null && normalInfo.has("singleTaskNoChoose") && normalInfo.get("singleTaskNoChoose") != null) {
                currentSingleTaskAuto = normalInfo.getBoolean("singleTaskNoChoose");
            }
            Map<NodeInfo, List<NodeInfo>> nodeInfoSonMap = new LinkedHashMap();
            for (NodeInfo nodeInfo : nodeInfoList) {
                nodeInfo.setCurrentTaskType(currentNodeType);
                nodeInfo.setCurrentSingleTaskAuto(currentSingleTaskAuto);
                if ("CounterSignNotEnd".equalsIgnoreCase(nodeInfo.getType())) {
                    continue;
                } else if ("serviceTask".equalsIgnoreCase(nodeInfo.getType())) {
                    nodeInfo.setUserVarName(nodeInfo.getId() + "_ServiceTask");
                    nodeInfo.setUiType("radiobox");
                    nodeInfo.setFlowTaskType("serviceTask");
                    String userId = ContextUtil.getSessionUser().getUserId();
                    //通过用户id获取用户信息
                    Executor executor = flowCommonUtil.getBasicUserExecutor(userId);
                    if (executor != null) {//服务任务默认选择当前操作人
                        Set<Executor> employeeSet = new HashSet<Executor>();
                        employeeSet.add(executor);
                        nodeInfo.setExecutorSet(employeeSet);
                    }
                } else if ("receiveTask".equalsIgnoreCase(nodeInfo.getType())) {
                    nodeInfo.setUserVarName(nodeInfo.getId() + "_ReceiveTask");
                    nodeInfo.setUiType("radiobox");
                    nodeInfo.setFlowTaskType("receiveTask");
                    String userId = ContextUtil.getSessionUser().getUserId();
                    //通过用户id获取用户信息
                    Executor executor = flowCommonUtil.getBasicUserExecutor(userId);
                    if (executor != null) {//接收任务默认选择当前操作人
                        Set<Executor> employeeSet = new HashSet<Executor>();
                        employeeSet.add(executor);
                        nodeInfo.setExecutorSet(employeeSet);
                    }
                } else if ("callActivity".equalsIgnoreCase(nodeInfo.getType())) {
                    List<NodeInfo> nodeInfoListSons = new ArrayList<NodeInfo>();
                    nodeInfoListSons = flowTaskTool.getCallActivityNodeInfo(flowTask, nodeInfo.getId(), nodeInfoListSons);
                    nodeInfoSonMap.put(nodeInfo, nodeInfoListSons);
                } else {
                    Set<Executor> executorSet = nodeInfo.getExecutorSet();
                    if (executorSet != null && !executorSet.isEmpty()) {
                        continue;
                    }
                    JSONObject currentNode = definition.getProcess().getNodes().getJSONObject(nodeInfo.getId());

                    try {
                        JSONObject normal = currentNode.getJSONObject(Constants.NODE_CONFIG).getJSONObject("normal");
                        Boolean allowChooseInstancy = normal.getBoolean("allowChooseInstancy");
                        nodeInfo.setAllowChooseInstancy(allowChooseInstancy);
                    } catch (Exception e) {
                    }

                    JSONObject executor = null;
                    net.sf.json.JSONArray executorList = null;//针对两个条件以上的情况
                    if (currentNode.getJSONObject(Constants.NODE_CONFIG).has(Constants.EXECUTOR)) {
                        try {
                            executor = currentNode.getJSONObject(Constants.NODE_CONFIG).getJSONObject(Constants.EXECUTOR);
                        } catch (Exception e) {
                            if (executor == null) {
                                try {
                                    executorList = currentNode.getJSONObject(Constants.NODE_CONFIG).getJSONArray(Constants.EXECUTOR);
                                } catch (Exception e2) {
                                    e2.printStackTrace();
                                }
                                if (executorList != null && executorList.size() == 1) {
                                    executor = executorList.getJSONObject(0);
                                }
                            }
                        }
                    }

                    UserTask userTaskTemp = (UserTask) JSONObject.toBean(currentNode, UserTask.class);
                    if ("EndEvent".equalsIgnoreCase(userTaskTemp.getType())) {
                        nodeInfo.setType("EndEvent");
                        continue;
                    }
                    if (StringUtils.isEmpty(nodeInfo.getUserVarName())) {
                        if ("Normal".equalsIgnoreCase(userTaskTemp.getNodeType())) {
                            nodeInfo.setUserVarName(userTaskTemp.getId() + "_Normal");
                        } else if ("SingleSign".equalsIgnoreCase(userTaskTemp.getNodeType())) {
                            nodeInfo.setUserVarName(userTaskTemp.getId() + "_SingleSign");
                            nodeInfo.setUiType("checkbox");
                        } else if ("Approve".equalsIgnoreCase(userTaskTemp.getNodeType())) {
                            nodeInfo.setUserVarName(userTaskTemp.getId() + "_Approve");
                        } else if ("CounterSign".equalsIgnoreCase(userTaskTemp.getNodeType()) || "ParallelTask".equalsIgnoreCase(userTaskTemp.getNodeType()) || "SerialTask".equalsIgnoreCase(userTaskTemp.getNodeType())) {
                            nodeInfo.setUserVarName(userTaskTemp.getId() + "_List_CounterSign");
                            nodeInfo.setUiType("checkbox");
                        }
                    }

                    if (executor != null && !executor.isEmpty()) {
                        String userType = (String) executor.get("userType");
                        String ids = (String) executor.get("ids");
                        Set<Executor> employeeSet = new HashSet<Executor>();
                        List<Executor> employees = null;
                        nodeInfo.setUiUserType(userType);
                        if ("StartUser".equalsIgnoreCase(userType)) {//获取流程实例启动者
                            FlowInstance flowInstance = flowTask.getFlowInstance();
                            while (flowInstance.getParent() != null) { //以父流程的启动人为准
                                flowInstance = flowInstance.getParent();
                            }
                            String startUserId = null;
                            HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery().processInstanceId(flowInstance.getActInstanceId()).singleResult();
                            if (historicProcessInstance == null) {//当第一个任务为服务任务的时候存在为空的情况发生
                                startUserId = ContextUtil.getUserId();
                            } else {
                                startUserId = historicProcessInstance.getStartUserId();
                            }
                            //根据用户的id列表获取执行人
                            employees = flowCommonUtil.getBasicUserExecutors(Arrays.asList(startUserId));
                        } else {
                            String selfDefId = (String) executor.get("selfDefId");
                            if (StringUtils.isNotEmpty(ids) || StringUtils.isNotEmpty(selfDefId)) {
                                if ("SelfDefinition".equalsIgnoreCase(userType)) {//通过业务ID获取自定义用户
                                    FlowExecutorConfig flowExecutorConfig = flowExecutorConfigDao.findOne(selfDefId);
                                    String path = flowExecutorConfig.getUrl();
                                    AppModule appModule = flowExecutorConfig.getBusinessModel().getAppModule();
                                    String appModuleCode = appModule.getApiBaseAddress();
                                    String businessId = flowTask.getFlowInstance().getBusinessId();
                                    String param = flowExecutorConfig.getParam();
                                    FlowInvokeParams flowInvokeParams = new FlowInvokeParams();
                                    flowInvokeParams.setId(businessId);
//                                    flowInvokeParams.setOrgId(""+flowStartVO.getVariables().get("orgId"));
                                    flowInvokeParams.setJsonParam(param);
                                    String nodeCode = "";
                                    try {
                                        JSONObject normal = currentNode.getJSONObject(Constants.NODE_CONFIG).getJSONObject("normal");
                                        nodeCode = normal.getString("nodeCode");
                                        if (StringUtils.isNotEmpty(nodeCode)) {
                                            Map<String, String> map = new HashMap<String, String>();
                                            map.put("nodeCode", nodeCode);
                                            flowInvokeParams.setParams(map);
                                        }
                                    } catch (Exception e) {
                                    }

//                                    employees = ApiClient.postViaProxyReturnResult(appModuleCode, path, new GenericType<List<Executor>>() {
//                                    }, flowInvokeParams);
                                    employees = flowCommonUtil.getExecutorsBySelfDef(appModuleCode, flowExecutorConfig.getName(), path, flowInvokeParams);
                                } else {
                                    //岗位或者岗位类型（Position、PositionType、AnyOne）、组织机构都改为单据的组织机构
                                    String currentOrgId = this.getOrgIdByFlowTask(flowTask);
                                    employees = flowTaskTool.getExecutors(userType, ids, currentOrgId);
                                }
                            }
                        }
                        if (employees != null && !employees.isEmpty()) {
                            employeeSet.addAll(employees);
                            nodeInfo.setExecutorSet(employeeSet);
                        }
                    } else if (executorList != null && executorList.size() > 1) {
                        Set<Executor> employeeSet = new HashSet<Executor>();
                        List<Executor> employees = null;
                        String selfDefId = null;
                        List<String> orgDimensionCodes = null;//组织维度代码集合
                        List<String> positionIds = null;//岗位代码集合
                        List<String> orgIds = null; //组织机构id集合
                        List<String> positionTypesIds = null;//岗位类别id集合
                        for (Object executorObject : executorList.toArray()) {
                            JSONObject executorTemp = (JSONObject) executorObject;
                            String userType = executorTemp.get("userType") + "";
                            String ids = executorTemp.get("ids") + "";
//                nodeInfo.setUiUserType(userType);
                            List<String> tempList = null;
                            if (StringUtils.isNotEmpty(ids)) {
                                String[] idsShuZhu = ids.split(",");
                                tempList = Arrays.asList(idsShuZhu);
                            }
                            if ("SelfDefinition".equalsIgnoreCase(userType)) {//通过业务ID获取自定义用户
                                selfDefId = executorTemp.get("selfDefOfOrgAndSelId") + "";
                            } else if ("Position".equalsIgnoreCase(userType)) {
                                positionIds = tempList;
                            } else if ("OrganizationDimension".equalsIgnoreCase(userType)) {
                                orgDimensionCodes = tempList;
                            } else if ("PositionType".equalsIgnoreCase(userType)) {
                                positionTypesIds = tempList;
                            } else if ("Org".equalsIgnoreCase(userType)) {
                                orgIds = tempList;
                            }
                        }
                        // 取得当前任务
                        String currentOrgId = this.getOrgIdByFlowTask(flowTask);
                        if (StringUtils.isNotEmpty(selfDefId) && !Constants.NULL_S.equalsIgnoreCase(selfDefId)) {
                            FlowExecutorConfig flowExecutorConfig = flowExecutorConfigDao.findOne(selfDefId);
                            String path = flowExecutorConfig.getUrl();
                            AppModule appModule = flowExecutorConfig.getBusinessModel().getAppModule();
                            String appModuleCode = appModule.getApiBaseAddress();
                            String param = flowExecutorConfig.getParam();
                            FlowInvokeParams flowInvokeParams = new FlowInvokeParams();
                            flowInvokeParams.setId(flowTask.getFlowInstance().getBusinessId());

                            flowInvokeParams.setOrgId(currentOrgId);
                            flowInvokeParams.setPositionIds(positionIds);
                            flowInvokeParams.setPositionTypeIds(positionTypesIds);
                            flowInvokeParams.setOrganizationIds(orgIds);
                            flowInvokeParams.setOrgDimensionCodes(orgDimensionCodes);

                            flowInvokeParams.setJsonParam(param);
                            String nodeCode = "";
                            try {
                                JSONObject normal = currentNode.getJSONObject(Constants.NODE_CONFIG).getJSONObject("normal");
                                nodeCode = normal.getString("nodeCode");
                                if (StringUtils.isNotEmpty(nodeCode)) {
                                    Map<String, String> map = new HashMap<String, String>();
                                    map.put("nodeCode", nodeCode);
                                    flowInvokeParams.setParams(map);
                                }
                            } catch (Exception e) {
                            }
//                            employees = ApiClient.postViaProxyReturnResult(appModuleCode, path, new GenericType<List<Executor>>() {
//                            }, flowInvokeParams);
                            employees = flowCommonUtil.getExecutorsBySelfDef(appModuleCode, flowExecutorConfig.getName(), path, flowInvokeParams);

                        } else {
                            if (positionTypesIds != null && orgIds != null) {
                                //新增根据（岗位类别+组织机构）获得执行人
                                employees = flowCommonUtil.getExecutorsByPostCatIdsAndOrgs(positionTypesIds, orgIds);
                            } else {
                                //通过岗位ids、组织维度ids和组织机构id来获取执行人
                                employees = flowCommonUtil.getExecutorsByPositionIdsAndorgDimIds(positionIds, orgDimensionCodes, currentOrgId);
                            }
                        }
                        if (employees != null && !employees.isEmpty()) {
                            employeeSet.addAll(employees);
                            nodeInfo.setExecutorSet(employeeSet);
                        }
                    }
                }
            }
            if (nodeInfoSonMap != null && !nodeInfoSonMap.isEmpty()) {
                for (Map.Entry<NodeInfo, List<NodeInfo>> entry : nodeInfoSonMap.entrySet()) {
                    nodeInfoList.remove(entry.getKey());
                    nodeInfoList.addAll(entry.getValue());
                }
            }
        }
        return nodeInfoList;
    }


    /**
     * 固化流程获取出口节点信息（带初始化流程设计器配置用户）
     *
     * @param flowTask
     * @param approved
     * @return
     * @throws NoSuchMethodException
     */
    public List<NodeInfo> findNexNodesWithUserSetSolidifyFlow(FlowTask flowTask, String approved, List<String> includeNodeIds) throws NoSuchMethodException {
        List<NodeInfo> nodeInfoList = flowTaskTool.findNextNodesWithCondition(flowTask, approved, includeNodeIds);

        if (nodeInfoList != null && !nodeInfoList.isEmpty()) {
            String flowDefJson = flowTask.getFlowInstance().getFlowDefVersion().getDefJson();
            JSONObject defObj = JSONObject.fromObject(flowDefJson);
            Definition definition = (Definition) JSONObject.toBean(defObj, Definition.class);

            String flowTaskDefJson = flowTask.getTaskJsonDef();
            JSONObject flowTaskDefObj = JSONObject.fromObject(flowTaskDefJson);
            String currentNodeType = flowTaskDefObj.get("nodeType") + "";
            JSONObject normalInfo = flowTaskDefObj.getJSONObject("nodeConfig").getJSONObject("normal");
            Boolean currentSingleTaskAuto = false;
            if (normalInfo != null && normalInfo.has("singleTaskNoChoose") && normalInfo.get("singleTaskNoChoose") != null) {
                currentSingleTaskAuto = normalInfo.getBoolean("singleTaskNoChoose");
            }
            Map<NodeInfo, List<NodeInfo>> nodeInfoSonMap = new LinkedHashMap();
            for (NodeInfo nodeInfo : nodeInfoList) {
                nodeInfo.setCurrentTaskType(currentNodeType);
                nodeInfo.setCurrentSingleTaskAuto(currentSingleTaskAuto);
                if ("CounterSignNotEnd".equalsIgnoreCase(nodeInfo.getType())) {
                    continue;
                } else if ("serviceTask".equalsIgnoreCase(nodeInfo.getType())) {
                    nodeInfo.setUserVarName(nodeInfo.getId() + "_ServiceTask");
                    nodeInfo.setUiType("radiobox");
                    nodeInfo.setFlowTaskType("serviceTask");
                    String userId = ContextUtil.getSessionUser().getUserId();
                    //通过用户id获取用户信息
                    Executor executor = flowCommonUtil.getBasicUserExecutor(userId);
                    if (executor != null) {//服务任务默认选择当前操作人
                        Set<Executor> employeeSet = new HashSet<Executor>();
                        employeeSet.add(executor);
                        nodeInfo.setExecutorSet(employeeSet);
                    }
                } else if ("receiveTask".equalsIgnoreCase(nodeInfo.getType())) {
                    nodeInfo.setUserVarName(nodeInfo.getId() + "_ReceiveTask");
                    nodeInfo.setUiType("radiobox");
                    nodeInfo.setFlowTaskType("receiveTask");
                    String userId = ContextUtil.getSessionUser().getUserId();
                    //通过用户id获取用户信息
                    Executor executor = flowCommonUtil.getBasicUserExecutor(userId);
                    if (executor != null) {//接收任务默认选择当前操作人
                        Set<Executor> employeeSet = new HashSet<Executor>();
                        employeeSet.add(executor);
                        nodeInfo.setExecutorSet(employeeSet);
                    }
                } else if ("callActivity".equalsIgnoreCase(nodeInfo.getType())) {
                    List<NodeInfo> nodeInfoListSons = new ArrayList<NodeInfo>();
                    nodeInfoListSons = flowTaskTool.getCallActivityNodeInfo(flowTask, nodeInfo.getId(), nodeInfoListSons);
                    nodeInfoSonMap.put(nodeInfo, nodeInfoListSons);
                } else {
                    Set<Executor> executorSet = nodeInfo.getExecutorSet();
                    if (executorSet != null && !executorSet.isEmpty()) {
                        continue;
                    }

                    JSONObject currentNode = definition.getProcess().getNodes().getJSONObject(nodeInfo.getId());
                    nodeInfo.setAllowChooseInstancy(false);

                    UserTask userTaskTemp = (UserTask) JSONObject.toBean(currentNode, UserTask.class);
                    if ("EndEvent".equalsIgnoreCase(userTaskTemp.getType())) {
                        nodeInfo.setType("EndEvent");
                        continue;
                    }
                    if (StringUtils.isEmpty(nodeInfo.getUserVarName())) {
                        if ("Normal".equalsIgnoreCase(userTaskTemp.getNodeType())) {
                            nodeInfo.setUserVarName(userTaskTemp.getId() + "_Normal");
                        } else if ("SingleSign".equalsIgnoreCase(userTaskTemp.getNodeType())) {
                            nodeInfo.setUserVarName(userTaskTemp.getId() + "_SingleSign");
                            nodeInfo.setUiType("checkbox");
                        } else if ("Approve".equalsIgnoreCase(userTaskTemp.getNodeType())) {
                            nodeInfo.setUserVarName(userTaskTemp.getId() + "_Approve");
                        } else if ("CounterSign".equalsIgnoreCase(userTaskTemp.getNodeType()) || "ParallelTask".equalsIgnoreCase(userTaskTemp.getNodeType()) || "SerialTask".equalsIgnoreCase(userTaskTemp.getNodeType())) {
                            nodeInfo.setUserVarName(userTaskTemp.getId() + "_List_CounterSign");
                            nodeInfo.setUiType("checkbox");
                        }
                    }

                    if ("pooltask".equalsIgnoreCase(nodeInfo.getFlowTaskType())) {
                        nodeInfo.setExecutorSet(null);
                    } else if ("serviceTask".equalsIgnoreCase(nodeInfo.getFlowTaskType())) {
                        String userId = ContextUtil.getSessionUser().getUserId();
                        Executor executor = flowCommonUtil.getBasicUserExecutor(userId);
                        if (executor != null) {
                            Set<Executor> employeeSet = new HashSet<Executor>();
                            employeeSet.add(executor);
                            nodeInfo.setExecutorSet(employeeSet);
                        }
                    } else {
                        String businessId = flowTask.getFlowInstance().getBusinessId();
                        Search search = new Search();
                        search.addFilter(new SearchFilter("businessId", businessId));
                        search.addFilter(new SearchFilter("actTaskDefKey", nodeInfo.getId()));
                        List<FlowSolidifyExecutor> solidifyExecutorlist = flowSolidifyExecutorDao.findByFilters(search);
                        if (solidifyExecutorlist != null && solidifyExecutorlist.size() > 0) {
                            String userIds = solidifyExecutorlist.get(0).getExecutorIds();
                            String[] idArray = userIds.split(",");
                            List<String> idList = Arrays.asList(idArray);
                            List<Executor> list = flowCommonUtil.getBasicUserExecutors(idList);
                            if (list != null && list.size() > 0) {
                                Set result = new HashSet(list);
                                nodeInfo.setExecutorSet(result);
                            }
                        }
                    }
                }
            }
            if (nodeInfoSonMap != null && !nodeInfoSonMap.isEmpty()) {
                for (Map.Entry<NodeInfo, List<NodeInfo>> entry : nodeInfoSonMap.entrySet()) {
                    nodeInfoList.remove(entry.getKey());
                    nodeInfoList.addAll(entry.getValue());
                }
            }
        }
        return nodeInfoList;
    }

    public List<NodeInfo> findNextNodesOfGateway(String id) throws NoSuchMethodException {
        return this.findNextNodes(id);
    }

    public List<NodeInfo> findNextNodes(String id) throws NoSuchMethodException {
        FlowTask flowTask = flowTaskDao.findOne(id);
        if (flowTask == null) {
            return null;
        }
        String businessId = flowTask.getFlowInstance().getBusinessId();
        return this.findNextNodes(id, businessId);
    }

    /**
     * 选择下一步执行的节点信息
     *
     * @param id
     * @return
     * @throws NoSuchMethodException
     */
    public List<NodeInfo> findNextNodes(String id, String businessId) throws NoSuchMethodException {
        return this.findNextNodes(id, businessId, null);
    }


    /**
     * 选择下一步执行的节点信息
     *
     * @param id
     * @return
     * @throws NoSuchMethodException
     */
    public List<NodeInfo> findNextNodes(String id, String businessId, List<String> includeNodeIds) throws NoSuchMethodException {
        FlowTask flowTask = flowTaskDao.findOne(id);
        return flowTaskTool.selectNextAllNodes(flowTask, includeNodeIds);
    }


    public ApprovalHeaderVO getApprovalHeaderVoOfGateway(String id) {
        return this.getApprovalHeaderVO(id);
    }


    public ApprovalHeaderVO getApprovalHeaderVO(String id) {
        FlowTask flowTask = flowTaskDao.findOne(id);
        if (flowTask == null) {
            return null;
        }
        String preId = flowTask.getPreId();
        FlowHistory preFlowTask = null;
        ApprovalHeaderVO result = new ApprovalHeaderVO();
        result.setBusinessId(flowTask.getFlowInstance().getBusinessId());
        result.setBusinessCode(flowTask.getFlowInstance().getBusinessCode());
        result.setCreateUser(flowTask.getFlowInstance().getCreatorName());
        result.setCreateTime(flowTask.getFlowInstance().getCreatedDate());
        result.setWorkAndAdditionRemark(flowTask.getFlowInstance().getBusinessModelRemark());
        //判断是否是固化流程
        if (flowTask.getFlowInstance().getFlowDefVersion().getSolidifyFlow() == null
                || flowTask.getFlowInstance().getFlowDefVersion().getSolidifyFlow() == false) {
            result.setSolidifyFlow(false);
        } else {
            result.setSolidifyFlow(true);
        }

        String defJson = flowTask.getTaskJsonDef();
        JSONObject defObj = JSONObject.fromObject(defJson);
        JSONObject normalInfo = defObj.getJSONObject("nodeConfig").getJSONObject("normal");
        //如果节点配置了默认意见，就在请求头中返回
        if (normalInfo.has("defaultOpinion")) {
            result.setCurrentNodeDefaultOpinion(normalInfo.getString("defaultOpinion"));
        }

        if (!StringUtils.isEmpty(preId)) {
            preFlowTask = flowHistoryDao.findOne(flowTask.getPreId());//上一个任务id
        }
        if (preFlowTask == null) {//如果没有上一步任务信息,默认上一步为开始节点
            result.setPrUser(flowTask.getFlowInstance().getCreatorAccount() + "[" + flowTask.getFlowInstance().getCreatorName() + "]");
            result.setPreCreateTime(flowTask.getFlowInstance().getCreatedDate());
            result.setPrOpinion("流程启动");
        } else {
            result.setPrUser(preFlowTask.getExecutorAccount() + "[" + preFlowTask.getExecutorName() + "]");
            result.setPreCreateTime(preFlowTask.getCreatedDate());
            result.setPrOpinion(preFlowTask.getDepict());
        }
        //处理界面用于判断按钮的参数（没有按钮可以不需要）
        result.setTrustState(flowTask.getTrustState());
        result.setCanReject(flowTask.getCanReject());
        result.setTaskJsonDef(flowTask.getTaskJsonDef());
        result.setActClaimTime(flowTask.getActClaimTime());
        result.setCanSuspension(flowTask.getCanSuspension());
        result.setExecutorId(flowTask.getExecutorId());
        result.setFlowInstanceCreatorId(flowTask.getFlowInstance().getCreatorId());
        return result;
    }

    public List<NodeInfo> findNexNodesWithUserSet(String id) throws NoSuchMethodException {
        FlowTask flowTask = flowTaskDao.findOne(id);
        if (flowTask == null) {
            return null;
        }
        return this.findNexNodesWithUserSet(flowTask, null, null);
    }

    public List<NodeInfo> findNexNodesWithUserSet(FlowTask flowTask) throws NoSuchMethodException {
        if (flowTask == null) {
            return null;
        }
        return this.findNexNodesWithUserSet(flowTask, null, null);
    }

    public List<NodeInfo> findNexNodesWithUserSetSolidifyFlow(FlowTask flowTask) throws NoSuchMethodException {
        if (flowTask == null) {
            return null;
        }
        return this.findNexNodesWithUserSetSolidifyFlow(flowTask, null, null);
    }

    public List<NodeInfo> findNexNodesWithUserSet(String id, String approved, List<String> includeNodeIds) throws NoSuchMethodException {
        FlowTask flowTask = flowTaskDao.findOne(id);
        if (flowTask == null) {
            return null;
        }
        List<NodeInfo> result = null;
        List<NodeInfo> nodeInfoList = this.findNexNodesWithUserSet(flowTask, approved, includeNodeIds);
        result = nodeInfoList;
        FlowInstance parentFlowInstance = flowTask.getFlowInstance().getParent();
        FlowTask flowTaskTempSrc = new FlowTask();
        BeanUtils.copyProperties(flowTask, flowTaskTempSrc);

        while (parentFlowInstance != null && nodeInfoList != null && !nodeInfoList.isEmpty() && nodeInfoList.size() == 1 && "EndEvent".equalsIgnoreCase(nodeInfoList.get(0).getType())) {//针对子流程结束节点
            FlowTask flowTaskTemp = new FlowTask();
            BeanUtils.copyProperties(flowTaskTempSrc, flowTaskTemp);

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
                String flowDefJson = parentFlowInstance.getFlowDefVersion().getDefJson();
                JSONObject defObj = JSONObject.fromObject(flowDefJson);
                Definition definition = (Definition) JSONObject.toBean(defObj, Definition.class);
                JSONObject currentNode = definition.getProcess().getNodes().getJSONObject(he.getActivityId());
                flowTaskTemp.setTaskJsonDef(currentNode.toString());
                result = this.findNexNodesWithUserSet(flowTaskTemp, approved, includeNodeIds);
                flowTaskTempSrc = flowTaskTemp;
            }
            parentFlowInstance = parentFlowInstance.getParent();
            nodeInfoList = result;
        }
        return result;
    }

    /**
     * 根据流程实例Id获取待办
     *
     * @param instanceId
     * @return
     */
    @Override
    public List<FlowTask> findByInstanceId(String instanceId) {
        return flowTaskDao.findByInstanceId(instanceId);
    }


    public ResponseData listFlowTaskHeader() {
        ResponseData responseData = new ResponseData();
        try {
            List<TodoBusinessSummaryVO> list = this.findTaskSumHeader("");
            responseData.setData(list);
        } catch (Exception e) {
            LogUtil.error(e.getMessage(), e);
            responseData.setSuccess(false);
            responseData.setMessage("操作失败！");
        }
        return responseData;
    }

    /**
     * 查询当前用户待办业务单据汇总信息
     *
     * @param appSign 应用标识
     * @return 汇总信息
     */
    public List<TodoBusinessSummaryVO> findTaskSumHeader(String appSign) {
        return this.findCommonTaskSumHeader(false, appSign);
    }


    public ResponseData findTaskSumHeaderOfPhone() {
        List<TodoBusinessSummaryVO> list = this.findTaskSumHeader("");
        ResponseData responseData = new ResponseData();
        responseData.setSuccess(true);
        responseData.setMessage("成功");
        if (list == null) {
            list = new ArrayList<TodoBusinessSummaryVO>();
        }
        responseData.setData(list);
        return responseData;
    }


    public ResponseData findTaskSumHeaderCanBatchApprovalOfPhone() {
        ResponseData responseData = new ResponseData();
        List<TodoBusinessSummaryVO> result = this.findCommonTaskSumHeader(true, "");
        responseData.setSuccess(true);
        responseData.setMessage("操作成功!");
        if (result == null) {
            result = new ArrayList<TodoBusinessSummaryVO>();
        }
        responseData.setData(result);
        return responseData;
    }


    public Integer getUserTodoSum(String userId) {
        //根据被授权人ID查看所有满足的转授权设置信息（共同查看模式）
        List<TaskMakeOverPower> list = taskMakeOverPowerService.findPowerByPowerUser(userId);
        int allTodoSum = 0;
        if (list != null && !list.isEmpty()) {
            for (int i = 0; i < list.size(); i++) {
                TaskMakeOverPower bean = list.get(i);
                if (StringUtils.isNotEmpty(bean.getFlowTypeId())) { //流程类型
                    int typeSum = flowTaskDao.findTodoSumByExecutorIdAndFlowTypeId(bean.getUserId(), bean.getFlowTypeId());
                    allTodoSum += typeSum;
                }
            }
        }
        int exeListSum = flowTaskDao.findTodoSumByExecutorId(userId);
        allTodoSum += exeListSum;
        return allTodoSum;
    }


    /**
     * 查询当前用户待办业务单据汇总信息,只有批量审批
     *
     * @param appSign 应用标识
     * @return
     */
    public List<TodoBusinessSummaryVO> findCommonTaskSumHeader(Boolean batchApproval, String appSign) {
        List<TodoBusinessSummaryVO> voList = new ArrayList<>();
        String userID = ContextUtil.getUserId();
        List groupResultList = new ArrayList();

        List<TaskMakeOverPower> powerList = taskMakeOverPowerService.findPowerByPowerUser(userID);
        if (powerList != null && !powerList.isEmpty()) {
            if (batchApproval == true) {
                for (int i = 0; i < powerList.size(); i++) {
                    TaskMakeOverPower bean = powerList.get(i);
                    if (StringUtils.isNotEmpty(bean.getFlowTypeId())) { //流程类型
                        List typeList = flowTaskDao.findCanBatchApprovalByExecutorAndFlowTypeId(bean.getUserId(), bean.getFlowTypeId());
                        groupResultList.addAll(typeList);
                    }
                }
            } else {
                for (int i = 0; i < powerList.size(); i++) {
                    TaskMakeOverPower bean = powerList.get(i);
                    if (StringUtils.isNotEmpty(bean.getFlowTypeId())) { //流程类型
                        List typeList = flowTaskDao.findGroupByExecutorIdAndAndFlowTypeId(bean.getUserId(), bean.getFlowTypeId());
                        groupResultList.addAll(typeList);
                    }
                }
            }
        }

        if (batchApproval == true) {
            List list = flowTaskDao.findByExecutorIdGroupCanBatchApprovalOfPower(userID);
            groupResultList.addAll(list);
        } else {
            List list = flowTaskDao.findByExecutorIdGroupOfPower(userID);
            groupResultList.addAll(list);
        }

        Map<BusinessModel, Integer> businessModelCountMap = new HashMap<BusinessModel, Integer>();
        if (groupResultList != null && !groupResultList.isEmpty()) {
            Iterator it = groupResultList.iterator();
            while (it.hasNext()) {
                Object[] res = (Object[]) it.next();
                int count = ((Number) res[0]).intValue();
                String flowDefinationId = res[1] + "";
                FlowDefination flowDefination = flowDefinationDao.findOne(flowDefinationId);
                if (flowDefination == null) {
                    continue;
                }
                // 获取业务类型
                BusinessModel businessModel = businessModelDao.findOne(flowDefination.getFlowType().getBusinessModel().getId());
                // 限制应用标识
                boolean canAdd = true;
                if (!StringUtils.isBlank(appSign)) {
                    // 判断应用模块代码是否以应用标识开头,不是就不添加
                    if (!businessModel.getAppModule().getCode().startsWith(appSign)) {
                        canAdd = false;
                    }
                }
                if (canAdd) {
                    Integer oldCount = businessModelCountMap.get(businessModel);
                    if (oldCount == null) {
                        oldCount = 0;
                    }
                    businessModelCountMap.put(businessModel, oldCount + count);
                }
            }
        }
        if (!businessModelCountMap.isEmpty()) {
            for (Map.Entry<BusinessModel, Integer> map : businessModelCountMap.entrySet()) {
                TodoBusinessSummaryVO todoBusinessSummaryVO = new TodoBusinessSummaryVO();
                todoBusinessSummaryVO.setBusinessModelCode(map.getKey().getClassName());
                todoBusinessSummaryVO.setBusinessModeId(map.getKey().getId());
                todoBusinessSummaryVO.setCount(map.getValue());
                todoBusinessSummaryVO.setBusinessModelName(map.getKey().getName() + "(" + map.getValue() + ")");
                voList.add(todoBusinessSummaryVO);
            }
        }
        return voList;
    }


    public PageResult<FlowTask> findAllByTenant(String appModuleId, String businessModelId, String flowTypeId, Search searchConfig) {
        SessionUser sessionUser = ContextUtil.getSessionUser();
        UserAuthorityPolicy authorityPolicy = sessionUser.getAuthorityPolicy();
        if (!authorityPolicy.equals(UserAuthorityPolicy.TenantAdmin)) {
            return null;
        }
        PageResult<FlowTask> pageResult = flowTaskDao.findByPageByTenant(appModuleId, businessModelId, flowTypeId, searchConfig);
        List<FlowTask> result = pageResult.getRows();
        initFlowTasks(result);
        return pageResult;
    }


    public PageResult<FlowTask> findByPageCanBatchApproval(Search searchConfig) {
        return this.findByPageCanBatchApprovalByBusinessModelId(null, searchConfig);
    }

    public PageResult<FlowTaskBatchPhoneVO> findByPageCanBatchApprovalOfMobile(String businessModelId, int page, int rows, String quickValue) {
        Search search = new Search();
        search.addQuickSearchProperty("flowName");
        search.addQuickSearchProperty("taskName");
        search.addQuickSearchProperty("flowInstance.businessCode");
        search.addQuickSearchProperty("flowInstance.businessModelRemark");
        search.addQuickSearchProperty("creatorName");
        search.setQuickSearchValue(quickValue);

        PageInfo pageInfo = new PageInfo();
        pageInfo.setPage(page);
        pageInfo.setRows(rows);
        search.setPageInfo(pageInfo);

        SearchOrder searchOrder = new SearchOrder("createdDate", SearchOrder.Direction.DESC);
        List<SearchOrder> list = new ArrayList<SearchOrder>();
        list.add(searchOrder);
        search.setSortOrders(list);

        PageResult<FlowTask> flowTaskPage = this.findByPageCanBatchApprovalByBusinessModelId(businessModelId, search);
        PageResult<FlowTaskBatchPhoneVO> phoneVoPage = new PageResult<FlowTaskBatchPhoneVO>();
        phoneVoPage.setPage(flowTaskPage.getPage());
        phoneVoPage.setRecords(flowTaskPage.getRecords());
        phoneVoPage.setTotal(flowTaskPage.getTotal());
        if (flowTaskPage.getRows() != null && flowTaskPage.getRows().size() > 0) {
            List<FlowTask> taskList = flowTaskPage.getRows();
            List<FlowTaskBatchPhoneVO> phoneVoList = new ArrayList<FlowTaskBatchPhoneVO>();
            taskList.forEach(bean -> {
                FlowTaskBatchPhoneVO beanVo = new FlowTaskBatchPhoneVO();
                FlowType flowType = bean.getFlowInstance().getFlowDefVersion().getFlowDefination().getFlowType();
                beanVo.setFlowInstanceBusinessCode(bean.getFlowInstance().getBusinessCode());
                beanVo.setTaskName(bean.getTaskName());
                beanVo.setFlowTypeName(flowType.getName());
                beanVo.setActClaimTime(bean.getActClaimTime());
                beanVo.setCreatedDate(bean.getCreatedDate());
                beanVo.setCanMobile(bean.getCanMobile());
                beanVo.setTaskId(bean.getId());
                beanVo.setFlowName(bean.getFlowName());

                String taskJsonDef = bean.getTaskJsonDef();
                JSONObject taskJsonDefObj = JSONObject.fromObject(taskJsonDef);
                String nodeType = taskJsonDefObj.get("nodeType") + "";
                beanVo.setNodeType(nodeType);

                String webBaseAddress = Constants.getConfigValueByWeb(flowType.getBusinessModel().getAppModule().getWebBaseAddress());
                if (StringUtils.isNotEmpty(webBaseAddress)) {
                    String[] tempWebBaseAddress = webBaseAddress.split("/");
                    if (tempWebBaseAddress != null && tempWebBaseAddress.length > 0) {
                        webBaseAddress = tempWebBaseAddress[tempWebBaseAddress.length - 1];
                        webBaseAddress = "/" + webBaseAddress + "/";
                    }
                }
                beanVo.setCompleteTaskUrl(webBaseAddress);
                phoneVoList.add(beanVo);
            });
            phoneVoPage.setRows(phoneVoList);
        } else {
            phoneVoPage.setRows(new ArrayList<FlowTaskBatchPhoneVO>());
        }

        return phoneVoPage;
    }


    public PageResult<FlowTask> findByPageCanBatchApprovalOfPhone(String businessModelId, String property, String direction, int page, int rows, String quickValue) {
        Search search = new Search();
        search.addQuickSearchProperty("flowName");
        search.addQuickSearchProperty("taskName");
        search.addQuickSearchProperty("flowInstance.businessCode");
        search.addQuickSearchProperty("flowInstance.businessModelRemark");
        search.addQuickSearchProperty("creatorName");
        search.setQuickSearchValue(quickValue);

        PageInfo pageInfo = new PageInfo();
        pageInfo.setPage(page);
        pageInfo.setRows(rows);
        search.setPageInfo(pageInfo);

        SearchOrder searchOrder;
        if (StringUtils.isNotEmpty(property) && StringUtils.isNotEmpty(direction)) {
            if (SearchOrder.Direction.ASC.equals(direction)) {
                searchOrder = new SearchOrder(property, SearchOrder.Direction.ASC);
            } else {
                searchOrder = new SearchOrder(property, SearchOrder.Direction.DESC);
            }
        } else {
            searchOrder = new SearchOrder("createdDate", SearchOrder.Direction.DESC);
        }
        List<SearchOrder> list = new ArrayList<SearchOrder>();
        list.add(searchOrder);
        search.setSortOrders(list);

        return this.findByPageCanBatchApprovalByBusinessModelId(businessModelId, search);
    }

    public PageResult<FlowTask> findByPageCanBatchApprovalByBusinessModelId(String businessModelId, Search searchConfig) {
        String userId = ContextUtil.getUserId();
        PageResult<FlowTask> flowTaskPageResult;

        //根据被授权人ID查看所有满足的转授权设置信息（共同查看模式）
        List<TaskMakeOverPower> powerList = taskMakeOverPowerService.findPowerByPowerUser(userId);
        if (StringUtils.isNotEmpty(businessModelId)) {
            flowTaskPageResult = flowTaskDao.findByPageCanBatchApprovalByBusinessModelIdOfPower(businessModelId, userId, powerList, searchConfig);
        } else {
            flowTaskPageResult = flowTaskDao.findByPageCanBatchApprovalOfPower(userId, powerList, searchConfig);
        }

        //说明添加授权人信息
        List<FlowTask> flowTaskList = flowTaskPageResult.getRows();

        initFlowTasks(flowTaskList);

        flowTaskList.forEach(a -> {
            if (!userId.equals(a.getExecutorId())) {
                a.getFlowInstance().setBusinessModelRemark("【" + a.getExecutorName() + "-转授权】" + a.getFlowInstance().getBusinessModelRemark());
            }
        });

        FlowTaskTool.changeTaskStatue(flowTaskPageResult);
        return flowTaskPageResult;
    }


    /**
     * 获取可批量审批待办信息(最新移动端专用)
     */
    public FlowTaskPageResultVO<FlowTaskPhoneVo> findByBusinessModelIdWithAllCountOfMobile(String businessModelId, int page, int rows, String quickValue) {
        Search search = new Search();
        search.addQuickSearchProperty("flowName");
        search.addQuickSearchProperty("taskName");
        search.addQuickSearchProperty("flowInstance.businessCode");
        search.addQuickSearchProperty("flowInstance.businessModelRemark");
        search.addQuickSearchProperty("creatorName");
        search.setQuickSearchValue(quickValue);

        PageInfo pageInfo = new PageInfo();
        pageInfo.setPage(page);
        pageInfo.setRows(rows);
        search.setPageInfo(pageInfo);

        SearchOrder searchOrder = new SearchOrder("createdDate", SearchOrder.Direction.ASC);
        List<SearchOrder> list = new ArrayList<SearchOrder>();
        list.add(searchOrder);
        search.setSortOrders(list);

        FlowTaskPageResultVO<FlowTask> flowTaskPage = findByBusinessModelIdWithAllCount(businessModelId, "", search);
        FlowTaskPageResultVO<FlowTaskPhoneVo> phoneVoPage = new FlowTaskPageResultVO<FlowTaskPhoneVo>();
        phoneVoPage.setAllTotal(flowTaskPage.getAllTotal());
        phoneVoPage.setPage(flowTaskPage.getPage());
        phoneVoPage.setRecords(flowTaskPage.getRecords());
        phoneVoPage.setTotal(flowTaskPage.getTotal());
        if (flowTaskPage.getAllTotal() != 0 && flowTaskPage.getRows() != null && flowTaskPage.getRows().size() > 0) {
            List<FlowTask> taskList = flowTaskPage.getRows();
            List<FlowTaskPhoneVo> phoneVoList = new ArrayList<FlowTaskPhoneVo>();
            taskList.forEach(bean -> {
                FlowTaskPhoneVo beanVo = new FlowTaskPhoneVo();
                FlowType flowType = bean.getFlowInstance().getFlowDefVersion().getFlowDefination().getFlowType();
                beanVo.setFlowInstanceBusinessCode(bean.getFlowInstance().getBusinessCode());
                beanVo.setFlowInstanceFlowName(bean.getFlowInstance().getFlowName());
                beanVo.setTaskName(bean.getTaskName());
                beanVo.setFlowTypeId(flowType.getId());
                beanVo.setFlowTypeName(flowType.getName());
                beanVo.setActClaimTime(bean.getActClaimTime());
                beanVo.setCreatedDate(bean.getCreatedDate());
                beanVo.setBusinessModelClassName(flowType.getBusinessModel().getClassName());
                beanVo.setFlowInstanceBusinessId(bean.getFlowInstance().getBusinessId());
                beanVo.setCanReject(bean.getCanReject());
                beanVo.setFlowInstanceId(bean.getFlowInstance().getId());
                beanVo.setCanSuspension(bean.getCanSuspension());
                beanVo.setCanMobile(bean.getCanMobile());
                beanVo.setTaskId(bean.getId());
                beanVo.setTrustState(bean.getTrustState());

                String taskJsonDef = bean.getTaskJsonDef();
                JSONObject taskJsonDefObj = JSONObject.fromObject(taskJsonDef);
                String nodeType = taskJsonDefObj.get("nodeType") + "";
                beanVo.setNodeType(nodeType);

                String apiBaseAddress = Constants.getConfigValueByApi(flowType.getBusinessModel().getAppModule().getApiBaseAddress());
                beanVo.setApiBaseAddress(apiBaseAddress);
                beanVo.setBusinessDetailServiceUrl(bean.getBusinessDetailServiceUrl());

                String webBaseAddress = Constants.getConfigValueByWeb(flowType.getBusinessModel().getAppModule().getWebBaseAddress());
                if (StringUtils.isNotEmpty(webBaseAddress)) {
                    String[] tempWebBaseAddress = webBaseAddress.split("/");
                    if (tempWebBaseAddress != null && tempWebBaseAddress.length > 0) {
                        webBaseAddress = tempWebBaseAddress[tempWebBaseAddress.length - 1];
                        webBaseAddress = "/" + webBaseAddress + "/";
                    }
                }
                beanVo.setCompleteTaskUrl(webBaseAddress + flowType.getBusinessModel().getCompleteTaskServiceUrl());

                phoneVoList.add(beanVo);
            });
            phoneVoPage.setRows(phoneVoList);
        } else {
            phoneVoPage.setRows(new ArrayList<FlowTaskPhoneVo>());
        }

        return phoneVoPage;
    }

    public FlowTaskPageResultVO<FlowTask> findByBusinessModelIdWithAllCountOfPhone(String businessModelId, String property,
                                                                                   String direction, int page, int rows, String quickValue) {
        Search search = new Search();
        search.addQuickSearchProperty("flowName");
        search.addQuickSearchProperty("taskName");
        search.addQuickSearchProperty("flowInstance.businessCode");
        search.addQuickSearchProperty("flowInstance.businessModelRemark");
        search.addQuickSearchProperty("creatorName");
        search.setQuickSearchValue(quickValue);

        PageInfo pageInfo = new PageInfo();
        pageInfo.setPage(page);
        pageInfo.setRows(rows);
        search.setPageInfo(pageInfo);

        SearchOrder searchOrder;
        if (StringUtils.isNotEmpty(property) && StringUtils.isNotEmpty(direction)) {
            if (SearchOrder.Direction.ASC.equals(direction)) {
                searchOrder = new SearchOrder(property, SearchOrder.Direction.ASC);
            } else {
                searchOrder = new SearchOrder(property, SearchOrder.Direction.DESC);
            }
        } else {
            searchOrder = new SearchOrder("createdDate", SearchOrder.Direction.ASC);
        }
        List<SearchOrder> list = new ArrayList<SearchOrder>();
        list.add(searchOrder);
        search.setSortOrders(list);

        return findByBusinessModelIdWithAllCount(businessModelId, "", search);
    }

    @Override
    public ResponseData listFlowTaskWithAllCount(Search search, String modelId) {
        ResponseData responseData = new ResponseData();
        try {
            FlowTaskPageResultVO<FlowTask> resVo = this.findByBusinessModelIdWithAllCount(modelId, "", search);
            responseData.setData(resVo);
        } catch (Exception e) {
            LogUtil.error(e.getMessage(), e);
            responseData.setSuccess(false);
            responseData.setMessage("操作失败！");
        }
        return responseData;
    }

    @Override
    public ResponseData listFlowTaskByUserId(Search search, String userId) {
        if (StringUtils.isEmpty(userId) || "anonymous".equalsIgnoreCase(userId)) {
            throw new FlowException("会话超时，请重新登录！");
        }
        FlowTaskPageResultVO<FlowTask> resultVO = new FlowTaskPageResultVO<FlowTask>();


        //根据被授权人ID查看所有满足的转授权设置信息（共同查看模式）
        List<TaskMakeOverPower> powerList = taskMakeOverPowerService.findPowerByPowerUser(userId);
        if (search == null) {
            search = new Search();
            PageInfo pageInfo = new PageInfo();
            pageInfo.setPage(1);
            pageInfo.setRows(1000);
            search.setPageInfo(pageInfo);
        }
        PageResult<FlowTask> pageResult = flowTaskDao.findByPageOfPower(userId, powerList, "", search);


        //说明添加授权人信息
        List<FlowTask> flowTaskList = pageResult.getRows();

        initFlowTasks(flowTaskList);

        flowTaskList.forEach(a -> {
            if (!userId.equals(a.getExecutorId())) {
                a.getFlowInstance().setBusinessModelRemark("【" + a.getExecutorName() + "-转授权】" + a.getFlowInstance().getBusinessModelRemark());
            }
        });
        resultVO.setRows(flowTaskList);
        resultVO.setRecords(pageResult.getRecords());
        resultVO.setPage(pageResult.getPage());
        resultVO.setTotal(pageResult.getTotal());
        resultVO.setAllTotal(pageResult.getRecords());
        return ResponseData.operationSuccessWithData(resultVO);
    }


    public FlowTaskPageResultVO<FlowTask> findByBusinessModelIdWithAllCount(String businessModelId, String appSign, Search searchConfig) {
        String userId = ContextUtil.getUserId();
        FlowTaskPageResultVO<FlowTask> resultVO = new FlowTaskPageResultVO<FlowTask>();
        if (StringUtils.isEmpty(userId) || "anonymous".equalsIgnoreCase(userId)) {
            throw new FlowException("会话超时，请重新登录！");
        }
        PageResult<FlowTask> pageResult = null;

        //根据被授权人ID查看所有满足的转授权设置信息（共同查看模式）
        List<TaskMakeOverPower> powerList = taskMakeOverPowerService.findPowerByPowerUser(userId);

        if (StringUtils.isNotEmpty(businessModelId)) {
            pageResult = flowTaskDao.findByPageByBusinessModelIdOfPower(businessModelId, userId, powerList, searchConfig);
        } else {
            pageResult = flowTaskDao.findByPageOfPower(userId, powerList, appSign, searchConfig);
        }
        //说明添加授权人信息
        List<FlowTask> flowTaskList = pageResult.getRows();

        initFlowTasks(flowTaskList);

        flowTaskList.forEach(a -> {
            if (!userId.equals(a.getExecutorId())) {
                a.getFlowInstance().setBusinessModelRemark("【" + a.getExecutorName() + "-转授权】" + a.getFlowInstance().getBusinessModelRemark());
            }
        });
        resultVO.setRows(flowTaskList);
        resultVO.setRecords(pageResult.getRecords());
        resultVO.setPage(pageResult.getPage());
        resultVO.setTotal(pageResult.getTotal());
        resultVO.setAllTotal(pageResult.getRecords());
        return resultVO;
    }


    public List<BatchApprovalFlowTaskGroupVO> getBatchApprovalFlowTasks(List<String> taskIdArray) throws NoSuchMethodException {
        List<BatchApprovalFlowTaskGroupVO> result = new ArrayList<>();
        List<FlowTask> flowTaskList = this.findByIds(taskIdArray);
        if (flowTaskList != null && !flowTaskList.isEmpty()) {
            for (FlowTask flowTask : flowTaskList) {
                List<NodeInfo> nodeInfoList = this.findNexNodesWithUserSet(flowTask, "true", null);
                BatchApprovalFlowTaskGroupVO batchApprovalFlowTaskGroupVO = new BatchApprovalFlowTaskGroupVO();
                String key = flowTask.getActTaskDefKey() + "@" + flowTask.getFlowInstance().getFlowDefVersion().getVersionCode() + "@" + flowTask.getFlowDefinitionId();
                batchApprovalFlowTaskGroupVO.setKey(key);
                int index = result.indexOf(batchApprovalFlowTaskGroupVO);
                if (index > -1) {
                    batchApprovalFlowTaskGroupVO = result.get(index);
                } else {
                    result.add(batchApprovalFlowTaskGroupVO);
                }
                Map<FlowTask, List<NodeInfo>> flowTaskNextNodesInfoMap = batchApprovalFlowTaskGroupVO.getFlowTaskNextNodesInfo();
                flowTaskNextNodesInfoMap.put(flowTask, nodeInfoList);
            }
        }
        return result;
    }

    public OperateResultWithData<FlowStatus> completeBatchApproval(List<FlowTaskCompleteVO> flowTaskCompleteVOList) throws Exception {
        for (FlowTaskCompleteVO flowTaskCompleteVO : flowTaskCompleteVOList) {
            OperateResultWithData<FlowStatus> tempResult = this.complete(flowTaskCompleteVO);
            if (!tempResult.successful()) {
                throw new FlowException("batch approval is failure! ");
            }
        }
        return OperateResultWithData.operationSuccess("10017");
    }

    public OperateResultWithData getSelectedNodesInfo(String taskId, String approved, String includeNodeIdsStr, Boolean solidifyFlow) throws NoSuchMethodException {
        OperateResultWithData operateResultWithData = null;
        List<String> includeNodeIds = null;
        if (StringUtils.isNotEmpty(includeNodeIdsStr)) {
            String[] includeNodeIdsStringArray = includeNodeIdsStr.split(",");
            includeNodeIds = Arrays.asList(includeNodeIdsStringArray);
        }
        if (StringUtils.isEmpty(approved)) {
            approved = "true";
        }
        List<NodeInfo> nodeInfoList = null;
        try {
            nodeInfoList = this.findNexNodesWithUserSet(taskId, approved, includeNodeIds);
        } catch (Exception e) {
            LogUtil.error("获取下一节点信息错误，详情请查看日志！", e);
            return OperateResultWithData.operationFailure("获取下一节点信息错误，详情请查看日志！");
        }

        if (nodeInfoList != null && !nodeInfoList.isEmpty()) {
            operateResultWithData = OperateResultWithData.operationSuccess();
            if (nodeInfoList.size() == 1 && "EndEvent".equalsIgnoreCase(nodeInfoList.get(0).getType())) {//只存在结束节点
                operateResultWithData.setData("EndEvent");
            } else if (nodeInfoList.size() == 1 && "CounterSignNotEnd".equalsIgnoreCase(nodeInfoList.get(0).getType())) {
                operateResultWithData.setData("CounterSignNotEnd");
            } else {
                if (solidifyFlow != null && solidifyFlow == true) { //表示为固化流程
                    FlowTask flowTask = flowTaskDao.findOne(taskId);
                    //设置固化执行人信息(只是前台展示使用)
                    nodeInfoList = flowSolidifyExecutorService.
                            setNodeExecutorByBusinessId(nodeInfoList, flowTask.getFlowInstance().getBusinessId());
                }
                operateResultWithData.setData(nodeInfoList);
            }
        } else if (nodeInfoList == null) {
            operateResultWithData = OperateResultWithData.operationFailure("任务不存在，可能已经被处理！");
        } else {
            operateResultWithData = OperateResultWithData.operationFailure("当前规则找不到符合条件的分支！");
        }
        return operateResultWithData;
    }


    public List<NodeInfo> findNexNodesWithUserSetCanBatch(String taskIds) throws NoSuchMethodException {
        List<NodeInfo> all = new ArrayList<>();
        List<String> taskIdList = null;
        if (StringUtils.isNotEmpty(taskIds)) {
            String[] includeNodeIdsStringArray = taskIds.split(",");
            taskIdList = Arrays.asList(includeNodeIdsStringArray);
        }
        if (taskIdList != null && !taskIdList.isEmpty()) {
            String approved = "true";
            for (String taskId : taskIdList) {
                List<NodeInfo> nodeInfoList = this.findNexNodesWithUserSet(taskId, approved, null);
                if (nodeInfoList != null && !nodeInfoList.isEmpty()) {
                    all.addAll(nodeInfoList);
                }
            }
        }
        return all;
    }

    public List<NodeGroupInfo> findNexNodesGroupWithUserSetCanBatch(String taskIds) throws NoSuchMethodException {
        List<NodeGroupInfo> all = new ArrayList<NodeGroupInfo>();
        List<String> taskIdList = null;
        if (StringUtils.isNotEmpty(taskIds)) {
            String[] includeNodeIdsStringArray = taskIds.split(",");
            taskIdList = Arrays.asList(includeNodeIdsStringArray);
        }
        if (taskIdList != null && !taskIdList.isEmpty()) {
            String approved = "true";
            Map<String, NodeGroupInfo> tempNodeGroupInfoMap = new HashMap<>();
            for (String taskId : taskIdList) {
                List<NodeInfo> nodeInfoList = this.findNexNodesWithUserSet(taskId, approved, null);
                if (nodeInfoList != null && !nodeInfoList.isEmpty()) {
                    for (NodeInfo nodeInfo : nodeInfoList) {
                        String flowDefVersionId = nodeInfo.getFlowDefVersionId();
                        NodeGroupInfo tempNodeGroupInfo = tempNodeGroupInfoMap.get(flowDefVersionId + nodeInfo.getId());
                        if (tempNodeGroupInfo == null) {
                            tempNodeGroupInfo = new NodeGroupInfo();
                            tempNodeGroupInfo.setFlowDefVersionId(flowDefVersionId);
                            tempNodeGroupInfo.setNodeId(nodeInfo.getId());
                            tempNodeGroupInfo.setFlowDefVersionName(nodeInfo.getFlowDefVersionName());
                            BeanUtils.copyProperties(nodeInfo, tempNodeGroupInfo);
                            tempNodeGroupInfo.getIds().add(nodeInfo.getFlowTaskId());
                            tempNodeGroupInfo.setExecutorSet(nodeInfo.getExecutorSet());
                            tempNodeGroupInfoMap.put(flowDefVersionId + nodeInfo.getId(), tempNodeGroupInfo);
                        } else {
                            tempNodeGroupInfo.getIds().add(nodeInfo.getFlowTaskId());
                        }
                    }
                }
            }
            all.addAll(tempNodeGroupInfoMap.values());
        }
        return all;
    }

    public ResponseData getSelectedCanBatchNodesInfoOfPhone(String taskIds) throws NoSuchMethodException {
        ResponseData responseData = new ResponseData();
        List<NodeGroupByFlowVersionInfo> nodeInfoList = this.findNexNodesGroupByVersionWithUserSetCanBatch(taskIds);
        if (nodeInfoList != null && !nodeInfoList.isEmpty()) {
            responseData.setSuccess(true);
            responseData.setMessage("成功");
            responseData.setData(nodeInfoList);
        } else {
            responseData.setSuccess(false);
            responseData.setMessage("选取的任务不存在，可能已经被处理");
        }
        return responseData;
    }


    public List<NodeGroupByFlowVersionInfo> findNexNodesGroupByVersionWithUserSetCanBatch(String taskIds) throws NoSuchMethodException {
        List<NodeGroupByFlowVersionInfo> all = new ArrayList<NodeGroupByFlowVersionInfo>();
        List<NodeGroupInfo> nodeGroupInfoList = this.findNexNodesGroupWithUserSetCanBatch(taskIds);
        Map<String, NodeGroupByFlowVersionInfo> nodeGroupByFlowVersionInfoMap = new HashMap<String, NodeGroupByFlowVersionInfo>();
        if (nodeGroupInfoList != null && !nodeGroupInfoList.isEmpty()) {
            for (NodeGroupInfo nodeGroupInfo : nodeGroupInfoList) {
                String flowDefVersionId = nodeGroupInfo.getFlowDefVersionId();
                NodeGroupByFlowVersionInfo nodeGroupByFlowVersionInfo = nodeGroupByFlowVersionInfoMap.get(flowDefVersionId);
                if (nodeGroupByFlowVersionInfo == null) {
                    nodeGroupByFlowVersionInfo = new NodeGroupByFlowVersionInfo();
                    nodeGroupByFlowVersionInfo.setId(flowDefVersionId);
                    nodeGroupByFlowVersionInfo.setName(nodeGroupInfo.getFlowDefVersionName());
                    FlowDefVersion flowDefVersion = flowDefVersionDao.findOne(flowDefVersionId);
                    if (flowDefVersion != null) {
                        Boolean boo = flowDefVersion.getSolidifyFlow() == null ? false : flowDefVersion.getSolidifyFlow();
                        nodeGroupByFlowVersionInfo.setSolidifyFlow(boo);
                    }
                    if (nodeGroupByFlowVersionInfo.getSolidifyFlow() == true) {
                        nodeGroupInfo.setExecutorSet(null);
                    }
                    nodeGroupByFlowVersionInfo.getNodeGroupInfos().add(nodeGroupInfo);
                    nodeGroupByFlowVersionInfoMap.put(flowDefVersionId, nodeGroupByFlowVersionInfo);
                } else {
                    if (nodeGroupByFlowVersionInfo.getSolidifyFlow() == true) {
                        nodeGroupInfo.setExecutorSet(null);
                    }
                    nodeGroupByFlowVersionInfo.getNodeGroupInfos().add(nodeGroupInfo);
                }
            }
            all.addAll(nodeGroupByFlowVersionInfoMap.values());
        }
        return all;
    }


    /**
     * 批量处理（逻辑重新整理，只适合于react版本的批量处理）
     *
     * @param flowTaskBatchCompleteWebVOList 任务传输对象
     * @return
     */
    public ResponseData completeTaskBatch(List<FlowTaskBatchCompleteWebVO> flowTaskBatchCompleteWebVOList) {
        if (flowTaskBatchCompleteWebVOList != null && flowTaskBatchCompleteWebVOList.size() > 0) {
            int total = 0;//记录处理任务总数
            for (FlowTaskBatchCompleteWebVO flowTaskBatchCompleteWebVO : flowTaskBatchCompleteWebVOList) {
                List<String> taskIdList = flowTaskBatchCompleteWebVO.getTaskIdList();
                if (taskIdList != null && !taskIdList.isEmpty()) {
                    for (String taskId : taskIdList) {
                        CompleteTaskVo completeTaskVo = new CompleteTaskVo();
                        completeTaskVo.setTaskId(taskId);
                        FlowTask flowTask = flowTaskDao.findOne(taskId);
                        if (flowTask != null && flowTask.getFlowInstance() != null) {
                            completeTaskVo.setBusinessId(flowTask.getFlowInstance().getBusinessId());
                        } else {
                            continue;
                        }
                        completeTaskVo.setOpinion("同意(批量)");
                        List<FlowTaskCompleteWebVO> flowTaskCompleteWebVOList = flowTaskBatchCompleteWebVO.getFlowTaskCompleteList();
                        Boolean endEventId = null;
                        if (flowTaskCompleteWebVOList != null && flowTaskCompleteWebVOList.size() > 0) {
                            for (FlowTaskCompleteWebVO f : flowTaskCompleteWebVOList) {
                                if (endEventId == null && f.getFlowTaskType() == null && f.getNodeId().indexOf("EndEvent") != -1) {
                                    endEventId = true;
                                }
                                f.setSolidifyFlow(flowTaskBatchCompleteWebVO.getSolidifyFlow());
                            }
                        }
                        completeTaskVo.setTaskList(JsonUtils.toJson(flowTaskCompleteWebVOList));
                        if (endEventId != null) {
                            completeTaskVo.setEndEventId("true");
                            completeTaskVo.setTaskList(null);
                        }
                        completeTaskVo.setManualSelected(false);  //审批和会签都不能连接人工排他网关
                        completeTaskVo.setApproved("true");
                        completeTaskVo.setLoadOverTime(new Long(7777));
                        try {
                            defaultFlowBaseService.completeTask(completeTaskVo);
                            total++;
                        } catch (Exception e) {
                            LogUtil.error(e.getMessage(), e);
                        }
                    }
                }
            }
            if (total > 0) {
                return ResponseData.operationSuccess("成功处理任务" + total + "条");
            } else {
                return ResponseData.operationFailure("批量处理失败!");
            }
        } else {
            return ResponseData.operationFailure("批量审批，参数不能为空！");
        }
    }


    public ResponseData completeTaskBatchOfPhone(String flowTaskBatchCompleteWebVoStrs) {
        ResponseData responseData = new ResponseData();
        List<FlowTaskBatchCompleteWebVO> flowTaskBatchCompleteWebVOList = null;
        if (StringUtils.isNotEmpty(flowTaskBatchCompleteWebVoStrs)) {
            JSONArray jsonArray = JSONArray.fromObject(flowTaskBatchCompleteWebVoStrs);//把String转换为json
            if (jsonArray != null && !jsonArray.isEmpty()) {
                flowTaskBatchCompleteWebVOList = new ArrayList<FlowTaskBatchCompleteWebVO>();
                for (int i = 0; i < jsonArray.size(); i++) {
                    FlowTaskBatchCompleteWebVO flowTaskBatchCompleteWebVO = new FlowTaskBatchCompleteWebVO();
                    JSONObject jsonObject = (JSONObject) jsonArray.get(i);
                    JSONArray taskIdListJsonArray = (JSONArray) jsonObject.get("taskIdList");
                    JSONArray flowTaskCompleteListJsonArray = (JSONArray) jsonObject.get("flowTaskCompleteList");
                    List<FlowTaskCompleteWebVO> flowTaskCompleteWebVOList = (List<FlowTaskCompleteWebVO>) JSONArray.toCollection(flowTaskCompleteListJsonArray, FlowTaskCompleteWebVO.class);
                    flowTaskBatchCompleteWebVO.setFlowTaskCompleteList(flowTaskCompleteWebVOList);
                    List<String> taskIdList = (List<String>) JSONArray.toCollection(taskIdListJsonArray, String.class);
                    flowTaskBatchCompleteWebVO.setTaskIdList(taskIdList);
                    flowTaskBatchCompleteWebVOList.add(flowTaskBatchCompleteWebVO);
                }
            }
            return this.completeTaskBatch(flowTaskBatchCompleteWebVOList);
        } else {
            responseData.setSuccess(false);
            responseData.setMessage("参数值错误！");
        }
        return responseData;
    }


    @Transactional(propagation = Propagation.REQUIRED)
    public OperateResultWithData<Integer> completeBatch(FlowTaskBatchCompleteVO flowTaskBatchCompleteVO) {
        List<String> taskIdList = flowTaskBatchCompleteVO.getTaskIdList();
        int total = 0;
        OperateResultWithData result = null;
        if (taskIdList != null && !taskIdList.isEmpty()) {
            for (String taskId : taskIdList) {
                FlowTaskCompleteVO flowTaskCompleteVO = new FlowTaskCompleteVO();
                BeanUtils.copyProperties(flowTaskBatchCompleteVO, flowTaskCompleteVO);
                flowTaskCompleteVO.setTaskId(taskId);
                try {
                    this.complete(flowTaskCompleteVO);
                    total++;
                } catch (Exception e) {
                    LogUtil.error(e.getMessage(), e);
                }
            }
            if (total > 0) {
                result = OperateResultWithData.operationSuccess();
                result.setData(total);
            } else {
                result = OperateResultWithData.operationFailure("10034");
            }
        } else {
            result = OperateResultWithData.operationFailure("10034");
        }
        return result;
    }

    public OperateResult taskTurnToDo(String taskId, String userId) {
        SessionUser sessionUser = ContextUtil.getSessionUser();
        OperateResult result = null;
        FlowTask flowTask = flowTaskDao.findOne(taskId);
        if (flowTask != null) {
            HistoricTaskInstance historicTaskInstance = historyService.createHistoricTaskInstanceQuery().taskId(flowTask.getActTaskId()).singleResult(); // 创建历史任务实例查询
            //根据用户的id获取执行人
            Executor executor = flowCommonUtil.getBasicUserExecutor(userId);
            if (executor != null) {
                FlowTask newFlowTask = new FlowTask();
                BeanUtils.copyProperties(flowTask, newFlowTask);
                FlowHistory flowHistory = flowTaskTool.initFlowHistory(flowTask, historicTaskInstance, true, null);//转办后先允许撤回
                //如果是转授权转办模式（获取转授权记录信息）
                String overPowerStr = taskMakeOverPowerService.getOverPowerStrByDepict(flowHistory.getDepict());
                flowHistory.setDepict(overPowerStr + "【被转办给：“" + executor.getName() + "”】");
                flowHistory.setFlowExecuteStatus(FlowExecuteStatus.TURNTODO.getCode());//转办
                newFlowTask.setId(null);
                //判断待办转授权模式(如果是转办模式，需要返回转授权信息，其余情况返回null)
                TaskMakeOverPower taskMakeOverPower = taskMakeOverPowerService.getMakeOverPowerByTypeAndUserId(executor.getId(), flowTask.getFlowInstance().getFlowDefVersion().getFlowDefination().getFlowType().getId());
                if (taskMakeOverPower != null) {
                    newFlowTask.setExecutorId(taskMakeOverPower.getPowerUserId());
                    newFlowTask.setExecutorAccount(taskMakeOverPower.getPowerUserAccount());
                    newFlowTask.setExecutorName(taskMakeOverPower.getPowerUserName());
                    newFlowTask.setDepict("【由：“" + sessionUser.getUserName() + "”转办】【转授权-" + executor.getName() + "授权】" + (StringUtils.isNotEmpty(flowTask.getDepict()) ? flowTask.getDepict() : ""));
                    //添加组织机构信息
                    newFlowTask.setExecutorOrgId(taskMakeOverPower.getPowerUserOrgId());
                    newFlowTask.setExecutorOrgCode(taskMakeOverPower.getPowerUserOrgCode());
                    newFlowTask.setExecutorOrgName(taskMakeOverPower.getPowerUserOrgName());
                } else {
                    newFlowTask.setExecutorId(executor.getId());
                    newFlowTask.setExecutorAccount(executor.getCode());
                    newFlowTask.setExecutorName(executor.getName());
                    newFlowTask.setDepict("【由：“" + sessionUser.getUserName() + "”转办】" + (StringUtils.isNotEmpty(flowTask.getDepict()) ? flowTask.getDepict() : ""));
                    //添加组织机构信息
                    newFlowTask.setExecutorOrgId(executor.getOrganizationId());
                    newFlowTask.setExecutorOrgCode(executor.getOrganizationCode());
                    newFlowTask.setExecutorOrgName(executor.getOrganizationName());
                }
                newFlowTask.setOwnerId(executor.getId());
                newFlowTask.setOwnerName(executor.getName());
                newFlowTask.setOwnerAccount(executor.getCode());
                //添加组织机构信息
                newFlowTask.setOwnerOrgId(executor.getOrganizationId());
                newFlowTask.setOwnerOrgCode(executor.getOrganizationCode());
                newFlowTask.setOwnerOrgName(executor.getOrganizationName());
                newFlowTask.setTrustState(0);
                taskService.setAssignee(flowTask.getActTaskId(), executor.getId());

                // 取得当前任务
                HistoricTaskInstance currTask = historyService.createHistoricTaskInstanceQuery().taskId(flowTask.getActTaskId())
                        .singleResult();
                String taskJsonDef = newFlowTask.getTaskJsonDef();
                JSONObject taskJsonDefObj = JSONObject.fromObject(taskJsonDef);
                String nodeType = taskJsonDefObj.get("nodeType") + "";//会签
                if ("CounterSign".equalsIgnoreCase(nodeType)) {//会签任务替换执行人集合
                    String processInstanceId = currTask.getProcessInstanceId();
                    String userListDesc = currTask.getTaskDefinitionKey() + "_List_CounterSign";
                    List<String> userList = (List<String>) runtimeService.getVariableLocal(processInstanceId, userListDesc);
                    Collections.replaceAll(userList, flowTask.getExecutorId(), userId);
                    runtimeService.setVariableLocal(processInstanceId, userListDesc, userList);
                }

                flowHistoryDao.save(flowHistory);
                //是否推送信息到baisc
                Boolean pushBasic = this.getBooleanPushTaskToBasic();
                //是否推送信息到业务模块或者直接配置的url
                Boolean pushModelOrUrl = this.getBooleanPushModelOrUrl(flowTask.getFlowInstance());

                List<FlowTask> needDelList = new ArrayList<FlowTask>();  //需要删除的待办
                if (pushBasic || pushModelOrUrl) {
                    needDelList.add(flowTask);
                }
                flowTaskDao.delete(flowTask);
                flowTaskDao.save(newFlowTask);
                List<FlowTask> needAddList = new ArrayList<FlowTask>(); //需要新增的待办
                if (pushBasic || pushModelOrUrl) {
                    needAddList.add(newFlowTask);
                    if (pushBasic) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                pushToBasic(needAddList, null, needDelList, null);
                            }
                        }).start();
                    }

                    if (pushModelOrUrl) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                pushTaskToModelOrUrl(flowTask.getFlowInstance(), needDelList, TaskStatus.DELETE);
                                pushTaskToModelOrUrl(flowTask.getFlowInstance(), needAddList, TaskStatus.INIT);
                            }
                        }).start();
                    }

                }
                result = OperateResult.operationSuccess();
            } else {
                result = OperateResult.operationFailure("10038");//执行人查询结果为空
            }
        } else {
            result = OperateResult.operationFailure("10033");//任务不存在，可能已经被处理
        }
        return result;
    }

    public OperateResult taskTrustToDo(String taskId, String userId) throws Exception {
        OperateResult result = null;
        FlowTask flowTask = flowTaskDao.findOne(taskId);
        if (flowTask != null) {
            HistoricTaskInstance historicTaskInstance = historyService.createHistoricTaskInstanceQuery().taskId(flowTask.getActTaskId()).singleResult(); // 创建历史任务实例查询
            //通过用户ID获取执行人
            Executor executor = flowCommonUtil.getBasicUserExecutor(userId);
            if (executor != null) {
                FlowTask newFlowTask = new FlowTask();
                BeanUtils.copyProperties(flowTask, newFlowTask);
                FlowHistory flowHistory = flowTaskTool.initFlowHistory(flowTask, historicTaskInstance, true, null); //委托后先允许撤回
                //如果是转授权转办模式（获取转授权记录信息）
                String overPowerStr = taskMakeOverPowerService.getOverPowerStrByDepict(flowHistory.getDepict());
                flowHistory.setDepict(overPowerStr + "【被委托给：" + executor.getName() + "】");
                flowHistory.setFlowExecuteStatus(FlowExecuteStatus.ENTRUST.getCode());//委托

                newFlowTask.setId(null);
                //判断待办转授权模式(如果是转办模式，需要返回转授权信息，其余情况返回null)
                TaskMakeOverPower taskMakeOverPower = taskMakeOverPowerService.getMakeOverPowerByTypeAndUserId(executor.getId(), flowTask.getFlowInstance().getFlowDefVersion().getFlowDefination().getFlowType().getId());
                if (taskMakeOverPower != null) {
                    newFlowTask.setExecutorId(taskMakeOverPower.getPowerUserId());
                    newFlowTask.setExecutorAccount(taskMakeOverPower.getPowerUserAccount());
                    newFlowTask.setExecutorName(taskMakeOverPower.getPowerUserName());
                    newFlowTask.setDepict("【由：“" + flowTask.getExecutorName() + "”委托】【转授权-" + executor.getName() + "授权】" + flowTask.getDepict());
                    //添加组织机构信息
                    newFlowTask.setExecutorOrgId(taskMakeOverPower.getPowerUserOrgId());
                    newFlowTask.setExecutorOrgCode(taskMakeOverPower.getPowerUserOrgCode());
                    newFlowTask.setExecutorOrgName(taskMakeOverPower.getPowerUserOrgName());
                } else {
                    newFlowTask.setExecutorId(executor.getId());
                    newFlowTask.setExecutorAccount(executor.getCode());
                    newFlowTask.setExecutorName(executor.getName());
                    newFlowTask.setDepict("【由：“" + flowTask.getExecutorName() + "”委托】" + flowTask.getDepict());
                    //添加组织机构信息
                    newFlowTask.setExecutorOrgId(executor.getOrganizationId());
                    newFlowTask.setExecutorOrgCode(executor.getOrganizationCode());
                    newFlowTask.setExecutorOrgName(executor.getOrganizationName());
                }

                newFlowTask.setOwnerId(executor.getId());
                newFlowTask.setOwnerAccount(executor.getCode());
                newFlowTask.setOwnerName(executor.getName());
                //添加组织机构信息
                newFlowTask.setOwnerOrgId(executor.getOrganizationId());
                newFlowTask.setOwnerOrgCode(executor.getOrganizationCode());
                newFlowTask.setOwnerOrgName(executor.getOrganizationName());
                newFlowTask.setPreId(flowHistory.getId());
                flowHistoryDao.save(flowHistory);
                flowTask.setTrustState(1);
                newFlowTask.setTrustState(2);
                newFlowTask.setTrustOwnerTaskId(flowTask.getId());
                //是否推送信息到baisc
                Boolean pushBasic = this.getBooleanPushTaskToBasic();
                //是否推送信息到业务模块或者直接配置的url
                Boolean pushModelOrUrl = this.getBooleanPushModelOrUrl(flowTask.getFlowInstance());
                List<FlowTask> needDelList = new ArrayList<FlowTask>(); //需要删除的待办
                List<FlowTask> needAddList = new ArrayList<FlowTask>(); //需要新增的待办
                if (pushBasic || pushModelOrUrl) {
                    needDelList.add(flowTask);
                }
                flowTaskDao.save(flowTask);
                flowTaskDao.save(newFlowTask);
                if (pushBasic || pushModelOrUrl) {
                    needAddList.add(newFlowTask);
                    //推送待办
                    if (pushModelOrUrl) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                pushTaskToModelOrUrl(flowTask.getFlowInstance(), needDelList, TaskStatus.DELETE);
                                pushTaskToModelOrUrl(flowTask.getFlowInstance(), needAddList, TaskStatus.INIT);
                            }
                        }).start();
                    }
                }

                //新增和删除待办
                if (pushBasic) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            pushToBasic(needAddList, null, needDelList, null);
                        }
                    }).start();
                }

                result = OperateResult.operationSuccess();
            } else {
                result = OperateResult.operationFailure("10038");//执行人查询结果为空
            }
        } else {
            result = OperateResult.operationFailure("10033");//任务不存在，可能已经被处理
        }
        return result;
    }


    public List<Executor> getCounterSignExecutorList(String actInstanceId, String taskActKey) throws Exception {
        List<Executor> result = null;
        List<FlowTask> flowTaskList = flowTaskDao.findByActTaskDefKeyAndActInstanceId(taskActKey, actInstanceId);
        if (flowTaskList != null && !flowTaskList.isEmpty()) {
            FlowTask flowTaskTemp = flowTaskList.get(0);
            // 取得当前任务
            HistoricTaskInstance currTask = historyService.createHistoricTaskInstanceQuery().taskId(flowTaskTemp.getActTaskId())
                    .singleResult();
            String taskJsonDef = flowTaskTemp.getTaskJsonDef();
            JSONObject taskJsonDefObj = JSONObject.fromObject(taskJsonDef);
            String nodeType = taskJsonDefObj.get("nodeType") + "";//会签
            if ("CounterSign".equalsIgnoreCase(nodeType)) {//会签任务做处理判断
                String processInstanceId = currTask.getProcessInstanceId();
                String userListDesc = currTask.getTaskDefinitionKey() + "_List_CounterSign";
                List<String> userList = (List<String>) runtimeService.getVariableLocal(processInstanceId, userListDesc);
                //根据用户的id列表获取执行人
                result = flowCommonUtil.getBasicUserExecutors(userList);
            } else {
                throw new FlowException("非会签节点！");
            }
        }
        return result;
    }

    public OperateResult taskTrustToReturn(String taskId, String opinion) throws Exception {
        OperateResult result = null;
        FlowTask flowTask = flowTaskDao.findOne(taskId);
        if (flowTask != null) {
            HistoricTaskInstance historicTaskInstance = historyService.createHistoricTaskInstanceQuery().taskId(flowTask.getActTaskId()).singleResult(); // 创建历史任务实例查询
            FlowTask oldFlowTask = flowTaskDao.findOne(flowTask.getTrustOwnerTaskId());
            if (oldFlowTask != null) {
                FlowHistory flowHistory = flowTaskTool.initFlowHistory(flowTask, historicTaskInstance, null, null);
                //如果是转授权转办模式（获取转授权记录信息）
                String overPowerStr = taskMakeOverPowerService.getOverPowerStrByDepict(flowHistory.getDepict());
                flowHistory.setDepict(overPowerStr + "【办理完成返回委托方】" + opinion);
                //判断待办转授权模式(如果是转办模式，需要返回转授权信息，其余情况返回null)
                TaskMakeOverPower taskMakeOverPower = taskMakeOverPowerService.getMakeOverPowerByTypeAndUserId(oldFlowTask.getExecutorId(), oldFlowTask.getFlowInstance().getFlowDefVersion().getFlowDefination().getFlowType().getId());
                if (taskMakeOverPower != null) {
                    oldFlowTask.setExecutorId(taskMakeOverPower.getPowerUserId());
                    oldFlowTask.setExecutorAccount(taskMakeOverPower.getPowerUserAccount());
                    oldFlowTask.setExecutorName(taskMakeOverPower.getPowerUserName());
                    //添加组织机构信息
                    oldFlowTask.setExecutorOrgId(taskMakeOverPower.getPowerUserOrgId());
                    oldFlowTask.setExecutorOrgCode(taskMakeOverPower.getPowerUserOrgCode());
                    oldFlowTask.setExecutorOrgName(taskMakeOverPower.getPowerUserOrgName());
                    oldFlowTask.setDepict("【委托完成】【转授权-" + taskMakeOverPower.getUserName() + "授权】" + opinion);
                } else {
                    oldFlowTask.setDepict("【委托完成】" + opinion);
                }
                oldFlowTask.setTrustState(3);  //委托完成
                oldFlowTask.setPreId(flowHistory.getId());
                flowHistoryDao.save(flowHistory);
                //是否推送信息到basic
                Boolean pushBasic = this.getBooleanPushTaskToBasic();
                //是否推送信息到业务模块或者直接配置的url
                Boolean pushModelOrUrl = this.getBooleanPushModelOrUrl(flowTask.getFlowInstance());
                if (pushBasic || pushModelOrUrl) {
                    List<FlowTask> needDelList = new ArrayList<FlowTask>();
                    needDelList.add(flowTask);
                    List<FlowTask> needAddList = new ArrayList<FlowTask>();
                    needAddList.add(oldFlowTask);

                    if (pushModelOrUrl) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                pushTaskToModelOrUrl(flowTask.getFlowInstance(), needDelList, TaskStatus.DELETE);
                                pushTaskToModelOrUrl(flowTask.getFlowInstance(), needAddList, TaskStatus.INIT);
                            }
                        }).start();
                    }

                    if (pushBasic) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                pushToBasic(needAddList, needDelList, null, null);
                            }
                        }).start();
                    }
                }
                flowTaskDao.save(oldFlowTask);
                flowTaskDao.delete(flowTask);
                result = OperateResult.operationSuccess();
            } else {
                result = OperateResult.operationFailure("10038");//执行人查询结果为空
            }
        } else {
            result = OperateResult.operationFailure("10033");//任务不存在，可能已经被处理
        }
        return result;
    }


    @Transactional(propagation = Propagation.REQUIRED)
    public OperateResult counterSignAdd(String actInstanceId, String taskActKey, String userIds) throws Exception {

        String[] userIdArray = null;
        StringBuffer resultDec = new StringBuffer();
        StringBuffer resultDecTrue = new StringBuffer();
        StringBuffer resultDecFalseOne = new StringBuffer();
        StringBuffer resultDecFalseTwo = new StringBuffer();
        StringBuffer resultDecFalseThree = new StringBuffer();
        OperateResult result = null;
        if (StringUtils.isNotEmpty(userIds)) {
            userIdArray = userIds.split(",");
            if (userIdArray != null && userIdArray.length > 0) {
                for (String userId : userIdArray) {
                    //检查用户是否存在
                    Executor executor = null;
                    try {
                        executor = flowCommonUtil.getBasicUserExecutor(userId);
                    } catch (IllegalArgumentException e) {
                        LogUtil.error(e.getMessage(), e);
                    }
                    if (executor == null) {
                        resultDecFalseOne.append("【ID='" + userId + "'】");
                        continue;
                    }
                    List<FlowTask> flowTaskList = flowTaskDao.findByActTaskDefKeyAndActInstanceId(taskActKey, actInstanceId);
                    if (flowTaskList != null && !flowTaskList.isEmpty()) {
                        FlowTask flowTaskTemp = flowTaskList.get(0);
                        // 取得当前任务
                        HistoricTaskInstance currTask = historyService.createHistoricTaskInstanceQuery().taskId(flowTaskTemp.getActTaskId())
                                .singleResult();
                        // 取得流程定义
                        ProcessDefinitionEntity definition = (ProcessDefinitionEntity) ((RepositoryServiceImpl) repositoryService)
                                .getDeployedProcessDefinition(currTask.getProcessDefinitionId());
                        if (definition == null) {
                            LogUtil.error(ContextUtil.getMessage("10003"));
                            return OperateResult.operationFailure("10003");//流程定义未找到找到");
                        }
                        FlowInstance flowInstance = flowTaskTemp.getFlowInstance();
                        String taskJsonDef = flowTaskTemp.getTaskJsonDef();
                        JSONObject taskJsonDefObj = JSONObject.fromObject(taskJsonDef);
                        String nodeType = taskJsonDefObj.get("nodeType") + "";//会签
                        if ("CounterSign".equalsIgnoreCase(nodeType)) {//会签任务做处理判断
                            String executionId = currTask.getExecutionId();
                            Execution execution = runtimeService.createExecutionQuery().executionId(executionId).singleResult();
                            ExecutionEntity executionEntity = (ExecutionEntity) execution;
                            String processInstanceId = currTask.getProcessInstanceId();
                            Map<String, VariableInstance> processVariables = runtimeService.getVariableInstances(executionId);
                            //总循环次数
                            Integer instanceOfNumbers = (Integer) processVariables.get("nrOfInstances").getValue();
                            runtimeService.setVariable(executionId, "nrOfInstances", (instanceOfNumbers + 1));
                            //判断是否是并行会签
                            Boolean isSequential = taskJsonDefObj.getJSONObject("nodeConfig").getJSONObject("normal").getBoolean("isSequential");
                            if (isSequential == false) {
                                Integer nrOfActiveInstancesNumbers = (Integer) processVariables.get("nrOfActiveInstances").getValue();
                                runtimeService.setVariable(executionId, "nrOfActiveInstances", (nrOfActiveInstancesNumbers + 1));
                            }
                            String userListDesc = currTask.getTaskDefinitionKey() + "_List_CounterSign";
                            List<String> userList = (List<String>) runtimeService.getVariableLocal(processInstanceId, userListDesc);
                            userList.add(userId);
                            runtimeService.setVariable(processInstanceId, userListDesc, userList);
                            if (isSequential == false) {//并行会签，需要立即初始化执行人
                                taskService.counterSignAddTask(userId, executionEntity, currTask);
                                String preId = flowTaskTemp.getPreId();
                                flowTaskTool.initCounterSignAddTask(flowInstance, currTask.getTaskDefinitionKey(), userId, preId);
                            }
                            resultDecTrue.append("【" + executor.getName() + "-" + executor.getCode() + "】");
                            LogUtil.info(executor.getName() + "【" + executor.getCode() + "】,id='" + executor.getId() + "'的加签操作执行成功;");
                            continue;
                        } else {
                            resultDecFalseTwo.append("【" + executor.getName() + "-" + executor.getCode() + "】");
                            LogUtil.info(executor.getName() + "【" + executor.getCode() + "】,id='" + executor.getId() + "'的执行节点为非会签节点，无法加签;");
                            continue;
                        }
                    } else {
                        resultDecFalseThree.append("【" + executor.getName() + "-" + executor.getCode() + "】");
                        LogUtil.info(executor.getName() + "【" + executor.getCode() + "】,id='" + executor.getId() + "'的用户,任务可能已经执行完，无法加签;");
                        continue;
                    }
                }
            } else {
                return OperateResult.operationFailure("执行人列表不能为空！");
            }
        } else {
            return OperateResult.operationFailure("执行人列表不能为空！");
        }

        if (resultDecTrue.length() > 0) {
            resultDecTrue.append("加签成功！");
        }
        if (resultDecFalseOne.length() > 0) {
            resultDecFalseOne.append("用户信息不存在,加签失败！");
        }
        if (resultDecFalseTwo.length() > 0) {
            resultDecFalseTwo.append("非会签节点，加签失败！");
        }
        if (resultDecFalseThree.length() > 0) {
            resultDecFalseThree.append("任务可能已经执行完，加签失败！");
        }
        resultDec.append(resultDecTrue).append(resultDecFalseOne).append(resultDecFalseTwo).append(resultDecFalseThree);
        if (resultDecTrue.length() > 0) {
            result = OperateResult.operationSuccess(resultDec.toString());
        } else {
            result = OperateResult.operationFailure(resultDec.toString());
        }
        return result;
    }

    /**
     * @param actInstanceId 流程实例id
     * @param taskActKey    节点key
     * @param userIds       被减签的人
     * @return
     * @throws Exception
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public OperateResult counterSignDel(String actInstanceId, String taskActKey, String userIds) throws Exception {
        String[] userIdArray = null;
        StringBuffer resultDec = new StringBuffer();
        StringBuffer resultDecTrue = new StringBuffer();
        StringBuffer resultDecFalseOne = new StringBuffer();
        StringBuffer resultDecFalseTwo = new StringBuffer();
        StringBuffer resultDecFalseThree = new StringBuffer();
        StringBuffer resultDecFalseFour = new StringBuffer();
        StringBuffer resultDecFalseFive = new StringBuffer();
        StringBuffer resultDecFalseSix = new StringBuffer();
        StringBuffer resultDecFalseSeven = new StringBuffer();
        StringBuffer resultDecFalseEight = new StringBuffer();
        OperateResult result = null;
        if (StringUtils.isNotEmpty(userIds)) {
            userIdArray = userIds.split(",");
            if (userIdArray != null && userIdArray.length > 0) {
                for (String userId : userIdArray) {
                    //检查用户是否存在
                    Executor executor = null;
                    try {
                        executor = flowCommonUtil.getBasicUserExecutor(userId);
                    } catch (IllegalArgumentException e) {
                        LogUtil.error(e.getMessage(), e);
                    }
                    if (executor == null) {
                        resultDecFalseOne.append("【ID=" + userId + "】");
                        continue;
                    }
                    List<FlowTask> flowTaskList = flowTaskDao.findByActTaskDefKeyAndActInstanceId(taskActKey, actInstanceId);
                    if (flowTaskList != null && !flowTaskList.isEmpty()) {
                        FlowTask flowTaskTemp = flowTaskList.get(0);
                        FlowInstance flowInstance = flowTaskTemp.getFlowInstance();
                        // 取得当前任务
                        HistoricTaskInstance currTask = historyService.createHistoricTaskInstanceQuery().taskId(flowTaskTemp.getActTaskId())
                                .singleResult();
                        // 取得流程定义
                        ProcessDefinitionEntity definition = (ProcessDefinitionEntity) ((RepositoryServiceImpl) repositoryService)
                                .getDeployedProcessDefinition(currTask.getProcessDefinitionId());
                        if (definition == null) {
                            LogUtil.error(ContextUtil.getMessage("10003"));
                            return OperateResult.operationFailure("10003");//流程定义未找到找到");
                        }
                        String taskJsonDef = flowTaskTemp.getTaskJsonDef();
                        JSONObject taskJsonDefObj = JSONObject.fromObject(taskJsonDef);
                        String nodeType = taskJsonDefObj.get("nodeType") + "";//会签
                        if ("CounterSign".equalsIgnoreCase(nodeType)) {//会签任务做处理判断
                            String processInstanceId = currTask.getProcessInstanceId();
                            String userListDesc = currTask.getTaskDefinitionKey() + "_List_CounterSign";
                            List<String> userListArray = (List<String>) runtimeService.getVariableLocal(processInstanceId, userListDesc);
                            List<String> userList = new ArrayList<>(userListArray);
                            if (!userList.contains(userId)) {
                                resultDecFalseTwo.append("【" + executor.getName() + "-" + executor.getCode() + "】");
                                LogUtil.info(executor.getName() + "【" + executor.getCode() + "】,id='" + executor.getId() + "'的用户,在当前任务节点找不到，减签失败;");
                                continue;
                            }
                            String executionId = currTask.getExecutionId();
                            Map<String, VariableInstance> processVariables = runtimeService.getVariableInstances(executionId);
                            //总循环次数
                            Integer instanceOfNumbers = (Integer) processVariables.get("nrOfInstances").getValue();
                            //完成会签的次数
                            Integer completeCounter = (Integer) processVariables.get("nrOfCompletedInstances").getValue();
                            if (completeCounter + 1 == instanceOfNumbers) {//最后一个任务
                                resultDecFalseThree.append("【" + executor.getName() + "-" + executor.getCode() + "】");
                                LogUtil.info(executor.getName() + "【" + executor.getCode() + "】,id='" + executor.getId() + "'的用户,任务已经到达最后一位执行人，减签失败;");
                                continue;
                            }

                            //判断是否是并行会签
                            Boolean isSequential = taskJsonDefObj.getJSONObject("nodeConfig").getJSONObject("normal").getBoolean("isSequential");

                            List<FlowTask> flowTaskListCurrent = flowTaskDao.findByActTaskDefKeyAndActInstanceIdAndExecutorId(taskActKey, actInstanceId, userId);
                            if (isSequential == false) {//并行会签，需要清空对应的执行人任务信息
                                if (flowTaskListCurrent != null && !flowTaskListCurrent.isEmpty()) {
                                    //是否推送信息到baisc
                                    Boolean pushBasic = this.getBooleanPushTaskToBasic();
                                    //是否推送信息到业务模块或者直接配置的url
                                    Boolean pushModelOrUrl = this.getBooleanPushModelOrUrl(flowInstance);
                                    runtimeService.setVariable(executionId, "nrOfInstances", (instanceOfNumbers - 1));
                                    if (isSequential == false) {
                                        Integer nrOfActiveInstancesNumbers = (Integer) processVariables.get("nrOfActiveInstances").getValue();
                                        runtimeService.setVariable(executionId, "nrOfActiveInstances", (nrOfActiveInstancesNumbers - 1));
                                    }
                                    userList.remove(userId);
                                    runtimeService.setVariable(processInstanceId, userListDesc, userList);//回写减签后的执行人列表

                                    List<FlowTask> delList = new ArrayList<>();
                                    for (FlowTask flowTask : flowTaskListCurrent) {
                                        if (pushBasic || pushModelOrUrl) {
                                            delList.add(flowTask);
                                        }
                                        taskService.deleteRuningTask(flowTask.getActTaskId(), true);
                                        flowTaskDao.delete(flowTask);
                                    }

                                    if (pushModelOrUrl) {
                                        new Thread(new Runnable() {
                                            @Override
                                            public void run() {
                                                pushTaskToModelOrUrl(flowInstance, delList, TaskStatus.DELETE);
                                            }
                                        }).start();
                                    }

                                    if (pushBasic) {
                                        new Thread(new Runnable() {
                                            @Override
                                            public void run() {
                                                pushToBasic(null, null, delList, null);
                                            }
                                        }).start();
                                    }

                                } else {
                                    resultDecFalseFour.append("【" + executor.getName() + "-" + executor.getCode() + "】");
                                    LogUtil.info(executor.getName() + "【" + executor.getCode() + "】,id='" + executor.getId() + "'的用户,当前任务节点已执行，减签失败;");
                                    continue;
                                }
                            } else {//串行会签不允许对当前在线的任务进行直接减签，未来可扩展允许
                                if (flowTaskListCurrent != null && !flowTaskListCurrent.isEmpty()) {
                                    resultDecFalseFive.append("【" + executor.getName() + "-" + executor.getCode() + "】");
                                    LogUtil.info(executor.getName() + "【" + executor.getCode() + "】,id='" + executor.getId() + "'的用户,串行会签不允许对当前在线的执行人直接减签操作，减签失败;");
                                    continue;
                                } else {
                                    List<FlowHistory> flowHistoryList = flowHistoryDao.findByActTaskDefKeyAndActInstanceId(taskActKey, actInstanceId);
                                    boolean canDel = true;
                                    if (flowHistoryList != null && !flowHistoryList.isEmpty()) {
                                        while (flowHistoryList.size() > userList.size()) {
                                            for (int index = 0; index < userList.size(); index++) {
                                                flowHistoryList.remove(index);
                                            }
                                        }
                                        for (FlowHistory flowHistory : flowHistoryList) {
                                            if (userId.equals(flowHistory.getExecutorId())) {
                                                canDel = false;
                                                break;
                                            }
                                        }
                                    }
                                    if (canDel) {
                                        runtimeService.setVariable(executionId, "nrOfInstances", (instanceOfNumbers - 1));
                                        if (isSequential == false) {
                                            Integer nrOfActiveInstancesNumbers = (Integer) processVariables.get("nrOfActiveInstances").getValue();
                                            runtimeService.setVariable(executionId, "nrOfActiveInstances", (nrOfActiveInstancesNumbers - 1));
                                        }
                                        userList.remove(userId);
                                        runtimeService.setVariable(processInstanceId, userListDesc, userList);//回写减签后的执行人列表
                                    } else {
                                        resultDecFalseSix.append("【" + executor.getName() + "-" + executor.getCode() + "】");
                                        LogUtil.info(executor.getName() + "【" + executor.getCode() + "】,id='" + executor.getId() + "'的用户,发现已经执行，减签操作执行失败;");
                                        continue;
                                    }
                                }
                            }

                            resultDecTrue.append("【" + executor.getName() + "-" + executor.getCode() + "】");
                            LogUtil.info(executor.getName() + "【" + executor.getCode() + "】,id='" + executor.getId() + "'的用户,的减签操作执行成;");
                        } else {
                            resultDecFalseSeven.append("【" + executor.getName() + "-" + executor.getCode() + "】");
                            LogUtil.info(executor.getName() + "【" + executor.getCode() + "】,id='" + executor.getId() + "'的用户,执行节点为非会签节点，无法减签;");
                            continue;
                        }
                    } else {
                        resultDecFalseEight.append("【" + executor.getName() + "-" + executor.getCode() + "】");
                        LogUtil.info(executor.getName() + "【" + executor.getCode() + "】,id='" + executor.getId() + "'的用户,任务可能已经执行完，无法减签;");
                        continue;
                    }
                }
            } else {
                return OperateResult.operationFailure("执行人列表不能为空！");
            }
        }

        if (resultDecTrue.length() > 0) {
            resultDecTrue.append("减签成功！");
        }
        if (resultDecFalseOne.length() > 0) {
            resultDecFalseOne.append("用户信息不存在，减签失败！");
        }
        if (resultDecFalseTwo.length() > 0) {
            resultDecFalseTwo.append("当前任务节点找不到，减签失败！");
        }
        if (resultDecFalseThree.length() > 0) {
            resultDecFalseThree.append("到达最后一位执行人，减签失败！");
        }
        if (resultDecFalseFour.length() > 0) {
            resultDecFalseFour.append("当前任务节点已执行，减签失败！");
        }
        if (resultDecFalseFive.length() > 0) {
            resultDecFalseFive.append("串行会签不能对当前执行人进行减签，减签失败！");
        }
        if (resultDecFalseSix.length() > 0) {
            resultDecFalseSix.append("发现已经执行，减签失败！");
        }
        if (resultDecFalseSeven.length() > 0) {
            resultDecFalseSeven.append("非会签节点，减签失败！");
        }
        if (resultDecFalseEight.length() > 0) {
            resultDecFalseEight.append("任务可能已经执行完，减签失败！");
        }
        resultDec.append(resultDecTrue).append(resultDecFalseOne).append(resultDecFalseTwo)
                .append(resultDecFalseThree).append(resultDecFalseFour).append(resultDecFalseFive)
                .append(resultDecFalseSix).append(resultDecFalseSeven).append(resultDecFalseEight);
        if (resultDecTrue.length() > 0) {
            result = OperateResult.operationSuccess(resultDec.toString());
        } else {
            result = OperateResult.operationFailure(resultDec.toString());
        }
        return result;
    }

    public List<CanAddOrDelNodeInfo> getAllCanAddNodeInfoList() throws Exception {
        List<CanAddOrDelNodeInfo> result = new ArrayList<CanAddOrDelNodeInfo>();
        List<CanAddOrDelNodeInfo> resultDai = flowTaskDao.findByAllowAddSign(ContextUtil.getUserId());
        List<CanAddOrDelNodeInfo> resultStart = flowTaskDao.findByAllowAddSignStart(ContextUtil.getUserId());
        result.addAll(resultStart);
        result.addAll(resultDai);
        Map<String, CanAddOrDelNodeInfo> tempMap = new HashMap<String, CanAddOrDelNodeInfo>();
        for (CanAddOrDelNodeInfo c : result) {
            tempMap.put(c.getActInstanceId() + c.getNodeKey(), c);
        }
        result.clear();
        result.addAll(tempMap.values());
        return result;
    }

    public List<CanAddOrDelNodeInfo> getAllCanDelNodeInfoList() throws Exception {
        List<CanAddOrDelNodeInfo> result = new ArrayList<CanAddOrDelNodeInfo>();
        List<CanAddOrDelNodeInfo> resultDai = flowTaskDao.findByAllowSubtractSign(ContextUtil.getUserId());
        List<CanAddOrDelNodeInfo> resultStart = flowTaskDao.findByAllowSubtractSignStart(ContextUtil.getUserId());
        result.addAll(resultStart);
        result.addAll(resultDai);
        Map<String, CanAddOrDelNodeInfo> tempMap = new HashMap<String, CanAddOrDelNodeInfo>();
        for (CanAddOrDelNodeInfo c : result) {
            tempMap.put(c.getActInstanceId() + c.getNodeKey(), c);
        }
        result.clear();
        result.addAll(tempMap.values());
        return result;
    }

    public OperateResult reminding() {
        OperateResult result = null;
        List<FlowTaskExecutorIdAndCount> executorIdAndCountList = flowTaskDao.findAllExecutorIdAndCount();
        if (executorIdAndCountList != null && !executorIdAndCountList.isEmpty()) {
            Map<String, Long> executorIdAndCountMap = executorIdAndCountList.stream().collect(Collectors.toMap(FlowTaskExecutorIdAndCount::getExecutorId, FlowTaskExecutorIdAndCount::getCount));
            //调用basic个人基本信息
            if (executorIdAndCountMap != null && !executorIdAndCountMap.isEmpty()) {
                Set<String> userIdSet = executorIdAndCountMap.keySet();

                String url = Constants.getUserEmailAlertFindByUserIdsUrl();
                List<UserEmailAlert> userEmailAlertList = ApiClient.postViaProxyReturnResult(url, new GenericType<List<UserEmailAlert>>() {
                }, userIdSet);
                if (userEmailAlertList != null && !userEmailAlertList.isEmpty()) {
                    for (UserEmailAlert userEmailAlert : userEmailAlertList) {
                        Integer jianGeTime = userEmailAlert.getHours();//间隔时间
                        Integer toDoAmount = userEmailAlert.getToDoAmount();//待办数量阀值
                        Date lastSendTime = userEmailAlert.getLastTime();
                        if (jianGeTime != null && jianGeTime > 0) {
                            if (lastSendTime == null) {
                                //直接发送邮件
                                emailSend(userEmailAlert.getUserId());
                                LogUtil.info("催办提醒：" + userEmailAlert.getUserId() + "，最长间隔时间到达，lastSendTime==null。");
                                continue;
                            }
                            double hours = (System.currentTimeMillis() - lastSendTime.getTime()) / (1000 * 60 * 60.0);
                            if (hours >= jianGeTime) {
                                //发送邮件
                                emailSend(userEmailAlert.getUserId());
                                LogUtil.info("催办提醒：" + userEmailAlert.getUserId() + "，最长间隔时间到达。");
                            }
                        }
                        if (toDoAmount >= executorIdAndCountMap.get(userEmailAlert.getUserId())) {
                            if (lastSendTime == null) {
                                //直接发送邮件
                                emailSend(userEmailAlert.getUserId());
                                LogUtil.info("催办提醒：" + userEmailAlert.getUserId() + "，待办数量超过阀值，lastSendTime==null。");
                                continue;
                            }
                            double hours = (System.currentTimeMillis() - lastSendTime.getTime()) / (1000 * 60 * 60.0);
                            if (hours >= 1) {
                                //发送邮件
                                emailSend(userEmailAlert.getUserId());
                                LogUtil.info("催办提醒：" + userEmailAlert.getUserId() + "，待办数量超过阀值。");
                            }
                        }
                    }
                }
            }
        }
        return result;
    }

    private void emailSend(String userId) {
        String userName;
        List<FlowTaskExecutorIdAndCount> list = flowTaskDao.findAllTaskKeyAndCountByExecutorId(userId);
        if (list != null && !list.isEmpty()) {
            Map<String, Object> contentTemplateParams = new HashMap<>();
            userName = list.get(0).getExecutorName();
            List<CuiBanEmailTemplate> toDoItems = new ArrayList<>();
            for (FlowTaskExecutorIdAndCount f : list) {
                CuiBanEmailTemplate template = new CuiBanEmailTemplate();
                template.setFlowName(f.getFlowName());
                template.setTaskName(f.getTaskName());
                template.setTaskCount(f.getCount());
                toDoItems.add(template);
            }

            EcmpMessage message = new EcmpMessage();
            String senderId = userId;
            message.setSenderId(senderId);
            List<String> receiverIds = new ArrayList<>();
            receiverIds.add(userId);
            message.setReceiverIds(receiverIds);
            contentTemplateParams.put("userName", userName);
            contentTemplateParams.put("toDoItems", toDoItems);
            message.setContentTemplateParams(contentTemplateParams);
            message.setContentTemplateCode("EMAIL_TEMPLATE_TODO_WARN");//模板代码

            message.setCanToSender(false);
//            INotifyService iNotifyService = ApiClient.createProxy(INotifyService.class);
            INotifyService iNotifyService = ContextUtil.getBean(INotifyService.class);
            message.setSubject("催办提醒");
            List<NotifyType> notifyTypes = new ArrayList<NotifyType>();
            notifyTypes.add(NotifyType.Email);
            message.setNotifyTypes(notifyTypes);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    iNotifyService.send(message);
                    String url = Constants.getUsderEmailAlertUpdateLastTimesUrl();
                    OperateResult result = ApiClient.postViaProxyReturnResult(url, new GenericType<OperateResult>() {
                    }, receiverIds);
                    LogUtil.info("催办send email to userId=" + userId + ",userName = " + userName + "，重置时间状态=" + (result != null ? result.getMessage() : "失败"));
                }
            }).start();
        }


    }

    /**
     * 获取指定用户的待办工作数量
     *
     * @param executorId   执行人用户Id
     * @param searchConfig 查询参数
     * @return 待办工作的数量
     */
    @Override
    public int findCountByExecutorId(String executorId, Search searchConfig) {
        Long count = flowTaskDao.findCountByExecutorId(executorId, searchConfig);
        return count.intValue();
    }

    /**
     * 通过Id获取一个待办任务(设置了办理任务URL)
     *
     * @param taskId 待办任务Id
     * @return 待办任务
     */
    @Override
    public FlowTask findTaskById(String taskId) {
        FlowTask flowTask = flowTaskDao.findTaskById(taskId);
        if (Objects.nonNull(flowTask)) {
            initFlowTask(flowTask);
        }
        return flowTask;
    }


    /**
     * 通过业务单据Id获取待办任务
     *
     * @param businessId 业务单据id
     * @return 待办任务集合
     */
    public ResponseData findTasksByBusinessId(String businessId) {
        ResponseData responseData = new ResponseData();
        if (StringUtils.isEmpty(businessId)) {
            responseData.setSuccess(false);
            responseData.setMessage("参数不能为空！");
            return responseData;
        }
        List<FlowTask> list = new ArrayList<FlowTask>();
        //通过业务单据id查询没有结束并且没有挂起的流程实例
        List<FlowInstance> flowInstanceList = flowInstanceDao.findNoEndByBusinessIdOrder(businessId);
        if (flowInstanceList != null && flowInstanceList.size() > 0) {
            FlowInstance instance = flowInstanceList.get(0);
            //根据流程实例id查询待办
            List<FlowTask> addList = flowTaskDao.findByInstanceId(instance.getId());
            //完成待办任务的URL设置
            this.initFlowTasks(addList);
            list.addAll(addList);
        }
        responseData.setSuccess(true);
        responseData.setData(list);
        return responseData;
    }


    public ResponseData getExecutorsByRequestExecutorsVoAndOrg(List<RequestExecutorsVo> requestExecutorsVos, String businessId, String orgId) {

        if (requestExecutorsVos == null || requestExecutorsVos.size() == 0 || StringUtils.isEmpty(businessId) || StringUtils.isEmpty(orgId)) {
            return this.writeErrorLogAndReturnData(null, "请求参数不能为空！");
        }

        ResponseData responseData = new ResponseData();
        List<Executor> executors = null;
        if (requestExecutorsVos.size() == 1) { //流程发起人、指定岗位、指定岗位类别、自定义执行人、任意执行人
            String userType = requestExecutorsVos.get(0).getUserType();
            if ("StartUser".equalsIgnoreCase(userType)) { //流程发起人
                String startUserId = ContextUtil.getSessionUser().getUserId();
                executors = flowCommonUtil.getBasicUserExecutors(Arrays.asList(startUserId));
            } else if ("Position".equalsIgnoreCase(userType)) {//指定岗位
                String ids = requestExecutorsVos.get(0).getIds();
                executors = flowTaskTool.getExecutors(userType, ids, orgId);
            } else if ("PositionType".equalsIgnoreCase(userType)) { //指定岗位类别
                String ids = requestExecutorsVos.get(0).getIds();
                executors = flowTaskTool.getExecutors(userType, ids, orgId);
            } else if ("SelfDefinition".equalsIgnoreCase(userType)) { //自定义执行人
                String selfDefId = requestExecutorsVos.get(0).getIds();
                if (StringUtils.isNotEmpty(selfDefId) && !Constants.NULL_S.equalsIgnoreCase(selfDefId)) {
//                    try {
                    FlowExecutorConfig flowExecutorConfig = flowExecutorConfigDao.findOne(selfDefId);
                    String path = flowExecutorConfig.getUrl();
                    AppModule appModule = flowExecutorConfig.getBusinessModel().getAppModule();
                    String appModuleCode = appModule.getApiBaseAddress();
                    String param = flowExecutorConfig.getParam();
                    FlowInvokeParams flowInvokeParams = new FlowInvokeParams();
                    flowInvokeParams.setId(businessId);
                    flowInvokeParams.setOrgId("" + orgId);
                    flowInvokeParams.setJsonParam(param);
                    executors = flowCommonUtil.getExecutorsBySelfDef(appModuleCode, flowExecutorConfig.getName(), path, flowInvokeParams);
//                        executors = ApiClient.postViaProxyReturnResult(appModuleCode, path, new GenericType<List<Executor>>() {
//                        }, flowInvokeParams);
//                    } catch (Exception e) {
//                        return this.writeErrorLogAndReturnData(e, "获取【自定义执行人】接口调用失败！");
//                    }
                } else {
                    return this.writeErrorLogAndReturnData(null, "自定义执行人参数为空！");
                }
            } else if ("AnyOne".equalsIgnoreCase(userType)) { //任意执行人

            }
        } else if (requestExecutorsVos.size() > 1) { //岗位+组织维度、岗位+组织维度+自定义执行人、岗位类别+组织机构
            String selfDefId = null; //自定义执行人id
            List<String> positionIds = null;//岗位代码集合
            List<String> orgDimensionCodes = null;//组织维度代码集合
            List<String> orgIds = null; //组织机构id集合
            List<String> positionTypesIds = null;//岗位类别id集合
            for (RequestExecutorsVo executorsVo : requestExecutorsVos) {
                String ids = executorsVo.getIds();
                List<String> tempList = null;
                if (StringUtils.isNotEmpty(ids)) {
                    String[] idsShuZhu = ids.split(",");
                    tempList = Arrays.asList(idsShuZhu);
                }
                if ("SelfDefinition".equalsIgnoreCase(executorsVo.getUserType())) {//通过业务ID获取自定义用户
                    selfDefId = ids;
                } else if ("Position".equalsIgnoreCase(executorsVo.getUserType())) {
                    positionIds = tempList;
                } else if ("OrganizationDimension".equalsIgnoreCase(executorsVo.getUserType())) {
                    orgDimensionCodes = tempList;
                } else if ("PositionType".equalsIgnoreCase(executorsVo.getUserType())) {
                    positionTypesIds = tempList;
                } else if ("Org".equalsIgnoreCase(executorsVo.getUserType())) {
                    orgIds = tempList;
                }
            }

            if (StringUtils.isNotEmpty(selfDefId) && !Constants.NULL_S.equalsIgnoreCase(selfDefId)) {
                FlowExecutorConfig flowExecutorConfig = flowExecutorConfigDao.findOne(selfDefId);
                String path = flowExecutorConfig.getUrl();
                AppModule appModule = flowExecutorConfig.getBusinessModel().getAppModule();
                String appModuleCode = appModule.getApiBaseAddress();
                String param = flowExecutorConfig.getParam();
                FlowInvokeParams flowInvokeParams = new FlowInvokeParams();
                flowInvokeParams.setId(businessId);
                flowInvokeParams.setOrgId("" + orgId);
                flowInvokeParams.setOrganizationIds(orgIds);
                flowInvokeParams.setOrgDimensionCodes(orgDimensionCodes);
                flowInvokeParams.setPositionIds(positionIds);
                flowInvokeParams.setPositionTypeIds(positionTypesIds);
                flowInvokeParams.setJsonParam(param);
                executors = flowCommonUtil.getExecutorsBySelfDef(appModuleCode, flowExecutorConfig.getName(), path, flowInvokeParams);
//                try {
//                    executors = ApiClient.postViaProxyReturnResult(appModuleCode, path, new GenericType<List<Executor>>() {
//                    }, flowInvokeParams);
//                } catch (Exception e) {
//                    return this.writeErrorLogAndReturnData(e, "获取【岗位+组织维度+自定义执行人】接口调用失败！");
//                }
            } else {
                if (positionTypesIds != null && orgIds != null) {
                    //新增根据（岗位类别+组织机构）获得执行人
                    executors = flowCommonUtil.getExecutorsByPostCatIdsAndOrgs(positionTypesIds, orgIds);
                } else {
                    //通过岗位ids、组织维度ids和组织机构id来获取执行人【岗位+组织维度】
                    executors = flowCommonUtil.getExecutorsByPositionIdsAndorgDimIds(positionIds, orgDimensionCodes, orgId);
                }
            }
        }
        responseData.setData(executors);
        return responseData;
    }


    @Override
    public ResponseData getExecutorsByExecutorsVos(FindExecutorsVo findExecutorsVo) {
        List<RequestExecutorsVo> requestExecutorsVoList = null;
        String requestExecutorsVos = findExecutorsVo.getRequestExecutorsVos();
        if (StringUtils.isNotEmpty(requestExecutorsVos)) {
            JSONArray jsonArray = JSONArray.fromObject(requestExecutorsVos);//把String转换为json
            requestExecutorsVoList = (List<RequestExecutorsVo>) JSONArray.toCollection(jsonArray, RequestExecutorsVo.class);
        }
        return this.getExecutorsByRequestExecutorsVo(requestExecutorsVoList, findExecutorsVo.getBusinessModelCode(), findExecutorsVo.getBusinessId());
    }

    @Override
    public ResponseData getExecutorsByVoAndInstanceId(List<RequestExecutorsVo> requestExecutorsVos, String instanceId) {
        if (StringUtils.isEmpty(instanceId)) {
            return ResponseData.operationFailure("流程实例ID不能为空！");
        }
        FlowInstance flowInstance = flowInstanceDao.findOne(instanceId);
        if (flowInstance == null) {
            return ResponseData.operationFailure("通过参数获取流程实例失败！");
        }
        String businessModelCode = flowInstance.getFlowDefVersion().getFlowDefination().getFlowType().getBusinessModel().getClassName();
        String businessId = flowInstance.getBusinessId();
        return getExecutorsByRequestExecutorsVo(requestExecutorsVos, businessModelCode, businessId);
    }

    @Override
    public ResponseData getExecutorsByRequestExecutorsVo(List<RequestExecutorsVo> requestExecutorsVos, String businessModelCode, String businessId) {
        ResponseData responseData = new ResponseData();
        if (requestExecutorsVos == null || requestExecutorsVos.size() == 0 || StringUtils.isEmpty(businessModelCode) || StringUtils.isEmpty(businessId)) {
            return this.writeErrorLogAndReturnData(null, "请求参数不能为空！");
        }

        String orgId = null;
        try {
            BusinessModel businessModel = businessModelDao.findByProperty("className", businessModelCode);
            Map<String, Object> businessV = ExpressionUtil.getPropertiesValuesMap(businessModel, businessId, true);
            orgId = (String) businessV.get(Constants.ORG_ID);
            if (StringUtils.isEmpty(orgId)) {
                return this.writeErrorLogAndReturnData(null, "业务单据组织机构为空！");
            }
        } catch (Exception e) {
            return this.writeErrorLogAndReturnData(e, "获取业务单据组织机构失败！");
        }

        responseData = this.getExecutorsByRequestExecutorsVoAndOrg(requestExecutorsVos, businessId, orgId);
        return responseData;
    }


    public ResponseData writeErrorLogAndReturnData(Exception e, String msg) {
        if (e != null) {
            LogUtil.error(e.getMessage(), e);
        }
        ResponseData responseData = new ResponseData();
        responseData.setSuccess(false);
        responseData.setMessage(msg);
        return responseData;
    }


    public String getOrgIdByFlowTask(FlowTask flowTask) {
        //从回调进来的参数flowTask.getActTaskId()可能为空（服务任务）
        String currentOrgId;
        String actInstanceId;
        if (StringUtils.isNotEmpty(flowTask.getActTaskId())) {
            HistoricTaskInstance currTask = historyService.createHistoricTaskInstanceQuery().taskId(flowTask.getActTaskId()).singleResult();
            actInstanceId = currTask.getProcessInstanceId();
        } else {
            actInstanceId = flowTask.getFlowInstance().getActInstanceId();
        }
        Map<String, VariableInstance> processVariables = runtimeService.getVariableInstances(actInstanceId);
        currentOrgId = processVariables.get("orgId").getValue() + "";
        return currentOrgId;
    }

    @Override
    public ResponseData getTaskFormUrlXiangDuiByTaskId(String taskId) {
        ResponseData res = new ResponseData();
        if (StringUtils.isNotEmpty(taskId)) {
            FlowTask flowTask = flowTaskDao.findOne(taskId);
            if (flowTask != null) {
                String webBaseAddressConfig = flowTask.getFlowInstance().getFlowDefVersion().getFlowDefination().getFlowType().getBusinessModel().getAppModule().getWebBaseAddress();
                String webBaseAddress = Constants.getConfigValueByWeb(webBaseAddressConfig);
                if (StringUtils.isNotEmpty(webBaseAddress)) {
                    String[] tempWebBaseAddress = webBaseAddress.split("/");
                    if (tempWebBaseAddress != null && tempWebBaseAddress.length > 0) {
                        webBaseAddress = tempWebBaseAddress[tempWebBaseAddress.length - 1];
                    }
                }
                WorkPageUrl workPageUrl = flowTask.getWorkPageUrl();
                if (workPageUrl != null) {
                    String taskFormUrlXiangDui = "/" + webBaseAddress + "/" + workPageUrl.getUrl();
                    taskFormUrlXiangDui = taskFormUrlXiangDui.replaceAll("\\//", "/");
                    flowTask.setTaskFormUrlXiangDui(taskFormUrlXiangDui); //处理界面是同一模块
                    String appModuleId = workPageUrl.getAppModuleId();
                    AppModule appModule = appModuleService.findOne(appModuleId);
                    if (appModule != null && !appModule.getId().equals(flowTask.getFlowInstance().getFlowDefVersion().getFlowDefination().getFlowType().getBusinessModel().getAppModule().getId())) {
                        webBaseAddressConfig = appModule.getWebBaseAddress();
                        webBaseAddress = Constants.getConfigValueByWeb(webBaseAddressConfig);
                        if (StringUtils.isNotEmpty(webBaseAddress)) {
                            String[] tempWebBaseAddress = webBaseAddress.split("/");
                            if (tempWebBaseAddress != null && tempWebBaseAddress.length > 0) {
                                webBaseAddress = tempWebBaseAddress[tempWebBaseAddress.length - 1];
                            }
                        }
                        taskFormUrlXiangDui = "/" + webBaseAddress + "/" + workPageUrl.getUrl();
                        taskFormUrlXiangDui = taskFormUrlXiangDui.replaceAll("\\//", "/");
                        flowTask.setTaskFormUrlXiangDui(taskFormUrlXiangDui);
                    }
                    res.setData(flowTask.getTaskFormUrlXiangDui());
                } else {
                    return ResponseData.operationFailure("当前待办页面信息不存在！");
                }
            } else {
                List<FlowHistory> historylist = flowHistoryService.findListByProperty("oldTaskId", taskId);
                if (historylist != null && historylist.size() > 0) {
                    FlowHistory history = historylist.get(0);
                    String webBaseAddressConfig = history.getFlowInstance().getFlowDefVersion().getFlowDefination().getFlowType().getBusinessModel().getAppModule().getWebBaseAddress();
                    String webBaseAddress = Constants.getConfigValueByWeb(webBaseAddressConfig);
                    if (StringUtils.isNotEmpty(webBaseAddress)) {
                        String[] tempWebBaseAddress = webBaseAddress.split("/");
                        if (tempWebBaseAddress != null && tempWebBaseAddress.length > 0) {
                            webBaseAddress = tempWebBaseAddress[tempWebBaseAddress.length - 1];
                        }
                    }
                    String lookUrl = history.getFlowInstance().getFlowDefVersion().getFlowDefination().getFlowType().getBusinessModel().getLookUrl();
                    String lookUrlXiangDui = "/" + webBaseAddress + "/" + lookUrl;
                    lookUrlXiangDui = lookUrlXiangDui.replaceAll("\\//", "/");
                    res.setData(lookUrlXiangDui);
                } else {
                    return ResponseData.operationFailure("当前任务不存在！");
                }
            }
        } else {
            return ResponseData.operationFailure("参数不能为空！");
        }
        return res;
    }


    /**
     * 完成待办任务的URL设置
     *
     * @param flowTasks 待办任务清单
     * @return 待办任务
     */
    public void initFlowTasks(List<FlowTask> flowTasks) {
        if (CollectionUtils.isEmpty(flowTasks)) {
            return;
        }
        flowTasks.forEach(this::initFlowTask);
    }


    /**
     * 完成待办任务的URL设置
     *
     * @param flowTask 待办任务
     * @return 待办任务
     */
    private void initFlowTask(FlowTask flowTask) {
        String apiBaseAddressConfig = flowTask.getFlowInstance().getFlowDefVersion().getFlowDefination().getFlowType().getBusinessModel().getAppModule().getApiBaseAddress();
        String apiBaseAddress = Constants.getConfigValueByApi(apiBaseAddressConfig);
        if (StringUtils.isNotEmpty(apiBaseAddress)) {
            flowTask.setApiBaseAddressAbsolute(apiBaseAddress);
//            String[] tempApiBaseAddress = apiBaseAddress.split("/");
//            if (tempApiBaseAddress != null && tempApiBaseAddress.length > 0) {
//                apiBaseAddress = tempApiBaseAddress[tempApiBaseAddress.length - 1];
//                flowTask.setApiBaseAddress("/" + apiBaseAddress + "/");
//            }
            String apiAddress = Constants.getConfigValueByApi(apiBaseAddressConfig);
            flowTask.setApiBaseAddress(apiAddress);
        }
        String webBaseAddressConfig = flowTask.getFlowInstance().getFlowDefVersion().getFlowDefination().getFlowType().getBusinessModel().getAppModule().getWebBaseAddress();
        String webBaseAddress = Constants.getConfigValueByWeb(webBaseAddressConfig);
        String webAddress = Constants.getConfigValueByWeb(webBaseAddressConfig);

        if (StringUtils.isNotEmpty(webBaseAddress)) {
            flowTask.setWebBaseAddressAbsolute(webBaseAddress);
            flowTask.setLookWebBaseAddressAbsolute(webBaseAddress);
//            String[] tempWebBaseAddress = webBaseAddress.split("/");
//            if (tempWebBaseAddress != null && tempWebBaseAddress.length > 0) {
//                webBaseAddress = tempWebBaseAddress[tempWebBaseAddress.length - 1];
//                flowTask.setWebBaseAddress("/" + webBaseAddress + "/");
//                flowTask.setLookWebBaseAddress("/" + webBaseAddress + "/");
//            }
            flowTask.setWebBaseAddress(webAddress);
            flowTask.setLookWebBaseAddress(webAddress);
        }
        WorkPageUrl workPageUrl = flowTask.getWorkPageUrl();
        String completeTaskServiceUrl = flowTask.getFlowInstance().getFlowDefVersion().getFlowDefination().getFlowType().getCompleteTaskServiceUrl();
        String businessDetailServiceUrl = flowTask.getFlowInstance().getFlowDefVersion().getFlowDefination().getFlowType().getBusinessDetailServiceUrl();
        if (StringUtils.isEmpty(completeTaskServiceUrl)) {
            completeTaskServiceUrl = flowTask.getFlowInstance().getFlowDefVersion().getFlowDefination().getFlowType().getBusinessModel().getCompleteTaskServiceUrl();
        }
        if (StringUtils.isEmpty(businessDetailServiceUrl)) {
            businessDetailServiceUrl = flowTask.getFlowInstance().getFlowDefVersion().getFlowDefination().getFlowType().getBusinessModel().getBusinessDetailServiceUrl();
        }
        flowTask.setCompleteTaskServiceUrl(completeTaskServiceUrl);
        flowTask.setBusinessDetailServiceUrl(businessDetailServiceUrl);
        if (workPageUrl != null) {
            flowTask.setTaskFormUrl(PageUrlUtil.buildUrl(Constants.getConfigValueByWeb(webBaseAddressConfig), workPageUrl.getUrl()));
            String taskFormUrlXiangDui = webAddress + "/" + workPageUrl.getUrl();
            taskFormUrlXiangDui = taskFormUrlXiangDui.replaceAll("\\//", "/");
            flowTask.setTaskFormUrlXiangDui(taskFormUrlXiangDui);
            String appModuleId = workPageUrl.getAppModuleId();
            AppModule appModule = appModuleDao.findOne(appModuleId);
            if (appModule != null && !appModule.getId().equals(flowTask.getFlowInstance().getFlowDefVersion().getFlowDefination().getFlowType().getBusinessModel().getAppModule().getId())) {
                webBaseAddressConfig = appModule.getWebBaseAddress();
                webBaseAddress = Constants.getConfigValueByWeb(webBaseAddressConfig);
                flowTask.setTaskFormUrl(PageUrlUtil.buildUrl(webBaseAddress, workPageUrl.getUrl()));
                if (StringUtils.isNotEmpty(webBaseAddress)) {
                    flowTask.setWebBaseAddressAbsolute(webBaseAddress);
//                    String[] tempWebBaseAddress = webBaseAddress.split("/");
//                    if (tempWebBaseAddress != null && tempWebBaseAddress.length > 0) {
//                        webBaseAddress = tempWebBaseAddress[tempWebBaseAddress.length - 1];
//                        flowTask.setWebBaseAddress("/" + webBaseAddress + "/");
//                    }
                    webAddress = Constants.getConfigValueByWeb(webBaseAddressConfig);
                    flowTask.setWebBaseAddress(webAddress);
                }
                taskFormUrlXiangDui = "/" + webBaseAddress + "/" + workPageUrl.getUrl();
                taskFormUrlXiangDui = taskFormUrlXiangDui.replaceAll("\\//", "/");
                flowTask.setTaskFormUrlXiangDui(taskFormUrlXiangDui);
            }
        }
    }


}

