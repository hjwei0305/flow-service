package com.ecmp.flow.entity;

import com.ecmp.flow.constant.FlowDefinationStatus;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class FlowDefination implements Serializable {

	/**
	 * 乐观锁-版本
	 */
	private Integer version = 0;

	/**
	 * 所属流程类型
	 */
	private FlowType flowType;

	/**
	 * 代码
	 */
	private String defKey;

	/**
	 * 名称
	 */
	private String name;

	/**
	 * 最新版本ID
	 */
	private String lastVersionId;

	/**
	 * 最新已发布版本ID
	 */
	private String lastDeloyVersionId;



	/**
	 * 启动条件UEL
	 */
	private String startUel;

	/**
	 * 描述
	 */
	private String depict;

	/**
	 * 当前流程定义状态
	 */
	private FlowDefinationStatus flowDefinationStatus;

	/**
	 * 优先级
	 */
	private Integer priority=0;

	/**
	 * 组织机构id
	 */
	private String orgId;

	/**
	 * 组织机构代码
	 */
	private String orgCode;

	/**
	 * 拥有的流程定义版本
	 */
	private Set<FlowDefVersion> flowDefVersions = new HashSet<FlowDefVersion>(0);


	/**
	 * 是否允许做为子流程来进行引用
	 */
	private Boolean subProcess;

	/**
	 * 是否为固化流程
	 */
	private Boolean solidifyFlow;


	/**
	 * 当前对应的流程版本
	 */
	private FlowDefVersion currentFlowDefVersion;


	/**
	 * 租户代码
	 */
	private String tenantCode;


	/**
	 * 创建者
	 */
	protected String creatorId;

	protected String creatorAccount;

	protected String creatorName;

	/**
	 * 创建时间
	 */
	protected Date createdDate;

	/**
	 * 最后修改者
	 */
	protected String lastEditorId;

	protected String lastEditorAccount;

	protected String lastEditorName;

	/**
	 * 最后修改时间
	 */
	protected Date lastEditedDate;

	public Integer getVersion() {
		return version;
	}

	public void setVersion(Integer version) {
		this.version = version;
	}

	public FlowType getFlowType() {
		return flowType;
	}

	public void setFlowType(FlowType flowType) {
		this.flowType = flowType;
	}

	public String getDefKey() {
		return defKey;
	}

	public void setDefKey(String defKey) {
		this.defKey = defKey;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getLastVersionId() {
		return lastVersionId;
	}

	public void setLastVersionId(String lastVersionId) {
		this.lastVersionId = lastVersionId;
	}

	public String getLastDeloyVersionId() {
		return lastDeloyVersionId;
	}

	public void setLastDeloyVersionId(String lastDeloyVersionId) {
		this.lastDeloyVersionId = lastDeloyVersionId;
	}

	public String getStartUel() {
		return startUel;
	}

	public void setStartUel(String startUel) {
		this.startUel = startUel;
	}

	public String getDepict() {
		return depict;
	}

	public void setDepict(String depict) {
		this.depict = depict;
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

	public String getOrgId() {
		return orgId;
	}

	public void setOrgId(String orgId) {
		this.orgId = orgId;
	}

	public String getOrgCode() {
		return orgCode;
	}

	public void setOrgCode(String orgCode) {
		this.orgCode = orgCode;
	}

	public Set<FlowDefVersion> getFlowDefVersions() {
		return flowDefVersions;
	}

	public void setFlowDefVersions(Set<FlowDefVersion> flowDefVersions) {
		this.flowDefVersions = flowDefVersions;
	}

	public Boolean getSubProcess() {
		return subProcess;
	}

	public void setSubProcess(Boolean subProcess) {
		this.subProcess = subProcess;
	}

	public Boolean getSolidifyFlow() {
		return solidifyFlow;
	}

	public void setSolidifyFlow(Boolean solidifyFlow) {
		this.solidifyFlow = solidifyFlow;
	}

	public FlowDefVersion getCurrentFlowDefVersion() {
		return currentFlowDefVersion;
	}

	public void setCurrentFlowDefVersion(FlowDefVersion currentFlowDefVersion) {
		this.currentFlowDefVersion = currentFlowDefVersion;
	}

	public String getTenantCode() {
		return tenantCode;
	}

	public void setTenantCode(String tenantCode) {
		this.tenantCode = tenantCode;
	}

	public String getCreatorId() {
		return creatorId;
	}

	public void setCreatorId(String creatorId) {
		this.creatorId = creatorId;
	}

	public String getCreatorAccount() {
		return creatorAccount;
	}

	public void setCreatorAccount(String creatorAccount) {
		this.creatorAccount = creatorAccount;
	}

	public String getCreatorName() {
		return creatorName;
	}

	public void setCreatorName(String creatorName) {
		this.creatorName = creatorName;
	}

	public Date getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}

	public String getLastEditorId() {
		return lastEditorId;
	}

	public void setLastEditorId(String lastEditorId) {
		this.lastEditorId = lastEditorId;
	}

	public String getLastEditorAccount() {
		return lastEditorAccount;
	}

	public void setLastEditorAccount(String lastEditorAccount) {
		this.lastEditorAccount = lastEditorAccount;
	}

	public String getLastEditorName() {
		return lastEditorName;
	}

	public void setLastEditorName(String lastEditorName) {
		this.lastEditorName = lastEditorName;
	}

	public Date getLastEditedDate() {
		return lastEditedDate;
	}

	public void setLastEditedDate(Date lastEditedDate) {
		this.lastEditedDate = lastEditedDate;
	}
}