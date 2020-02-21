package com.ecmp.flow.entity;


import java.io.Serializable;
import java.util.Date;


public class FlowHistory implements Serializable {

    /**
     * 所属流程实例
     */
    private FlowInstance flowInstance;

    /**
     * 流程名称
     */
    private String flowName;

    /**
     * 流程任务名
     */
    private String flowTaskName;

    /**
     * 流程运行ID
     */
    private String flowRunId;

    /**
     * 流程定义ID
     */
    private String flowDefId;


    /**
     * 关联的实际流程引擎历史ID
     */
    private String actHistoryId;

    /**
     * 描述
     */
    private String depict;


    /**
     * 租户代码
     */
    private String tenantCode;

    /**
     * 流程任务引擎实际开始时间，
     * Time when the task started.
     */
    private Date actStartTime;

    /**
     * 流程任务引擎实际结束时间，
     * Time when the task was deleted or completed.
     */
    private Date actEndTime;

    /**
     * 流程任务引擎实际执行的时间间隔
     * Difference between {@link #getActEndTime()} and {@link #getActStartTime()} in milliseconds.
     */
    private Long actDurationInMillis;


    /**
     * 流程任务引擎实际执行的工作时间间隔，
     * Difference between {@link #getActEndTime()} and {@link #getActClaimTime()} in milliseconds.
     */
    private Long actWorkTimeInMillis;


    /**
     * 流程任务引擎实际的任务签收时间
     */
    private Date actClaimTime;


    /**
     * activtiti对应任务类型,如assinge、candidate
     */
    private String actType;

    /**
     * 流程引擎的实际任务定义KEY
     */
    private String actTaskDefKey;


    /**
     * 执行人名称
     */
    private String executorName;

    /**
     * 执行人账号
     */
    private String executorAccount;

    /**
     * 候选人账号
     */
    private String candidateAccount;

    /**
     * 任务所属人账号（拥有人）
     */

    private String ownerAccount;

    /**
     * 任务所属人名称（拥有人）
     */
    private String ownerName;

    /**
     * 记录上一个流程历史任务的id
     */
    private String preId;

    /**
     * 记录下一个流程历史任务的id
     */
    private String nextId;

    /**
     * 任务状态
     */
    private String taskStatus;


    /**
     * 是否允许撤销任务
     */
    private Boolean canCancel;


    /**
     * 任务定义JSON
     */
    private String taskJsonDef;

    /**
     * 流程节点执行状态
     */
    private String flowExecuteStatus;

    /**
     * 业务摘要(工作说明)
     */
    private String businessModelRemark;


    /**
     * 执行人ID
     */
    private String executorId;


    /**
     * 候选人ID
     */
    private String candidateId;


    /**
     * 候选人ID
     */
    private String ownerId;


    /**
     * 关联的已删除的flowTaskId
     */
    private String oldTaskId;


    /**
     * web基地址
     */
    private String webBaseAddress;


    /**
     * web基地址绝对路径
     */
    private String webBaseAddressAbsolute;

    /**
     * api基地址
     */
    private String apiBaseAddress;

    /**
     * api基地址
     */
    private String apiBaseAddressAbsolute;


    /**
     * 创建者
     */
    protected String creatorId;

    protected String creatorAccount;

    protected String creatorName;

    /**
     * 创建时间
     */
    protected Date createdDate;

    /**
     * 最后修改者
     */
    protected String lastEditorId;

    protected String lastEditorAccount;

    protected String lastEditorName;

    /**
     * 最后修改时间
     */
    protected Date lastEditedDate;

    protected String id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public FlowHistory() {
    }


    public FlowInstance getFlowInstance() {
        return flowInstance;
    }

    public void setFlowInstance(FlowInstance flowInstance) {
        this.flowInstance = flowInstance;
    }

    public String getFlowName() {
        return flowName;
    }

    public void setFlowName(String flowName) {
        this.flowName = flowName;
    }

    public String getFlowTaskName() {
        return flowTaskName;
    }

    public void setFlowTaskName(String flowTaskName) {
        this.flowTaskName = flowTaskName;
    }

    public String getFlowRunId() {
        return flowRunId;
    }

    public void setFlowRunId(String flowRunId) {
        this.flowRunId = flowRunId;
    }

    public String getFlowDefId() {
        return flowDefId;
    }

    public void setFlowDefId(String flowDefId) {
        this.flowDefId = flowDefId;
    }

    public String getActHistoryId() {
        return actHistoryId;
    }

    public void setActHistoryId(String actHistoryId) {
        this.actHistoryId = actHistoryId;
    }

    public String getDepict() {
        return depict;
    }

    public void setDepict(String depict) {
        this.depict = depict;
    }

    public String getTenantCode() {
        return tenantCode;
    }

    public void setTenantCode(String tenantCode) {
        this.tenantCode = tenantCode;
    }

    public Date getActStartTime() {
        return actStartTime;
    }

    public void setActStartTime(Date actStartTime) {
        this.actStartTime = actStartTime;
    }

    public Date getActEndTime() {
        return actEndTime;
    }

    public void setActEndTime(Date actEndTime) {
        this.actEndTime = actEndTime;
    }

    public Long getActDurationInMillis() {
        return actDurationInMillis;
    }

    public void setActDurationInMillis(Long actDurationInMillis) {
        this.actDurationInMillis = actDurationInMillis;
    }

    public Long getActWorkTimeInMillis() {
        return actWorkTimeInMillis;
    }

    public void setActWorkTimeInMillis(Long actWorkTimeInMillis) {
        this.actWorkTimeInMillis = actWorkTimeInMillis;
    }

    public Date getActClaimTime() {
        return actClaimTime;
    }

    public void setActClaimTime(Date actClaimTime) {
        this.actClaimTime = actClaimTime;
    }

    public String getActType() {
        return actType;
    }

    public void setActType(String actType) {
        this.actType = actType;
    }

    public String getActTaskDefKey() {
        return actTaskDefKey;
    }

    public void setActTaskDefKey(String actTaskDefKey) {
        this.actTaskDefKey = actTaskDefKey;
    }

    public String getExecutorName() {
        return executorName;
    }

    public void setExecutorName(String executorName) {
        this.executorName = executorName;
    }

    public String getExecutorAccount() {
        return executorAccount;
    }

    public void setExecutorAccount(String executorAccount) {
        this.executorAccount = executorAccount;
    }

    public String getCandidateAccount() {
        return candidateAccount;
    }

    public void setCandidateAccount(String candidateAccount) {
        this.candidateAccount = candidateAccount;
    }

    public String getOwnerAccount() {
        return ownerAccount;
    }

    public void setOwnerAccount(String ownerAccount) {
        this.ownerAccount = ownerAccount;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public String getPreId() {
        return preId;
    }

    public void setPreId(String preId) {
        this.preId = preId;
    }

    public String getNextId() {
        return nextId;
    }

    public void setNextId(String nextId) {
        this.nextId = nextId;
    }

    public String getTaskStatus() {
        return taskStatus;
    }

    public void setTaskStatus(String taskStatus) {
        this.taskStatus = taskStatus;
    }

    public Boolean getCanCancel() {
        return canCancel;
    }

    public void setCanCancel(Boolean canCancel) {
        this.canCancel = canCancel;
    }

    public String getTaskJsonDef() {
        return taskJsonDef;
    }

    public void setTaskJsonDef(String taskJsonDef) {
        this.taskJsonDef = taskJsonDef;
    }

    public String getFlowExecuteStatus() {
        return flowExecuteStatus;
    }

    public void setFlowExecuteStatus(String flowExecuteStatus) {
        this.flowExecuteStatus = flowExecuteStatus;
    }

    public String getBusinessModelRemark() {
        return businessModelRemark;
    }

    public void setBusinessModelRemark(String businessModelRemark) {
        this.businessModelRemark = businessModelRemark;
    }

    public String getExecutorId() {
        return executorId;
    }

    public void setExecutorId(String executorId) {
        this.executorId = executorId;
    }

    public String getCandidateId() {
        return candidateId;
    }

    public void setCandidateId(String candidateId) {
        this.candidateId = candidateId;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public String getOldTaskId() {
        return oldTaskId;
    }

    public void setOldTaskId(String oldTaskId) {
        this.oldTaskId = oldTaskId;
    }

    public String getWebBaseAddress() {
        return webBaseAddress;
    }

    public void setWebBaseAddress(String webBaseAddress) {
        this.webBaseAddress = webBaseAddress;
    }

    public String getWebBaseAddressAbsolute() {
        return webBaseAddressAbsolute;
    }

    public void setWebBaseAddressAbsolute(String webBaseAddressAbsolute) {
        this.webBaseAddressAbsolute = webBaseAddressAbsolute;
    }

    public String getApiBaseAddress() {
        return apiBaseAddress;
    }

    public void setApiBaseAddress(String apiBaseAddress) {
        this.apiBaseAddress = apiBaseAddress;
    }

    public String getApiBaseAddressAbsolute() {
        return apiBaseAddressAbsolute;
    }

    public void setApiBaseAddressAbsolute(String apiBaseAddressAbsolute) {
        this.apiBaseAddressAbsolute = apiBaseAddressAbsolute;
    }

    public String getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(String creatorId) {
        this.creatorId = creatorId;
    }

    public String getCreatorAccount() {
        return creatorAccount;
    }

    public void setCreatorAccount(String creatorAccount) {
        this.creatorAccount = creatorAccount;
    }

    public String getCreatorName() {
        return creatorName;
    }

    public void setCreatorName(String creatorName) {
        this.creatorName = creatorName;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public String getLastEditorId() {
        return lastEditorId;
    }

    public void setLastEditorId(String lastEditorId) {
        this.lastEditorId = lastEditorId;
    }

    public String getLastEditorAccount() {
        return lastEditorAccount;
    }

    public void setLastEditorAccount(String lastEditorAccount) {
        this.lastEditorAccount = lastEditorAccount;
    }

    public String getLastEditorName() {
        return lastEditorName;
    }

    public void setLastEditorName(String lastEditorName) {
        this.lastEditorName = lastEditorName;
    }

    public Date getLastEditedDate() {
        return lastEditedDate;
    }

    public void setLastEditedDate(Date lastEditedDate) {
        this.lastEditedDate = lastEditedDate;
    }
}
