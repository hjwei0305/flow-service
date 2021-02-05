package com.ecmp.flow.entity;


import javax.persistence.*;

import com.ecmp.core.entity.ITenant;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.Date;

/**
 * 流程待办任务
 */
@Entity
@Table(name = "flow_task")
public class FlowTask extends com.ecmp.core.entity.BaseAuditableEntity implements ITenant {


    /**
     * 表单相对路径
     */
    @Transient
    private String taskFormUrlXiangDui;

    /**
     * web基地址（跟随节点配置的工作界面模块）
     */
    @Transient
    private String webBaseAddress;


    /**
     * web基地址绝对路径（跟随节点配置的工作界面模块）
     */
    @Transient
    private String webBaseAddressAbsolute;


    /**
     * web基地址
     */
    @Transient
    private String lookWebBaseAddress;


    /**
     * web基地址绝对路径
     */
    @Transient
    private String lookWebBaseAddressAbsolute;

    /**
     * api基地址
     */
    @Transient
    private String apiBaseAddress;

    /**
     * api基地址绝对路径
     */
    @Transient
    private String apiBaseAddressAbsolute;


    /**
     * 提交任务地址（react不在使用）
     */
    @Transient
    private String completeTaskServiceUrl;

    /**
     * 表单明细地址
     */
    @Transient
    private String businessDetailServiceUrl;

    /**
     * 乐观锁- 版本
     */
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
    @Column(name = "flow_name")
    private String flowName;

    /**
     * 任务名
     */
    @Column(name = "task_name")
    private String taskName;

    /**
     * 任务定义KEY
     */
    @Column(name = "act_task_def_key")
    private String actTaskDefKey;

    /**
     * 任务表单URL
     */
    @Transient
    private String taskFormUrl;

    /**
     * 任务状态
     */
    @Column(name = "task_status")
    private String taskStatus;

    /**
     * 代理状态
     */
    @Column(name = "proxy_status")
    private String proxyStatus;


    /**
     * 所属流程实例
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "work_page_url_id")
    private WorkPageUrl workPageUrl;

    /**
     * 流程定义ID
     */
    @Column(name = "flow_definition_id")
    private String flowDefinitionId;

    /**
     * 关联的实际流程引擎任务ID
     */
    @Column(name = "act_task_id")
    private String actTaskId;

    /**
     * 执行人名称
     */
    @Column(name = "executor_name")
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
    @Column(name = "execute_date")
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
    @Column(name = "priority")
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
    @Column(name = "can_reject")
    private Boolean canReject;

    /**
     * 是否允许流程中止（退出流程）
     */
    @Column(name = "can_suspension")
    private Boolean canSuspension;

    /**
     * 任务定义JSON
     */
    @Column(name = "task_json_def")
    @Lob
    private String taskJsonDef;

    /**
     * 额定工时（分钟）
     */
    @Column(name = "execute_time")
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

    /**
     * 1：发起委托的任务，2：被委托的任务，3：委托完成
     * 0：转办的任务（现取消，转办后任然可以转办）
     */
    @Column(name = "trust_state")
    private Integer trustState;

    /**
     * 被委托的任务id
     */
    @Column(name = "trust_owner_task_id")
    private String trustOwnerTaskId;

    /**
     * 允许加签
     */
    @Column(name = "allow_add_sign")
    private Boolean allowAddSign;


    /**
     * 允许减签
     */
    @Column(name = "allow_subtract_sign")
    private Boolean allowSubtractSign;//允许减签


    /**
     * 租户代码
     */
    @Column(name = "tenant_code", length = 10)
    private String tenantCode;


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
     * 任务额定工时（小时）
     */
    @Column(name = "timing")
    private Double timing;


    /**
     * 预警状态
     */
    @Transient
    private String warningStatus;


    public FlowTask() {
    }


    public FlowTask(String flowName, String taskName, String actTaskDefKey,
                    String flowDefinitionId, Date executeDate) {
        this.flowName = flowName;
        this.taskName = taskName;
        this.actTaskDefKey = actTaskDefKey;
        this.flowDefinitionId = flowDefinitionId;
        this.executeDate = executeDate;
    }

    public FlowTask(FlowInstance flowInstance, String flowName,
                    String taskName, String actTaskDefKey, String taskFormUrl,
                    String taskStatus, String proxyStatus,
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
        this.flowDefinitionId = flowDefinitionId;
        this.executorName = executorName;
        this.executorAccount = executorAccount;
        this.candidateAccount = candidateAccount;
        this.executeDate = executeDate;
        this.depict = depict;
    }

    public Double getTiming() {
        return timing;
    }

    public void setTiming(Double timing) {
        this.timing = timing;
    }

    public String getWarningStatus() {
        return warningStatus;
    }

    public void setWarningStatus(String warningStatus) {
        this.warningStatus = warningStatus;
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
    public String toString() {
        return new ToStringBuilder(this)
                .append("id", this.getId())
                .append("webBaseAddress", this.getWebBaseAddress())
                .append("apiBaseAddress", this.getApiBaseAddress())
                .append("webBaseAddressAbsolute", this.getWebBaseAddressAbsolute())
                .append("apiBaseAddressAbsolute", this.getApiBaseAddressAbsolute())
                .append("completeTaskServiceUrl", this.getCompleteTaskServiceUrl())
                .append("taskFormUrl", this.getTaskFormUrl())
                .append("canMobile", canMobile)
                .append("canBatchApproval", canBatchApproval)
                .append("flowInstance", flowInstance)
                .append("flowName", flowName)
                .append("taskName", taskName)
                .append("taskDefKey", actTaskDefKey)
                .append("taskStatus", taskStatus)
                .append("proxyStatus", proxyStatus)
                .append("flowDefinitionId", flowDefinitionId)
                .append("executorName", executorName)
                .append("executorAccount", executorAccount)
                .append("candidateAccount", candidateAccount)
                .append("executeDate", executeDate)
                .append("depict", depict)
                .append("trustState", trustState)
                .append("trustOwnerTaskId", trustOwnerTaskId)
                .append("businessDetailServiceUrl", businessDetailServiceUrl)
                .toString();
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

    public String getBusinessDetailServiceUrl() {
        return businessDetailServiceUrl;
    }

    public void setBusinessDetailServiceUrl(String businessDetailServiceUrl) {
        this.businessDetailServiceUrl = businessDetailServiceUrl;
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

    public WorkPageUrl getWorkPageUrl() {
        return workPageUrl;
    }

    public void setWorkPageUrl(WorkPageUrl workPageUrl) {
        this.workPageUrl = workPageUrl;
    }


    public String getTaskFormUrlXiangDui() {
        return taskFormUrlXiangDui;
    }


    public void setTaskFormUrlXiangDui(String taskFormUrlXiangDui) {
        this.taskFormUrlXiangDui = taskFormUrlXiangDui;
    }

    public String getWebBaseAddressAbsolute() {
        return webBaseAddressAbsolute;
    }

    public void setWebBaseAddressAbsolute(String webBaseAddressAbsolute) {
        this.webBaseAddressAbsolute = webBaseAddressAbsolute;
    }

    public String getApiBaseAddressAbsolute() {
        return apiBaseAddressAbsolute;
    }

    public void setApiBaseAddressAbsolute(String apiBaseAddressAbsolute) {
        this.apiBaseAddressAbsolute = apiBaseAddressAbsolute;
    }

    public String getCompleteTaskServiceUrl() {
        return completeTaskServiceUrl;
    }

    public void setCompleteTaskServiceUrl(String completeTaskServiceUrl) {
        this.completeTaskServiceUrl = completeTaskServiceUrl;
    }

    public String getTaskFormUrl() {
        return taskFormUrl;
    }

    public void setTaskFormUrl(String taskFormUrl) {
        this.taskFormUrl = taskFormUrl;
    }

    public Integer getTrustState() {
        return trustState;
    }

    public void setTrustState(Integer trustState) {
        this.trustState = trustState;
    }

    public String getTrustOwnerTaskId() {
        return trustOwnerTaskId;
    }

    public void setTrustOwnerTaskId(String trustOwnerTaskId) {
        this.trustOwnerTaskId = trustOwnerTaskId;
    }

    @Override
    public String getTenantCode() {
        return tenantCode;
    }

    @Override
    public void setTenantCode(String tenantCode) {
        this.tenantCode = tenantCode;
    }

    public Boolean getAllowAddSign() {
        return allowAddSign;
    }

    public void setAllowAddSign(Boolean allowAddSign) {
        this.allowAddSign = allowAddSign;
    }

    public Boolean getAllowSubtractSign() {
        return allowSubtractSign;
    }

    public void setAllowSubtractSign(Boolean allowSubtractSign) {
        this.allowSubtractSign = allowSubtractSign;
    }

    public String getLookWebBaseAddress() {
        return lookWebBaseAddress;
    }

    public void setLookWebBaseAddress(String lookWebBaseAddress) {
        this.lookWebBaseAddress = lookWebBaseAddress;
    }

    public String getLookWebBaseAddressAbsolute() {
        return lookWebBaseAddressAbsolute;
    }

    public void setLookWebBaseAddressAbsolute(String lookWebBaseAddressAbsolute) {
        this.lookWebBaseAddressAbsolute = lookWebBaseAddressAbsolute;
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