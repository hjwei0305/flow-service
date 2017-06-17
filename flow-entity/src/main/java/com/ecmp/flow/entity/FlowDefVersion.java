package com.ecmp.flow.entity;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

/**
 * *************************************************************************************************
 * <p/>
 * 实现功能：
 * 流程定义版本实体模型Entity定义
 * <p>
 * ------------------------------------------------------------------------------------------------
 * 版本          变更时间             变更人                     变更原因
 * ------------------------------------------------------------------------------------------------
 * 1.0.00      2017/3/21 10:20      詹耀(zhanyao)                新建
 * <p/>
 * *************************************************************************************************
 */
@Entity
@Table(name = "flow_def_version", catalog = "ecmp_flow", uniqueConstraints = @UniqueConstraint(columnNames = "def_key"))
public class FlowDefVersion extends com.ecmp.core.entity.BaseAuditableEntity implements Cloneable {

    /**
     * 乐观锁-版本
     */
    //@Version
    @Column(name = "version")
    private Integer version = 0;

    /**
     * 所属流程定义
     */
    @ManyToOne()
    @JoinColumn(name = "flow_defination_id")
    private FlowDefination flowDefination;


    /**
     * 定义ID
     */

    @Column(name = "act_def_id", length = 36)
    private String actDefId;

    /**
     * 定义KEY
     */
    @Column(name = "def_key", nullable = false)
    private String defKey;

    /**
     * 名称
     */
    @Column(name = "name", nullable = false, length = 80)
    private String name;

    /**
     * 部署ID
     */
    @Column(name = "act_deploy_id", length = 36)
    private String actDeployId;

    /**
     * 启动条件UEL
     */
    @Column(name = "start_uel")
    private String startUel;

    /**
     * 版本号
     */
    @Column(name = "version_code")
    private Integer versionCode;

    /**
     * 优先级
     */
    @Column(name = "priority")
    private Integer priority;

    /**
     * 流程JSON文本
     */
    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "def_json", length = 65535)
    private String defJson;

    /**
     * 流程BPMN文本
     */
    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "def_bpmn", length = 65535)
    private String defBpmn;

    /**
     * 最终定义XML
     */
    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "def_xml", length = 65535)
    private String defXml;

    /**
     * 描述
     */
    @Column(name = "depict")
    private String depict;

    /**
     * 拥有的流程实例
     */
    @Transient
//	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "flowDefVersion")
    private Set<FlowInstance> flowInstances = new HashSet<FlowInstance>(0);

    // Constructors

    /**
     * default constructor
     */
    public FlowDefVersion() {
    }

    /**
     * minimal constructor
     */
    public FlowDefVersion(String defKey, String name) {
        this.defKey = defKey;
        this.name = name;

    }

    /**
     * full constructor
     */
    public FlowDefVersion(FlowDefination flowDefination, String defKey,
                          String name, String actDeployId, String startUel,
                          Integer versionCode, Integer priority, String defJson,
                          String defBpmn, String defXml, String depict, Set<FlowInstance> flowInstances) {
        this.flowDefination = flowDefination;
        this.defKey = defKey;
        this.name = name;
        this.actDeployId = actDeployId;
        this.startUel = startUel;
        this.versionCode = versionCode;
        this.priority = priority;
        this.defJson = defJson;
        this.defBpmn = defBpmn;
        this.defXml = defXml;
        this.depict = depict;
        this.flowInstances = flowInstances;
    }


    public FlowDefination getFlowDefination() {
        return this.flowDefination;
    }

    public void setFlowDefination(FlowDefination flowDefination) {
        this.flowDefination = flowDefination;
    }

    public String getActDefId() {
        return actDefId;
    }

    public void setActDefId(String actDefId) {
        this.actDefId = actDefId;
    }

    public String getDefKey() {
        return this.defKey;
    }

    public void setDefKey(String defKey) {
        this.defKey = defKey;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getActDeployId() {
        return this.actDeployId;
    }

    public void setActDeployId(String actDeployId) {
        this.actDeployId = actDeployId;
    }

    public String getStartUel() {
        return this.startUel;
    }

    public void setStartUel(String startUel) {
        this.startUel = startUel;
    }

    public Integer getVersionCode() {
        return this.versionCode;
    }

    public void setVersionCode(Integer versionCode) {
        this.versionCode = versionCode;
    }

    public Integer getPriority() {
        return this.priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public String getDefJson() {
        return this.defJson;
    }

    public void setDefJson(String defJson) {
        this.defJson = defJson;
    }

    public String getDefBpmn() {
        return this.defBpmn;
    }

    public void setDefBpmn(String defBpmn) {
        this.defBpmn = defBpmn;
    }

    public String getDefXml() {
        return this.defXml;
    }

    public void setDefXml(String defXml) {
        this.defXml = defXml;
    }

    public String getDepict() {
        return this.depict;
    }

    public void setDepict(String depict) {
        this.depict = depict;
    }

    public Set<FlowInstance> getFlowInstances() {
        return this.flowInstances;
    }

    public void setFlowInstances(Set<FlowInstance> flowInstances) {
        this.flowInstances = flowInstances;
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
                .append("flowDefination", flowDefination)
                .append("defKey", defKey)
                .append("name", name)
                .append("actDeployId", actDeployId)
                .append("startUel", startUel)
                .append("versionCode", versionCode)
                .append("priority", priority)
                .append("defJson", defJson)
                .append("defBpmn", defBpmn)
                .append("defXml", defXml)
                .append("depict", depict)
                .append("flowInstances", flowInstances)
                .toString();
    }

    @Override
    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj);
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        FlowDefVersion o = null;
        // Object中的clone()识别出你要复制的是哪一个对象。
        o = (FlowDefVersion) super.clone();
        return o;
    }
}