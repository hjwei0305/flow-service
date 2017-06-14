package com.ecmp.flow.vo;

import java.io.Serializable;

/**
 * *************************************************************************************************
 * <p/>
 * 实现功能： 待办汇总VO对象
 * <p>
 * ------------------------------------------------------------------------------------------------
 * 版本          变更时间             变更人                     变更原因
 * ------------------------------------------------------------------------------------------------
 * 1.0.00      2017/6/14 13:59      谭军(tanjun)                    新建
 * <p/>
 * *************************************************************************************************
 */
public class TodoBusinessSummaryVO implements Serializable {
    private String businessModeId;
    private String businessModelCode;
    private int count;
    private String getBusinessName;

    public String getBusinessModeId() {
        return businessModeId;
    }

    public void setBusinessModeId(String businessModeId) {
        this.businessModeId = businessModeId;
    }

    public String getBusinessModelCode() {
        return businessModelCode;
    }

    public void setBusinessModelCode(String businessModelCode) {
        this.businessModelCode = businessModelCode;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getGetBusinessName() {
        return getBusinessName;
    }

    public void setGetBusinessName(String getBusinessName) {
        this.getBusinessName = getBusinessName;
    }
}
