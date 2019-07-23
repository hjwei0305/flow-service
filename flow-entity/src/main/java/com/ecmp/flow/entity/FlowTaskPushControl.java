package com.ecmp.flow.entity;

import com.ecmp.core.entity.BaseAuditableEntity;
import com.ecmp.core.entity.ITenant;

import javax.persistence.*;
import java.util.Date;

/**
 * 推送任务记录父表
 */
@Entity
@Table(name = "flow_task_push_control")
@Access(AccessType.FIELD)
public class FlowTaskPushControl extends BaseAuditableEntity implements ITenant {

    /**
     * 应用模块ID
     */
    @Column(name = "app_module_id")
    private String appModuleId;

    /**
     * 业务实体ID
     */
    @Column(name = "business_model_id")
    private String businessModelId;


    /**
     * 流程类型id
     */
    @Column(name = "flow_type_id")
    private String flowTypeId;

    /**
     * 流程实例id
     */
    @Column(name = "flow_instance_id")
    private String flowInstanceId;

    /**
     * 流程实例名称
     */
    @Column(name = "flow_instance_name")
    private String flowInstanceName;

    /**
     * 流程节点ID
     */
    @Column(name = "flow_act_task_def_key")
    private String flowActTaskDefKey;

    /**
     * 任务名称
     */
    @Column(name = "flow_task_name")
    private String flowTaskName;


    /**
     * 业务id
     */
    @Column(name = "business_id")
    private String businessId;

    /**
     * 业务单号
     */
    @Column(name = "business_code")
    private String businessCode;


    /**
     * 执行人名称列表
     */
    @Column(name = "executor_name_list")
    private String executorNameList;

    /**
     * 推送类型
     */
    @Column(name = "push_type")
    private String pushType;

    /**
     * 推送状态
     */
    @Column(name = "push_status")
    private String pushStatus;

    /**
     * 最近一次推送地址
     */
    @Column(name = "push_url")
    private String pushUrl;


    /**
     * 推送总次数
     */
    @Column(name = "push_number")
    private Integer pushNumber;

    /**
     * 成功次数
     */
    @Column(name = "push_success")
    private Integer pushSuccess;

    /**
     * 失败次数
     */
    @Column(name = "push_false")
    private Integer pushFalse;

    /**
     * 第一次推送时间
     */
    @Column(name = "push_start_date")
    private Date pushStartDate;

    /**
     * 最近一次推送时间
     */
    @Column(name = "push_end_date")
    private Date pushEndDate;
    /**
     * 租户代码
     */
    @Column(name = "tenant_code")
    private String tenantCode;


    public String getAppModuleId() {
        return appModuleId;
    }

    public void setAppModuleId(String appModuleId) {
        this.appModuleId = appModuleId;
    }

    public String getBusinessModelId() {
        return businessModelId;
    }

    public void setBusinessModelId(String businessModelId) {
        this.businessModelId = businessModelId;
    }

    public String getFlowTypeId() {
        return flowTypeId;
    }

    public void setFlowTypeId(String flowTypeId) {
        this.flowTypeId = flowTypeId;
    }

    public String getFlowInstanceId() {
        return flowInstanceId;
    }

    public void setFlowInstanceId(String flowInstanceId) {
        this.flowInstanceId = flowInstanceId;
    }

    public String getFlowActTaskDefKey() {
        return flowActTaskDefKey;
    }

    public void setFlowActTaskDefKey(String flowActTaskDefKey) {
        this.flowActTaskDefKey = flowActTaskDefKey;
    }

    public String getFlowInstanceName() {
        return flowInstanceName;
    }

    public void setFlowInstanceName(String flowInstanceName) {
        this.flowInstanceName = flowInstanceName;
    }

    public String getFlowTaskName() {
        return flowTaskName;
    }

    public void setFlowTaskName(String flowTaskName) {
        this.flowTaskName = flowTaskName;
    }

    public String getBusinessId() {
        return businessId;
    }

    public void setBusinessId(String businessId) {
        this.businessId = businessId;
    }

    public String getBusinessCode() {
        return businessCode;
    }

    public void setBusinessCode(String businessCode) {
        this.businessCode = businessCode;
    }

    public String getExecutorNameList() {
        return executorNameList;
    }

    public void setExecutorNameList(String executorNameList) {
        this.executorNameList = executorNameList;
    }

    public String getPushType() {
        return pushType;
    }

    public void setPushType(String pushType) {
        this.pushType = pushType;
    }

    public String getPushStatus() {
        return pushStatus;
    }

    public void setPushStatus(String pushStatus) {
        this.pushStatus = pushStatus;
    }

    public String getPushUrl() {
        return pushUrl;
    }

    public void setPushUrl(String pushUrl) {
        this.pushUrl = pushUrl;
    }

    public Integer getPushNumber() {
        return pushNumber;
    }

    public void setPushNumber(Integer pushNumber) {
        this.pushNumber = pushNumber;
    }

    public Integer getPushSuccess() {
        return pushSuccess;
    }

    public void setPushSuccess(Integer pushSuccess) {
        this.pushSuccess = pushSuccess;
    }

    public Integer getPushFalse() {
        return pushFalse;
    }

    public void setPushFalse(Integer pushFalse) {
        this.pushFalse = pushFalse;
    }

    public Date getPushStartDate() {
        return pushStartDate;
    }

    public void setPushStartDate(Date pushStartDate) {
        this.pushStartDate = pushStartDate;
    }

    public Date getPushEndDate() {
        return pushEndDate;
    }

    public void setPushEndDate(Date pushEndDate) {
        this.pushEndDate = pushEndDate;
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
