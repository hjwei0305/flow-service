package com.ecmp.flow.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
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
public class FlowHistory  extends com.ecmp.core.entity.BaseEntity  {

	/**
	 * 所属流程实例
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "flowInstance_id")
	private FlowInstance flowInstance;

	/**
	 * 流程名称
	 */
	@Column(name = "flowName", nullable = false, length = 80)
	private String flowName;

	/**
	 * 流程任务名
	 */
	@Column(name = "flowTaskName", nullable = false, length = 80)
	private String flowTaskName;

	/**
	 * 流程运行ID
	 */
	@Column(name = "flowRunId", nullable = false, length = 36)
	private String flowRunId;

	/**
	 * 流程实例ID
	 */
	@Column(name = "flowInstanceId", nullable = false, length = 36)
	private String flowInstanceId;

	/**
	 * 流程定义ID
	 */
	@Column(name = "flowDefId", length = 36)
	private String flowDefId;


	/**
	 * 关联的实际流程引擎历史ID
	 */
	@Column(name = "actHistoryId", nullable = false, length = 36)
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
	@Column(name = "actType")
	private String actType;

	/**
	 * 流程引擎的实际任务定义KEY
	 */
	private String actTaskKey;


	/**
	 * 执行人名称
	 */
	@Column(name = "executorName", length = 80)
	private String executorName;

	/**
	 * 执行人账号
	 */
	@Column(name = "executorAccount")
	private String executorAccount;

	/**
	 * 候选人账号
	 */
	@Column(name = "candidateAccount")
	private String candidateAccount;

	/**
	 * 任务所属人账号（拥有人）
	 */
	private String ownerAccount;

	/**
	 * 任务所属人名称（拥有人）
	 */
	private String ownerName;

	public FlowHistory() {
	}

	/** minimal constructor */
	public FlowHistory(String flowName, String flowTaskName, String flowRunId,
			String flowInstanceId) {
		this.flowName = flowName;
		this.flowTaskName = flowTaskName;
		this.flowRunId = flowRunId;
		this.flowInstanceId = flowInstanceId;
	}

	/** full constructor */
	public FlowHistory(FlowInstance flowInstance, String flowName,
			String flowTaskName, String flowRunId, String flowInstanceId,
			String flowDefId, String depict) {
		this.flowInstance = flowInstance;
		this.flowName = flowName;
		this.flowTaskName = flowTaskName;
		this.flowRunId = flowRunId;
		this.flowInstanceId = flowInstanceId;
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

	public String getFlowInstanceId() {
		return this.flowInstanceId;
	}

	public void setFlowInstanceId(String flowInstanceId) {
		this.flowInstanceId = flowInstanceId;
	}

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


	public String getActTaskKey() {
		return actTaskKey;
	}

	public void setActTaskKey(String actTaskKey) {
		this.actTaskKey = actTaskKey;
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

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.append("id", this.getId())
				.append("flowInstance", flowInstance)

				.append("flowName", flowName)
				.append("flowTaskName", flowTaskName)
				.append("flowRunId", flowRunId)

				.append("flowInstanceId", flowInstanceId)
				.append("flowDefId", flowDefId)
				.append("depict", depict)
				.toString();
	}
}
