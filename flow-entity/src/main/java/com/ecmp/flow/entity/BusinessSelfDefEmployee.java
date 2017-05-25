package com.ecmp.flow.entity;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Version;


/**
 * *************************************************************************************************
 * <p/>
 * 实现功能：
 * 业务实体自定义执行人配置Entity定义
 * <p>
 * ------------------------------------------------------------------------------------------------
 * 版本          变更时间             变更人                     变更原因
 * ------------------------------------------------------------------------------------------------
 * 1.0.00      2017/5/24 10:20      谭军(tanjun)                新建
 * <p/>
 * *************************************************************************************************
 */

@Entity(name = "business_model_selfDefEmployee")
@DynamicInsert
@DynamicUpdate
public class BusinessSelfDefEmployee extends com.ecmp.core.entity.BaseAuditableEntity {

    /**
     * 乐观锁-版本
     */
    @Version
    @Column(name = "version")
    private Integer version=0;

    /**
     * 关联的业务实体ID
     */
    @Column(length = 36,name = "business_model_id")
    private String businessModuleId;

    /**
     * 企业员工ID
     */
    @Column(length = 36,name = "employee_id")
    private String employeeId;

    /**
     * 企业员工名称
     */
    @Column(length = 80,name = "employee_name")
    private String employeeName;



    public String getBusinessModuleId() {
        return businessModuleId;
    }

    public void setBusinessModuleId(String businessModuleId) {
        this.businessModuleId = businessModuleId;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }



    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("id", this.getId())
                .append("businessModuleId", this.businessModuleId)
                .append("employeeId", this.employeeId)
                .append("employeeName", this.employeeName)
                .toString();
    }

    @Override
    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj);
    }
}
