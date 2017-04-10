package com.ecmp.flow.entity;

import java.util.HashSet;
import java.util.Set;
import javax.persistence.*;

import org.hibernate.annotations.GenericGenerator;

/**
 * *************************************************************************************************
 * <p/>
 * 实现功能：
 * 流程定义版本实体模型Entity定义
 * <p>
 * ------------------------------------------------------------------------------------------------
 * 版本          变更时间             变更人                     变更原因
 * ------------------------------------------------------------------------------------------------
 * 1.0.00      2017/3/21 10:20      詹耀(zhanyao)                新建
 * <p/>
 * *************************************************************************************************
 */
@Entity
@Table(name = "flow_defVersion", catalog = "ecmp_flow", uniqueConstraints = @UniqueConstraint(columnNames = "defKey"))
public class FlowDefVersion extends com.ecmp.core.entity.BaseEntity {


	/**
	 * 所属流程定义
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "flowDefination_id")
	private FlowDefination flowDefination;


	/**
	 * 定义ID
	 */

	@Column(name = "actDefId", length = 36)
	private String actDefId;

	/**
	 * 定义KEY
	 */
	@Column(name = "defKey", unique = true, nullable = false)
	private String defKey;

	/**
	 * 名称
	 */
	@Column(name = "name", nullable = false, length = 80)
	private String name;

	/**
	 * 部署ID
	 */
	@Column(name = "actDeployId", length = 36)
	private String actDeployId;

	/**
	 * 启动条件UEL
	 */
	@Column(name = "startUel")
	private String startUel;

	/**
	 * 版本号
	 */
	@Column(name = "versionCode")
	private Integer versionCode;

	/**
	 * 优先级
	 */
	@Column(name = "priority")
	private Integer priority;

	/**
	 * 流程JSON文本
	 */
	@Column(name = "defJson", length = 65535)
	private String defJson;

	/**
	 * 流程BPMN文本
	 */
	@Column(name = "defBpmn", length = 65535)
	private String defBpmn;

	/**
	 * 最终定义XML
	 */
	@Column(name = "defXML", length = 65535)
	private String defXml;

	/**
	 * 描述
	 */
	@Column(name = "depict")
	private String depict;

	/**
	 * 拥有的流程实例
	 */
	@Transient
//	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "flowDefVersion")
	private Set<FlowInstance> flowInstances = new HashSet<FlowInstance>(0);

	// Constructors

	/** default constructor */
	public FlowDefVersion() {
	}

	/** minimal constructor */
	public FlowDefVersion(String defKey, String name) {
		this.defKey = defKey;
		this.name = name;

	}

	/** full constructor */
	public FlowDefVersion(FlowDefination flowDefination, String defKey,
			String name, String actDeployId, String startUel,
			Integer versionCode, Integer priority, String defJson,
			String defBpmn, String defXml, String depict, Set<FlowInstance> flowInstances) {
		this.flowDefination = flowDefination;
		this.defKey = defKey;
		this.name = name;
		this.actDeployId = actDeployId;
		this.startUel = startUel;
		this.versionCode = versionCode;
		this.priority = priority;
		this.defJson = defJson;
		this.defBpmn = defBpmn;
		this.defXml = defXml;
		this.depict = depict;
		this.flowInstances = flowInstances;
	}




	public FlowDefination getFlowDefination() {
		return this.flowDefination;
	}

	public void setFlowDefination(FlowDefination flowDefination) {
		this.flowDefination = flowDefination;
	}

	public String getActDefId() {
		return actDefId;
	}

	public void setActDefId(String actDefId) {
		this.actDefId = actDefId;
	}

	public String getDefKey() {
		return this.defKey;
	}

	public void setDefKey(String defKey) {
		this.defKey = defKey;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getActDeployId() {
		return this.actDeployId;
	}

	public void setActDeployId(String actDeployId) {
		this.actDeployId = actDeployId;
	}

	public String getStartUel() {
		return this.startUel;
	}

	public void setStartUel(String startUel) {
		this.startUel = startUel;
	}

	public Integer getVersionCode() {
		return this.versionCode;
	}

	public void setVersionCode(Integer versionCode) {
		this.versionCode = versionCode;
	}

	public Integer getPriority() {
		return this.priority;
	}

	public void setPriority(Integer priority) {
		this.priority = priority;
	}

	public String getDefJson() {
		return this.defJson;
	}

	public void setDefJson(String defJson) {
		this.defJson = defJson;
	}

	public String getDefBpmn() {
		return this.defBpmn;
	}

	public void setDefBpmn(String defBpmn) {
		this.defBpmn = defBpmn;
	}

	public String getDefXml() {
		return this.defXml;
	}

	public void setDefXml(String defXml) {
		this.defXml = defXml;
	}

	public String getDepict() {
		return this.depict;
	}

	public void setDepict(String depict) {
		this.depict = depict;
	}


	public Set<FlowInstance> getFlowInstances() {
		return this.flowInstances;
	}

	public void setFlowInstances(Set<FlowInstance> flowInstances) {
		this.flowInstances = flowInstances;
	}

}