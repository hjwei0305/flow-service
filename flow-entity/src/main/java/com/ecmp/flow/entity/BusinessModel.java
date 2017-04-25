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
 * 业务实体模型Entity定义
 * <p>
 * ------------------------------------------------------------------------------------------------
 * 版本          变更时间             变更人                     变更原因
 * ------------------------------------------------------------------------------------------------
 * 1.0.00      2017/3/21 10:20      谭军(tanjun)                新建
 * <p/>
 * *************************************************************************************************
 */
@Entity(name = "business_model")
@DynamicInsert
@DynamicUpdate
public class BusinessModel extends com.ecmp.core.entity.BaseEntity {

    /**
     * 名称
     */
    @Column(length = 80, nullable = false)
    private String name;

    /**
     * 类全路径
     */
    @Column(length = 255, nullable = false, unique = true, name = "class_name")
    private String className;


    /**
     * 转换对象
     */
    @Column(length = 255, name = "conditon_bean")
    private String conditonBean;

    /**
     * 描述
     */
    @Column(length = 250)
    private String depict;

    /**
     * 所属应用模块
     */
    @ManyToOne()
    @JoinColumn(name = "app_module_id")
    private AppModule appModule;

    /**
     * 拥有的流程类型
     */
    @Transient
//    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "businessModel")
    private Set<FlowType> flowTypes = new HashSet<FlowType>();


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getDepict() {
        return depict;
    }

    public void setDepict(String depict) {
        this.depict = depict;
    }

    public AppModule getAppModule() {
        return appModule;
    }

    public void setAppModule(AppModule appModule) {
        this.appModule = appModule;
    }

    public Set<FlowType> getFlowTypes() {
        return flowTypes;
    }

    public void setFlowTypes(Set<FlowType> flowTypes) {
        this.flowTypes = flowTypes;
    }


    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

//    @Override
//    public String toString() {
//        return new ToStringBuilder(this)
//                .append("id", this.getId())
//                .append("name", this.name)
//                .append("className", this.className)
//                .append("depict",this.depict)

    public String getConditonBean() {
        return conditonBean;
    }

    public void setConditonBean(String conditonBean) {
        this.conditonBean = conditonBean;
    }

//                .toString();
//    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("id", this.getId())
                .append("name", name)
                .append("className", className)
                .append("conditonBean", conditonBean)
                .append("depict", depict)
                .append("appModule", appModule)
                .append("flowTypes", flowTypes)
                .toString();
    }

    @Override
    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj);
    }
}
