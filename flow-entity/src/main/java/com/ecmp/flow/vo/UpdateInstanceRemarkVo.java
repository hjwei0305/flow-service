package com.ecmp.flow.vo;

import java.io.Serializable;

public class UpdateInstanceRemarkVo implements Serializable {

    /**
     * 业务单据ID
     */
    private String businessId;

    /**
     * 修改的备注字段
     */
    private String updateRemark;


    public String getBusinessId() {
        return businessId;
    }

    public void setBusinessId(String businessId) {
        this.businessId = businessId;
    }

    public String getUpdateRemark() {
        return updateRemark;
    }

    public void setUpdateRemark(String updateRemark) {
        this.updateRemark = updateRemark;
    }
}
