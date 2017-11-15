package com.ecmp.flow.entity;


import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.Date;

/**
 * *************************************************************************************************
 * <p/>
 * 实现功能：
 * 流程任务实例模型Entity定义
 * <p>
 * ------------------------------------------------------------------------------------------------
 * 版本          变更时间             变更人                     变更原因
 * ------------------------------------------------------------------------------------------------
 * 1.0.00      2017/3/21 10:20      詹耀(zhanyao)                新建
 * <p/>
 * *************************************************************************************************
 */
@Entity
@Table(name = "flow_task", catalog = "ecmp_flow")
public class FlowTask extends com.ecmp.core.entity.BaseAuditableEntity {


    /**
     * web基地址
     */
    @Transient
    private String webBaseAddress;

    /**
     * api基地址
     */
    @Transient
    private String apiBaseAddress;


    /**
     * api基地址
     */
    @Transient
    private String completeTaskServiceUrl;

    /**
     * 乐观锁- 版本
     */
//	@Version
    @Column(name = "version")
    private Integer version = 0;

    /**
     * 所属流程实例
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "flow_instance_id")
    private FlowInstance flowInstance;

    /**
     * 名称
     */
    @Column(name = "flow_name", nullable = false, length = 80)
    private String flowName;

    /**
     * 任务名
     */
    @Column(name = "task_name", nullable = false, length = 80)
    private String taskName;

    /**
     * 任务定义KEY
     */
    @Column(name = "act_task_def_key", nullable = false)
    private String actTaskDefKey;

    /**
     * 任务表单URL
     */
    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "task_form_url", length = 65535)
    private String taskFormUrl;

    /**
     * 任务状态
     */
    @Column(name = "task_status", length = 80)
    private String taskStatus;

    /**
     * 代理状态
     */
    @Column(name = "proxy_status", length = 80)
    private String proxyStatus;

    /**
     * 流程实例ID
     */
//	@Column(name = "flow_instanceId", nullable = false, length = 36)
//	private String flowInstanceId;

    /**
     * 流程定义ID
     */
    @Column(name = "flow_definition_id", length = 36)
    private String flowDefinitionId;

    /**
     * 关联的实际流程引擎任务ID
     */
    @Column(name = "act_task_id", nullable = false, length = 36)
    private String actTaskId;

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
     * 执行时间
     */
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "execute_date", length = 19)
    private Date executeDate;


    /**
     * 描述
     */
    @Column(name = "depict")
    private String depict;


    /**
     * activtiti对应任务类型,如assinge、candidate
     */
    @Column(name = "act_type")
    private String actType;

    /**
     * 流程任务引擎实际的任务签收时间
     */
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "act_claim_time")
    private Date actClaimTime;


    /**
     * 优先级
     */
    private int priority;

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
     * 流程引擎的实际触发时间
     */
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "act_due_date")
    private Date actDueDate;


    /**
     * 流程引擎的实际任务定义KEY
     */
    @Column(name = "act_task_key")
    private String actTaskKey;

    /**
     * 记录上一个流程历史任务的id
     */
    @Column(name = "pre_id")
    private String preId;


    /**
     * 是否允许驳回
     */
    @Column(name = "canReject")
    private Boolean canReject;

    /**
     * 是否允许流程中止（退出流程）
     */
    @Column(name = "canSuspension")
    private Boolean canSuspension;

    /**
     * 任务定义JSON
     */
    @Column(name = "taskJsonDef")
    @Lob
    private String taskJsonDef;

//	/**
//	 * 业务摘要(工作说明)
//	 */
//	@Transient
//	private String businessModelRemark;

    /**
     * 额定工时（分钟）
     */
    @Column(name = "executeTime")
    private Integer executeTime;


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
     * 能否批量审批
     */
    @Column(name = "can_batch_approval")
    private Boolean canBatchApproval;

    /**
     * 移动端能否
     */
    @Column(name = "can_mobile")
    private Boolean canMobile;

    public FlowTask() {
    }


    public FlowTask(String flowName, String taskName, String actTaskDefKey,
                    String flowInstanceId, String flowDefinitionId, Date executeDate) {
        this.flowName = flowName;
        this.taskName = taskName;
        this.actTaskDefKey = actTaskDefKey;
//		this.flowInstanceId = flowInstanceId;
        this.flowDefinitionId = flowDefinitionId;

        this.executeDate = executeDate;

    }

    public FlowTask(FlowInstance flowInstance, String flowName,
                    String taskName, String actTaskDefKey, String taskFormUrl,
                    String taskStatus, String proxyStatus, String flowInstanceId,
                    String flowDefinitionId, String executorName,
                    String executorAccount, String candidateAccount,
                    Date executeDate, String depict) {
        this.flowInstance = flowInstance;
        this.flowName = flowName;
        this.taskName = taskName;
        this.actTaskDefKey = actTaskDefKey;
        this.taskFormUrl = taskFormUrl;
        this.taskStatus = taskStatus;
        this.proxyStatus = proxyStatus;
//		this.flowInstanceId = flowInstanceId;
        this.flowDefinitionId = flowDefinitionId;
        this.executorName = executorName;
        this.executorAccount = executorAccount;
        this.candidateAccount = candidateAccount;
        this.executeDate = executeDate;
        this.depict = depict;
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

    public String getTaskName() {
        return this.taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public String getActTaskDefKey() {
        return actTaskDefKey;
    }

    public void setActTaskDefKey(String actTaskDefKey) {
        this.actTaskDefKey = actTaskDefKey;
    }

    public String getTaskFormUrl() {
        return this.taskFormUrl;
    }

    public void setTaskFormUrl(String taskFormUrl) {
        this.taskFormUrl = taskFormUrl;
    }

    public String getTaskStatus() {
        return this.taskStatus;
    }

    public void setTaskStatus(String taskStatus) {
        this.taskStatus = taskStatus;
    }

    public String getProxyStatus() {
        return this.proxyStatus;
    }

    public void setProxyStatus(String proxyStatus) {
        this.proxyStatus = proxyStatus;
    }

    public String getFlowDefinitionId() {
        return this.flowDefinitionId;
    }

    public void setFlowDefinitionId(String flowDefinitionId) {
        this.flowDefinitionId = flowDefinitionId;
    }

    public String getExecutorName() {
        return this.executorName;
    }

    public void setExecutorName(String executorName) {
        this.executorName = executorName;
    }

    public String getExecutorAccount() {
        return this.executorAccount;
    }

    public void setExecutorAccount(String executorAccount) {
        this.executorAccount = executorAccount;
    }

    public String getCandidateAccount() {
        return this.candidateAccount;
    }

    public void setCandidateAccount(String candidateAccount) {
        this.candidateAccount = candidateAccount;
    }

    public String getActTaskId() {
        return actTaskId;
    }

    public void setActTaskId(String actTaskId) {
        this.actTaskId = actTaskId;
    }

    public Date getExecuteDate() {
        return this.executeDate;
    }

    public Date getActClaimTime() {
        return actClaimTime;
    }

    public void setActClaimTime(Date actClaimTime) {
        this.actClaimTime = actClaimTime;
    }

    public String getActTaskKey() {
        return actTaskKey;
    }

    public void setActTaskKey(String actTaskKey) {
        this.actTaskKey = actTaskKey;
    }

    public void setExecuteDate(Date executeDate) {
        this.executeDate = executeDate;
    }

    public String getDepict() {
        return this.depict;
    }

    public void setDepict(String depict) {
        this.depict = depict;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
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

    public Date getActDueDate() {
        return actDueDate;
    }

    public void setActDueDate(Date actDueDate) {
        this.actDueDate = actDueDate;
    }

    public String getActType() {
        return actType;
    }

    public void setActType(String actType) {
        this.actType = actType;
    }

    public String getPreId() {
        return preId;
    }

    public void setPreId(String preId) {
        this.preId = preId;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public Boolean getCanReject() {
        return canReject;
    }

    public void setCanReject(Boolean canReject) {
        this.canReject = canReject;
    }

    public Boolean getCanSuspension() {
        return canSuspension;
    }

    public void setCanSuspension(Boolean canSuspension) {
        this.canSuspension = canSuspension;
    }

    public String getTaskJsonDef() {
        return taskJsonDef;
    }

    public void setTaskJsonDef(String taskJsonDef) {
        this.taskJsonDef = taskJsonDef;
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

    public Integer getExecuteTime() {
        return executeTime;
    }

    public void setExecuteTime(Integer executeTime) {
        this.executeTime = executeTime;
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("id", this.getId())
                .append("webBaseAddress", this.getWebBaseAddress())
                .append("apiBaseAddress", this.getApiBaseAddress())
                .append("canMobile", canMobile)
                .append("canBatchApproval", canBatchApproval)
                .append("flowInstance", flowInstance)
                .append("flowName", flowName)
                .append("taskName", taskName)
                .append("taskDefKey", actTaskDefKey)
                .append("taskFormUrl", taskFormUrl)
                .append("taskStatus", taskStatus)
                .append("proxyStatus", proxyStatus)
                .append("flowDefinitionId", flowDefinitionId)
                .append("executorName", executorName)
                .append("executorAccount", executorAccount)
                .append("candidateAccount", candidateAccount)
                .append("executeDate", executeDate)
                .append("depict", depict)
                .append("completeTaskServiceUrl",completeTaskServiceUrl)
                .toString();
    }

    @Override
    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj);
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

    public Boolean getCanBatchApproval() {
        return canBatchApproval;
    }

    public void setCanBatchApproval(Boolean canBatchApproval) {
        this.canBatchApproval = canBatchApproval;
    }


    public String getWebBaseAddress() {
        return webBaseAddress;
    }

    public void setWebBaseAddress(String webBaseAddress) {
        this.webBaseAddress = webBaseAddress;
    }

    public String getApiBaseAddress() {
        return apiBaseAddress;
    }

    public void setApiBaseAddress(String apiBaseAddress) {
        this.apiBaseAddress = apiBaseAddress;
    }

    public Boolean getCanMobile() {
        return canMobile;
    }

    public void setCanMobile(Boolean canMobile) {
        this.canMobile = canMobile;
    }

    public String getCompleteTaskServiceUrl() {
        return this.getFlowInstance().getFlowDefVersion().getFlowDefination().getFlowType().getBusinessModel().getCompleteTaskServiceUrl();
    }

    public void setCompleteTaskServiceUrl(String completeTaskServiceUrl) {
        this.completeTaskServiceUrl = completeTaskServiceUrl;
    }
}