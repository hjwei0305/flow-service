package com.ecmp.flow.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * *************************************************************************************************
 * <p/>
 * 实现功能：常用联系人
 * <p>
 * ------------------------------------------------------------------------------------------------
 * 版本          变更时间             变更人                     变更原因
 * ------------------------------------------------------------------------------------------------
 * 1.0.00      2020/08/31          何灿坤(AK)                  新建
 * <p/>
 * *************************************************************************************************
 */
@Entity
@Table(name = "common_contact_people")
public class CommonContactPeople  extends com.ecmp.core.entity.BaseAuditableEntity {

    /**
     * 所属常用联系组ID
     */
    @Column(name = "group_id")
    private String commonContactGroupId;

    /**
     *  用户ID
     */
    @Column(name = "user_id")
    private String userId;

    /**
     *  用户code
     */
    @Column(name = "user_code")
    private String userCode;

    /**
     * 用户名称
     */
    @Column(name = "user_name")
    private String userName;

    /**
     *  组织机构ID
     */
    @Column(name = "org_id")
    private String orgId;


    /**
     *  组织机构Code
     */
    @Column(name = "org_code")
    private String orgCode;


    /**
     *  组织机构名称
     */
    @Column(name = "org_name")
    private String orgName;


    /**
     *  岗位ID
     */
    @Column(name = "position_id")
    private String positionId;


    /**
     *  岗位Code
     */
    @Column(name = "position_code")
    private String positionCode;


    /**
     *  岗位名称
     */
    @Column(name = "position_name")
    private String positionName;

    public String getCommonContactGroupId() {
        return commonContactGroupId;
    }

    public void setCommonContactGroupId(String commonContactGroupId) {
        this.commonContactGroupId = commonContactGroupId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserCode() {
        return userCode;
    }

    public void setUserCode(String userCode) {
        this.userCode = userCode;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
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

    public String getOrgName() {
        return orgName;
    }

    public void setOrgName(String orgName) {
        this.orgName = orgName;
    }

    public String getPositionId() {
        return positionId;
    }

    public void setPositionId(String positionId) {
        this.positionId = positionId;
    }

    public String getPositionCode() {
        return positionCode;
    }

    public void setPositionCode(String positionCode) {
        this.positionCode = positionCode;
    }

    public String getPositionName() {
        return positionName;
    }

    public void setPositionName(String positionName) {
        this.positionName = positionName;
    }
}
