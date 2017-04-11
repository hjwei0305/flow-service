package com.ecmp.flow.com.ecmp.flow.util;

/**
 * *************************************************************************************************
 * <p/>
 * 实现功能：定义任务状态
 * <p>
 * ------------------------------------------------------------------------------------------------
 * 版本          变更时间             变更人                     变更原因
 * ------------------------------------------------------------------------------------------------
 * 1.0.00      2017/4/11 10:04      谭军(tanjun)                    新建
 * <p/>
 * *************************************************************************************************
 */
public enum TaskStatus {

    /**
     * 待办、签收、撤销、完成、挂起
     */
    INIT("init"),
    CLAIM("claim"),
    CANCLE("cancel"),
    COMPLETED("completed"),
    SUSPEND("suspend");
    private String value;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    TaskStatus(String value) {
        this.value = value;
    }
}
