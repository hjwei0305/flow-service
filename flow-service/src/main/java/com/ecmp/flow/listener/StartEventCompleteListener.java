package com.ecmp.flow.listener;

import com.ecmp.config.util.ApiClient;
import com.ecmp.context.ContextUtil;
import com.ecmp.flow.common.util.Constants;
import com.ecmp.flow.constant.FlowStatus;
import com.ecmp.flow.dao.*;
import com.ecmp.flow.dao.util.PageUrlUtil;
import com.ecmp.flow.entity.*;
import com.ecmp.flow.util.ExpressionUtil;
import com.ecmp.flow.util.FlowCommonUtil;
import com.ecmp.flow.util.FlowException;
import com.ecmp.flow.vo.FlowInvokeParams;
import com.ecmp.flow.vo.FlowOperateResult;
import com.ecmp.log.util.LogUtil;
import com.ecmp.util.JsonUtils;
import com.ecmp.vo.ResponseData;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.ExecutionListener;
import org.activiti.engine.impl.RepositoryServiceImpl;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.runtime.ProcessInstance;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
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
public class StartEventCompleteListener implements ExecutionListener {

    public StartEventCompleteListener() {
    }

    private static final long serialVersionUID = 1L;

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private FlowCommonUtil flowCommonUtil;

    @Autowired
    private FlowDefVersionDao flowDefVersionDao;

    @Autowired
    private FlowDefinationDao flowDefinationDao;

    @Autowired
    private FlowInstanceDao flowInstanceDao;

    @Autowired
    private FlowServiceUrlDao flowServiceUrlDao;

    @Transactional(propagation = Propagation.REQUIRED)
    public void notify(DelegateExecution delegateTask) {
        ExecutionEntity taskEntity = (ExecutionEntity) delegateTask;
        Map<String, Object> variables = delegateTask.getVariables();
        ProcessInstance processInstance = taskEntity.getProcessInstance();
        ProcessInstance parentProcessInstance = null;
        ExecutionEntity parent = taskEntity.getSuperExecution();
        String currentBusinessId = null;
        String callActivityPath = null;
        if (parent != null) {
            StringBuffer sonBusinessVNameBuff = new StringBuffer();
            ExecutionEntity parentTemp = parent;
            while (parentTemp != null) {
                parentProcessInstance = parentTemp.getProcessInstance();
                Map<String, Object> variablesParent = runtimeService.getVariables(parentTemp.getId());
                variables.putAll(variablesParent);
                delegateTask.setVariables(variables);
                String parentDefinitionKey = parentProcessInstance.getProcessDefinitionKey();
                if (StringUtils.isEmpty(parentDefinitionKey)) {
                    // 取得流程定义
                    ProcessDefinitionEntity definition = (ProcessDefinitionEntity) ((RepositoryServiceImpl) repositoryService)
                            .getDeployedProcessDefinition(parentProcessInstance.getProcessDefinitionId());
                    parentDefinitionKey = definition.getKey();
                }
                sonBusinessVNameBuff.insert(0, "/" + parentDefinitionKey + "/" + parentTemp.getActivityId());
                parentTemp = ((ExecutionEntity) parentProcessInstance).getSuperExecution();
            }
            callActivityPath = sonBusinessVNameBuff.toString();
            parentProcessInstance = parent.getProcessInstance();
            Map<String, Object> variablesParent = runtimeService.getVariables(parent.getId());
            variables.putAll(variablesParent);
            delegateTask.setVariables(variables);
            String parentDefinitionKey = parentProcessInstance.getProcessDefinitionKey();
            if (StringUtils.isEmpty(parentDefinitionKey)) {
                // 取得流程定义
                ProcessDefinitionEntity definition = (ProcessDefinitionEntity) ((RepositoryServiceImpl) repositoryService)
                        .getDeployedProcessDefinition(parentProcessInstance.getProcessDefinitionId());
                parentDefinitionKey = definition.getKey();
            }
            FlowDefination flowDefinationParent = flowDefinationDao.findByDefKey(parentDefinitionKey);
            String definitionKey = processInstance.getProcessDefinitionKey();
            FlowDefination flowDefination = flowDefinationDao.findByDefKey(definitionKey);
            String parentBusinessModelCode = flowDefinationParent.getFlowType().getBusinessModel().getClassName();
            String sonBusinessModelCode = flowDefination.getFlowType().getBusinessModel().getClassName();
            sonBusinessVNameBuff.append("/" + definitionKey);
            currentBusinessId = (String) delegateTask.getVariable(sonBusinessVNameBuff.toString());
            List<String> userVarNameList = (List) delegateTask.getVariable(sonBusinessVNameBuff + Constants.SON_PROCESS_SELECT_NODE_USER);
            if (userVarNameList != null && !userVarNameList.isEmpty()) {
                for (String userVarName : userVarNameList) {
                    Object userValue = delegateTask.getVariable(sonBusinessVNameBuff + "/" + userVarName);
                    delegateTask.setVariable(userVarName, userValue);
                }
            }
            if (StringUtils.isEmpty(currentBusinessId)) {
                if (parentBusinessModelCode.equals(sonBusinessModelCode)) {//非跨业务实体子流程
                    //设置子流程businessKey
                    String parentBusinessKey = parentProcessInstance.getBusinessKey();
                    runtimeService.updateBusinessKey(processInstance.getId(), parentBusinessKey);
                    currentBusinessId = parentBusinessKey;
                } else {//跨业务实体子流程,必须指定子流程关联单据id
                    String message = ContextUtil.getMessage("10039");
                    throw new FlowException(message);//子流程关联的单据找不到！
                }
            } else {
                runtimeService.updateBusinessKey(processInstance.getId(), currentBusinessId);
            }

        }
        FlowInstance flowInstance = new FlowInstance();
        flowInstance.setTenantCode(ContextUtil.getTenantCode());
        flowInstance.setBusinessId(processInstance.getBusinessKey());
        String workCaption = null;
        if (variables.containsKey(Constants.WORK_CAPTION)) {
            workCaption = (String) variables.get(Constants.WORK_CAPTION);//工作说明
            if (StringUtils.isNotEmpty(workCaption) && workCaption.length() > 2000) {
                throw new FlowException("工作说明限定长度2000，实际长度为：" + workCaption.length());
            }
        }
        flowInstance.setBusinessModelRemark(workCaption);

        String businessCode = null;
        if (variables.containsKey(Constants.BUSINESS_CODE)) {
            businessCode = (String) variables.get(Constants.BUSINESS_CODE);//业务code
            if (StringUtils.isNotEmpty(businessCode) && businessCode.length() > 2000) {
                throw new FlowException("业务单号限定长度2000,实际长度为：" + businessCode.length());
            }
        }
        flowInstance.setBusinessCode(businessCode);

        String businessName = null;
        if (variables.containsKey(Constants.NAME)) {
            businessName = (String) variables.get(Constants.NAME);//业务单据名称
            if (StringUtils.isNotEmpty(businessName) && businessName.length() > 100) {
                throw new FlowException("业务单据名称限定长度100,实际长度为：" + businessName.length());
            }
        }
        flowInstance.setBusinessName(businessName);

        String orgId = (String) variables.get(Constants.ORG_ID);
        flowInstance.setBusinessOrgId(orgId);


        String flowDefVersionId = null;
        if (variables.containsKey(Constants.FLOW_DEF_VERSION_ID)) {
            flowDefVersionId = (String) variables.get(Constants.FLOW_DEF_VERSION_ID);//流程定义版本id
        }
        FlowDefVersion flowDefVersion = flowCommonUtil.getLastFlowDefVersion(flowDefVersionId);
        flowDefVersionDao.findOne(flowDefVersionId);
        if (flowDefVersion == null) {
            String message = ContextUtil.getMessage("10040");
            throw new FlowException(message);//"流程版本找不到！
        }
        flowInstance.setFlowDefVersion(flowDefVersion);
        Date now = new Date();
        flowInstance.setStartDate(now);
        flowInstance.setEndDate(now);
        flowInstance.setFlowName(flowDefVersion.getName());
        flowInstance.setActInstanceId(processInstance.getId());
        if (parentProcessInstance != null) {
            FlowInstance flowInstanceP = flowInstanceDao.findByActInstanceId(parentProcessInstance.getId());
            String actDefinitionKey = processInstance.getProcessDefinitionKey();
            List<FlowDefVersion> flowDefVersionList = flowDefVersionDao.findByKeyActivate(actDefinitionKey);
            if (flowDefVersionList == null || flowDefVersionList.isEmpty()) {
                String message = ContextUtil.getMessage("10041");
                throw new FlowException(message);//子流程的流程版本找不到！
            }
            flowDefVersion = flowDefVersionList.get(0);
            flowInstance.setFlowDefVersion(flowDefVersion);
            flowInstance.setBusinessId(currentBusinessId);
            flowInstance.setCallActivityPath(callActivityPath);
            flowInstance.setParent(flowInstanceP);
        }
        flowInstanceDao.save(flowInstance);

        BusinessModel businessModel = flowInstance.getFlowDefVersion().getFlowDefination().getFlowType().getBusinessModel();

        FlowOperateResult callAfterStartResult = callAfterStart(flowInstance.getBusinessId(), flowInstance.getFlowDefVersion());
        if (callAfterStartResult != null && callAfterStartResult.isSuccess() != true) {
            throw new FlowException(callAfterStartResult.getMessage());
        }

        ResponseData responseData = ExpressionUtil.resetState(businessModel, flowInstance.getBusinessId(), FlowStatus.INPROCESS);
        if (!responseData.getSuccess()) {
            throw new FlowException("流程启动-调用重置表单服务失败："+responseData.getMessage() );
        }
    }

    /**
     * 流程启动结束后调用调用服务
     *
     * @param businessKey
     * @param flowDefVersion
     * @return
     */
    private FlowOperateResult callAfterStart(String businessKey, FlowDefVersion flowDefVersion) {
        FlowOperateResult result = null;
        if (flowDefVersion != null && StringUtils.isNotEmpty(businessKey)) {
            String afterStartServiceId = flowDefVersion.getAfterStartServiceId();
            Boolean afterStartServiceAync = flowDefVersion.getAfterStartServiceAync();

            if (StringUtils.isNotEmpty(afterStartServiceId)) {
                FlowServiceUrl flowServiceUrl = flowServiceUrlDao.findOne(afterStartServiceId);
                if (flowServiceUrl == null) {
                    LogUtil.error("获取启动后事件失败，可能已经被删除，serviceId = " + afterStartServiceId);
                    throw new FlowException("获取启动后事件失败，可能已经被删除，详情请查看日志!");
                }
                String checkUrl = flowServiceUrl.getUrl();
                if (StringUtils.isNotEmpty(checkUrl)) {
                    String apiBaseAddressConfig = flowDefVersion.getFlowDefination().getFlowType().getBusinessModel().getAppModule().getApiBaseAddress();
                    String baseUrl = Constants.getConfigValueByApi(apiBaseAddressConfig);
                    String checkUrlPath = PageUrlUtil.buildUrl(baseUrl, checkUrl);
                    FlowInvokeParams flowInvokeParams = new FlowInvokeParams();
                    flowInvokeParams.setId(businessKey);

                    Map<String, String> paramMap = new HashMap<>();
                    paramMap.put("flowInstanceName",flowDefVersion.getName());
                    flowInvokeParams.setParams(paramMap);

                    String msg = "启动后事件【" + flowServiceUrl.getName() + "】";
                    String urlAndData = "-请求地址：" + checkUrlPath + "，参数：" + JsonUtils.toJson(flowInvokeParams);
                    if (afterStartServiceAync == true) {
                        new Thread(new Runnable() {//模拟异步
                            @Override
                            public void run() {
                                try {
                                    ResponseData res = ApiClient.postViaProxyReturnResult(checkUrlPath, new GenericType<ResponseData>() {
                                    }, flowInvokeParams);
                                    if (!res.successful()) {
                                        LogUtil.error(msg + "异步调用报错!" + urlAndData + ", 【返回错误结果：" + res.getMessage() + "】");
                                    }
                                } catch (Exception e) {
                                    LogUtil.error(msg + "异步调用内部报错!" + urlAndData, e);
                                }
                            }
                        }).start();
                        result = new FlowOperateResult(true, "事件已异步调用！");
                    } else {
                        try {
                            ResponseData res = ApiClient.postViaProxyReturnResult(checkUrlPath, new GenericType<ResponseData>() {
                            }, flowInvokeParams);
                            if (res.successful()) {
                                LogUtil.bizLog(msg + urlAndData + ",【返回信息：" + JsonUtils.toJson(result) + "】");
                                result = new FlowOperateResult(true, res.getMessage());
                            } else {
                                LogUtil.error(msg + "内部报错!" + urlAndData + "【返回信息：" + JsonUtils.toJson(res) + "】");
                                result = new FlowOperateResult(false, msg + "返回信息：【" + res.getMessage() + "】");
                            }
                        } catch (Exception e) {
                            LogUtil.error(msg + "内部报错!" + urlAndData, e);
                            throw new FlowException(msg + "内部报错，详情请查看日志！");
                        }
                    }
                }
            }
        }
        return result;
    }

}
