package com.ecmp.flow.basic.vo;

import com.ecmp.core.entity.BaseAuditableEntity;
import com.ecmp.core.entity.ICodeUnique;
import com.ecmp.core.entity.ITenant;

import java.io.Serializable;


public class Position extends BaseAuditableEntity
        implements ITenant,ICodeUnique, Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 代码
     */
    private String code;
    /**
     * 名称
     */
    private String name;

    /**
     * 租户代码
     */
    private String tenantCode;

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
     * 组织机构中文路径
     */
    private String organizationNamePath;

    /**
     * 岗位类别Id
     */
    private String positionCategoryId;

    /**
     * 岗位类别代码
     */
    private String positionCategoryCode;

    /**
     * 岗位类别名称
     */
    private String positionCategoryName;


    @Override
    public String getCode() {
        return code;
    }

    @Override
    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getTenantCode() {
        return tenantCode;
    }

    @Override
    public void setTenantCode(String tenantCode) {
        this.tenantCode = tenantCode;
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

    public String getPositionCategoryId() {
        return positionCategoryId;
    }

    public void setPositionCategoryId(String positionCategoryId) {
        this.positionCategoryId = positionCategoryId;
    }

    public String getPositionCategoryCode() {
        return positionCategoryCode;
    }

    public void setPositionCategoryCode(String positionCategoryCode) {
        this.positionCategoryCode = positionCategoryCode;
    }

    public String getPositionCategoryName() {
        return positionCategoryName;
    }

    public void setPositionCategoryName(String positionCategoryName) {
        this.positionCategoryName = positionCategoryName;
    }

    public String getOrganizationNamePath() {
        return organizationNamePath;
    }

    public void setOrganizationNamePath(String organizationNamePath) {
        this.organizationNamePath = organizationNamePath;
    }
}
