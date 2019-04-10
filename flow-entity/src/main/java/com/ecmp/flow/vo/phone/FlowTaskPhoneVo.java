package com.ecmp.flow.vo.phone;

import java.io.Serializable;
import java.util.Date;

/**
 * 待办VO（移动端专用）
 * 新建：何灿坤
 */
public class FlowTaskPhoneVo implements Serializable {

    /**
     * 待办id
     */
    private String taskId;

    /**
     * 待办名称
     */
    private String taskName;

    /**
     * 是否允许驳回
     */
    private Boolean canReject;

    /**
     * 是否允许流程中止（退出流程）
     */
    private Boolean canSuspension;

    /**
     * 移动端能否
     */
    private Boolean canMobile;

    /**
     * 是被转办的状态0，委托状态，1发起委托的任务，2被委托的任务,非委托状态为null,委托完成为3
     */
    private Integer trustState;


    /**
     * 流程任务引擎实际的任务签收时间
     */
    private Date actClaimTime;


    /**
     * 创建时间
     */
    private Date createdDate;

    /**
     * 流程节点任务类型
     * JSON.parse(taskJsonDef).nodeType
     */
    private String nodeType;

    /**
     * 流程实例id
     * flowInstance.id
     */
    private String flowInstanceId;

    /**
     * 业务ID
     * flowInstance.businessId
     */
    private String flowInstanceBusinessId;

    /**
     * 业务单号
     * flowInstance.businessCode
     */
    private String flowInstanceBusinessCode;

    /**
     * 流程名称
     * flowInstance.flowName
     */
    private String flowInstanceFlowName;


    /**
     * 流程类型名称
     * flowInstance.flowDefVersion.flowDefination.flowType.name
     */
    private String flowTypeName;


    /**
     * 业务实体类全路径
     * flowInstance.flowDefVersion.flowDefination.flowType.businessModel.className
     */
    private String businessModelClassName;


    /**
     * 提交地址
     *  ContextUtil.getGlobalProperty(flowInstance.flowDefVersion.flowDefination.flowType.businessModel.appModule.webBaseAddress) +
     *  flowInstance.flowDefVersion.flowDefination.flowType.businessModel.completeTaskServiceUrl
     */
    private String completeTaskUrl;


    /**
     * 模块api地址
     */
    private String apiBaseAddress;

    /**
     *表单明细地址
     *  ContextUtil.getGlobalProperty(flowInstance.flowDefVersion.flowDefination.flowType.businessModel.appModule.apiBaseAddress) +
     *  businessDetailServiceUrl
     */
    private String businessDetailServiceUrl;


    public String getApiBaseAddress() {
        return apiBaseAddress;
    }

    public void setApiBaseAddress(String apiBaseAddress) {
        this.apiBaseAddress = apiBaseAddress;
    }

    public String getBusinessDetailServiceUrl() {
        return businessDetailServiceUrl;
    }

    public void setBusinessDetailServiceUrl(String businessDetailServiceUrl) {
        this.businessDetailServiceUrl = businessDetailServiceUrl;
    }

    public Integer getTrustState() {
        return trustState;
    }

    public void setTrustState(Integer trustState) {
        this.trustState = trustState;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public Boolean getCanSuspension() {
        return canSuspension;
    }

    public void setCanSuspension(Boolean canSuspension) {
        this.canSuspension = canSuspension;
    }

    public Boolean getCanMobile() {
        return canMobile;
    }

    public void setCanMobile(Boolean canMobile) {
        this.canMobile = canMobile;
    }

    public String getFlowInstanceBusinessCode() {
        return flowInstanceBusinessCode;
    }

    public void setFlowInstanceBusinessCode(String flowInstanceBusinessCode) {
        this.flowInstanceBusinessCode = flowInstanceBusinessCode;
    }

    public String getFlowInstanceFlowName() {
        return flowInstanceFlowName;
    }

    public void setFlowInstanceFlowName(String flowInstanceFlowName) {
        this.flowInstanceFlowName = flowInstanceFlowName;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public String getFlowTypeName() {
        return flowTypeName;
    }

    public void setFlowTypeName(String flowTypeName) {
        this.flowTypeName = flowTypeName;
    }

    public Date getActClaimTime() {
        return actClaimTime;
    }

    public void setActClaimTime(Date actClaimTime) {
        this.actClaimTime = actClaimTime;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public String getNodeType() {
        return nodeType;
    }

    public void setNodeType(String nodeType) {
        this.nodeType = nodeType;
    }

    public String getBusinessModelClassName() {
        return businessModelClassName;
    }

    public void setBusinessModelClassName(String businessModelClassName) {
        this.businessModelClassName = businessModelClassName;
    }

    public String getFlowInstanceBusinessId() {
        return flowInstanceBusinessId;
    }

    public void setFlowInstanceBusinessId(String flowInstanceBusinessId) {
        this.flowInstanceBusinessId = flowInstanceBusinessId;
    }

    public Boolean getCanReject() {
        return canReject;
    }

    public void setCanReject(Boolean canReject) {
        this.canReject = canReject;
    }

    public String getFlowInstanceId() {
        return flowInstanceId;
    }

    public void setFlowInstanceId(String flowInstanceId) {
        this.flowInstanceId = flowInstanceId;
    }

    public String getCompleteTaskUrl() {
        return completeTaskUrl;
    }

    public void setCompleteTaskUrl(String completeTaskUrl) {
        this.completeTaskUrl = completeTaskUrl;
    }
}
