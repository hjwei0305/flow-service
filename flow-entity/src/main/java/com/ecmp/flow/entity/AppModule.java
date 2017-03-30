package com.ecmp.flow.entity;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

/**
 * *************************************************************************************************
 * <p/>
 * 实现功能：
 * 应用模块Entity定义
 * <p>
 * ------------------------------------------------------------------------------------------------
 * 版本          变更时间             变更人                     变更原因
 * ------------------------------------------------------------------------------------------------
 * 1.0.00      2017/3/21 10:20      谭军(tanjun)                新建
 * <p/>
 * *************************************************************************************************
 */
@Entity()
@Table(name ="flow_appModule")
@DynamicInsert
@DynamicUpdate
public class AppModule extends com.ecmp.core.entity.BaseEntity {

    /**
     * 名称
     */
    @Column(length = 80, nullable = false)
    private String name;

    /**
     * 代码
     */
    @Column(length = 60, nullable = false,unique = true)
    private String code;

    /**
     * 描述
     */
    @Column(length = 250)
    private String depict;


    /**
     * 拥有的业务实体模型
     */
    @Transient
//    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "appModule")
    private Set<BusinessModel> businessModels = new HashSet<BusinessModel>();
    

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDepict() {
        return depict;
    }

    public void setDepict(String depict) {
        this.depict = depict;
    }

    public Set<BusinessModel> getBusinessModels() {
        return businessModels;
    }

    public void setBusinessModels(Set<BusinessModel> businessModels) {
        this.businessModels = businessModels;
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
                .append("code", this.code)
                .append("depict",this.depict)
                .toString();
    }

    @Override
    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj);
    }



}
