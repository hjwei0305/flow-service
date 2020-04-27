package com.ecmp.flow.entity;

import com.ecmp.core.entity.ITenant;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.Date;


@Entity
@Table(name = "task_make_over_power")
public class TaskMakeOverPower extends com.ecmp.core.entity.BaseAuditableEntity implements ITenant {

    /**
     * 授权人id
     */
    @Column(name = "user_id")
    private String userId;

    /**
     * 授权人账户
     */
    @Column(name = "user_account")
    private String userAccount;

    /**
     * 授权人名称
     */
    @Column(name = "user_name")
    private String userName;

    /**
     * 授权类型（枚举）:MakeOverPowerType
     */
    @Column(name = "make_over_power_type")
    private String makeOverPowerType;

    /**
     * 应用模块ID
     */
    @Column(name = "app_module_id")
    private String appModuleId;

    /**
     * 应用模块名称
     */
    @Column(name = "app_module_name")
    private String appModuleName;

    /**
     * 业务实体ID
     */
    @Column(name = "business_model_id")
    private String businessModelId;

    /**
     * 业务实体名称
     */
    @Column(name = "business_model_name")
    private String businessModelName;

    /**
     * 流程类型ID
     */
    @Column(name = "flow_type_id")
    private String flowTypeId;

    /**
     * 流程类型名称
     */
    @Column(name = "flow_type_name")
    private String flowTypeName;

    /**
     * 被授权人id
     */
    @Column(name = "power_user_id")
    private String powerUserId;

    /**
     * 被授权人账户
     */
    @Column(name = "power_user_account")
    private String powerUserAccount;

    /**
     * 被授权人名称
     */
    @Column(name = "power_user_name")
    private String powerUserName;

    /**
     * 被授权人组织机构ID
     */
    @Column(name = "power_user_org_id")
    private String powerUserOrgId;


    /**
     * 被授权人组织机构code
     */
    @Column(name = "power_user_org_code")
    private String powerUserOrgCode;


    /**
     * 被授权人组织机构名称
     */
    @Column(name = "power_user_org_name")
    private String powerUserOrgName;


    /**
     * 授权开始日期
     */
    @Column(name = "power_start_date")
    private Date powerStartDate;

    /**
     * 授权结束日期
     */
    @Column(name = "power_end_date")
    private Date powerEndDate;

    /**
     * 启用状态
     */
    @Column(name = "open_status")
    private Boolean openStatus;


    /**
     * 租户代码
     */
    @Column(name = "tenant_code")
    private String tenantCode;


    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserAccount() {
        return userAccount;
    }

    public void setUserAccount(String userAccount) {
        this.userAccount = userAccount;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPowerUserId() {
        return powerUserId;
    }

    public String getMakeOverPowerType() {
        return makeOverPowerType;
    }

    public void setMakeOverPowerType(String makeOverPowerType) {
        this.makeOverPowerType = makeOverPowerType;
    }

    public String getAppModuleId() {
        return appModuleId;
    }

    public void setAppModuleId(String appModuleId) {
        this.appModuleId = appModuleId;
    }

    public String getAppModuleName() {
        return appModuleName;
    }

    public void setAppModuleName(String appModuleName) {
        this.appModuleName = appModuleName;
    }

    public String getBusinessModelId() {
        return businessModelId;
    }

    public void setBusinessModelId(String businessModelId) {
        this.businessModelId = businessModelId;
    }

    public String getBusinessModelName() {
        return businessModelName;
    }

    public void setBusinessModelName(String businessModelName) {
        this.businessModelName = businessModelName;
    }

    public String getFlowTypeId() {
        return flowTypeId;
    }

    public void setFlowTypeId(String flowTypeId) {
        this.flowTypeId = flowTypeId;
    }

    public String getFlowTypeName() {
        return flowTypeName;
    }

    public void setFlowTypeName(String flowTypeName) {
        this.flowTypeName = flowTypeName;
    }

    public void setPowerUserId(String powerUserId) {
        this.powerUserId = powerUserId;
    }

    public String getPowerUserAccount() {
        return powerUserAccount;
    }

    public void setPowerUserAccount(String powerUserAccount) {
        this.powerUserAccount = powerUserAccount;
    }

    public String getPowerUserName() {
        return powerUserName;
    }

    public void setPowerUserName(String powerUserName) {
        this.powerUserName = powerUserName;
    }

    public Date getPowerStartDate() {
        return powerStartDate;
    }

    public void setPowerStartDate(Date powerStartDate) {
        this.powerStartDate = powerStartDate;
    }

    public Date getPowerEndDate() {
        return powerEndDate;
    }

    public void setPowerEndDate(Date powerEndDate) {
        this.powerEndDate = powerEndDate;
    }

    public Boolean getOpenStatus() {
        return openStatus;
    }

    public void setOpenStatus(Boolean openStatus) {
        this.openStatus = openStatus;
    }

    @Override
    public String getTenantCode() {
        return tenantCode;
    }

    @Override
    public void setTenantCode(String tenantCode) {
        this.tenantCode = tenantCode;
    }

    public String getPowerUserOrgId() {
        return powerUserOrgId;
    }

    public void setPowerUserOrgId(String powerUserOrgId) {
        this.powerUserOrgId = powerUserOrgId;
    }

    public String getPowerUserOrgCode() {
        return powerUserOrgCode;
    }

    public void setPowerUserOrgCode(String powerUserOrgCode) {
        this.powerUserOrgCode = powerUserOrgCode;
    }

    public String getPowerUserOrgName() {
        return powerUserOrgName;
    }

    public void setPowerUserOrgName(String powerUserOrgName) {
        this.powerUserOrgName = powerUserOrgName;
    }
}
