package com.ecmp.flow.vo;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.Serializable;
import java.util.Date;

/**
 * *************************************************************************************************
 * <br>
 * 实现功能：
 * <br>
 * ------------------------------------------------------------------------------------------------
 * <br>
 * 版本          变更时间             变更人                     变更原因
 * <br>
 * ------------------------------------------------------------------------------------------------
 * <br>
 * 1.0.00      2017/5/27 10:24      詹耀(xxxlimit)                    新建
 * <br>
 * *************************************************************************************************<br>
 */
public class FlowHandleHistoryVO implements Serializable {

    /**
     * 流程历史任务名
     */
    private String flowHistoryTaskName;

    /**
     * 流程历史任务执行人
     */
    private String flowHistoryTaskExecutorName;

    /**
     * 流程任务结束时间，
     */
    private Date flowHistoryTaskEndTime;

    /**
     * 流程任务执行消耗时间
     */
    private Long flowHistoryTaskDurationInMillis;

    /**
     * 流程任务处理摘要
     */
    private String flowHistoryTaskRemark;

    public String getFlowHistoryTaskName() {
        return flowHistoryTaskName;
    }

    public void setFlowHistoryTaskName(String flowHistoryTaskName) {
        this.flowHistoryTaskName = flowHistoryTaskName;
    }

    public String getFlowHistoryTaskExecutorName() {
        return flowHistoryTaskExecutorName;
    }

    public void setFlowHistoryTaskExecutorName(String flowHistoryTaskExecutorName) {
        this.flowHistoryTaskExecutorName = flowHistoryTaskExecutorName;
    }

    public Date getFlowHistoryTaskEndTime() {
        return flowHistoryTaskEndTime;
    }

    public void setFlowHistoryTaskEndTime(Date flowHistoryTaskEndTime) {
        this.flowHistoryTaskEndTime = flowHistoryTaskEndTime;
    }

    public Long getFlowHistoryTaskDurationInMillis() {
        return flowHistoryTaskDurationInMillis;
    }

    public void setFlowHistoryTaskDurationInMillis(Long flowHistoryTaskDurationInMillis) {
        this.flowHistoryTaskDurationInMillis = flowHistoryTaskDurationInMillis;
    }

    public String getFlowHistoryTaskRemark() {
        return flowHistoryTaskRemark;
    }

    public void setFlowHistoryTaskRemark(String flowHistoryTaskRemark) {
        this.flowHistoryTaskRemark = flowHistoryTaskRemark;
    }
}
