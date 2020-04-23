package com.ecmp.flow.entity;

import com.ecmp.core.entity.ITenant;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.Date;


@Entity
@Table(name = "task_make_over_power")
public class TaskMakeOverPower extends com.ecmp.core.entity.BaseAuditableEntity  implements ITenant {

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
