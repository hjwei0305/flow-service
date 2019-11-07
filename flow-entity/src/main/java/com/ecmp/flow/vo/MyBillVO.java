package com.ecmp.flow.vo;

import java.io.Serializable;
import java.util.Date;

/**
 * *************************************************************************************************
 * <p/>
 * 实现功能：我的单据
 * <p>
 * ------------------------------------------------------------------------------------------------
 * 版本          变更时间             变更人                     变更原因
 * ------------------------------------------------------------------------------------------------
 * 1.0.00      2017/6/18 22:14      谭军(tanjun)                    新建
 * <p/>
 * *************************************************************************************************
 */
public class MyBillVO implements Serializable{

    private String businessModelCode;//业务模型code

    private String businessDetailServiceUrl;//表单明细地址

    private String businessName;//业务单据名称

    private String creatorId;//流程发起人id

    private String creatorAccount;//流程发起人账号

    private String creatorName;//流程发起人姓名

    private Date createdDate;//流程发起时间

    private String businessId; //业务单据id

    private String businessCode;//业务单据号

    private String businessModelRemark;//业务工作说明

    private String flowName;//流程名称

    private Date endDate;//流程结束时间

    private String lookUrl;//表单查看url

    private Boolean canManuallyEnd=false;//是否可以人工终止流程实例

    private Boolean ManuallyEnd;//是否异常结束

    private String flowInstanceId;//流程实例ID

    private String webBaseAddress;

    private String webBaseAddressAbsolute;

    private String apiBaseAddress;

    private String apiBaseAddressAbsolute;

    private Boolean ended; //是否结束

    private String taskExecutors; //待办执行人（中泰）


    public Boolean getManuallyEnd() {
        return ManuallyEnd;
    }

    public void setManuallyEnd(Boolean manuallyEnd) {
        ManuallyEnd = manuallyEnd;
    }

    public Boolean getEnded() {
        return ended;
    }

    public void setEnded(Boolean ended) {
        this.ended = ended;
    }

    public String getTaskExecutors() {
        return taskExecutors;
    }

    public void setTaskExecutors(String taskExecutors) {
        this.taskExecutors = taskExecutors;
    }

    public String getBusinessName() {
        return businessName;
    }

    public void setBusinessName(String businessName) {
        this.businessName = businessName;
    }

    public String getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(String creatorId) {
        this.creatorId = creatorId;
    }

    public String getCreatorAccount() {
        return creatorAccount;
    }

    public void setCreatorAccount(String creatorAccount) {
        this.creatorAccount = creatorAccount;
    }

    public String getCreatorName() {
        return creatorName;
    }

    public void setCreatorName(String creatorName) {
        this.creatorName = creatorName;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
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

    public String getBusinessModelRemark() {
        return businessModelRemark;
    }

    public void setBusinessModelRemark(String businessModelRemark) {
        this.businessModelRemark = businessModelRemark;
    }

    public String getFlowName() {
        return flowName;
    }

    public void setFlowName(String flowName) {
        this.flowName = flowName;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public String getLookUrl() {
        return lookUrl;
    }

    public void setLookUrl(String lookUrl) {
        this.lookUrl = lookUrl;
    }

    public Boolean getCanManuallyEnd() {
        return canManuallyEnd;
    }

    public void setCanManuallyEnd(Boolean canManuallyEnd) {
        this.canManuallyEnd = canManuallyEnd;
    }

    public String getFlowInstanceId() {
        return flowInstanceId;
    }

    public void setFlowInstanceId(String flowInstanceId) {
        this.flowInstanceId = flowInstanceId;
    }

    public String getWebBaseAddress() {
        return webBaseAddress;
    }

    public void setWebBaseAddress(String webBaseAddress) {
        this.webBaseAddress = webBaseAddress;
    }

    public String getWebBaseAddressAbsolute() {
        return webBaseAddressAbsolute;
    }

    public void setWebBaseAddressAbsolute(String webBaseAddressAbsolute) {
        this.webBaseAddressAbsolute = webBaseAddressAbsolute;
    }

    public String getApiBaseAddress() {
        return apiBaseAddress;
    }

    public void setApiBaseAddress(String apiBaseAddress) {
        this.apiBaseAddress = apiBaseAddress;
    }

    public String getApiBaseAddressAbsolute() {
        return apiBaseAddressAbsolute;
    }

    public void setApiBaseAddressAbsolute(String apiBaseAddressAbsolute) {
        this.apiBaseAddressAbsolute = apiBaseAddressAbsolute;
    }

    public String getBusinessModelCode() {
        return businessModelCode;
    }

    public void setBusinessModelCode(String businessModelCode) {
        this.businessModelCode = businessModelCode;
    }

    public String getBusinessDetailServiceUrl() {
        return businessDetailServiceUrl;
    }

    public void setBusinessDetailServiceUrl(String businessDetailServiceUrl) {
        this.businessDetailServiceUrl = businessDetailServiceUrl;
    }
}
