package com.ecmp.flow.basic.vo;

import com.ecmp.core.entity.BaseAuditableEntity;
import com.ecmp.core.entity.ITenant;

/**
 * *************************************************************************************************
 * <p/>
 * 实现功能：企业员工实体
 * <p>
 * ------------------------------------------------------------------------------------------------
 * 版本          变更时间             变更人                     变更原因
 * ------------------------------------------------------------------------------------------------
 * 1.0.00      2017/5/5 11:35      秦有宝                     新建
 * <p/>
 * *************************************************************************************************
 */
public class Employee extends BaseAuditableEntity implements ITenant {
    /**
     * 员工编号
     */
    private String code;

    /**
     * 租户代码
     */
    private String tenantCode;


    /**
     * 用户姓名
     */
    private String userName;

    /**
     * 是否冻结
     */
    private boolean frozen;

    /**
     * 是否是创建租户管理员
     */
    private boolean createAdmin;

    /**
     * 邮箱,创建租户管理员时发送邮件
     */
    private String email;

    /**
     * 组织机构Id
     */
    private String organizationId;

    /**
     * 组织机构代码
     */
    private String organizationCode;

    /**
     * 组织机构名称
     */
    private String organizationName;

    /**
     * 组织机构代码路径
     */
    private String organizationCodePath;

    /**
     * 组织机构名称路径
     */
    private String organizationNamePath;




    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public boolean isCreateAdmin() {
        return createAdmin;
    }

    public void setCreateAdmin(boolean createAdmin) {
        this.createAdmin = createAdmin;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isFrozen() {
        return frozen;
    }

    public void setFrozen(boolean frozen) {
        this.frozen = frozen;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(String organizationId) {
        this.organizationId = organizationId;
    }

    public String getOrganizationCode() {
        return organizationCode;
    }

    public void setOrganizationCode(String organizationCode) {
        this.organizationCode = organizationCode;
    }

    public String getOrganizationName() {
        return organizationName;
    }

    public void setOrganizationName(String organizationName) {
        this.organizationName = organizationName;
    }

    public String getOrganizationCodePath() {
        return organizationCodePath;
    }

    public void setOrganizationCodePath(String organizationCodePath) {
        this.organizationCodePath = organizationCodePath;
    }

    public String getOrganizationNamePath() {
        return organizationNamePath;
    }

    public void setOrganizationNamePath(String organizationNamePath) {
        this.organizationNamePath = organizationNamePath;
    }

    /**
     * @return 租户代码
     */
    @Override
    public String getTenantCode() {
        return tenantCode;
    }

    /**
     * 设置租户代码
     *
     * @param tenantCode 租户代码
     */
    @Override
    public void setTenantCode(String tenantCode) {
        this.tenantCode = tenantCode;
    }



}
