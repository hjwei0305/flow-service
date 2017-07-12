package com.ecmp.flow.listener;

import com.ecmp.flow.dao.FlowDefVersionDao;
import com.ecmp.flow.dao.FlowTaskDao;
import com.ecmp.flow.entity.FlowDefVersion;
import com.ecmp.flow.vo.bpmn.Definition;
import net.sf.json.JSONObject;
import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.TaskListener;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.runtime.ProcessInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;



/**
 * *************************************************************************************************
 * <p/>
 * 实现功能： 通用会签任务监听器，当会签任务完成时统计投票数量
 *   1代表同意、-1代表不同意、0代表弃权
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


	public CommonCounterSignCompleteListener(){
		System.out.println("commonCounterSignCompleteListener-------------------------");
	}
    private static final long serialVersionUID = 1L;

    private final Logger logger = LoggerFactory.getLogger(CommonCounterSignCompleteListener.class);
    public void notify(DelegateTask delegateTask) {
        Integer counterSignAgree = (Integer) delegateTask.getVariable("counterSign_agree");//同意票数
        if(counterSignAgree==null) {
            counterSignAgree = 0;
        }
        Integer counterSignOpposition = (Integer) delegateTask.getVariable("counterSign_opposition");//反对票数
        if(counterSignOpposition==null) {
            counterSignOpposition = 0;
        }
        Integer counterSignWaiver = (Integer) delegateTask.getVariable("counterSign_waiver");//弃权票数
        if(counterSignWaiver==null) {
            counterSignWaiver = 0;
        }
        String approved = (String) delegateTask.getVariable("approved");
        Integer value = 0;//默认弃权
        if("true".equalsIgnoreCase(approved)){
            counterSignAgree++;
        }else if("false".equalsIgnoreCase(approved)){
            counterSignOpposition++;
        }else {
            counterSignWaiver++;
        }

        delegateTask.setVariable("counterSign_agree", counterSignAgree);
        delegateTask.setVariable("counterSign_opposition", counterSignOpposition);
        delegateTask.setVariable("counterSign_waiver", counterSignWaiver);
        //完成会签的次数
        Integer completeCounter=(Integer)delegateTask.getVariable("nrOfCompletedInstances");
        //总循环次数
        Integer instanceOfNumbers=(Integer)delegateTask.getVariable("nrOfInstances");
        //当前处于激活状态的任务实例
        Integer nrOfActiveInstances=(Integer)delegateTask.getVariable("nrOfActiveInstances");
//        Boolean  approveResult = null;
        if(nrOfActiveInstances==1){//会签最后一个执行人
            int counterDecision=100;
            try {

                ExecutionEntity taskEntity = (ExecutionEntity) delegateTask.getExecution();
                String actTaskDefKey = taskEntity.getActivityId();
                String actProcessDefinitionId = delegateTask.getProcessDefinitionId();
                ProcessInstance instance = taskEntity.getProcessInstance();
                String businessId = instance.getBusinessKey();
                FlowDefVersion flowDefVersion = flowDefVersionDao.findByActDefId(actProcessDefinitionId);
                String flowDefJson = flowDefVersion.getDefJson();
                JSONObject defObj = JSONObject.fromObject(flowDefJson);
                Definition definition = (Definition) JSONObject.toBean(defObj, Definition.class);
//        net.sf.json.JSONObject currentNode = definition.getProcess().getNodes().getJSONObject(currentTaskId);
                net.sf.json.JSONObject currentNode = definition.getProcess().getNodes().getJSONObject(actTaskDefKey);
                counterDecision = currentNode.getJSONObject("nodeConfig").getJSONObject("normal").getInt("counterDecision");
            }catch (Exception e){
                logger.error(e.getMessage());
            }
//            if(counterDecision<=((counterSignAgree/instanceOfNumbers)*100)){//获取通过节点
//                approveResult = true;}
//             else{
//                approveResult=false;
//            }
//            delegateTask.setVariable("approveResult",approveResult);
        }
        System.out.println("success call commonCounterSignCompleteListener------------");

    }
}
