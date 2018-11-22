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
    private String executorName;
    private String flowDefinitionId;
    private String flowName;
    private String taskKey;
    private String taskName;
    private long count;

    public FlowTaskExecutorIdAndCount(){}

    public FlowTaskExecutorIdAndCount(String executorId,long count){
        this.executorId = executorId;
        this.count = count;
    }

    public FlowTaskExecutorIdAndCount(String executorId,String executorName,String flowDefinitionId,String flowName,String taskKey,String taskName,long count){
        this.executorId = executorId;
        this.executorName = executorName;
        this.flowDefinitionId = flowDefinitionId;
        this.flowName = flowName;
        this.taskKey = taskKey;
        this.taskName = taskName;
        this.count = count;
    }

    public FlowTaskExecutorIdAndCount(String executorId,String flowDefinitionId,String flowName,String taskKey,String taskName,long count){
        this.executorId = executorId;
        this.flowDefinitionId = flowDefinitionId;
        this.flowName = flowName;
        this.taskKey = taskKey;
        this.taskName = taskName;
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

    public String getFlowDefinitionId() {
        return flowDefinitionId;
    }

    public void setFlowDefinitionId(String flowDefinitionId) {
        this.flowDefinitionId = flowDefinitionId;
    }

    public String getFlowName() {
        return flowName;
    }

    public void setFlowName(String flowName) {
        this.flowName = flowName;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public String getTaskKey() {
        return taskKey;
    }

    public void setTaskKey(String taskKey) {
        this.taskKey = taskKey;
    }

    public String getExecutorName() {
        return executorName;
    }

    public void setExecutorName(String executorName) {
        this.executorName = executorName;
    }
}
