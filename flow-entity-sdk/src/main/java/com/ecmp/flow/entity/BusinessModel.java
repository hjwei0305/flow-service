package com.ecmp.flow.entity;


import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;


public class BusinessModel implements Serializable {

    /**
     * 乐观锁-版本
     */
    private Integer version = 0;

    /**
     * 名称
     */
    private String name;

    /**
     * 类全路径
     */
    private String className;


    /**
     * 转换对象，全路径
     */
    private String conditonBean;

    /**
     * 数据访问对象,spring管理的bean名称
     */
    private String daoBean;

    /**
     * 条件属性说明服务地址
     */
    private String conditonProperties;

    /**
     * 条件属性值服务地址
     */
    private String conditonPValue;

    /**
     * 流程状态重置服务地址
     */
    private String conditonStatusRest;

    /**
     * 条件属性初始值服务地址
     */
    private String conditonPSValue;


    /**
     * 完成任务时调用的web地址
     */
    private String completeTaskServiceUrl;


    /**
     * 业务单据明细服务地址-主要供移动端使用
     */
    private String businessDetailServiceUrl;


    /**
     * 描述
     */
    private String depict;

    /**
     * 所属应用模块
     */
    private AppModule appModule;


    /**
     * 关联的应用模块Code
     */
    private String appModuleCode;

    /**
     * 关联的应用模块Name
     */
    private String appModuleName;

    /**
     * 查看单据的URL
     */
    private String lookUrl;

    /**
     * 租户代码
     */
    private String tenantCode;

    /**
     * 推送待办接口地址
     */
    private String  pushMsgUrl;

    /**
     * 拥有的流程类型
     */
    private Set<FlowType> flowTypes = new HashSet<FlowType>();


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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getConditonBean() {
        return conditonBean;
    }

    public void setConditonBean(String conditonBean) {
        this.conditonBean = conditonBean;
    }

    public String getDaoBean() {
        return daoBean;
    }

    public void setDaoBean(String daoBean) {
        this.daoBean = daoBean;
    }

    public String getConditonProperties() {
        return conditonProperties;
    }

    public void setConditonProperties(String conditonProperties) {
        this.conditonProperties = conditonProperties;
    }

    public String getConditonPValue() {
        return conditonPValue;
    }

    public void setConditonPValue(String conditonPValue) {
        this.conditonPValue = conditonPValue;
    }

    public String getConditonStatusRest() {
        return conditonStatusRest;
    }

    public void setConditonStatusRest(String conditonStatusRest) {
        this.conditonStatusRest = conditonStatusRest;
    }

    public String getConditonPSValue() {
        return conditonPSValue;
    }

    public void setConditonPSValue(String conditonPSValue) {
        this.conditonPSValue = conditonPSValue;
    }

    public String getCompleteTaskServiceUrl() {
        return completeTaskServiceUrl;
    }

    public void setCompleteTaskServiceUrl(String completeTaskServiceUrl) {
        this.completeTaskServiceUrl = completeTaskServiceUrl;
    }

    public String getBusinessDetailServiceUrl() {
        return businessDetailServiceUrl;
    }

    public void setBusinessDetailServiceUrl(String businessDetailServiceUrl) {
        this.businessDetailServiceUrl = businessDetailServiceUrl;
    }

    public String getDepict() {
        return depict;
    }

    public void setDepict(String depict) {
        this.depict = depict;
    }

    public AppModule getAppModule() {
        return appModule;
    }

    public void setAppModule(AppModule appModule) {
        this.appModule = appModule;
    }

    public String getAppModuleCode() {
        return appModuleCode;
    }

    public void setAppModuleCode(String appModuleCode) {
        this.appModuleCode = appModuleCode;
    }

    public String getAppModuleName() {
        return appModuleName;
    }

    public void setAppModuleName(String appModuleName) {
        this.appModuleName = appModuleName;
    }

    public String getLookUrl() {
        return lookUrl;
    }

    public void setLookUrl(String lookUrl) {
        this.lookUrl = lookUrl;
    }

    public String getTenantCode() {
        return tenantCode;
    }

    public void setTenantCode(String tenantCode) {
        this.tenantCode = tenantCode;
    }

    public String getPushMsgUrl() {
        return pushMsgUrl;
    }

    public void setPushMsgUrl(String pushMsgUrl) {
        this.pushMsgUrl = pushMsgUrl;
    }

    public Set<FlowType> getFlowTypes() {
        return flowTypes;
    }

    public void setFlowTypes(Set<FlowType> flowTypes) {
        this.flowTypes = flowTypes;
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
