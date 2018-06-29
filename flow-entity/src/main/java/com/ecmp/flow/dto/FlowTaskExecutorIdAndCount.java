package com.ecmp.flow.dto;

import java.io.Serializable;

/**
 * *************************************************************************************************
 * <p/>
 * 实现功能：执行人待办统计dto对象
 * <p>
 * ------------------------------------------------------------------------------------------------
 * 版本          变更时间             变更人                     变更原因
 * ------------------------------------------------------------------------------------------------
 * 1.0.00      2018/6/29 11:45      谭军(tanjun)                    新建
 * <p/>
 * *************************************************************************************************
 */
public class FlowTaskExecutorIdAndCount implements Serializable {
    private String executorId;
    private long count;

    public FlowTaskExecutorIdAndCount(){}

    public FlowTaskExecutorIdAndCount(String executorId,long count){
        this.executorId = executorId;
        this.count = count;
    }

    public String getExecutorId() {
        return executorId;
    }

    public void setExecutorId(String executorId) {
        this.executorId = executorId;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }
}
