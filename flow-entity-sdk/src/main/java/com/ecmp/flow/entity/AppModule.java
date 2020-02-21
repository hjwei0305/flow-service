package com.ecmp.flow.entity;


import java.io.Serializable;
import java.util.Date;

public class AppModule implements Serializable {

    /**
     * 名称
     */
    private String name;

    /**
     * 代码
     */
    private String code;

    /**
     * 备注
     */
    private String remark;


    /**
     * web基地址
     */
    private String webBaseAddress;

    /**
     * api基地址
     */
    private String apiBaseAddress;


    /**
     * 租户代码
     */
    private String tenantCode;

    /**
     * 排序号
     */
    private Integer rank;

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

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getWebBaseAddress() {
        return webBaseAddress;
    }

    public void setWebBaseAddress(String webBaseAddress) {
        this.webBaseAddress = webBaseAddress;
    }

    public String getApiBaseAddress() {
        return apiBaseAddress;
    }

    public void setApiBaseAddress(String apiBaseAddress) {
        this.apiBaseAddress = apiBaseAddress;
    }

    public String getTenantCode() {
        return tenantCode;
    }

    public void setTenantCode(String tenantCode) {
        this.tenantCode = tenantCode;
    }

    public Integer getRank() {
        return rank;
    }

    public void setRank(Integer rank) {
        this.rank = rank;
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
