package com.ecmp.flow.listener;

import com.ecmp.flow.dao.FlowDefVersionDao;
import com.ecmp.flow.dao.FlowDefinationDao;
import com.ecmp.flow.dao.FlowTaskDao;
import com.ecmp.flow.entity.FlowDefVersion;
import com.ecmp.flow.util.ServiceCallUtil;
import com.ecmp.flow.vo.FlowOpreateResult;
import com.ecmp.flow.vo.bpmn.Definition;
import com.ecmp.util.JsonUtils;
import net.sf.json.JSONObject;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.ExecutionListener;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.pvm.process.TransitionImpl;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;


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
public class CommonUserTaskCreateListener implements ExecutionListener {

    private final Logger logger = LoggerFactory.getLogger(CommonUserTaskCreateListener.class);
	public CommonUserTaskCreateListener(){
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
    public void notify(DelegateExecution delegateTask) {
        try {
            String actTaskDefKey = delegateTask.getCurrentActivityId();
            String actProcessDefinitionId = delegateTask.getProcessDefinitionId();
            String businessId =delegateTask.getProcessBusinessKey();
            FlowDefVersion flowDefVersion = flowDefVersionDao.findByActDefId(actProcessDefinitionId);
            String flowDefJson = flowDefVersion.getDefJson();
            JSONObject defObj = JSONObject.fromObject(flowDefJson);
            Definition definition = (Definition) JSONObject.toBean(defObj, Definition.class);
            net.sf.json.JSONObject currentNode = definition.getProcess().getNodes().getJSONObject(actTaskDefKey);
            net.sf.json.JSONObject event = currentNode.getJSONObject("nodeConfig").getJSONObject("event");
            if (event != null) {
                String beforeExcuteServiceId = (String) event.get("beforeExcuteServiceId");
                boolean async = true;//默认为异步
                String beforeAsyncStr = event.get("beforeAsync")+"";
                if("false".equalsIgnoreCase(beforeAsyncStr)){
                    async=false;
                }
                if (!StringUtils.isEmpty(beforeExcuteServiceId)) {
                    String multiInstance =  (String)((ExecutionEntity) delegateTask).getActivity().getProperty("multiInstance");
                    Boolean isMmultiInstance = StringUtils.isNotEmpty(multiInstance);
                    if(isMmultiInstance){//控制会签任务、串行任务、并行任务只达到节点时只触发一次到达事件
                        TransitionImpl transiton =  ((ExecutionEntity) delegateTask).getTransition();
                        if(transiton==null){
                            return;
                        }
                    }
                    try {
                        Map<String,Object> tempV = delegateTask.getVariables();
                        String param = JsonUtils.toJson(tempV);
                        if(async){
                            new Thread(new Runnable() {//模拟异步
                                @Override
                                public void run() {
                                    ServiceCallUtil.callService(beforeExcuteServiceId, businessId, param);
                                }
                            }).start();
                        }else {
                            Object result = ServiceCallUtil.callService(beforeExcuteServiceId, businessId, param);
                            FlowOpreateResult flowOpreateResult = (FlowOpreateResult) result;
                            if(true!=flowOpreateResult.isSuccess()){
                                throw new RuntimeException("执行逻辑失败，"+flowOpreateResult.getMessage());
                            }
                        }
                    }catch (Exception e){
                        logger.error(e.getMessage());
                        if(!async){
                            throw e;
                        }
                    }
                }
            }
        }catch(Exception e){
            logger.error(e.getMessage());
            throw e;
        }
    }
}
