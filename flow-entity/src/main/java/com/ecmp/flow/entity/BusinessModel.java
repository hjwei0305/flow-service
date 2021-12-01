package com.ecmp.flow.entity;

import com.ecmp.core.entity.ITenant;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.hibernate.annotations.CacheConcurrencyStrategy;
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
@Cacheable(true)
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class BusinessModel extends com.ecmp.core.entity.BaseAuditableEntity  {

    /**
     * 乐观锁-版本
     */
    //@Version
    @Column(name = "version")
    private Integer version = 0;

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
     * 转换对象，全路径
     */
    @Column(length = 255, name = "conditon_bean")
    private String conditonBean;

    /**
     * 数据访问对象,spring管理的bean名称
     */
    @Column(length = 80, name = "dao_bean")
    private String daoBean;

    /**
     * 条件属性说明服务地址
     */
    @Column(length = 255, name = "conditon_properties")
    private String conditonProperties;

    /**
     * 条件属性值服务地址
     */
    @Column(length = 255, name = "conditon_p_value")
    private String conditonPValue;

    /**
     * 流程状态重置服务地址
     */
    @Column(length = 255, name = "conditon_status_rest")
    private String conditonStatusRest;

    /**
     * 条件属性初始值服务地址
     */
    @Column(length = 255, name = "conditon_p_s_value")
    private String conditonPSValue;


    /**
     * 完成任务时调用的web地址
     */
    @Column(length = 255, name = "complete_task_service_url")
    private String completeTaskServiceUrl;


    /**
     * 业务单据明细服务地址-主要供移动端使用
     */
    @Column(name="business_detail_service_url")
    private String businessDetailServiceUrl;


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

//    /**
//     * 关联的应用模块ID
//     */
//    @Column(length = 36,name = "app_module_id")
//    private String appModuleId;

    /**
     * 关联的应用模块Code
     */
    @Column(length = 20,name = "app_module_code")
    private String appModuleCode;

    /**
     * 关联的应用模块Name
     */
    @Column(length = 80,name = "app_module_name")
    private String appModuleName;

    /**
     * 查看单据的URL
     */
    @Column(name = "look_url")
    private String lookUrl;

    /**
     * 移动端查看单据的URL
     */
    @Column(name = "phone_look_url")
    private String phoneLookUrl;

    /**
     * 租户代码
     */
    @Column(name = "tenant_code", length = 10)
    private String tenantCode;

    /**
     * 推送待办接口地址
     */
    @Column(name = "push_msg_url", length = 255)
    private String  pushMsgUrl;

    /**
     * 拥有的流程类型
     */
    @Transient
//    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "businessModel")
    private Set<FlowType> flowTypes = new HashSet<FlowType>();


    public String getPushMsgUrl() {
        return pushMsgUrl;
    }

    public void setPushMsgUrl(String pushMsgUrl) {
        this.pushMsgUrl = pushMsgUrl;
    }

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


    public Set<FlowType> getFlowTypes() {
        return flowTypes;
    }

    public void setFlowTypes(Set<FlowType> flowTypes) {
        this.flowTypes = flowTypes;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public String getDaoBean() {
        return daoBean;
    }

    public void setDaoBean(String daoBean) {
        this.daoBean = daoBean;
    }

    public String getAppModuleCode() {
        return appModuleCode;
    }

    public void setAppModuleCode(String appModuleCode) {
        this.appModuleCode = appModuleCode;
    }

    public String getLookUrl() {
        return lookUrl;
    }

    public void setLookUrl(String lookUrl) {
        this.lookUrl = lookUrl;
    }

    public String getPhoneLookUrl() {
        return phoneLookUrl;
    }

    public void setPhoneLookUrl(String phoneLookUrl) {
        this.phoneLookUrl = phoneLookUrl;
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }


    public String getConditonBean() {
        return conditonBean;
    }

    public void setConditonBean(String conditonBean) {
        this.conditonBean = conditonBean;
    }

    public String getAppModuleName() {
        return appModuleName;
    }

    public void setAppModuleName(String appModuleName) {
        this.appModuleName = appModuleName;
    }


    public String getConditonProperties() {
        return conditonProperties;
    }

    public void setConditonProperties(String conditonProperties) {
        this.conditonProperties = conditonProperties;
    }

    public String getConditonPValue() {
        return conditonPValue;
    }

    public void setConditonPValue(String conditonPValue) {
        this.conditonPValue = conditonPValue;
    }

    public String getConditonStatusRest() {
        return conditonStatusRest;
    }

    public void setConditonStatusRest(String conditonStatusRest) {
        this.conditonStatusRest = conditonStatusRest;
    }

    public String getConditonPSValue() {
        return conditonPSValue;
    }

    public void setConditonPSValue(String conditonPSValue) {
        this.conditonPSValue = conditonPSValue;
    }

    public String getCompleteTaskServiceUrl() {
        return completeTaskServiceUrl;
    }

    public void setCompleteTaskServiceUrl(String completeTaskServiceUrl) {
        this.completeTaskServiceUrl = completeTaskServiceUrl;
    }

    public String getBusinessDetailServiceUrl() {
        return businessDetailServiceUrl;
    }

    public void setBusinessDetailServiceUrl(String businessDetailServiceUrl) {
        this.businessDetailServiceUrl = businessDetailServiceUrl;
    }

    //    public String getAppModuleId() {
//        return appModuleId;
//    }
//
//    public void setAppModuleId(String appModuleId) {
//        this.appModuleId = appModuleId;
//    }

    public AppModule getAppModule() {
        return appModule;
    }

    public void setAppModule(AppModule appModule) {
        this.appModule = appModule;
    }

    public BusinessModel(Integer version, String name, String className, String conditonBean, String daoBean, String depict, String appModuleCode, String appModuleName, String lookUrl, Set<FlowType> flowTypes) {
        this.version = version;
        this.name = name;
        this.className = className;
        this.conditonBean = conditonBean;
        this.daoBean = daoBean;
        this.depict = depict;
        this.appModuleCode = appModuleCode;
        this.appModuleName = appModuleName;
        this.lookUrl = lookUrl;
        this.flowTypes = flowTypes;
    }


    public BusinessModel() {
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("id", this.getId())
                .append("name", name)
                .append("className", className)
                .append("conditonBean", conditonBean)
                .append("daoBean", daoBean)
                .append("depict", depict)
                .append("appModuleCode", appModuleCode)
                .append("flowTypes", flowTypes)
                .append("lookUrl", lookUrl)
                .append("completeTaskServiceUrl",completeTaskServiceUrl)
                .append("businessDetailServiceUrl",businessDetailServiceUrl)
                .toString();
    }

    @Override
    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj);
    }


//    @Override
    public String getTenantCode() {
        return tenantCode;
    }

//    @Override
    public void setTenantCode(String tenantCode) {
        this.tenantCode = tenantCode;
    }
}
