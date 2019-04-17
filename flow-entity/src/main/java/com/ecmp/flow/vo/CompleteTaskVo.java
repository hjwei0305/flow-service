package com.ecmp.flow.vo;

import java.io.Serializable;

/**
 * 提交任务Vo对象
 */
public class CompleteTaskVo implements Serializable {

    private String taskId;

    private String businessId;

    private String opinion;

    private String taskList;

    private String endEventId;

    private boolean manualSelected;

    private String approved;

    private Long loadOverTime;


    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getBusinessId() {
        return businessId;
    }

    public void setBusinessId(String businessId) {
        this.businessId = businessId;
    }

    public String getOpinion() {
        return opinion;
    }

    public void setOpinion(String opinion) {
        this.opinion = opinion;
    }

    public String getTaskList() {
        return taskList;
    }

    public void setTaskList(String taskList) {
        this.taskList = taskList;
    }

    public String getEndEventId() {
        return endEventId;
    }

    public void setEndEventId(String endEventId) {
        this.endEventId = endEventId;
    }

    public boolean isManualSelected() {
        return manualSelected;
    }

    public void setManualSelected(boolean manualSelected) {
        this.manualSelected = manualSelected;
    }

    public String getApproved() {
        return approved;
    }

    public void setApproved(String approved) {
        this.approved = approved;
    }

    public Long getLoadOverTime() {
        return loadOverTime;
    }

    public void setLoadOverTime(Long loadOverTime) {
        this.loadOverTime = loadOverTime;
    }
}
