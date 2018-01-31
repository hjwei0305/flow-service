package com.ecmp.flow.entity;

import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.hibernate.annotations.CacheConcurrencyStrategy;
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
@Cacheable(true)
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
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
	 * 业务单号
	 */
	@Column(name = "business_code", nullable = false, length = 2000)
	private String businessCode;

	/**
	 * 业务单据名称
	 */
	@Column(name = "business_name", nullable = false, length = 100)
	private String businessName;


	/**
	 * 业务摘要(工作说明)
	 */
	@Column(name = "businessModelRemark")
	private String businessModelRemark;

//	/**
//	 * 业务额外属性
//	 */
//	@Lob
//	@Basic(fetch = FetchType.LAZY)
//	@Column(name = "business_extra_map",columnDefinition="CLOB")
//	private Map<String,Object> businessExtraMap;

	/**
	 * 开始时间
	 */
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "start_date", nullable = false, length = 19)
	private Date startDate;

	/**
	 * 结束时间
	 */
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "end_date", nullable = false, length = 19)
	private Date endDate;

	/**
	 * 关联的实际流程引擎实例ID
	 */
	@Column(name = "act_instance_id", nullable = false, length = 36)
	private String actInstanceId;


	/**
	 * 所属流程定义版本
	 */
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "parent_id")
	private FlowInstance parent;

	/**
	 * 实例调用路径，针对作为子流程被调用时
	 */
	@Column(name = "call_activity_path", length = 5000)
	private String callActivityPath;


	/**
	 * 是否挂起
	 */
	private Boolean suspended=false;

	/**
	 * 是否结束
	 */
	private Boolean ended=false;


	/**
	 * 是否是手动结束（发起人手动终止任务的情况）
	 */
	@Column(name = "manuallyEnd")
	private Boolean manuallyEnd=false;


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

	public Boolean isSuspended() {
		return suspended;
	}

	public void setSuspended(Boolean suspended) {
		this.suspended = suspended;
	}

	public Boolean isEnded() {
		return ended;
	}

	public void setEnded(Boolean ended) {
		this.ended = ended;
	}

	public Integer getVersion() {
		return version;
	}

	public void setVersion(Integer version) {
		this.version = version;
	}

	public String getBusinessCode() {
		return businessCode;
	}

	public void setBusinessCode(String businessCode) {
		this.businessCode = businessCode;
	}

	public String getBusinessModelRemark() {
		return businessModelRemark;
	}

	public void setBusinessModelRemark(String businessModelRemark) {
		this.businessModelRemark = businessModelRemark;
	}

	public String getBusinessName() {
		return businessName;
	}

	public void setBusinessName(String businessName) {
		this.businessName = businessName;
	}

//	public Map<String, Object> getBusinessExtraMap() {
//		return businessExtraMap;
//	}
//
//	public void setBusinessExtraMap(Map<String, Object> businessExtraMap) {
//		this.businessExtraMap = businessExtraMap;
//	}

	public Boolean isManuallyEnd() {
		return manuallyEnd;
	}

	public void setManuallyEnd(Boolean manuallyEnd) {
		this.manuallyEnd = manuallyEnd;
	}

	public String getCallActivityPath() {
		return callActivityPath;
	}

	public void setCallActivityPath(String callActivityPath) {
		this.callActivityPath = callActivityPath;
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
				.append("webBaseAddress", this.getWebBaseAddress())
				.append("apiBaseAddress", this.getApiBaseAddress())
				.append("webBaseAddressAbsolute",this.getWebBaseAddressAbsolute())
				.append("apiBaseAddressAbsolute",this.getApiBaseAddressAbsolute())
				.toString();
	}

	@Override
	public boolean equals(Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj);
	}

	public FlowInstance getParent() {
		return parent;
	}

	public void setParent(FlowInstance parent) {
		this.parent = parent;
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
	@JsonIgnore(false)
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


}