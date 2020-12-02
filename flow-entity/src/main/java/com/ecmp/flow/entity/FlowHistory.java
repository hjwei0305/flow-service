package com.ecmp.flow.entity;

import javax.persistence.*;

import com.ecmp.core.entity.ITenant;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.Date;

/**
 * *************************************************************************************************
 * <p/>
 * 实现功能：
 * 流程历史模型Entity定义
 * <p>
 * ------------------------------------------------------------------------------------------------
 * 版本          变更时间             变更人                     变更原因
 * ------------------------------------------------------------------------------------------------
 * 1.0.00      2017/3/21 10:20      詹耀(zhanyao)                新建
 * <p/>
 * *************************************************************************************************
 */
@Entity
@Table(name = "flow_history")
public class FlowHistory extends com.ecmp.core.entity.BaseAuditableEntity implements Cloneable, ITenant {

    /**
     * 所属流程实例
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "flow_instance_id")
    private FlowInstance flowInstance;

    /**
     * 流程名称
     */
    @Column(name = "flow_name", nullable = false, length = 80)
    private String flowName;

    /**
     * 流程任务名
     */
    @Column(name = "flow_task_name", nullable = false, length = 80)
    private String flowTaskName;

    /**
     * 流程运行ID
     */
    @Column(name = "flow_run_id", length = 36, nullable = true)
    private String flowRunId;

    /**
     * 流程定义ID
     */
    @Column(name = "flow_def_id", length = 36)
    private String flowDefId;


    /**
     * 关联的实际流程引擎历史ID
     */
    @Column(name = "act_history_id", nullable = false, length = 36)
    private String actHistoryId;

    /**
     * 不同意原因ID
     */
    @Column(name = "disagree_reason_id")
    private String disagreeReasonId;


    /**
     * 不同意原因code
     */
    @Column(name = "disagree_reason_code")
    private String disagreeReasonCode;


    /**
     * 不同意原因简称
     */
    @Column(name = "disagree_reason_name")
    private String disagreeReasonName;


    /**
     * 描述
     */
    @Column(name = "depict")
    private String depict;


    /**
     * 租户代码
     */
    @Column(name = "tenant_code", length = 10)
    private String tenantCode;

    /**
     * 流程任务引擎实际开始时间，
     * Time when the task started.
     */
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "act_start_time")
    private Date actStartTime;

    /**
     * 流程任务引擎实际结束时间，
     * Time when the task was deleted or completed.
     */
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "act_end_time")
    private Date actEndTime;

    /**
     * 流程任务引擎实际执行的时间间隔
     * Difference between {@link #getActEndTime()} and {@link #getActStartTime()} in milliseconds.
     */
    @Column(name = "act_duration_in_millis")
    private Long actDurationInMillis;


    /**
     * 流程任务引擎实际执行的工作时间间隔，
     * Difference between {@link #getActEndTime()} and {@link #getActClaimTime()} in milliseconds.
     */
    @Column(name = "act_work_time_in_millis")
    private Long actWorkTimeInMillis;


    /**
     * 流程任务引擎实际的任务签收时间
     */
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "act_claim_time")
    private Date actClaimTime;


    /**
     * activtiti对应任务类型,如assinge、candidate
     */
    @Column(name = "act_type")
    private String actType;

    /**
     * 流程引擎的实际任务定义KEY
     */
    @Column(name = "act_task_def_key", nullable = false)
    private String actTaskDefKey;


    /**
     * 执行人名称
     */
    @Column(name = "executor_name", length = 80)
    private String executorName;

    /**
     * 执行人账号
     */
    @Column(name = "executor_account")
    private String executorAccount;

    /**
     * 候选人账号
     */
    @Column(name = "candidate_account")
    private String candidateAccount;

    /**
     * 任务所属人账号（拥有人）
     */

    @Column(name = "owner_account")
    private String ownerAccount;

    /**
     * 任务所属人名称（拥有人）
     */
    @Column(name = "owner_name")
    private String ownerName;

    /**
     * 记录上一个流程历史任务的id
     */
    @Column(name = "pre_id")
    private String preId;

    /**
     * 记录下一个流程历史任务的id
     */
    @Column(name = "next_id")
    private String nextId;

    /**
     * 任务状态
     */
    @Column(name = "task_status", length = 80)
    private String taskStatus;


    /**
     * 是否允许撤销任务
     */
    @Column(name = "can_cancel")
    private Boolean canCancel;


    /**
     * 任务定义JSON
     */
    @Column(name = "task_json_def")
    @Lob
    private String taskJsonDef;

    /**
     * 流程节点执行状态
     */
    @Column(name = "flow_execute_status")
    private String flowExecuteStatus;

    /**
     * 业务摘要(工作说明)
     */
    @Transient
    private String businessModelRemark;


    /**
     * 执行人ID
     */
    @Column(name = "executor_id")
    private String executorId;


    /**
     * 候选人ID
     */
    @Column(name = "candidate_id")
    private String candidateId;


    /**
     * 候选人ID
     */
    @Column(name = "owner_id")
    private String ownerId;


    /**
     * 关联的已删除的flowTaskId
     */
    @Column(name = "old_task_id")
    private String oldTaskId;


    /**
     * 执行人组织机构ID
     */
    @Column(name = "executor_org_id")
    private String executorOrgId;

    /**
     * 执行人组织机构code
     */
    @Column(name = "executor_org_code")
    private String executorOrgCode;

    /**
     * 执行人组织机构名称
     */
    @Column(name = "executor_org_name")
    private String executorOrgName;

    /**
     * 拥有者组织机构ID
     */
    @Column(name = "owner_org_id")
    private String ownerOrgId;

    /**
     * 拥有者组织机构code
     */
    @Column(name = "owner_org_code")
    private String ownerOrgCode;

    /**
     * 拥有者组织机构名称
     */
    @Column(name = "owner_org_name")
    private String ownerOrgName;


    /**
     * web基地址
     */
    @Transient
    private String webBaseAddress;


    /**
     * web基地址绝对路径
     */
    @Transient
    private String webBaseAddressAbsolute;

    /**
     * api基地址
     */
    @Transient
    private String apiBaseAddress;

    /**
     * api基地址
     */
    @Transient
    private String apiBaseAddressAbsolute;


    /**
     * 待办执行人（中泰）
     */
    @Transient
    private String taskExecutors;


    public FlowHistory() {
    }

    /**
     * minimal constructor
     */
    public FlowHistory(String flowName, String flowTaskName, String flowRunId,
                       String flowInstanceId) {
        this.flowName = flowName;
        this.flowTaskName = flowTaskName;
        this.flowRunId = flowRunId;
//		this.flowInstanceId = flowInstanceId;
    }

    /**
     * full constructor
     */
    public FlowHistory(FlowInstance flowInstance, String flowName,
                       String flowTaskName, String flowRunId, String flowInstanceId,
                       String flowDefId, String depict) {
        this.flowInstance = flowInstance;
        this.flowName = flowName;
        this.flowTaskName = flowTaskName;
        this.flowRunId = flowRunId;
//		this.flowInstanceId = flowInstanceId;
        this.flowDefId = flowDefId;
        this.depict = depict;
    }

    public String getTaskExecutors() {
        return taskExecutors;
    }

    public void setTaskExecutors(String taskExecutors) {
        this.taskExecutors = taskExecutors;
    }

    public String getFlowExecuteStatus() {
        return flowExecuteStatus;
    }

    public void setFlowExecuteStatus(String flowExecuteStatus) {
        this.flowExecuteStatus = flowExecuteStatus;
    }

    public FlowInstance getFlowInstance() {
        return this.flowInstance;
    }

    public void setFlowInstance(FlowInstance flowInstance) {
        this.flowInstance = flowInstance;
    }

    public String getFlowName() {
        return this.flowName;
    }

    public void setFlowName(String flowName) {
        this.flowName = flowName;
    }

    public String getFlowTaskName() {
        return this.flowTaskName;
    }

    public void setFlowTaskName(String flowTaskName) {
        this.flowTaskName = flowTaskName;
    }

    public String getFlowRunId() {
        return this.flowRunId;
    }

    public void setFlowRunId(String flowRunId) {
        this.flowRunId = flowRunId;
    }

//	public String getFlowInstanceId() {
//		return this.flowInstanceId;
//	}
//
//	public void setFlowInstanceId(String flowInstanceId) {
//		this.flowInstanceId = flowInstanceId;
//	}

    public String getFlowDefId() {
        return this.flowDefId;
    }

    public void setFlowDefId(String flowDefId) {
        this.flowDefId = flowDefId;
    }

    public String getDisagreeReasonId() {
        return disagreeReasonId;
    }

    public void setDisagreeReasonId(String disagreeReasonId) {
        this.disagreeReasonId = disagreeReasonId;
    }

    public String getDisagreeReasonCode() {
        return disagreeReasonCode;
    }

    public void setDisagreeReasonCode(String disagreeReasonCode) {
        this.disagreeReasonCode = disagreeReasonCode;
    }

    public String getDisagreeReasonName() {
        return disagreeReasonName;
    }

    public void setDisagreeReasonName(String disagreeReasonName) {
        this.disagreeReasonName = disagreeReasonName;
    }

    public String getDepict() {
        return this.depict;
    }

    public void setDepict(String depict) {
        this.depict = depict;
    }

    public String getActHistoryId() {
        return actHistoryId;
    }

    public void setActHistoryId(String actHistoryId) {
        this.actHistoryId = actHistoryId;
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

//	public String getNextId() {
//		return nextId;
//	}
//
//	public void setNextId(String nextId) {
//		this.nextId = nextId;
//	}

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

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("id", this.getId())
                .append("flowInstance", flowInstance)
                .append("flowName", flowName)
                .append("flowTaskName", flowTaskName)
                .append("flowRunId", flowRunId)
//				.append("flowInstanceId", flowInstanceId)
                .append("webBaseAddress", this.getWebBaseAddress())
                .append("apiBaseAddress", this.getApiBaseAddress())
                .append("webBaseAddressAbsolute", this.getWebBaseAddressAbsolute())
                .append("apiBaseAddressAbsolute", this.getApiBaseAddressAbsolute())
                .append("flowDefId", flowDefId)
                .append("depict", depict)
                .toString();
    }

    @Override
    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj);
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        FlowHistory o = null;
        // Object中的clone()识别出你要复制的是哪一个对象。
        o = (FlowHistory) super.clone();
        return o;
    }

    @Override
    @JsonIgnore
    public String getCreatorId() {
        return super.getCreatorId();
    }

    @Override
    @JsonIgnore(false)
    public String getCreatorAccount() {
        return super.getCreatorAccount();
    }

    @Override
    @JsonIgnore(false)
    public String getCreatorName() {
        return super.getCreatorName();
    }

    @Override
    @JsonIgnore(false)
    public Date getCreatedDate() {
        return super.getCreatedDate();
    }

    @Override
    @JsonIgnore(false)
    public String getLastEditorId() {
        return super.getLastEditorId();
    }

    @Override
    @JsonIgnore(false)
    public String getLastEditorAccount() {
        return super.getLastEditorAccount();
    }

    @Override
    @JsonIgnore(false)
    public String getLastEditorName() {
        return super.getLastEditorName();
    }

    @Override
    @JsonIgnore(false)
    public Date getLastEditedDate() {
        return super.getLastEditedDate();
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

    @Override
    public String getTenantCode() {
        return tenantCode;
    }

    @Override
    public void setTenantCode(String tenantCode) {
        this.tenantCode = tenantCode;
    }

    public String getExecutorOrgId() {
        return executorOrgId;
    }

    public void setExecutorOrgId(String executorOrgId) {
        this.executorOrgId = executorOrgId;
    }

    public String getExecutorOrgCode() {
        return executorOrgCode;
    }

    public void setExecutorOrgCode(String executorOrgCode) {
        this.executorOrgCode = executorOrgCode;
    }

    public String getExecutorOrgName() {
        return executorOrgName;
    }

    public void setExecutorOrgName(String executorOrgName) {
        this.executorOrgName = executorOrgName;
    }

    public String getOwnerOrgId() {
        return ownerOrgId;
    }

    public void setOwnerOrgId(String ownerOrgId) {
        this.ownerOrgId = ownerOrgId;
    }

    public String getOwnerOrgCode() {
        return ownerOrgCode;
    }

    public void setOwnerOrgCode(String ownerOrgCode) {
        this.ownerOrgCode = ownerOrgCode;
    }

    public String getOwnerOrgName() {
        return ownerOrgName;
    }

    public void setOwnerOrgName(String ownerOrgName) {
        this.ownerOrgName = ownerOrgName;
    }
}
