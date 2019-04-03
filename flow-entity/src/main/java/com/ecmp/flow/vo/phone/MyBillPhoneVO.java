package com.ecmp.flow.vo.phone;

import java.io.Serializable;
import java.util.Date;


/**
 * 我的单据VO（移动端专用）
 * 新建：何灿坤
 */
public class MyBillPhoneVO  implements Serializable {


    private String businessId; //业务单据id

    private String flowName;//流程名称

    private Date createdDate;//流程发起时间

    private Boolean canManuallyEnd=false;//是否可以人工终止流程实例

    private String businessModelRemark;//业务工作说明

    private String businessCode;//业务单据号

    private String businessModelCode;//业务模型code

    private String detailUrl;//详情查看地址拼接（apiBaseAddressAbsolute+businessDetailServiceUrl）

    private String flowInstanceId;//流程实例ID


    public String getFlowInstanceId() {
        return flowInstanceId;
    }

    public void setFlowInstanceId(String flowInstanceId) {
        this.flowInstanceId = flowInstanceId;
    }

    public String getBusinessId() {
        return businessId;
    }

    public void setBusinessId(String businessId) {
        this.businessId = businessId;
    }

    public String getFlowName() {
        return flowName;
    }

    public void setFlowName(String flowName) {
        this.flowName = flowName;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public Boolean getCanManuallyEnd() {
        return canManuallyEnd;
    }

    public void setCanManuallyEnd(Boolean canManuallyEnd) {
        this.canManuallyEnd = canManuallyEnd;
    }

    public String getBusinessModelRemark() {
        return businessModelRemark;
    }

    public void setBusinessModelRemark(String businessModelRemark) {
        this.businessModelRemark = businessModelRemark;
    }

    public String getBusinessCode() {
        return businessCode;
    }

    public void setBusinessCode(String businessCode) {
        this.businessCode = businessCode;
    }

    public String getBusinessModelCode() {
        return businessModelCode;
    }

    public void setBusinessModelCode(String businessModelCode) {
        this.businessModelCode = businessModelCode;
    }

    public String getDetailUrl() {
        return detailUrl;
    }

    public void setDetailUrl(String detailUrl) {
        this.detailUrl = detailUrl;
    }
}
