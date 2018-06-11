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

@Entity(name = "work_page_url")
@DynamicInsert
@DynamicUpdate
@Cacheable(true)
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class WorkPageUrl extends com.ecmp.core.entity.BaseAuditableEntity implements ITenant {

    /**
     * 乐观锁-版本
     */
   // @Version
    @Column(name = "version")
    private Integer version = 0;

    /**
     * 名称
     */
    @Column(length = 80, nullable = false)
    private String name;

    /**
     * URL界面地址
     */
    @Lob
    @Basic(fetch=FetchType.LAZY)
    private String url;

    /**
     * 描述
     */
    @Column(length = 250)
    private String depict;

    /**
     * 关联的应用模块ID
     */
    @Column(length = 36,name = "app_module_id")
    private String appModuleId;


    /**
     * 是否必须提交
     */
    @Column(name = "must_commit")
    private Boolean mustCommit;

    /**
     * 租户代码
     */
    @Column(name = "tenant_code", length = 10)
    private String tenantCode;


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
                .append("name", this.name)
                .append("appModuleId", this.appModuleId)
                .append("url", this.url)
                .append("depict",this.depict)
                 .append("mustCommit",this.mustCommit)
                .toString();
    }

    @Override
    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj);
    }

    public Boolean getMustCommit() {
        return mustCommit;
    }

    public void setMustCommit(Boolean mustCommit) {
        this.mustCommit = mustCommit;
    }


    @Override
    public String getTenantCode() {
        return tenantCode;
    }

    @Override
    public void setTenantCode(String tenantCode) {
        this.tenantCode = tenantCode;
    }
}
