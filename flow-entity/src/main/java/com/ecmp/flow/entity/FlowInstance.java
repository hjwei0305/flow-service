package com.ecmp.flow.entity;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.*;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.hibernate.annotations.GenericGenerator;

/**
 * *************************************************************************************************
 * <p/>
 * 实现功能：
 * 流程实例模型Entity定义
 * <p>
 * ------------------------------------------------------------------------------------------------
 * 版本          变更时间             变更人                     变更原因
 * ------------------------------------------------------------------------------------------------
 * 1.0.00      2017/3/21 10:20      詹耀(zhanyao)                新建
 * <p/>
 * *************************************************************************************************
 */
@Entity
@Table(name = "flow_instance", catalog = "ecmp_flow")
public class FlowInstance extends com.ecmp.core.entity.BaseAuditableEntity {

	/**
	 * 乐观锁-版本
	 */
	//@Version
	@Column(name = "version")
	private Integer version = 0;

	/**
	 * 所属流程定义版本
	 */
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "flow_def_version_id")
	private FlowDefVersion flowDefVersion;

	/**
	 * 流程名称
	 */
	@Column(name = "flow_name", nullable = false, length = 80)
	private String flowName;

	/**
	 * 业务ID
	 */
	@Column(name = "business_id", nullable = false, length = 36)
	private String businessId;

	/**
	 * 开始时间
	 */
	@Column(name = "start_date", nullable = false, length = 19)
	private Date startDate;

	/**
	 * 结束时间
	 */
	@Column(name = "end_date", nullable = false, length = 19)
	private Date endDate;

	/**
	 * 关联的实际流程引擎实例ID
	 */
	@Column(name = "act_instance_id", nullable = false, length = 36)
	private String actInstanceId;


	/**
	 * 是否挂起
	 */
	private boolean suspended;

	/**
	 * 是否结束
	 */
	private boolean ended;

	/**
	 * 拥有的流程历史
	 */
	@Transient
//	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "flowInstance")
	private Set<FlowHistory> flowHistories = new HashSet<FlowHistory>(0);

	/**
	 * 拥有的流程任务
	 */
	@Transient
//	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "flowInstance")
	private Set<FlowTask> flowTasks = new HashSet<FlowTask>(0);


	public FlowInstance() {
	}

	public FlowInstance(String flowName, String businessId,
						Date startDate, Date endDate) {
		this.flowName = flowName;
		this.businessId = businessId;
		this.startDate = startDate;
		this.endDate = endDate;
	}

	public FlowInstance(FlowDefVersion flowDefVersion, String flowName,
			String businessId, Date startDate, Date endDate,
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

	public Date getStartDate() {
		return this.startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public Date getEndDate() {
		return this.endDate;
	}

	public void setEndDate(Date endDate) {
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

	public String getActInstanceId() {
		return actInstanceId;
	}

	public void setActInstanceId(String actInstanceId) {
		this.actInstanceId = actInstanceId;
	}

	public boolean isSuspended() {
		return suspended;
	}

	public void setSuspended(boolean suspended) {
		this.suspended = suspended;
	}

	public boolean isEnded() {
		return ended;
	}

	public void setEnded(boolean ended) {
		this.ended = ended;
	}

	public Integer getVersion() {
		return version;
	}

	public void setVersion(Integer version) {
		this.version = version;
	}



	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this);
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

	@Override
	public boolean equals(Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj);
	}
}