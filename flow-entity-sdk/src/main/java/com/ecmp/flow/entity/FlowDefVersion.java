package com.ecmp.flow.entity;


import com.ecmp.flow.constant.FlowDefinationStatus;
import com.ecmp.flow.vo.SolidifyStartExecutorVo;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
public class FlowDefVersion  implements Serializable {

    /**
     * 乐观锁-版本
     */
    private Integer version = 0;

    /**
     * 所属流程定义
     */
    private FlowDefination flowDefination;


    /**
     * 定义ID
     */

    private String actDefId;

    /**
     * 定义KEY
     */
    private String defKey;

    /**
     * 名称
     */
    private String name;

    /**
     * 部署ID
     */
    private String actDeployId;

    /**
     * 启动条件UEL
     */
    private String startUel;

    /**
     * 版本号
     */
    private Integer versionCode;

    /**
     * 优先级
     */
    private Integer priority;

    /**
     * 流程JSON文本
     */
    private String defJson;


    /**
     * 租户代码
     */
    private String tenantCode;


    /**
     * 最终定义XML
     */
    private String defXml;

    /**
     * 描述
     */
    private String depict;

    /**
     * 当前流程版本状态
     */
    private FlowDefinationStatus flowDefinationStatus;

    /**
     * 拥有的流程实例
     */
    private Set<FlowInstance> flowInstances = new HashSet<FlowInstance>(0);

    /**
     * 固化流程初始化时用的单个执行人列表
     */
    private Map<String, SolidifyStartExecutorVo> solidifyExecutorOfOnly;

    /**
     * 是否允许做为子流程来进行引用
     */
    private Boolean subProcess;

    /**
     * 是否为固化流程
     */
    private Boolean solidifyFlow;


    /**
     * 启动时调用检查服务ID
     */
    private String startCheckServiceUrlId;

    /**
     * 启动时调用检查服务名称
     */
    private String startCheckServiceUrlName;

    /**
     * 启动时调用检查服务是否异步
     */
    private Boolean startCheckServiceAync;


    /**
     * 启动完成时调用服务id
     */
    private String afterStartServiceId;

    /**
     * 启动完成时调用服务名称
     */
    private String afterStartServiceName;

    /**
     * 启动完成时调用服务是否异步
     */
    private Boolean afterStartServiceAync;

    /**
     * 流程结束前检查，调用服务ID
     */
    private String endBeforeCallServiceUrlId;

    /**
     * 流程结束时，调用服务名称
     */
    private String endBeforeCallServiceUrlName;


    /**
     * 流程结束前检查服务是否异步
     */
    private Boolean endBeforeCallServiceAync;

    /**
     * 流程结束时，调用服务，异步
     */
    private String endCallServiceUrlId;

    /**
     * 流程结束时，调用服务名称，异步
     */
    private String endCallServiceUrlName;


    /**
     * 流程结束调用服务是否异步
     */
    private Boolean endCallServiceAync;


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


    /**
     * default constructor
     */
    public FlowDefVersion() {
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public FlowDefination getFlowDefination() {
        return flowDefination;
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

    public String getActDeployId() {
        return actDeployId;
    }

    public void setActDeployId(String actDeployId) {
        this.actDeployId = actDeployId;
    }

    public String getStartUel() {
        return startUel;
    }

    public void setStartUel(String startUel) {
        this.startUel = startUel;
    }

    public Integer getVersionCode() {
        return versionCode;
    }

    public void setVersionCode(Integer versionCode) {
        this.versionCode = versionCode;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public String getDefJson() {
        return defJson;
    }

    public void setDefJson(String defJson) {
        this.defJson = defJson;
    }

    public String getTenantCode() {
        return tenantCode;
    }

    public void setTenantCode(String tenantCode) {
        this.tenantCode = tenantCode;
    }

    public String getDefXml() {
        return defXml;
    }

    public void setDefXml(String defXml) {
        this.defXml = defXml;
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

    public Set<FlowInstance> getFlowInstances() {
        return flowInstances;
    }

    public void setFlowInstances(Set<FlowInstance> flowInstances) {
        this.flowInstances = flowInstances;
    }

    public Map<String, SolidifyStartExecutorVo> getSolidifyExecutorOfOnly() {
        return solidifyExecutorOfOnly;
    }

    public void setSolidifyExecutorOfOnly(Map<String, SolidifyStartExecutorVo> solidifyExecutorOfOnly) {
        this.solidifyExecutorOfOnly = solidifyExecutorOfOnly;
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

    public String getStartCheckServiceUrlId() {
        return startCheckServiceUrlId;
    }

    public void setStartCheckServiceUrlId(String startCheckServiceUrlId) {
        this.startCheckServiceUrlId = startCheckServiceUrlId;
    }

    public String getStartCheckServiceUrlName() {
        return startCheckServiceUrlName;
    }

    public void setStartCheckServiceUrlName(String startCheckServiceUrlName) {
        this.startCheckServiceUrlName = startCheckServiceUrlName;
    }

    public Boolean getStartCheckServiceAync() {
        return startCheckServiceAync;
    }

    public void setStartCheckServiceAync(Boolean startCheckServiceAync) {
        this.startCheckServiceAync = startCheckServiceAync;
    }

    public String getAfterStartServiceId() {
        return afterStartServiceId;
    }

    public void setAfterStartServiceId(String afterStartServiceId) {
        this.afterStartServiceId = afterStartServiceId;
    }

    public String getAfterStartServiceName() {
        return afterStartServiceName;
    }

    public void setAfterStartServiceName(String afterStartServiceName) {
        this.afterStartServiceName = afterStartServiceName;
    }

    public Boolean getAfterStartServiceAync() {
        return afterStartServiceAync;
    }

    public void setAfterStartServiceAync(Boolean afterStartServiceAync) {
        this.afterStartServiceAync = afterStartServiceAync;
    }

    public String getEndBeforeCallServiceUrlId() {
        return endBeforeCallServiceUrlId;
    }

    public void setEndBeforeCallServiceUrlId(String endBeforeCallServiceUrlId) {
        this.endBeforeCallServiceUrlId = endBeforeCallServiceUrlId;
    }

    public String getEndBeforeCallServiceUrlName() {
        return endBeforeCallServiceUrlName;
    }

    public void setEndBeforeCallServiceUrlName(String endBeforeCallServiceUrlName) {
        this.endBeforeCallServiceUrlName = endBeforeCallServiceUrlName;
    }

    public Boolean getEndBeforeCallServiceAync() {
        return endBeforeCallServiceAync;
    }

    public void setEndBeforeCallServiceAync(Boolean endBeforeCallServiceAync) {
        this.endBeforeCallServiceAync = endBeforeCallServiceAync;
    }

    public String getEndCallServiceUrlId() {
        return endCallServiceUrlId;
    }

    public void setEndCallServiceUrlId(String endCallServiceUrlId) {
        this.endCallServiceUrlId = endCallServiceUrlId;
    }

    public String getEndCallServiceUrlName() {
        return endCallServiceUrlName;
    }

    public void setEndCallServiceUrlName(String endCallServiceUrlName) {
        this.endCallServiceUrlName = endCallServiceUrlName;
    }

    public Boolean getEndCallServiceAync() {
        return endCallServiceAync;
    }

    public void setEndCallServiceAync(Boolean endCallServiceAync) {
        this.endCallServiceAync = endCallServiceAync;
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