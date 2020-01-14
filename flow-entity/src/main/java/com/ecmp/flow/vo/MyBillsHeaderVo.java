package com.ecmp.flow.vo;

import java.io.Serializable;

public class MyBillsHeaderVo implements Serializable {

    private String orderType;  //流程状态：all-全部、inFlow-流程中、ended-正常完成、abnormalEnd-异常结束

    private String appModelCode;  //应用模块代码

    public String getOrderType() {
        return orderType;
    }

    public void setOrderType(String orderType) {
        this.orderType = orderType;
    }

    public String getAppModelCode() {
        return appModelCode;
    }

    public void setAppModelCode(String appModelCode) {
        this.appModelCode = appModelCode;
    }
}
