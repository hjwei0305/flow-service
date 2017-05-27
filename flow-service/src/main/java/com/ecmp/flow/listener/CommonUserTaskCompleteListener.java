package com.ecmp.flow.listener;

import com.ecmp.core.dao.BaseEntityDao;
import com.ecmp.flow.dao.FlowHistoryDao;
import com.ecmp.flow.dao.FlowInstanceDao;
import com.ecmp.flow.dao.FlowTaskDao;
import com.ecmp.flow.dao.FlowVariableDao;
import com.ecmp.flow.entity.FlowTask;
import com.ecmp.flow.service.FlowDefinationService;
import com.ecmp.flow.util.ServiceCallUtil;
import com.ecmp.flow.vo.NodeInfo;
import com.ecmp.flow.vo.bpmn.Definition;
import com.ecmp.flow.vo.bpmn.UserTask;
import net.sf.json.JSONObject;
import org.activiti.engine.*;
import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.TaskListener;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


/**
 * *************************************************************************************************
 * <p/>
 * 实现功能： 通用用户任务完成监听器
 * <p>
 * ------------------------------------------------------------------------------------------------
 * 版本          变更时间             变更人                     变更原因
 * ------------------------------------------------------------------------------------------------
 * 1.0.00      2017/5/24 13:40      谭军(tanjun)                    新建
 * <p/>
 * *************************************************************************************************
 */
@Component(value="commonUserTaskCompleteListener")
public class CommonUserTaskCompleteListener implements TaskListener{

    @Autowired
    private FlowTaskDao flowTaskDao;

    private final Logger logger = LoggerFactory.getLogger(FlowDefinationService.class);

	public CommonUserTaskCompleteListener(){
		System.out.println("commonUserTaskCompleteListener-------------------------");
	}
    private static final long serialVersionUID = 1L;

    public void notify(DelegateTask delegateTask) {
      String currentTaskId =  delegateTask.getId();
      FlowTask flowTask = flowTaskDao.findByActTaskId(currentTaskId);
      flowTask.getFlowInstance().getFlowDefVersion().getDefJson();
      String flowDefJson = flowTask.getFlowInstance().getFlowDefVersion().getDefJson();
      JSONObject defObj = JSONObject.fromObject(flowDefJson);
      Definition definition = (Definition) JSONObject.toBean(defObj, Definition.class);
      net.sf.json.JSONObject currentNode = definition.getProcess().getNodes().getJSONObject(flowTask.getActTaskDefKey());
//      net.sf.json.JSONObject executor = currentNode.getJSONObject("nodeConfig").getJSONObject("executor");
      net.sf.json.JSONObject event =    currentNode.getJSONObject("nodeConfig").getJSONObject("event");
      UserTask userTaskTemp = (UserTask) JSONObject.toBean(currentNode, UserTask.class);
      if(event !=null){
//          event.get("beforeExcuteService");
//          String beforeExcuteServiceId =  (String)event.get("beforeExcuteServiceId");
//          if(!StringUtils.isEmpty(beforeExcuteServiceId)){
//              ServiceCallUtil.callService(beforeExcuteServiceId);
//          }

//          event.get("afterExcuteService");
          String afterExcuteServiceId =  (String)event.get("afterExcuteServiceId");
          if(!StringUtils.isEmpty(afterExcuteServiceId)){
              ServiceCallUtil.callService(afterExcuteServiceId);
          }
      }

    }
}
