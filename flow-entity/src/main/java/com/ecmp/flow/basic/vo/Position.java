package com.ecmp.flow.basic.vo;

import com.ecmp.core.entity.BaseAuditableEntity;
import com.ecmp.core.entity.ICodeUnique;
import com.ecmp.core.entity.ITenant;

/**
 * *************************************************************************************************
 * <p/>
 * 实现功能：岗位实体
 * <p>
 * ------------------------------------------------------------------------------------------------
 * 版本          变更时间                  变更人                 变更原因
 * ------------------------------------------------------------------------------------------------
 * 1.0.00      2017/4/17 11:02            高银军                  新建
 * <p/>
 * *************************************************************************************************
 */
//@Access(AccessType.FIELD)
//@Entity
//@Table(name = "position")
//@DynamicInsert
//@DynamicUpdate
//@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class Position extends BaseAuditableEntity
        implements ITenant,ICodeUnique {
    /**
     * 代码
     */
//    @Column(name = "code",unique = true, length = 8, nullable = false)
    private String code;
    /**
     * 名称
     */
//    @Column(name = "name", length = 50, nullable = false)
    private String name;

    /**
     * 租户代码
     */
//    @Column(name = "tenant_code", length = 10, nullable = false, unique = true)
    private String tenantCode;

//    @ManyToOne
//    @JoinColumn(name = "organization_Id",nullable = false)
    private Organization organization;

//    @ManyToOne
//    @JoinColumn(name = "category_id",nullable = false)
    private PositionCategory positionCategory;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public PositionCategory getPositionCategory() {
        return positionCategory;
    }

    public void setPositionCategory(PositionCategory positionCategory) {
        this.positionCategory = positionCategory;
    }

    public String getTenantCode() {
        return tenantCode;
    }

    public void setTenantCode(String tenantCode) {
        this.tenantCode = tenantCode;
    }

    public Organization getOrganization() {
        return organization;
    }

    public void setOrganization(Organization organization) {
        this.organization = organization;
    }

}
