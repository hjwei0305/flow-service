package com.ecmp.flow.vo;

import java.io.Serializable;

public class CleaningPushHistoryVO implements Serializable {

    /**
     * 应用模块ID
     */
    private String appModuleId;

    /**
     * 业务实体ID
     */
    private String businessModelId;

    /**
     * 流程类型ID
     */
    private String flowTypeId;

    /**
     * 保留最近时间段：3、6、12个月
     */
    private Integer recentDate;

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

    public Integer getRecentDate() {
        return recentDate;
    }

    public void setRecentDate(Integer recentDate) {
        this.recentDate = recentDate;
    }
}
