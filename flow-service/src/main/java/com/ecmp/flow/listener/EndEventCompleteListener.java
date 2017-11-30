package com.ecmp.flow.listener;

import com.ecmp.config.util.ApiClient;
import com.ecmp.flow.constant.FlowStatus;
import com.ecmp.flow.dao.*;
import com.ecmp.flow.entity.*;
import com.ecmp.flow.util.FlowException;
import com.ecmp.flow.vo.FlowInvokeParams;
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
import org.springframework.stereotype.Component;
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
@Component(value="endEventCompleteListener")
public class EndEventCompleteListener implements ExecutionListener {

    private final Logger logger = LoggerFactory.getLogger(EndEventCompleteListener.class);
	public EndEventCompleteListener(){
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

    @Autowired
    private FlowInstanceDao flowInstanceDao;

    @Autowired
    private FlowServiceUrlDao flowServiceUrlDao;

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
             throw new FlowException("流程实例不存在！");
        }else {
            if (processInstance.isEnded()) {//针对启动时只有服务任务这种情况（即启动就结束）
                BusinessModel businessModel = flowInstance.getFlowDefVersion().getFlowDefination().getFlowType().getBusinessModel();
                AppModule appModule = businessModel.getAppModule();
                FlowOperateResult callBeforeEndResult = callBeforeEnd(processInstance.getBusinessKey(), flowInstance.getFlowDefVersion(),endSign);
                if(callBeforeEndResult!=null && callBeforeEndResult.isSuccess()!=true){
                    String message = "单据id="+flowInstance.getBusinessId()
                            +",流程版本id="+flowInstance.getFlowDefVersion()
                            +",业务对象="+appModule.getCode()
                            +",流程结束前检查出错，返回消息:"+callBeforeEndResult.getMessage();
                    logger.info(message);
                    throw new FlowException(message);
                }
                flowInstance.setEnded(true);
                flowInstance.setEndDate(new Date());
                flowInstanceDao.save(flowInstance);

                //回写状态
                FlowInstance flowInstanceP = flowInstance.getParent();

                Map<String, Object> params = new HashMap<String,Object>();;
                params.put("businessModelCode",businessModel.getClassName());
                params.put("id",flowInstance.getBusinessId());
                params.put("status",FlowStatus.COMPLETED);
                String url = appModule.getApiBaseAddress()+"/"+businessModel.getConditonStatusRest();
                Boolean result = ApiClient.postViaProxyReturnResult(url,new GenericType<Boolean>() {}, params);
                if(!result){
                    throw new FlowException("调用重置表单流程结束状态失败");
                }
                if(flowInstanceP!=null){
                    ExecutionEntity parent = taskEntity.getSuperExecution();
                    if(parent != null){
                        ProcessInstance   parentProcessInstance = parent.getProcessInstance();
                        Map<String,Object> variablesParent = runtimeService.getVariables(parent.getId());
                        String superExecutionId = processInstance.getSuperExecutionId();
                        variablesParent.putAll(variables);
                        variablesParent.put(processInstance.getId()+"_superExecutionId",superExecutionId);
                        runtimeService.setVariables(parentProcessInstance.getId(),variablesParent);
                    }
                }
//                flowTaskDao.deleteByFlowInstanceId(flowInstance.getId());//针对终止结束时，删除所有待办
                try {
                    String businessId = delegateTask.getProcessBusinessKey();
                    this.callEndService(businessId, flowInstance.getFlowDefVersion(),endSign);
                }catch (Exception e){
                    logger.error(e.getMessage());
                }
            }
        }
    }

    private void callEndService( String businessKey,FlowDefVersion flowDefVersion,int endSign){
        if(flowDefVersion!=null && StringUtils.isNotEmpty(businessKey)){
            String endCallServiceUrlId = flowDefVersion.getEndCallServiceUrlId();
            if(StringUtils.isNotEmpty(endCallServiceUrlId)){
                FlowServiceUrl flowServiceUrl = flowServiceUrlDao.findOne(endCallServiceUrlId);
                String checkUrl = flowServiceUrl.getUrl();
                if(StringUtils.isNotEmpty(checkUrl)){
                String baseUrl= flowDefVersion.getFlowDefination().getFlowType().getBusinessModel().getAppModule().getApiBaseAddress();
                String endCallServiceUrlPath = baseUrl+checkUrl;
                    FlowInvokeParams flowInvokeParams = new FlowInvokeParams();
                    flowInvokeParams.setId(businessKey);
                    Map<String,String> params = new HashMap<String,String>();
                    params.put("endSign",endSign+"");
                    flowInvokeParams.setParams(params);
                new Thread(new Runnable() {//模拟异步
                    @Override
                    public void run() {
                        FlowOperateResult flowOperateResult =   ApiClient.postViaProxyReturnResult(endCallServiceUrlPath,new GenericType<FlowOperateResult>() {},flowInvokeParams);
                        logger.info(flowOperateResult.toString());
                    }
                }).start();
            }
         }
        }
    }

    /**
     *  流程即将结束时调用服务检查，如果失败流程结束失败，同步
     * @param businessKey
     * @param flowDefVersion
     * @return
     */
    private FlowOperateResult callBeforeEnd(String businessKey, FlowDefVersion flowDefVersion,int endSign){
        FlowOperateResult result = null;
        if(flowDefVersion!=null && StringUtils.isNotEmpty(businessKey)){
            String endBeforeCallServiceUrlId = flowDefVersion.getEndBeforeCallServiceUrlId();

            if(StringUtils.isNotEmpty(endBeforeCallServiceUrlId)){
                FlowServiceUrl flowServiceUrl = flowServiceUrlDao.findOne(endBeforeCallServiceUrlId);
                String checkUrl = flowServiceUrl.getUrl();
                if(StringUtils.isNotEmpty(checkUrl)){
                    String baseUrl= flowDefVersion.getFlowDefination().getFlowType().getBusinessModel().getAppModule().getApiBaseAddress();
                    String checkUrlPath = baseUrl+checkUrl;
                    FlowInvokeParams flowInvokeParams = new FlowInvokeParams();
                    flowInvokeParams.setId(businessKey);
                    Map<String,String> params = new HashMap<String,String>();
                    params.put("endSign",endSign+"");
                    flowInvokeParams.setParams(params);
                    result = ApiClient.postViaProxyReturnResult(checkUrlPath,new GenericType<FlowOperateResult>() {},flowInvokeParams);
                }
            }
        }
        return result;
    }
}
