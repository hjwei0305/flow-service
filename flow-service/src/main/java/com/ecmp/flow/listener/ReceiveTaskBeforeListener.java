package com.ecmp.flow.listener;

import com.ecmp.config.util.ApiClient;
import com.ecmp.context.ContextUtil;
import com.ecmp.flow.dao.*;
import com.ecmp.flow.entity.FlowDefVersion;
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
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * *************************************************************************************************
 * <p/>
 * 实现功能：
 * <p>
 * ------------------------------------------------------------------------------------------------
 * 版本          变更时间             变更人                     变更原因
 * ------------------------------------------------------------------------------------------------
 * 1.0.00      2017/8/14 9:40      谭军(tanjun)                    新建
 * <p/>
 * *************************************************************************************************
 */
@Component(value="receiveTaskBeforeListener")
public class ReceiveTaskBeforeListener implements org.activiti.engine.delegate.JavaDelegate {

    private final Logger logger = LoggerFactory.getLogger(ReceiveTaskBeforeListener.class);


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
            Date now = new Date();
            ExecutionEntity taskEntity = (ExecutionEntity) delegateTask;
            String actTaskDefKey = delegateTask.getCurrentActivityId();
            String actProcessDefinitionId = delegateTask.getProcessDefinitionId();
            String businessId =delegateTask.getProcessBusinessKey();
//            if(StringUtils.isEmpty(businessId)){
//                ExecutionEntity parentExecutionEntity = ((ExecutionEntity) delegateTask).getSuperExecution();
//                if(parentExecutionEntity != null){
//                    businessId =  parentExecutionEntity.getProcessInstance().getBusinessKey();
//                }
//            }
            FlowDefVersion flowDefVersion = flowDefVersionDao.findByActDefId(actProcessDefinitionId);
            String flowDefJson = flowDefVersion.getDefJson();
            JSONObject defObj = JSONObject.fromObject(flowDefJson);
            Definition definition = (Definition) JSONObject.toBean(defObj, Definition.class);
            JSONObject currentNode = definition.getProcess().getNodes().getJSONObject(actTaskDefKey);
            JSONObject normal = currentNode.getJSONObject("nodeConfig").getJSONObject("normal");
            if (normal != null) {
                String serviceTaskId = (String) normal.get("serviceTaskId");
                if (!StringUtils.isEmpty(serviceTaskId)) {
                    Map<String,Object> tempV = delegateTask.getVariables();
                    tempV.put("receiveTaskActDefId",actTaskDefKey);

                    String flowTaskName = (String) normal.get("name");
                    FlowTask flowTask = new FlowTask();
                    flowTask.setTaskJsonDef(currentNode.toString());
                    flowTask.setFlowName(definition.getProcess().getName());
                    flowTask.setDepict("接收任务【等待执行】");
                    flowTask.setTaskName(flowTaskName);
                    flowTask.setFlowDefinitionId(flowDefVersion.getFlowDefination().getId());
                    String actProcessInstanceId = delegateTask.getProcessInstanceId();
                    FlowInstance flowInstance = flowInstanceDao.findByActInstanceId(actProcessInstanceId);
                    flowTask.setFlowInstance(flowInstance);
                    String ownerName = flowDefVersion.getFlowDefination().getFlowType().getBusinessModel().getName();
                    String appModuleId = flowDefVersion.getFlowDefination().getFlowType().getBusinessModel().getAppModuleId();
                    com.ecmp.flow.api.IAppModuleService proxy = ApiClient.createProxy(com.ecmp.flow.api.IAppModuleService.class);
                    com.ecmp.flow.entity.AppModule appModule = proxy.findOne(appModuleId);
                    if(appModule!=null && StringUtils.isNotEmpty(appModule.getName())){
                        ownerName = appModule.getName();
                    }
                    flowTask.setOwnerAccount("admin");
                    flowTask.setOwnerName(ownerName);
                    flowTask.setExecutorAccount("admin");
                    flowTask.setExecutorId("");
                    flowTask.setExecutorName(ownerName);
                    flowTask.setCandidateAccount("");
                    flowTask.setActDueDate(now);

                    flowTask.setActTaskDefKey(actTaskDefKey);
                    flowTask.setPreId(null);
                    flowTask.setTaskStatus(TaskStatus.COMPLETED.toString());
                    flowTask.setTaskStatus(TaskStatus.INIT.toString());

                    //选择下一步执行人，默认选择第一个，会签、串、并行选择全部
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
                   ServiceCallUtil.callService(serviceTaskId, businessId, param);
                   flowTaskDao.save(flowTask);

                }else{
                    throw new RuntimeException("服务地址不能为空！");
                }
            }
    }
}
