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
public class FlowTask extends com.ecmp.core.entity.BaseEntity {


	/**
	 * 所属流程实例
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "flowInstance_id")
	private FlowInstance flowInstance;

	/**
	 * 名称
	 */
	@Column(name = "flowName", nullable = false, length = 80)
	private String flowName;

	/**
	 * 任务名
	 */
	@Column(name = "taskName", nullable = false, length = 80)
	private String taskName;

	/**
	 * 任务定义KEY
	 */
	@Column(name = "actTaskDefKey", nullable = false)
	private String actTaskDefKey ;

	/**
	 * 任务表单URL
	 */
	@Column(name = "taskFormUrl", length = 65535)
	private String taskFormUrl;

	/**
	 * 任务状态
	 */
	@Column(name = "taskStatus", length = 80)
	private String taskStatus;

	/**
	 * 代理状态
	 */
	@Column(name = "proxyStatus", length = 80)
	private String proxyStatus;

	/**
	 * 流程实例ID
	 */
	@Column(name = "flowInstanceId", nullable = false, length = 36)
	private String flowInstanceId;

	/**
	 * 流程定义ID
	 */
	@Column(name = "flowDefinitionId", nullable = false, length = 36)
	private String flowDefinitionId;

	/**
	 * 关联的实际流程引擎任务ID
	 */
	@Column(name = "actTaskId", nullable = false, length = 36)
	private String actTaskId;

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
	 * 执行时间
	 */
	@Column(name = "executeDate", nullable = false,length = 19)
	private Date executeDate;


	/**
	 * 描述
	 */
	@Column(name = "depict")
	private String depict;


	/**
	 * activtiti对应任务类型,如assinge、candidate
	 */
	@Column(name = "actType")
	private String actType;

	/**
	 * 流程任务引擎实际的任务签收时间
	 */
	private java.util.Date actClaimTime;


	/**

	 * 优先级
	 */
	private int priority;

	/**
	 * 任务所属人账号（拥有人）
	 */
	private String ownerAccount;

	/**
	 * 任务所属人名称（拥有人）
	 */
	private String ownerName;


	/**
	 * 流程引擎的实际触发时间
	 */
	private Date actDueDate;


	/**
	 * 流程引擎的实际任务定义KEY
	 */
	private String actTaskKey;

	/**
	 * 记录上一个流程历史任务的id
	 */
	private String preId;


	public FlowTask() {
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.append("id", this.getId())
				.append("flowInstance", flowInstance)
				.append("flowName", flowName)
				.append("taskName", taskName)
				.append("taskDefKey", actTaskDefKey )
				.append("taskFormUrl", taskFormUrl)
				.append("taskStatus", taskStatus)
				.append("proxyStatus", proxyStatus)
				.append("flowInstanceId", flowInstanceId)
				.append("flowDefinitionId", flowDefinitionId)
				.append("executorName", executorName)
				.append("executorAccount", executorAccount)
				.append("candidateAccount", candidateAccount)
				.append("executeDate", executeDate)
				.append("depict", depict)
				.toString();
	}

	public FlowTask(String flowName, String taskName, String actTaskDefKey ,
			String flowInstanceId, String flowDefinitionId, Date executeDate) {
		this.flowName = flowName;
		this.taskName = taskName;
		this.actTaskDefKey  = actTaskDefKey ;
		this.flowInstanceId = flowInstanceId;
		this.flowDefinitionId = flowDefinitionId;

		this.executeDate = executeDate;

	}

	public FlowTask(FlowInstance flowInstance, String flowName,
			String taskName, String actTaskDefKey , String taskFormUrl,
			String taskStatus, String proxyStatus, String flowInstanceId,
			String flowDefinitionId, String executorName,
					String executorAccount, String candidateAccount,
					Date executeDate, String depict) {
		this.flowInstance = flowInstance;
		this.flowName = flowName;
		this.taskName = taskName;
		this.actTaskDefKey  = actTaskDefKey ;
		this.taskFormUrl = taskFormUrl;
		this.taskStatus = taskStatus;
		this.proxyStatus = proxyStatus;
		this.flowInstanceId = flowInstanceId;
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

	public String getFlowInstanceId() {
		return this.flowInstanceId;
	}

	public void setFlowInstanceId(String flowInstanceId) {
		this.flowInstanceId = flowInstanceId;
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

	public java.util.Date getActClaimTime() {
		return actClaimTime;
	}

	public void setActClaimTime(java.util.Date actClaimTime) {
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
}