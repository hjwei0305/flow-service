package com.ecmp.flow.vo;

import java.io.Serializable;

public class CleaningPushHistoryVO implements Serializable {

    /**
     * 流程类型ID
     */
    private String flowTypeId;

    /**
     * 保留最近时间段：3、6、12个月
     */
    private Integer recentDate;

    /**
     * 是否异步处理
     */
    private Boolean isAsyn = true;


    public Boolean getAsyn() {
        return isAsyn;
    }

    public void setAsyn(Boolean asyn) {
        isAsyn = asyn;
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
