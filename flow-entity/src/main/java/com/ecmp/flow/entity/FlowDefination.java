package com.ecmp.flow.entity;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;


/**
 * *************************************************************************************************
 * <p/>
 * 实现功能：
 * 流程定义模块Entity定义
 * <p>
 * ------------------------------------------------------------------------------------------------
 * 版本          变更时间             变更人                     变更原因
 * ------------------------------------------------------------------------------------------------
 * 1.0.00      2017/3/21 10:20      谭军(tanjun)                新建
 * <p/>
 * *************************************************************************************************
 */

@Entity(name = "flow_defination")
@DynamicInsert
@DynamicUpdate
public class FlowDefination extends com.ecmp.core.entity.BaseEntity {
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
     * 最新版本
     */
    @Column
    private Integer lastVersionId;

    /**
     * 启动条件UEL
     */
    @Column(length = 255)
    private String startUel;


    /**
     * 描述
     */
    @Column(length = 250)
    private String depict;


    /**
     * 关联流程类型
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "flowType_id")
    private FlowType flowType;


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

    public Integer getLastVersionId() {
        return lastVersionId;
    }

    public void setLastVersionId(Integer lastVersionId) {
        this.lastVersionId = lastVersionId;
    }

    public String getStartUel() {
        return startUel;
    }

    public void setStartUel(String startUel) {
        this.startUel = startUel;
    }

    public FlowType getFlowType() {
        return flowType;
    }

    public void setFlowType(FlowType flowType) {
        this.flowType = flowType;
    }

    public String getDepict() {
        return depict;
    }

    public void setDepict(String depict) {
        this.depict = depict;
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
                .append("lastVersionId", this.lastVersionId)
                .append("startUel", this.startUel)
                .append("depict",this.depict)
                .toString();
    }

    @Override
    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj);
    }
}
