package com.ecmp.flow.entity;


import java.io.Serializable;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;


public class FlowType implements Serializable {

    public FlowType(){}

    /**
     * 乐观锁-版本
     */
    private Integer version = 0;

    /**
     * 名称
     */
    private String name;

    /**
     * 代码
     */
    private String code;

    /**
     * 描述
     */
    private String depict;


    /**
     * 关联业务实体模型
     */
    private BusinessModel businessModel;

    /**
     * 拥有的流程定义
     */
    private Set<FlowDefination> flowDefinations = new LinkedHashSet<>(0);

    /**
     * 完成任务时调用的web地址
     */
    private String completeTaskServiceUrl;

    /**
     * 查看单据的URL
     */
    private String lookUrl;

    /**
     * 业务单据明细服务地址-主要供移动端使用
     */
    private String businessDetailServiceUrl;



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

    protected String id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

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

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDepict() {
        return depict;
    }

    public void setDepict(String depict) {
        this.depict = depict;
    }

    public BusinessModel getBusinessModel() {
        return businessModel;
    }

    public void setBusinessModel(BusinessModel businessModel) {
        this.businessModel = businessModel;
    }

    public Set<FlowDefination> getFlowDefinations() {
        return flowDefinations;
    }

    public void setFlowDefinations(Set<FlowDefination> flowDefinations) {
        this.flowDefinations = flowDefinations;
    }

    public String getCompleteTaskServiceUrl() {
        return completeTaskServiceUrl;
    }

    public void setCompleteTaskServiceUrl(String completeTaskServiceUrl) {
        this.completeTaskServiceUrl = completeTaskServiceUrl;
    }

    public String getLookUrl() {
        return lookUrl;
    }

    public void setLookUrl(String lookUrl) {
        this.lookUrl = lookUrl;
    }

    public String getBusinessDetailServiceUrl() {
        return businessDetailServiceUrl;
    }

    public void setBusinessDetailServiceUrl(String businessDetailServiceUrl) {
        this.businessDetailServiceUrl = businessDetailServiceUrl;
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
