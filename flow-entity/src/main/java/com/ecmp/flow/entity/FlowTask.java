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
import org.hibernate.annotations.GenericGenerator;
import java.sql.Timestamp;

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
	@Column(name = "taskDefKey", nullable = false)
	private String taskDefKey;

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
	 * 执行人名称
	 */
	@Column(name = "executorName", length = 80)
	private String executorName;

	/**
	 * 执行人账号
	 */
	@Column(name = "executorAccount")
	private Integer executorAccount;

	/**
	 * 候选人账号
	 */
	@Column(name = "candidateAccount")
	private Integer candidateAccount;

	/**
	 * 执行时间
	 */
	@Column(name = "executeDate", nullable = false, length = 19)
	private Timestamp executeDate;

	/**
	 * 描述
	 */
	@Column(name = "depict")
	private String depict;


	public FlowTask() {
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.append("id", this.getId())
				.append("flowInstance", flowInstance)
				.append("flowName", flowName)
				.append("taskName", taskName)
				.append("taskDefKey", taskDefKey)
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

	public FlowTask(String flowName, String taskName, String taskDefKey,
			String flowInstanceId, String flowDefinitionId, Timestamp executeDate) {
		this.flowName = flowName;
		this.taskName = taskName;
		this.taskDefKey = taskDefKey;
		this.flowInstanceId = flowInstanceId;
		this.flowDefinitionId = flowDefinitionId;

		this.executeDate = executeDate;

	}

	public FlowTask(FlowInstance flowInstance, String flowName,
			String taskName, String taskDefKey, String taskFormUrl,
			String taskStatus, String proxyStatus, String flowInstanceId,
			String flowDefinitionId, String executorName,
			Integer executorAccount, Integer candidateAccount,
					Timestamp executeDate, String depict) {
		this.flowInstance = flowInstance;
		this.flowName = flowName;
		this.taskName = taskName;
		this.taskDefKey = taskDefKey;
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

	public String getTaskDefKey() {
		return this.taskDefKey;
	}

	public void setTaskDefKey(String taskDefKey) {
		this.taskDefKey = taskDefKey;
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

	public Integer getExecutorAccount() {
		return this.executorAccount;
	}

	public void setExecutorAccount(Integer executorAccount) {
		this.executorAccount = executorAccount;
	}

	public Integer getCandidateAccount() {
		return this.candidateAccount;
	}

	public void setCandidateAccount(Integer candidateAccount) {
		this.candidateAccount = candidateAccount;
	}



	public Timestamp getExecuteDate() {
		return this.executeDate;
	}

	public void setExecuteDate(Timestamp executeDate) {
		this.executeDate = executeDate;
	}

	public String getDepict() {
		return this.depict;
	}

	public void setDepict(String depict) {
		this.depict = depict;
	}


}