package com.ecmp.flow.entity;

import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.*;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.hibernate.annotations.GenericGenerator;

/**
 * 流程实例实体
 */
@Entity
@Table(name = "flow_instance", catalog = "ecmp_flow")
public class FlowInstance extends com.ecmp.core.entity.BaseEntity {

	/**
	 * 所属流程定义版本
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "flowDefVersion_id")
	private FlowDefVersion flowDefVersion;

	/**
	 * 流程名称
	 */
	@Column(name = "flowName", nullable = false, length = 80)
	private String flowName;

	/**
	 * 业务ID
	 */
	@Column(name = "businessId", nullable = false, length = 36)
	private String businessId;

	/**
	 * 开始时间
	 */
	@Column(name = "startDate", nullable = false, length = 19)
	private Timestamp startDate;

	/**
	 * 结束时间
	 */
	@Column(name = "endDate", nullable = false, length = 19)
	private Timestamp endDate;

	/**
	 * 拥有的流程历史
	 */
	@Transient
	private Set<FlowHistory> flowHistories = new HashSet<FlowHistory>(0);

	/**
	 * 拥有的流程任务
	 */
	@Transient
	private Set<FlowTask> flowTasks = new HashSet<FlowTask>(0);


	public FlowInstance() {
	}

	public FlowInstance(String flowName, String businessId,
						Timestamp startDate, Timestamp endDate) {
		this.flowName = flowName;
		this.businessId = businessId;
		this.startDate = startDate;
		this.endDate = endDate;
	}

	public FlowInstance(FlowDefVersion flowDefVersion, String flowName,
			String businessId, Timestamp startDate, Timestamp endDate,
			Set<FlowHistory> flowHistories, Set<FlowTask> flowTasks) {
		this.flowDefVersion = flowDefVersion;
		this.flowName = flowName;
		this.businessId = businessId;
		this.startDate = startDate;
		this.endDate = endDate;
		this.flowHistories = flowHistories;
		this.flowTasks = flowTasks;
	}



	public FlowDefVersion getFlowDefVersion() {
		return this.flowDefVersion;
	}

	public void setFlowDefVersion(FlowDefVersion flowDefVersion) {
		this.flowDefVersion = flowDefVersion;
	}

	public String getFlowName() {
		return this.flowName;
	}

	public void setFlowName(String flowName) {
		this.flowName = flowName;
	}

	public String getBusinessId() {
		return this.businessId;
	}

	public void setBusinessId(String businessId) {
		this.businessId = businessId;
	}

	public Timestamp getStartDate() {
		return this.startDate;
	}

	public void setStartDate(Timestamp startDate) {
		this.startDate = startDate;
	}

	public Timestamp getEndDate() {
		return this.endDate;
	}

	public void setEndDate(Timestamp endDate) {
		this.endDate = endDate;
	}

	public Set<FlowHistory> getFlowHistories() {
		return this.flowHistories;
	}

	public void setFlowHistories(Set<FlowHistory> flowHistories) {
		this.flowHistories = flowHistories;
	}

	public Set<FlowTask> getFlowTasks() {
		return this.flowTasks;
	}

	public void setFlowTasks(Set<FlowTask> flowTasks) {
		this.flowTasks = flowTasks;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.append("id", this.getId())
				.append("flowDefVersion", flowDefVersion)
				.append("flowName", flowName)
				.append("businessId", businessId)
				.append("startDate", startDate)
				.append("endDate", endDate)
				.append("flowHistories", flowHistories)
				.append("flowTasks", flowTasks)
				.toString();
	}
}