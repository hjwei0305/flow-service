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
 * 流程类型模块Entity定义
 * <p>
 * ------------------------------------------------------------------------------------------------
 * 版本          变更时间             变更人                     变更原因
 * ------------------------------------------------------------------------------------------------
 * 1.0.00      2017/3/21 10:20      谭军(tanjun)                新建
 * <p/>
 * *************************************************************************************************
 */

@Entity(name = "flow_defVersion")
@DynamicInsert
@DynamicUpdate
public class FlowDefVersion extends com.ecmp.core.entity.BaseEntity {
    /**
     * 名称
     */
    @Column(length = 80, nullable = false)
    private String name;


    /**
     * 定义key
     */
    private String defKey;

    /**
     * 部署ID
     */
    private String actDeployId;

    /**
     * 启动条件UEL
     */
    private String startUel;

    /**
     * 版本号
     */
    private Integer versionCode;

    /**
     * 优先级
     */
    private Integer priority;

    /**
     * 流程定义的JSON字符窜
     */
    private String defJson;

    /**
     * 流程定义的Bpmn字符窜
     */
    private String defBpmn;

    /**
     * 流程定义的最终Xml字符窜
     */
    private String defXml;

    /**
     * 描述
     */
    @Column(length = 250)
    private String depict;


    /**
     * 关联业务实体模型
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "flowDefination_id")
    private FlowDefination flowDefination;



    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDefKey() {
        return defKey;
    }

    public void setDefKey(String defKey) {
        this.defKey = defKey;
    }

    public String getActDeployId() {
        return actDeployId;
    }

    public void setActDeployId(String actDeployId) {
        this.actDeployId = actDeployId;
    }

    public String getStartUel() {
        return startUel;
    }

    public void setStartUel(String startUel) {
        this.startUel = startUel;
    }

    public Integer getVersionCode() {
        return versionCode;
    }

    public void setVersionCode(Integer versionCode) {
        this.versionCode = versionCode;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public String getDefJson() {
        return defJson;
    }

    public void setDefJson(String defJson) {
        this.defJson = defJson;
    }

    public String getDefBpmn() {
        return defBpmn;
    }

    public void setDefBpmn(String defBpmn) {
        this.defBpmn = defBpmn;
    }

    public String getDefXml() {
        return defXml;
    }

    public void setDefXml(String defXml) {
        this.defXml = defXml;
    }

    public FlowDefination getFlowDefination() {
        return flowDefination;
    }

    public void setFlowDefination(FlowDefination flowDefination) {
        this.flowDefination = flowDefination;
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
                .append("defKey", this.defKey)
                .append("depict",this.depict)
                .toString();
    }

    @Override
    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj);
    }
}
