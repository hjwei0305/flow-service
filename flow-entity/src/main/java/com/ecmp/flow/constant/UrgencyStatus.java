package com.ecmp.flow.constant;

import com.ecmp.annotation.Remark;

import java.io.Serializable;

/**
 * *************************************************************************************************
 * <p/>
 * 实现功能：定义任务紧急程度
 * <p>
 * ------------------------------------------------------------------------------------------------
 * 版本          变更时间             变更人                     变更原因
 * ------------------------------------------------------------------------------------------------
 * 1.0.00      2018/6/28 13:41      谭军(tanjun)                    新建
 * <p/>
 * *************************************************************************************************
 */
public enum UrgencyStatus implements Serializable {
    /**
     * 一般状态
     */
    @Remark("一般")
    NORMAL(0),
    /**
     * 紧急
     */
    @Remark("紧急")
    URGENT(1);

    private int value;

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    UrgencyStatus(int value) {
        this.value = value;
    }
}
