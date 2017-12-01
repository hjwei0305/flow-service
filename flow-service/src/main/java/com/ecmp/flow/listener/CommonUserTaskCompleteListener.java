package com.ecmp.flow.listener;

import com.ecmp.flow.dao.FlowDefVersionDao;
import com.ecmp.flow.dao.FlowDefinationDao;
import com.ecmp.flow.dao.FlowTaskDao;
import com.ecmp.flow.entity.FlowDefVersion;
import com.ecmp.flow.util.FlowException;
import com.ecmp.flow.util.ServiceCallUtil;
import com.ecmp.flow.vo.FlowOperateResult;
import com.ecmp.flow.vo.bpmn.Definition;
import com.ecmp.log.util.LogUtil;
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

import java.util.Map;


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
//@Component(value="commonUserTaskCompleteListener")
public class CommonUserTaskCompleteListener implements ExecutionListener {

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private FlowTaskDao flowTaskDao;

    @Autowired
    private FlowDefVersionDao flowDefVersionDao;

    @Autowired
    private FlowDefinationDao flowDefinationDao;


    private final Logger logger = LoggerFactory.getLogger(CommonUserTaskCompleteListener.class);

	public CommonUserTaskCompleteListener(){
	}
    private static final long serialVersionUID = 1L;

    public void notify(DelegateExecution delegateTask) {
        try {
            String deleteReason = ((ExecutionEntity) delegateTask).getDeleteReason();
            if(StringUtils.isNotEmpty(deleteReason)){
                return;
            }
            String actTaskDefKey = delegateTask.getCurrentActivityId();
            String actProcessDefinitionId = delegateTask.getProcessDefinitionId();
            String businessId =delegateTask.getProcessBusinessKey();
            FlowDefVersion flowDefVersion = flowDefVersionDao.findByActDefId(actProcessDefinitionId);
            String flowDefJson = flowDefVersion.getDefJson();
            JSONObject defObj = JSONObject.fromObject(flowDefJson);
            Definition definition = (Definition) JSONObject.toBean(defObj, Definition.class);
            net.sf.json.JSONObject currentNode = definition.getProcess().getNodes().getJSONObject(actTaskDefKey);
            net.sf.json.JSONObject event =null;
            if(currentNode.has("nodeConfig")&&currentNode.getJSONObject("nodeConfig").has("event")){
                event = currentNode.getJSONObject("nodeConfig").getJSONObject("event");
            }
            if (event != null) {
                String afterExcuteServiceIdTemp = null;
                if(event.has("afterExcuteServiceId")){
                    afterExcuteServiceIdTemp = event.getString("afterExcuteServiceId");
                }
                String afterExcuteServiceId = afterExcuteServiceIdTemp;
                boolean async = false;//默认为同步
                String afterAsyncStr = null;
                if(event.has("afterAsync")){
                    afterAsyncStr =  event.getString("afterAsync");
                }

                if("true".equalsIgnoreCase(afterAsyncStr)){
                    async=true;
                }
                if (!StringUtils.isEmpty(afterExcuteServiceId)) {
                    String multiInstance =  (String)((ExecutionEntity) delegateTask).getActivity().getProperty("multiInstance");
                    Boolean isMmultiInstance = StringUtils.isNotEmpty(multiInstance);
                    if(isMmultiInstance){//控制会签任务、串行任务、并行任务 所有执行完成时只触发一次完成事件（可能后续需要扩展控制）
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
                                    ServiceCallUtil.callService(afterExcuteServiceId, businessId, param);
                                }
                            }).start();
                        }else {
                            Object result = ServiceCallUtil.callService(afterExcuteServiceId, businessId, param);
                            FlowOperateResult flowOpreateResult = (FlowOperateResult) result;
                            if(true!=flowOpreateResult.isSuccess()){
                                throw new FlowException("执行逻辑失败，"+flowOpreateResult.getMessage());
                            }
                        }
                    }catch (Exception e){
                        logger.error(e.getMessage());
                        if(!async){
                              throw new FlowException(e.getMessage());
                        }
                        new Thread(new Runnable() {//模拟异步
                            @Override
                            public void run() {
                                LogUtil.addExceptionLog(e.getMessage());
                            }
                        }).start();
                    }
//                    if(!async){
//                        throw new FlowException("自定义异常信息");
//                    }
                }
            }
        }catch (Exception e){
            logger.error(e.getMessage());
            new Thread(new Runnable() {//模拟异步
                @Override
                public void run() {
                    LogUtil.addExceptionLog(e.getMessage());
                }
            }).start();
            throw new FlowException(e.getMessage());
        }
    }
}
