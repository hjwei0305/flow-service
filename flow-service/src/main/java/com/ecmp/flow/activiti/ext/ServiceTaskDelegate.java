package com.ecmp.flow.activiti.ext;

import com.ecmp.flow.common.util.Constants;
import com.ecmp.flow.constant.FlowStatus;
import com.ecmp.flow.dao.FlowDefVersionDao;
import com.ecmp.flow.dao.FlowHistoryDao;
import com.ecmp.flow.dao.FlowInstanceDao;
import com.ecmp.flow.entity.*;
import com.ecmp.flow.service.FlowDefinationService;
import com.ecmp.flow.service.FlowTaskService;
import com.ecmp.flow.util.*;
import com.ecmp.flow.vo.FlowOperateResult;
import com.ecmp.flow.vo.NodeInfo;
import com.ecmp.flow.vo.bpmn.Definition;
import com.ecmp.log.util.LogUtil;
import com.ecmp.util.JsonUtils;
import net.sf.json.JSONObject;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

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
public class ServiceTaskDelegate implements org.activiti.engine.delegate.JavaDelegate {

    public ServiceTaskDelegate() {
    }

    @Autowired
    private FlowDefVersionDao flowDefVersionDao;

    @Autowired
    FlowHistoryDao flowHistoryDao;

    @Autowired
    private FlowInstanceDao flowInstanceDao;

    @Autowired
    FlowDefinationService flowDefinationService;

    @Autowired
    private FlowListenerTool flowListenerTool;

    @Autowired
    private FlowTaskService flowTaskService;


    @Override
    public void execute(DelegateExecution delegateTask) throws Exception {

        String actProcessInstanceId = delegateTask.getProcessInstanceId();
        FlowInstance flowInstance = flowInstanceDao.findByActInstanceId(actProcessInstanceId);
        List<FlowTask> flowTaskList = flowTaskService.findByInstanceId(flowInstance.getId());
        List<FlowHistory> flowHistoryList = flowHistoryDao.findByInstanceId(flowInstance.getId());

        try {
            ExecutionEntity taskEntity = (ExecutionEntity) delegateTask;
            String actTaskDefKey = delegateTask.getCurrentActivityId();
            String actProcessDefinitionId = delegateTask.getProcessDefinitionId();
            String businessId = delegateTask.getProcessBusinessKey();

            FlowDefVersion flowDefVersion = flowDefVersionDao.findByActDefId(actProcessDefinitionId);
            String flowDefJson = flowDefVersion.getDefJson();
            JSONObject defObj = JSONObject.fromObject(flowDefJson);
            Definition definition = (Definition) JSONObject.toBean(defObj, Definition.class);
            net.sf.json.JSONObject currentNode = definition.getProcess().getNodes().getJSONObject(actTaskDefKey);
            net.sf.json.JSONObject normal = currentNode.getJSONObject(Constants.NODE_CONFIG).getJSONObject(Constants.NORMAL);
            if (normal != null) {
                String serviceTaskId = (String) normal.get(Constants.SERVICE_TASK_ID);
                String serviceTaskName = (String) normal.get(Constants.SERVICE_TASK);
                String flowTaskName = (String) normal.get(Constants.NAME);
                if (!StringUtils.isEmpty(serviceTaskId)) {
                    Map<String, Object> tempV = delegateTask.getVariables();

                    FlowHistory flowHistory = new FlowHistory();
                    flowHistory.setTaskJsonDef(currentNode.toString());
                    flowHistory.setFlowName(definition.getProcess().getName());
                    flowHistory.setDepict("服务任务【自动执行】");//服务任务【自动执行】
                    flowHistory.setFlowTaskName(flowTaskName);
                    flowHistory.setFlowDefId(flowDefVersion.getFlowDefination().getId());
                    flowHistory.setFlowInstance(flowInstance);

                    flowHistory.setOwnerAccount(Constants.ADMIN);
                    flowHistory.setOwnerName("系统自动");
                    flowHistory.setExecutorAccount(Constants.ADMIN);
                    flowHistory.setExecutorId("");
                    flowHistory.setExecutorName("系统自动");
                    flowHistory.setCandidateAccount("");
                    flowHistory.setActStartTime(new Date());
                    flowHistory.setActHistoryId(null);
                    flowHistory.setActTaskDefKey(actTaskDefKey);
                    flowHistory.setPreId(null);

                    FlowTask flowTask = new FlowTask();
                    BeanUtils.copyProperties(flowHistory, flowTask);
                    flowTask.setTaskStatus(TaskStatus.INIT.toString());

                    List<String> paths = flowListenerTool.getCallActivitySonPaths(flowTask);
                    if (!CollectionUtils.isEmpty(paths)) {
                        tempV.put(Constants.CALL_ACTIVITY_SON_PATHS, paths);//提供给调用服务，子流程的绝对路径，用于存入单据id
                    }
                    String param = JsonUtils.toJson(tempV);

                    FlowOperateResult flowOperateResult = null;
                    String callMessage;

                    try {
                        flowOperateResult = ServiceCallUtil.callService(serviceTaskId, serviceTaskName, flowTaskName, businessId, param);
                        callMessage = flowOperateResult.getMessage();
                    } catch (Exception e) {
                        callMessage = e.getMessage();
                    }
                    if ((flowOperateResult == null || !flowOperateResult.isSuccess())) {
                        throw new FlowException(callMessage);//抛出异常
                    } else {
                        //可能有动态改变属性的情况（页面上没有控制的地方，这里统一重新设置业务模块的参数）
                        BusinessModel businessModel = flowDefVersion.getFlowDefination().getFlowType().getBusinessModel();
                        Map<String, Object> variables = ExpressionUtil.getPropertiesValuesMap(businessModel, businessId, false);
                        delegateTask.setVariables(variables);
                    }

                    Calendar c = new GregorianCalendar();
                    c.setTime(new Date());
                    c.add(Calendar.SECOND, 10);
                    flowHistory.setActEndTime(c.getTime());//服务任务，默认延后10S
                    flowHistory.setTaskStatus(TaskStatus.COMPLETED.toString());
                    if (flowHistory.getActDurationInMillis() == null) {
                        Long actDurationInMillis = flowHistory.getActEndTime().getTime() - flowHistory.getActStartTime().getTime();
                        flowHistory.setActDurationInMillis(actDurationInMillis);
                    }
                    flowHistoryDao.save(flowHistory);

                    //选择下一步执行人，默认选择第一个(会签、单签、串、并行选择全部)
                    List<NodeInfo> results;
                    if (flowDefVersion != null && flowDefVersion.getSolidifyFlow() == true) { //固化流程
                        results = flowListenerTool.nextNodeInfoList(flowTask, delegateTask, true);
                    } else {
                        results = flowListenerTool.nextNodeInfoList(flowTask, delegateTask, false);
                    }
                    //初始化节点执行人
                    List<NodeInfo> nextNodes = flowListenerTool.initNodeUsers(results, delegateTask, actTaskDefKey);
                    //初始化下一步任务信息
                    flowListenerTool.initNextAllTask(nextNodes, taskEntity, flowHistory);
                } else {
                    throw new FlowException("服务任务未配置服务事件！");
                }
            }
        } catch (Exception e) {

            if (CollectionUtils.isEmpty(flowTaskList) && CollectionUtils.isEmpty(flowHistoryList)) { //如果是开始节点，手动回滚
                new Thread() {
                    public void run() {
                        BusinessModel businessModel = flowInstance.getFlowDefVersion().getFlowDefination().getFlowType().getBusinessModel();
                        Boolean result = false;
                        int index = 5;
                        while (!result && index > 0) {
                            try {
                                Thread.sleep(1000 * (6 - index));
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            try {
                                result = ExpressionUtil.resetState(businessModel, flowInstance.getBusinessId(), FlowStatus.INIT);
                            } catch (Exception e) {
                                LogUtil.error(e.getMessage(), e);
                            }
                            index--;
                        }
                    }
                }.start();
            }

            if (e.getClass() != FlowException.class) {
                LogUtil.error(e.getMessage(), e);
            }

            throw e;
        }
    }
}
