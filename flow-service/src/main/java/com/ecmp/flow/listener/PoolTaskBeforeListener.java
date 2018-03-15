package com.ecmp.flow.listener;

import com.ecmp.context.ContextUtil;
import com.ecmp.flow.common.util.Constants;
import com.ecmp.flow.dao.FlowDefVersionDao;
import com.ecmp.flow.dao.FlowHistoryDao;
import com.ecmp.flow.dao.FlowInstanceDao;
import com.ecmp.flow.dao.FlowTaskDao;
import com.ecmp.flow.entity.AppModule;
import com.ecmp.flow.entity.FlowDefVersion;
import com.ecmp.flow.entity.FlowInstance;
import com.ecmp.flow.entity.FlowTask;
import com.ecmp.flow.service.FlowTaskService;
import com.ecmp.flow.util.FlowException;
import com.ecmp.flow.util.ServiceCallUtil;
import com.ecmp.flow.util.TaskStatus;
import com.ecmp.flow.vo.NodeInfo;
import com.ecmp.flow.vo.bpmn.Definition;
import com.ecmp.util.JsonUtils;
import net.sf.json.JSONObject;
import org.activiti.engine.delegate.DelegateExecution;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * *************************************************************************************************
 * <p/>
 * 实现功能：接收任务触发前监听事件
 * <p>
 * ------------------------------------------------------------------------------------------------
 * 版本          变更时间             变更人                     变更原因
 * ------------------------------------------------------------------------------------------------
 * 1.0.00      2017/8/14 9:40      谭军(tanjun)                    新建
 * <p/>
 * *************************************************************************************************
 */
//@Component(value="poolTaskBeforeListener")
public class PoolTaskBeforeListener implements org.activiti.engine.delegate.JavaDelegate {

    @Autowired
    private FlowTaskDao flowTaskDao;

    @Autowired
    private FlowDefVersionDao flowDefVersionDao;

    @Autowired
    FlowHistoryDao  flowHistoryDao;

    @Autowired
    private FlowInstanceDao flowInstanceDao;

    @Override
    public void execute(DelegateExecution delegateTask) throws Exception {
            Date now = new Date();
            String actTaskDefKey = delegateTask.getCurrentActivityId();
            String actProcessDefinitionId = delegateTask.getProcessDefinitionId();
            String businessId =delegateTask.getProcessBusinessKey();
            FlowDefVersion flowDefVersion = flowDefVersionDao.findByActDefId(actProcessDefinitionId);
            String flowDefJson = flowDefVersion.getDefJson();
            JSONObject defObj = JSONObject.fromObject(flowDefJson);
            Definition definition = (Definition) JSONObject.toBean(defObj, Definition.class);
            JSONObject currentNode = definition.getProcess().getNodes().getJSONObject(actTaskDefKey);
            JSONObject normal = currentNode.getJSONObject(Constants.NODE_CONFIG).getJSONObject(Constants.NORMAL);
            if (normal != null) {
                String serviceTaskId = (String) normal.get(Constants.SERVICE_TASK_ID);
                if (!StringUtils.isEmpty(serviceTaskId)) {
                    Map<String,Object> tempV = delegateTask.getVariables();
                    tempV.put(Constants.POOL_TASK_ACT_DEF_ID,actTaskDefKey);
                    String flowTaskName = (String) normal.get(Constants.NAME);
                    FlowTask flowTask = new FlowTask();
                    flowTask.setTaskJsonDef(currentNode.toString());
                    flowTask.setFlowName(definition.getProcess().getName());
                    flowTask.setDepict( ContextUtil.getMessage("10052"));//"用户池任务【等待执行】"
                    flowTask.setTaskName(flowTaskName);
                    flowTask.setFlowDefinitionId(flowDefVersion.getFlowDefination().getId());
                    String actProcessInstanceId = delegateTask.getProcessInstanceId();
                    FlowInstance flowInstance = flowInstanceDao.findByActInstanceId(actProcessInstanceId);
                    flowTask.setFlowInstance(flowInstance);
                    String ownerName = flowDefVersion.getFlowDefination().getFlowType().getBusinessModel().getName();
                    AppModule appModule = flowDefVersion.getFlowDefination().getFlowType().getBusinessModel().getAppModule();
                    if(appModule!=null && StringUtils.isNotEmpty(appModule.getName())){
                        ownerName = appModule.getName();
                    }
                    flowTask.setOwnerAccount(Constants.ANONYMOUS);
                    flowTask.setOwnerName(ownerName);
                    flowTask.setExecutorAccount(Constants.ANONYMOUS);
                    flowTask.setExecutorId("");
                    flowTask.setExecutorName(ownerName);
                    flowTask.setCandidateAccount("");
                    flowTask.setActDueDate(now);

                    flowTask.setActTaskDefKey(actTaskDefKey);
                    flowTask.setPreId(null);
                    flowTask.setTaskStatus(TaskStatus.COMPLETED.toString());
                    flowTask.setTaskStatus(TaskStatus.INIT.toString());

                    //选择下一步可能的执行子流程路径
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
                        tempV.put(Constants.CALL_ACTIVITY_SON_PATHS,paths);//提供给调用服务，子流程的绝对路径，用于存入单据id
                    }
                    String param = JsonUtils.toJson(tempV);
                   ServiceCallUtil.callService(serviceTaskId, businessId, param);
//                   flowTaskDao.save(flowTask);

                }else{
                    String message = ContextUtil.getMessage("10044");
                    throw new FlowException(message);//服务地址为空！
                }
            }
    }
}
