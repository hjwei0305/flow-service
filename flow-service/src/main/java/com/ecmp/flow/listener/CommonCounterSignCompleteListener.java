package com.ecmp.flow.listener;

import com.ecmp.flow.dao.FlowDefVersionDao;
import com.ecmp.flow.dao.FlowTaskDao;
import com.ecmp.flow.entity.FlowDefVersion;
import com.ecmp.flow.vo.bpmn.Definition;
import net.sf.json.JSONObject;
import org.activiti.engine.HistoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.TaskListener;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.VariableInstance;
import org.activiti.engine.runtime.ProcessInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;


/**
 * *************************************************************************************************
 * <p/>
 * 实现功能： 通用会签任务监听器，当会签任务完成时统计投票数量
 *   counterSignAgree代表同意、counterSignOpposition代表不同意、counterSignWaiver代表弃权
 * <p>
 * ------------------------------------------------------------------------------------------------
 * 版本          变更时间             变更人                     变更原因
 * ------------------------------------------------------------------------------------------------
 * 1.0.00      2017/5/24 11:40      谭军(tanjun)                    新建
 * <p/>
 * *************************************************************************************************
 */
@Component(value="commonCounterSignCompleteListener")
public class CommonCounterSignCompleteListener implements TaskListener{

    @Autowired
    private FlowTaskDao flowTaskDao;

    @Autowired
    private FlowDefVersionDao flowDefVersionDao;

    @Autowired
    private HistoryService historyService;

    @Autowired
    private RuntimeService runtimeService;

    public CommonCounterSignCompleteListener(){
	}
    private static final long serialVersionUID = 1L;

    private final Logger logger = LoggerFactory.getLogger(CommonCounterSignCompleteListener.class);
    public void notify(DelegateTask delegateTask) {
        Map<String,VariableInstance> processVariables =  runtimeService.getVariableInstances(delegateTask.getProcessInstanceId());
        Integer counterSignAgree = 0;//同意票数
        if(processVariables.get("counterSign_agree"+delegateTask.getTaskDefinitionKey())!=null) {
            counterSignAgree = (Integer) processVariables.get("counterSign_agree"+delegateTask.getTaskDefinitionKey()).getValue();//同意票数
        }
        Integer counterSignOpposition = 0;//反对
        if( processVariables.get("counterSign_opposition"+delegateTask.getTaskDefinitionKey())!=null) {
             counterSignOpposition = (Integer) processVariables.get("counterSign_opposition"+delegateTask.getTaskDefinitionKey()).getValue();
        }

        Integer counterSignWaiver = 0;//弃权
        if( processVariables.get("counterSign_waiver"+delegateTask.getTaskDefinitionKey())!=null) {
            counterSignOpposition = (Integer) processVariables.get("counterSign_waiver"+delegateTask.getTaskDefinitionKey()).getValue();
        }

        String approved = (String) delegateTask.getVariable("approved");

        if("true".equalsIgnoreCase(approved)){
            counterSignAgree++;
        }else if("false".equalsIgnoreCase(approved)){
            counterSignOpposition++;
        }else {
            counterSignWaiver++;
        }
        runtimeService.setVariable(delegateTask.getProcessInstanceId(),"counterSign_agree"+delegateTask.getTaskDefinitionKey(), counterSignAgree);
        runtimeService.setVariable(delegateTask.getProcessInstanceId(),"counterSign_opposition"+delegateTask.getTaskDefinitionKey(), counterSignOpposition);
        runtimeService.setVariable(delegateTask.getProcessInstanceId(),"counterSign_waiver"+delegateTask.getTaskDefinitionKey(), counterSignWaiver);
    }
}
