package com.ecmp.flow.entity;


import java.util.HashSet;
import java.util.Set;
import javax.persistence.*;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.hibernate.annotations.GenericGenerator;

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
@Table(name = "flow_defination", catalog = "ecmp_flow", uniqueConstraints = @UniqueConstraint(columnNames = "code"))
public class FlowDefination extends com.ecmp.core.entity.BaseEntity {

	/**
	 * 所属流程类型
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "flowType_id")
	private FlowType flowType;

	/**
	 * 代码
	 */
	@Column(name = "code", unique = true, nullable = false)
	private String code;

	/**
	 * 名称
	 */
	@Column(name = "name", nullable = false, length = 80)
	private String name;

	/**
	 * 最新版本ID
	 */
	@Column(name = "lastVersionId")
	private Integer lastVersionId;

	/**
	 * 启动条件UEL
	 */
	@Column(name = "startUel")
	private String startUel;

	/**
	 * 描述
	 */
	@Column(name = "depict")
	private String depict;

	/**
	 * 拥有的流程定义版本
	 */
	@Transient
//	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "flowDefination")
	private Set<FlowDefVersion> flowDefVersions = new HashSet<FlowDefVersion>(0);


	public FlowDefination() {
	}


	public FlowDefination(String code, String name) {
		this.code = code;
		this.name = name;

	}


	public FlowDefination(FlowType flowType, String code, String name,
			Integer lastVersionId, String startUel, String depict,
		Set<FlowDefVersion> flowDefVersions) {
		this.flowType = flowType;
		this.code = code;
		this.name = name;
		this.lastVersionId = lastVersionId;
		this.startUel = startUel;
		this.depict = depict;
		this.flowDefVersions = flowDefVersions;
	}




	public FlowType getFlowType() {
		return this.flowType;
	}

	public void setFlowType(FlowType flowType) {
		this.flowType = flowType;
	}

	public String getCode() {
		return this.code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getLastVersionId() {
		return this.lastVersionId;
	}

	public void setLastVersionId(Integer lastVersionId) {
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

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.append("id", this.getId())
				.append("flowType", flowType)
				.append("code", code)
				.append("name", name)
				.append("lastVersionId", lastVersionId)
				.append("startUel", startUel)
				.append("depict", depict)
				.append("flowDefVersions", flowDefVersions)
				.toString();
	}
}