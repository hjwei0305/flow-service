package com.ecmp.flow.listener;

import com.ecmp.context.ContextUtil;
import com.ecmp.flow.common.util.Constants;
import com.ecmp.flow.constant.FlowStatus;
import com.ecmp.flow.dao.FlowInstanceDao;
import com.ecmp.flow.entity.BusinessModel;
import com.ecmp.flow.entity.FlowInstance;
import com.ecmp.flow.service.DefaultFlowBaseService;
import com.ecmp.flow.util.ExpressionUtil;
import com.ecmp.flow.util.FlowException;
import com.ecmp.flow.util.FlowListenerTool;
import com.ecmp.flow.vo.FlowOperateResult;
import com.ecmp.vo.ResponseData;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.ExecutionListener;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.runtime.ProcessInstance;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Map;



public class EndEventCompleteListener implements ExecutionListener {

    public EndEventCompleteListener() {
    }

    private static final long serialVersionUID = 1L;

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private FlowInstanceDao flowInstanceDao;

    @Autowired
    private FlowListenerTool flowListenerTool;

    @Autowired
    private DefaultFlowBaseService defaultFlowBaseService;

    @Transactional(propagation = Propagation.REQUIRED)
    public void notify(DelegateExecution delegateTask) {
        ExecutionEntity taskEntity = (ExecutionEntity) delegateTask;
        Map<String, Object> variables = delegateTask.getVariables();
        ProcessInstance processInstance = taskEntity.getProcessInstance();
        FlowInstance flowInstance = flowInstanceDao.findByActInstanceId(processInstance.getId());
        String deleteReason = ((ExecutionEntity) delegateTask).getDeleteReason();
        int endSign = 0;
        String endCode = taskEntity.getActivityId();
        if (endCode.contains("TerminateEndEvent")) { //TerminateEndEvent：强制终止   EndEvent:终止
            endSign = 3;
        }
        if (StringUtils.isNotEmpty(deleteReason)) {
            if ("10035".equals(deleteReason)) {//"被管理员强制终止流程
                endSign = 2;
            } else if ("10036".equals(deleteReason)) {//被发起人终止流程
                endSign = 1;
            }
        }
        if (flowInstance == null) {
            throw new FlowException(ContextUtil.getMessage("10002"));//流程实例不存在
        } else {
            if (processInstance.isEnded()) {//针对启动时只有服务任务这种情况（即启动就结束）
                Boolean solidifyFlow = flowInstance.getFlowDefVersion().getSolidifyFlow();
                BusinessModel businessModel = flowInstance.getFlowDefVersion().getFlowDefination().getFlowType().getBusinessModel();
                FlowOperateResult callBeforeEndResult = flowListenerTool.callBeforeEnd(processInstance.getBusinessKey(), flowInstance.getFlowDefVersion(), endSign, variables);
                if (callBeforeEndResult != null && !callBeforeEndResult.isSuccess()) {
                    throw new FlowException(callBeforeEndResult.getMessage());
                }
                flowInstance.setEnded(true);
                flowInstance.setEndDate(new Date());
                flowInstanceDao.save(flowInstance);
                //回写状态
                FlowInstance flowInstanceP = flowInstance.getParent();
                ResponseData result = ExpressionUtil.resetState(businessModel, flowInstance.getBusinessId(), FlowStatus.COMPLETED);
                if (!result.getSuccess()) {
                    throw new FlowException(ContextUtil.getMessage("10360", result.getMessage()));
                }
                if (flowInstanceP != null) {
                    ExecutionEntity parent = taskEntity.getSuperExecution();
                    if (parent != null) {
                        ProcessInstance parentProcessInstance = parent.getProcessInstance();
                        Map<String, Object> variablesParent = runtimeService.getVariables(parent.getId());
                        String superExecutionId = processInstance.getSuperExecutionId();
                        variablesParent.putAll(variables);
                        variablesParent.put(processInstance.getId() + Constants.SUPER_EXECUTION_ID, superExecutionId);
                        runtimeService.setVariables(parentProcessInstance.getId(), variablesParent);
                    }
                }

                String businessId = delegateTask.getProcessBusinessKey();
                FlowOperateResult callEndResult;
                try {
                    callEndResult = flowListenerTool.callEndService(businessId, flowInstance.getFlowDefVersion(), endSign, variables);
                } catch (Exception e) {
                    //轮询修改状态为：流程中
                    ExpressionUtil.pollingResetState(businessModel, flowInstance.getBusinessId(), FlowStatus.INPROCESS);
                    throw e;
                }

                if (callEndResult != null && !callEndResult.isSuccess()) {
                    //轮询修改状态为：流程中
                    ExpressionUtil.pollingResetState(businessModel, flowInstance.getBusinessId(), FlowStatus.INPROCESS);
                    throw new FlowException(callEndResult.getMessage());
                }else{
                    //固化流程的结束并抄送
                    if (solidifyFlow) {
                        defaultFlowBaseService.checkEndAndCopy(flowInstance, endCode);
                    }
                }
            }
        }
    }
}
