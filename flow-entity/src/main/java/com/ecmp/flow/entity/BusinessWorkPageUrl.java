package com.ecmp.flow.entity;

import com.ecmp.core.entity.ITenant;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;


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

@Entity(name = "business_model_page_url")
@DynamicInsert
@DynamicUpdate
@Cacheable(true)
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class BusinessWorkPageUrl extends com.ecmp.core.entity.BaseAuditableEntity  implements ITenant {

    /**
     * 乐观锁-版本
     */
    @Version
    @Column(name = "version")
    private Integer version=0;

    /**
     * 租户代码
     */
    @Column(name = "tenant_code", length = 10)
    private String tenantCode;

    /**
     * 关联的业务实体ID
     */
    @Column(length = 36,name = "business_model_id")
    private String businessModuleId;

    /**
     * 关联的工作页面ID
     */
    @Column(length = 36,name = "work_page_url_id")
    private String workPageUrlId;


    @Override
    public String getTenantCode() {
        return tenantCode;
    }

    @Override
    public void setTenantCode(String tenantCode) {
        this.tenantCode = tenantCode;
    }

    public String getBusinessModuleId() {
        return businessModuleId;
    }

    public void setBusinessModuleId(String businessModuleId) {
        this.businessModuleId = businessModuleId;
    }

    public String getWorkPageUrlId() {
        return workPageUrlId;
    }

    public void setWorkPageUrlId(String workPageUrlId) {
        this.workPageUrlId = workPageUrlId;
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
                .append("workPageUrlId", this.workPageUrlId)
                .toString();
    }

    @Override
    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj);
    }
}
