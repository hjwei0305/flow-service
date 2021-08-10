package com.ecmp.flow.vo;

public class UpdateRemarkByInstanceVo {

    /**
     * 流程实例ID
     */
    private String instanceId;

    /**
     * 修改的备注字段
     */
    private String updateRemark;


    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    public String getUpdateRemark() {
        return updateRemark;
    }

    public void setUpdateRemark(String updateRemark) {
        this.updateRemark = updateRemark;
    }
}
