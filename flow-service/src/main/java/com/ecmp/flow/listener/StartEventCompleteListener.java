package com.ecmp.flow.listener;

import com.ecmp.config.util.ApiClient;
import com.ecmp.flow.constant.FlowStatus;
import com.ecmp.flow.dao.*;
import com.ecmp.flow.entity.*;
import com.ecmp.flow.util.FlowException;
import com.ecmp.flow.vo.FlowOperateResult;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.ExecutionListener;
import org.activiti.engine.impl.RepositoryServiceImpl;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.runtime.ProcessInstance;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.ws.rs.core.GenericType;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
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
@Component(value="startEventCompleteListener")
public class StartEventCompleteListener implements ExecutionListener {

    private final Logger logger = LoggerFactory.getLogger(StartEventCompleteListener.class);
	public StartEventCompleteListener(){
	}
    private static final long serialVersionUID = 1L;

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private RepositoryService repositoryService;

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
    public void notify(DelegateExecution delegateTask) {
        ExecutionEntity taskEntity = (ExecutionEntity) delegateTask;
        Map<String,Object> variables = delegateTask.getVariables();
        ProcessInstance processInstance  = taskEntity.getProcessInstance();
        ProcessInstance parentProcessInstance = null;
        ExecutionEntity son = taskEntity.getSubProcessInstance();
        ExecutionEntity parent = taskEntity.getSuperExecution();
        String currentBusinessId = null;
        String callActivityPath = null;
        if(parent != null){
            StringBuffer sonBusinessVNameBuff = new StringBuffer();
            ExecutionEntity parentTemp = parent;
            while (parentTemp!=null){
                parentProcessInstance = parentTemp.getProcessInstance();
                Map<String,Object> variablesParent = runtimeService.getVariables(parentTemp.getId());
                variables.putAll(variablesParent);
                delegateTask.setVariables(variables);
                String parentDefinitionKey =  parentProcessInstance.getProcessDefinitionKey();
                if(StringUtils.isEmpty(parentDefinitionKey)){
                    // 取得流程定义
                    ProcessDefinitionEntity definition = (ProcessDefinitionEntity) ((RepositoryServiceImpl) repositoryService)
                            .getDeployedProcessDefinition( parentProcessInstance.getProcessDefinitionId());                   ;
                    parentDefinitionKey = definition.getKey();
                }
                sonBusinessVNameBuff.insert(0,"/"+parentDefinitionKey+"/"+parentTemp.getActivityId());
                parentTemp = ((ExecutionEntity)parentProcessInstance).getSuperExecution();
            }
            callActivityPath = sonBusinessVNameBuff.toString();
            parentProcessInstance = parent.getProcessInstance();
            Map<String,Object> variablesParent = runtimeService.getVariables(parent.getId());
            variables.putAll(variablesParent);
            delegateTask.setVariables(variables);
            String parentDefinitionKey =  parentProcessInstance.getProcessDefinitionKey();
            if(StringUtils.isEmpty(parentDefinitionKey)){
                // 取得流程定义
                ProcessDefinitionEntity definition = (ProcessDefinitionEntity) ((RepositoryServiceImpl) repositoryService)
                        .getDeployedProcessDefinition( parentProcessInstance.getProcessDefinitionId());                   ;
                parentDefinitionKey = definition.getKey();
            }
            FlowDefination flowDefinationParent = flowDefinationDao.findByDefKey(parentDefinitionKey);
            String definitionKey =  processInstance.getProcessDefinitionKey();
            FlowDefination flowDefination = flowDefinationDao.findByDefKey(definitionKey);
            String parentBusinessModelCode = flowDefinationParent.getFlowType().getBusinessModel().getClassName();
            String sonBusinessModelCode = flowDefination.getFlowType().getBusinessModel().getClassName();
//            //父流程业务实体代码+callActivtiy的key+子流程key+'_sonBusinessId'
//            String sonBusinessVName = parentBusinessModelCode+"_"+parent.getActivityId()+"_"+definitionKey+"_sonBusinessId";
            sonBusinessVNameBuff.append("/"+definitionKey);
            currentBusinessId =(String )delegateTask.getVariable(sonBusinessVNameBuff.toString());
            List<String> userVarNameList = (List)delegateTask.getVariable(sonBusinessVNameBuff+"_sonProcessSelectNodeUserV");
            if(userVarNameList!=null && !userVarNameList.isEmpty()){
                for(String userVarName :userVarNameList){
                   Object userValue = delegateTask.getVariable(sonBusinessVNameBuff+"/"+userVarName);
                    delegateTask.setVariable(userVarName,userValue);
//                    delegateTask.removeVariable(callActivityPath+"/"+userVarName);
                }
//                variablesParent.remove(callActivityPath+"_sonProcessSelectNodeUserV");
//                delegateTask.removeVariable(callActivityPath+"_sonProcessSelectNodeUserV");
            }
            if(StringUtils.isEmpty(currentBusinessId)){
                if(parentBusinessModelCode.equals(sonBusinessModelCode)){//非跨业务实体子流程
                    //设置子流程businessKey
                    String  parentBusinessKey = parentProcessInstance.getBusinessKey();
                    runtimeService.updateBusinessKey(processInstance.getId(),parentBusinessKey);
                    currentBusinessId = parentBusinessKey;
                }else{//跨业务实体子流程,必须指定子流程关联单据id
                        throw new FlowException("子流程关联的单据找不到！");
                }
            }else {
                    runtimeService.updateBusinessKey(processInstance.getId(),currentBusinessId);
            }

        }
        FlowInstance  flowInstance = flowInstanceDao.findByActInstanceId(processInstance.getId());
        if(flowInstance==null){
            flowInstance = new FlowInstance();
            flowInstance.setBusinessId(processInstance.getBusinessKey());
            String workCaption = variables.get("workCaption")+"";//工作说明
            flowInstance.setBusinessModelRemark(workCaption);
            String businessCode = variables.get("businessCode")+"";
            flowInstance.setBusinessCode(businessCode);
            String businessName = variables.get("name")+"";//业务单据名称
            flowInstance.setBusinessName(businessName);
            String flowDefVersionId= (String) variables.get("flowDefVersionId");
            FlowDefVersion flowDefVersion = flowDefVersionDao.findOne(flowDefVersionId);
            if(flowDefVersion==null){
                throw new FlowException("流程版本找不到！");
            }
            flowInstance.setFlowDefVersion(flowDefVersion);
            flowInstance.setStartDate(new Date());
            flowInstance.setFlowName(flowDefVersion.getName());
            flowInstance.setActInstanceId(processInstance.getId());
            if(parentProcessInstance !=null){
                FlowInstance flowInstanceP = flowInstanceDao.findByActInstanceId(parentProcessInstance.getId());
                String actDefinitionKey = processInstance.getProcessDefinitionKey();
               List<FlowDefVersion> flowDefVersionList = flowDefVersionDao.findByKeyActivate(actDefinitionKey);
                if(flowDefVersionList == null || flowDefVersionList.isEmpty()){
                    throw new FlowException("子流程的流程版本找不到！");
                }
                flowDefVersion = flowDefVersionList.get(0);
                flowInstance.setFlowDefVersion(flowDefVersion);
                flowInstance.setBusinessId(currentBusinessId);
                flowInstance.setCallActivityPath(callActivityPath);
                flowInstance.setParent(flowInstanceP);
            }
            flowInstanceDao.save(flowInstance);
        }



        BusinessModel businessModel = flowInstance.getFlowDefVersion().getFlowDefination().getFlowType().getBusinessModel();
        AppModule appModule = businessModel.getAppModule();

        FlowOperateResult callAfterStartResult = callAfterStart(flowInstance.getBusinessId(),flowInstance.getFlowDefVersion());
        if(callAfterStartResult!=null && callAfterStartResult.isSuccess()!=true){
            String message = "单据id="+flowInstance.getBusinessId()
                    +",流程版本id="+flowInstance.getFlowDefVersion()
                    +",业务对象="+appModule.getCode()
                    +",流程启动调用服务失败，返回消息:"+callAfterStartResult.getMessage();
            logger.info(message);
            throw new FlowException(message);
        }

        Map<String, Object> params = new HashMap<String,Object>();;
        params.put("businessModelCode",businessModel.getClassName());
        params.put("id",flowInstance.getBusinessId());
        params.put("status", FlowStatus.INPROCESS);
        String url = appModule.getApiBaseAddress()+"/"+businessModel.getConditonStatusRest();
        Boolean result = ApiClient.postViaProxyReturnResult(url,new GenericType<Boolean>() {}, params);
        if(!result){
            throw new FlowException("调用重置表单进入流程中状态失败");
        }
    }

    /**
     *  流程启动结束后调用调用服务
     * @param businessKey
     * @param flowDefVersion
     * @return
     */
    private FlowOperateResult callAfterStart( String businessKey,FlowDefVersion flowDefVersion){
        FlowOperateResult result = null;
        if(flowDefVersion!=null && StringUtils.isNotEmpty(businessKey)){
            String afterStartServiceId = flowDefVersion.getAfterStartServiceId();
            Boolean afterStartServiceAync = flowDefVersion.getAfterStartServiceAync();

            if(StringUtils.isNotEmpty(afterStartServiceId)){
                FlowServiceUrl flowServiceUrl = flowServiceUrlDao.findOne(afterStartServiceId);
                String checkUrl = flowServiceUrl.getUrl();
                if(StringUtils.isNotEmpty(checkUrl)){
                    String baseUrl= flowDefVersion.getFlowDefination().getFlowType().getBusinessModel().getAppModule().getApiBaseAddress();
                    String checkUrlPath = baseUrl+checkUrl;
                    Map<String, Object> params = new HashMap<>();
                    params.put("id",businessKey);
                    if(afterStartServiceAync == true){
                        new Thread(new Runnable() {//模拟异步
                            @Override
                            public void run() {
                                FlowOperateResult resultAync =  ApiClient.getEntityViaProxy(checkUrlPath,new GenericType<FlowOperateResult>() {},params);
                                logger.info(resultAync.toString());
                            }
                        }).start();
                    }else {
                        result = ApiClient.getEntityViaProxy(checkUrlPath,new GenericType<FlowOperateResult>() {},params);
                        logger.info(result.toString());
                    }
                }
            }
        }
        return result;
    }
}
