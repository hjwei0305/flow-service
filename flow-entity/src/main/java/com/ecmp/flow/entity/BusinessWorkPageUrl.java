package com.ecmp.flow.entity;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.Version;


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
public class BusinessWorkPageUrl extends com.ecmp.core.entity.BaseEntity {

    /**
     * 乐观锁-版本
     */
    @Version
    @Column(name = "version")
    private Integer version=0;

    /**
     * 名称
     */
    @Column(length = 80, nullable = false)
    private String name;

    /**
     * URL界面地址
     */
    @Lob
    private String url;

    /**
     * 描述
     */
    @Column(length = 250)
    private String depict;

    /**
     * 关联的业务实体ID
     */
    @Column(length = 36,name = "business_model_id ")
    private String businessModuleId;



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

    public String getBusinessModuleId() {
        return businessModuleId;
    }

    public void setBusinessModuleId(String businessModuleId) {
        this.businessModuleId = businessModuleId;
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
                .append("businessModuleId", this.businessModuleId)
                .append("url", this.url)
                .append("depict",this.depict)
                .toString();
    }

    @Override
    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj);
    }
}
