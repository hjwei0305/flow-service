package com.ecmp.flow.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Version;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.hibernate.annotations.GenericGenerator;

import java.util.Date;

/**
 * *************************************************************************************************
 * <p/>
 * 实现功能：
 * 参数Entity定义
 * <p>
 * ------------------------------------------------------------------------------------------------
 * 版本          变更时间             变更人                     变更原因
 * ------------------------------------------------------------------------------------------------
 * 1.0.00      2017/4/25 10:20      谭军(tanjun)                新建
 * <p/>
 * *************************************************************************************************
 */
@Entity
@Table(name = "flow_variable", catalog = "ecmp_flow")
public class FlowVariable extends com.ecmp.core.entity.BaseAuditableEntity implements
		java.io.Serializable {

	/**
	 * 乐观锁-版本
	 */
	//@Version
	@Column(name = "version")
	private Integer version = 0;
	/**
	 * 关联流程实例
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "instance_id")
	private FlowInstance flowInstance;

	/**
	 * 关联任务历史
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "task_id")
	private FlowTask flowTask;

	/**
	 * java对应的类型
	 */
	@Column(name = "type", nullable = false, length = 20)
	private String type;

	/**
	 * field名称
	 */
	@Column(name = "name", nullable = false, length = 80)
	private String name;

	/**
	 * 流程定义版本ID
	 */
	@Column(name = "def_version_id", length = 36)
	private String defVersionId;

	/**
	 * 流程定义ID
	 */
	@Column(name = "defination_id", length = 36)
	private String definationId;

	/**
	 * 流程引擎实际的任务ID
	 */
	@Column(name = "act_task_id", length = 36)
	private String actTaskId;

	/**
	 * 流程引擎实际的流程实例ID
	 */
	@Column(name = "act_instance_id", length = 36)
	private String actInstanceId;

	/**
	 * 流程引擎实际的流程定义ID
	 */
	@Column(name = "act_defination_id", length = 36)
	private String actDefinationId;

	/**
	 * 描述
	 */
	@Column(name = "depict")
	private String depict;

	/**
	 * float、double值
	 */
	@Column(name = "v_double", precision = 22, scale = 0)
	private Double VDouble;

	/**
	 * long值
	 */
	@Column(name = "v_long")
	private Long VLong;

	/**
	 * 文本值
	 */
	@Column(name = "v_text", length = 4000)
	private String VText;

	/** default constructor */
	public FlowVariable() {
	}


	public Integer getVersion() {
		return version;
	}

	public void setVersion(Integer version) {
		this.version = version;
	}

	public FlowInstance getFlowInstance() {
		return flowInstance;
	}

	public void setFlowInstance(FlowInstance flowInstance) {
		this.flowInstance = flowInstance;
	}

	public FlowTask getFlowTask() {
		return flowTask;
	}

	public void setFlowTask(FlowTask flowTask) {
		this.flowTask = flowTask;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDefVersionId() {
		return defVersionId;
	}

	public void setDefVersionId(String defVersionId) {
		this.defVersionId = defVersionId;
	}

	public String getDefinationId() {
		return definationId;
	}

	public void setDefinationId(String definationId) {
		this.definationId = definationId;
	}

	public String getActTaskId() {
		return actTaskId;
	}

	public void setActTaskId(String actTaskId) {
		this.actTaskId = actTaskId;
	}

	public String getActInstanceId() {
		return actInstanceId;
	}

	public void setActInstanceId(String actInstanceId) {
		this.actInstanceId = actInstanceId;
	}

	public String getActDefinationId() {
		return actDefinationId;
	}

	public void setActDefinationId(String actDefinationId) {
		this.actDefinationId = actDefinationId;
	}

	public String getDepict() {
		return depict;
	}

	public void setDepict(String depict) {
		this.depict = depict;
	}

	public Double getVDouble() {
		return VDouble;
	}

	public void setVDouble(Double VDouble) {
		this.VDouble = VDouble;
	}

	public Long getVLong() {
		return VLong;
	}

	public void setVLong(Long VLong) {
		this.VLong = VLong;
	}

	public String getVText() {
		return VText;
	}

	public void setVText(String VText) {
		this.VText = VText;
	}

	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this);
	}


	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.append("id", this.getId())
				.append("name", name)
				.append("type", type)
				.append("depict", depict)
				.append("flowInstance", flowInstance)
				.append("flowTask", flowTask)
				.toString();
	}

	@Override
	public boolean equals(Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj);
	}
}