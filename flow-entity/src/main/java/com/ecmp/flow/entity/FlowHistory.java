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

/**
 * 流程历史实体
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
	 * 任务拥有人
	 */
	@Column(name = "taskOwner", length = 100)
	private String taskOwner;

	/**
	 * 任务执行人
	 */
	@Column(name = "taskExecutor", length = 100)
	private String taskExecutor;

	/**
	 * 任务候选人
	 */
	@Column(name = "taskCandidate", length = 100)
	private String taskCandidate;

	/**
	 * 描述
	 */
	@Column(name = "depict")
	private String depict;

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
			String flowDefId, String taskOwner, String taskExecutor,
			String taskCandidate, String depict) {
		this.flowInstance = flowInstance;
		this.flowName = flowName;
		this.flowTaskName = flowTaskName;
		this.flowRunId = flowRunId;
		this.flowInstanceId = flowInstanceId;
		this.flowDefId = flowDefId;
		this.taskOwner = taskOwner;
		this.taskExecutor = taskExecutor;
		this.taskCandidate = taskCandidate;
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

	public String getTaskOwner() {
		return this.taskOwner;
	}

	public void setTaskOwner(String taskOwner) {
		this.taskOwner = taskOwner;
	}

	public String getTaskExecutor() {
		return this.taskExecutor;
	}

	public void setTaskExecutor(String taskExecutor) {
		this.taskExecutor = taskExecutor;
	}

	public String getTaskCandidate() {
		return this.taskCandidate;
	}

	public void setTaskCandidate(String taskCandidate) {
		this.taskCandidate = taskCandidate;
	}

	public String getDepict() {
		return this.depict;
	}

	public void setDepict(String depict) {
		this.depict = depict;
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
				.append("taskOwner", taskOwner)
				.append("taskExecutor", taskExecutor)
				.append("taskCandidate", taskCandidate)
				.append("depict", depict)
				.toString();
	}
}
