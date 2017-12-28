package com.ecmp.flow.listener;

import com.ecmp.config.util.ApiClient;
import com.ecmp.context.ContextUtil;
import com.ecmp.flow.common.util.Constants;
import com.ecmp.flow.constant.FlowStatus;
import com.ecmp.flow.dao.FlowInstanceDao;
import com.ecmp.flow.entity.AppModule;
import com.ecmp.flow.entity.BusinessModel;
import com.ecmp.flow.entity.FlowInstance;
import com.ecmp.flow.util.FlowException;
import com.ecmp.flow.util.FlowListenerTool;
import com.ecmp.flow.vo.FlowOperateResult;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.ExecutionListener;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.runtime.ProcessInstance;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.ws.rs.core.GenericType;
import java.lang.reflect.InvocationTargetException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


/**
 * *************************************************************************************************
 * <p/>
 * 实现功能： 启动完成监听器
 * <p>
 * ------------------------------------------------------------------------------------------------
 * 版本          变更时间             变更人                     变更原因
 * ------------------------------------------------------------------------------------------------
 * 1.0.00      2017/5/24 13:40      谭军(tanjun)                    新建
 * <p/>
 * *************************************************************************************************
 */
//@Component(value="endEventCompleteListener")
public class EndEventCompleteListener implements ExecutionListener {

    private final Logger logger = LoggerFactory.getLogger(EndEventCompleteListener.class);
	public EndEventCompleteListener(){
	}
    private static final long serialVersionUID = 1L;

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private FlowInstanceDao flowInstanceDao;

    @Autowired
    private FlowListenerTool flowListenerTool;

    @Transactional( propagation= Propagation.REQUIRED)
    public void notify(DelegateExecution delegateTask) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, ClassNotFoundException, InstantiationException{
        ExecutionEntity taskEntity = (ExecutionEntity) delegateTask;
        Map<String,Object> variables = delegateTask.getVariables();
        ProcessInstance processInstance  = taskEntity.getProcessInstance();
        FlowInstance  flowInstance = flowInstanceDao.findByActInstanceId(processInstance.getId());
        String deleteReason = ((ExecutionEntity) delegateTask).getDeleteReason();
        int endSign = 0;
        if(StringUtils.isNotEmpty(deleteReason)){
            if("10035".equals(deleteReason)){//"被管理员强制终止流程
                endSign = 2;
            }else if( "10036".equals(deleteReason)) {//被发起人终止流程
                endSign = 1;
            }
        }
        if(flowInstance==null){
             throw new FlowException(ContextUtil.getMessage("10002"));//流程实例不存在
        }else {
            if (processInstance.isEnded()) {//针对启动时只有服务任务这种情况（即启动就结束）
                BusinessModel businessModel = flowInstance.getFlowDefVersion().getFlowDefination().getFlowType().getBusinessModel();
                AppModule appModule = businessModel.getAppModule();
                FlowOperateResult callBeforeEndResult = flowListenerTool.callBeforeEnd(processInstance.getBusinessKey(), flowInstance.getFlowDefVersion(),endSign);
                if(callBeforeEndResult!=null && callBeforeEndResult.isSuccess()!=true){
                    String message = "BusinessId="+flowInstance.getBusinessId()
                            +",FlowDefVersion.id="+flowInstance.getFlowDefVersion().getId()
                            +",appModule.code="+appModule.getCode()
                            +",Check the error before the end of the process and return the message :"+callBeforeEndResult.getMessage();
                    logger.info(message);
                    throw new FlowException(message);
                }
                flowInstance.setEnded(true);
                flowInstance.setEndDate(new Date());
                flowInstanceDao.save(flowInstance);
                //回写状态
                FlowInstance flowInstanceP = flowInstance.getParent();
                Map<String, Object> params = new HashMap<String,Object>();;
                params.put(Constants.BUSINESS_MODEL_CODE,businessModel.getClassName());
                params.put(Constants.ID,flowInstance.getBusinessId());
                params.put(Constants.STATUS,FlowStatus.COMPLETED);
                String url = appModule.getApiBaseAddress()+"/"+businessModel.getConditonStatusRest();
                Boolean result = ApiClient.postViaProxyReturnResult(url,new GenericType<Boolean>() {}, params);
                if(!result){
                    throw new FlowException(ContextUtil.getMessage("10049"));//流程结束-调用重置表单状态失败!
                }
                if(flowInstanceP!=null){
                    ExecutionEntity parent = taskEntity.getSuperExecution();
                    if(parent != null){
                        ProcessInstance   parentProcessInstance = parent.getProcessInstance();
                        Map<String,Object> variablesParent = runtimeService.getVariables(parent.getId());
                        String superExecutionId = processInstance.getSuperExecutionId();
                        variablesParent.putAll(variables);
                        variablesParent.put(processInstance.getId() + Constants.SUPER_EXECUTION_ID,superExecutionId);
                        runtimeService.setVariables(parentProcessInstance.getId(),variablesParent);
                    }
                }
                try {
                    String businessId = delegateTask.getProcessBusinessKey();
                    flowListenerTool.callEndService(businessId, flowInstance.getFlowDefVersion(),endSign);
                }catch (Exception e){
                    logger.error(e.getMessage());
                }
            }
        }
    }
}
