package com.ecmp.flow.listener;

import com.ecmp.flow.dao.FlowDefVersionDao;
import com.ecmp.flow.dao.FlowDefinationDao;
import com.ecmp.flow.dao.FlowTaskDao;
import com.ecmp.flow.entity.FlowDefVersion;
import com.ecmp.flow.entity.FlowTask;
import com.ecmp.flow.util.ServiceCallUtil;
import com.ecmp.flow.vo.bpmn.Definition;
import com.ecmp.flow.vo.bpmn.UserTask;
import net.sf.json.JSONObject;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.TaskListener;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.runtime.ProcessInstance;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;


/**
 * *************************************************************************************************
 * <p/>
 * 实现功能： 通用用户任务创建监听器
 * <p>
 * ------------------------------------------------------------------------------------------------
 * 版本          变更时间             变更人                     变更原因
 * ------------------------------------------------------------------------------------------------
 * 1.0.00      2017/5/24 13:40      谭军(tanjun)                    新建
 * <p/>
 * *************************************************************************************************
 */
@Component(value="commonUserTaskCreateListener")
public class CommonUserTaskCreateListener implements TaskListener{

    private final Logger logger = LoggerFactory.getLogger(CommonUserTaskCreateListener.class);
	public CommonUserTaskCreateListener(){
		System.out.println("commonUserTaskCreateListener-------------------------");
	}
    private static final long serialVersionUID = 1L;

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private FlowTaskDao flowTaskDao;

    @Autowired
    private FlowDefVersionDao flowDefVersionDao;

    @Autowired
    private FlowDefinationDao flowDefinationDao;

    @Transactional( propagation= Propagation.REQUIRED)
    public void notify(DelegateTask delegateTask) {
        ExecutionEntity taskEntity = (ExecutionEntity)delegateTask.getExecution();
        String actTaskDefKey =  taskEntity.getActivityId();
        String actProcessDefinitionId = delegateTask.getProcessDefinitionId();
        ProcessInstance instance =  taskEntity.getProcessInstance();
        String businessId = instance.getBusinessKey();
        FlowDefVersion flowDefVersion = flowDefVersionDao.findByActDefId(actProcessDefinitionId);
        String flowDefJson = flowDefVersion.getDefJson();
        JSONObject defObj = JSONObject.fromObject(flowDefJson);
        Definition definition = (Definition) JSONObject.toBean(defObj, Definition.class);
//        net.sf.json.JSONObject currentNode = definition.getProcess().getNodes().getJSONObject(currentTaskId);
        net.sf.json.JSONObject currentNode = definition.getProcess().getNodes().getJSONObject(actTaskDefKey);
        //        net.sf.json.JSONObject executor = currentNode.getJSONObject("nodeConfig").getJSONObject("executor");
        net.sf.json.JSONObject event =    currentNode.getJSONObject("nodeConfig").getJSONObject("event");
        UserTask userTaskTemp = (UserTask) JSONObject.toBean(currentNode, UserTask.class);
        if(event !=null){
          String beforeExcuteServiceId =  (String)event.get("beforeExcuteServiceId");
          if(!StringUtils.isEmpty(beforeExcuteServiceId)){
              ServiceCallUtil.callService(beforeExcuteServiceId,businessId,"before");
          }
        }

    }
}
