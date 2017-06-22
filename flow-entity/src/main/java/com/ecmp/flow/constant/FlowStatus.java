package com.ecmp.flow.constant;

import java.io.Serializable;

/**
 * *************************************************************************************************
 * <p/>
 * 实现功能：业务实体状态
 * <p>
 * ------------------------------------------------------------------------------------------------
 * 版本          变更时间             变更人                     变更原因
 * ------------------------------------------------------------------------------------------------
 * 1.0.00      2017/5/15 10:04      谭军(tanjun)                    新建
 * <p/>
 * *************************************************************************************************
 */
public enum FlowStatus implements Serializable{

    /**
     * 未进入流程、流程处理中、流程处理完成
     */
    INIT("init"),
    INPROCESS("inProcess"),
    COMPLETED("completed");
    private String value;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    FlowStatus(String value) {
        this.value = value;
    }

}
