package com.ecmp.flow.entity;


import java.io.Serializable;
import java.util.Date;

/**
 * *************************************************************************************************
 * <p/>
 * 实现功能：
 * 工作界面配置管理Entity定义
 * <p>
 * ------------------------------------------------------------------------------------------------
 * 版本          变更时间             变更人                     变更原因
 * ------------------------------------------------------------------------------------------------
 * 1.0.00      2017/3/21 10:20      谭军(tanjun)                新建
 * <p/>
 * *************************************************************************************************
 */

public class WorkPageUrl implements Serializable {

    /**
     * 乐观锁-版本
     */
    private Integer version = 0;

    /**
     * 名称
     */
    private String name;

    /**
     * URL界面地址
     */
    private String url;

    /**
     * 描述
     */
    private String depict;

    /**
     * 关联的应用模块ID
     */
    private String appModuleId;


    /**
     * 是否必须提交
     */
    private Boolean mustCommit;

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDepict() {
        return depict;
    }

    public void setDepict(String depict) {
        this.depict = depict;
    }

    public String getAppModuleId() {
        return appModuleId;
    }

    public void setAppModuleId(String appModuleId) {
        this.appModuleId = appModuleId;
    }

    public Boolean getMustCommit() {
        return mustCommit;
    }

    public void setMustCommit(Boolean mustCommit) {
        this.mustCommit = mustCommit;
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
