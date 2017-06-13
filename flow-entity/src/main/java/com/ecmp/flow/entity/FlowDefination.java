package com.ecmp.flow.entity;


import java.util.HashSet;
import java.util.Set;
import javax.persistence.*;

import com.ecmp.flow.constant.FlowDefinationStatus;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * *************************************************************************************************
 * <p/>
 * 实现功能：
 * 流程定义实体模型Entity定义
 * <p>
 * ------------------------------------------------------------------------------------------------
 * 版本          变更时间             变更人                     变更原因
 * ------------------------------------------------------------------------------------------------
 * 1.0.00      2017/3/21 10:20      詹耀(zhanyao)                新建
 * <p/>
 * *************************************************************************************************
 */
@Entity
@Table(name = "flow_defination", catalog = "ecmp_flow", uniqueConstraints = @UniqueConstraint(columnNames = "def_key"))
public class FlowDefination extends com.ecmp.core.entity.BaseAuditableEntity {

	/**
	 * 乐观锁-版本
	 */
	//@Version
	@Column(name = "version")
	private Integer version = 0;

	/**
	 * 所属流程类型
	 */
	@ManyToOne()
	@JoinColumn(name = "flow_type_id")
	private FlowType flowType;

	/**
	 * 代码
	 */
	@Column(name = "def_key", unique = true, nullable = false)
	private String defKey;

	/**
	 * 名称
	 */
	@Column(name = "name", nullable = false, length = 80)
	private String name;

	/**
	 * 最新版本ID
	 */
	@Column(name = "last_version_id",length = 36)
	private String lastVersionId;


	/**
	 * 启动条件UEL
	 */
	@Column(name = "start_uel")
	private String startUel;

	/**
	 * 描述
	 */
	@Column(name = "depict")
	private String depict;

	/**
	 * 当前流程定义状态
	 */
	@Column(name = "flowDefinationStatus",length = 10,nullable = false)
	private FlowDefinationStatus flowDefinationStatus;

	/**
	 * 优先级
	 */
	@Column(name = "priority")
	private Integer priority;

	/**
	 * 组织机构id
	 */
	@Column(name="basic_org_id")
	private String orgId;

	/**
	 * 拥有的流程定义版本
	 */
	@Transient
//	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "flowDefination")
	private Set<FlowDefVersion> flowDefVersions = new HashSet<FlowDefVersion>(0);


	/**
	 * 当前对应的流程版本
	 */
	@Transient
	private FlowDefVersion currentFlowDefVersion;

	public FlowDefination() {
	}


	public FlowDefination(String defKey, String name) {
		this.defKey = defKey;
		this.name = name;

	}


	public FlowDefination(FlowType flowType, String defKey, String name,
						  String lastVersionId, String startUel, String depict,
		Set<FlowDefVersion> flowDefVersions) {
		this.flowType = flowType;
		this.defKey = defKey;
		this.name = name;
		this.lastVersionId = lastVersionId;
		this.startUel = startUel;
		this.depict = depict;
		this.flowDefVersions = flowDefVersions;
	}



	public FlowDefinationStatus getFlowDefinationStatus() {
		return flowDefinationStatus;
	}

	public void setFlowDefinationStatus(FlowDefinationStatus flowDefinationStatus) {
		this.flowDefinationStatus = flowDefinationStatus;
	}

	public Integer getPriority() {
		return priority;
	}

	public void setPriority(Integer priority) {
		this.priority = priority;
	}

	public FlowType getFlowType() {
		return this.flowType;
	}

	public void setFlowType(FlowType flowType) {
		this.flowType = flowType;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getLastVersionId() {
		return this.lastVersionId;
	}

	public void setLastVersionId(String lastVersionId) {
		this.lastVersionId = lastVersionId;
	}

	public String getStartUel() {
		return this.startUel;
	}

	public void setStartUel(String startUel) {
		this.startUel = startUel;
	}

	public String getDepict() {
		return this.depict;
	}

	public void setDepict(String depict) {
		this.depict = depict;
	}

	public Set<FlowDefVersion> getFlowDefVersions() {
		return this.flowDefVersions;
	}

	public void setFlowDefVersions(Set<FlowDefVersion> flowDefVersions) {
		this.flowDefVersions = flowDefVersions;
	}

	public FlowDefVersion getCurrentFlowDefVersion() {
		return currentFlowDefVersion;
	}

	public void setCurrentFlowDefVersion(FlowDefVersion currentFlowDefVersion) {
		this.currentFlowDefVersion = currentFlowDefVersion;
	}

	public String getDefKey() {
		return defKey;
	}

	public void setDefKey(String defKey) {
		this.defKey = defKey;
	}

	public Integer getVersion() {
		return version;
	}

	public void setVersion(Integer version) {
		this.version = version;
	}

	public String getOrgId() {
		return orgId;
	}

	public void setOrgId(String orgId) {
		this.orgId = orgId;
	}

	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this);
	}


	@Override
	public String toString() {

		return new ToStringBuilder(this)
				.append("id", this.getId())
				.append("flowType", flowType)
				.append("defKey", defKey)
				.append("name", name)
				.append("lastVersionId", lastVersionId)
				.append("startUel", startUel)
				.append("depict", depict)
				.append("flowDefVersions", flowDefVersions)
				.toString();
	}

	@Override
	public boolean equals(Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj);
	}
}