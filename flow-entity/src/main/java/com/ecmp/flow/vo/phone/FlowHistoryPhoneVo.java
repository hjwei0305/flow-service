package com.ecmp.flow.vo.phone;


import java.io.Serializable;
import java.util.Date;

/**
 * 流程历史VO（移动端专用）
 * 新建：何灿坤
 */
public class FlowHistoryPhoneVo implements Serializable {


    /**
     * 流程历史id
     */
    private String id;

    /**
     * 流程名称
     */
    private String flowName;

    /**
     * 流程任务名
     */
    private String flowTaskName;

    /**
     * 创建时间
     */
    private Date createdDate;

    /**
     * 任务状态
     */
    private String taskStatus;

    /**
     * 是否允许撤销任务
     */
    private Boolean canCancel;


    /**
     * 业务ID
     * flowInstance.businessId
     */
    private String flowInstanceBusinessId;

    /**
     * 业务单号
     * flowInstance.businessCode
     */
    private String flowInstanceBusinessCode;

    /**
     * 是否结束
     * flowInstance.ended
     */
    private Boolean flowInstanceEnded;

    /**
     * 业务单据明细服务地址-主要供移动端使用
     * apiBaseAddressAbsolute+flowInstance.flowDefVersion.flowDefination.flowType.businessModel.businessDetailServiceUrl
     */
    private String businessDetailServiceUrl;




    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFlowName() {
        return flowName;
    }

    public void setFlowName(String flowName) {
        this.flowName = flowName;
    }

    public String getFlowTaskName() {
        return flowTaskName;
    }

    public void setFlowTaskName(String flowTaskName) {
        this.flowTaskName = flowTaskName;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public String getTaskStatus() {
        return taskStatus;
    }

    public void setTaskStatus(String taskStatus) {
        this.taskStatus = taskStatus;
    }

    public Boolean getCanCancel() {
        return canCancel;
    }

    public void setCanCancel(Boolean canCancel) {
        this.canCancel = canCancel;
    }

    public String getFlowInstanceBusinessId() {
        return flowInstanceBusinessId;
    }

    public void setFlowInstanceBusinessId(String flowInstanceBusinessId) {
        this.flowInstanceBusinessId = flowInstanceBusinessId;
    }

    public String getFlowInstanceBusinessCode() {
        return flowInstanceBusinessCode;
    }

    public void setFlowInstanceBusinessCode(String flowInstanceBusinessCode) {
        this.flowInstanceBusinessCode = flowInstanceBusinessCode;
    }

    public Boolean getFlowInstanceEnded() {
        return flowInstanceEnded;
    }

    public void setFlowInstanceEnded(Boolean flowInstanceEnded) {
        this.flowInstanceEnded = flowInstanceEnded;
    }

    public String getBusinessDetailServiceUrl() {
        return businessDetailServiceUrl;
    }

    public void setBusinessDetailServiceUrl(String businessDetailServiceUrl) {
        this.businessDetailServiceUrl = businessDetailServiceUrl;
    }
}
