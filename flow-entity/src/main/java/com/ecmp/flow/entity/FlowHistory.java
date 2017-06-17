package com.ecmp.flow.entity;

import javax.persistence.*;

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
@Table(name = "flow_history", catalog = "ecmp_flow")
public class FlowHistory  extends com.ecmp.core.entity.BaseAuditableEntity  {

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
	@Column(name = "flow_run_id", length = 36,nullable = true)
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
	 * 描述
	 */
	@Column(name = "depict")
	private String depict;


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
	private String actTaskDefKey ;


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
	@Column(name = "canCancel")
	private Boolean canCancel;


	/**
	 * 任务定义JSON
	 */
	@Column(name = "taskJsonDef")
	@Lob
	private String taskJsonDef;

	/**
	 * 业务摘要(工作说明)
	 */
	@Transient
	private String businessModelRemark;



	public FlowHistory() {
	}

	/** minimal constructor */
	public FlowHistory(String flowName, String flowTaskName, String flowRunId,
			String flowInstanceId) {
		this.flowName = flowName;
		this.flowTaskName = flowTaskName;
		this.flowRunId = flowRunId;
//		this.flowInstanceId = flowInstanceId;
	}

	/** full constructor */
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
				.append("flowDefId", flowDefId)
				.append("depict", depict)
				.toString();
	}

	@Override
	public boolean equals(Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj);
	}
}
