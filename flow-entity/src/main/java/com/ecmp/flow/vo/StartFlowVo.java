package com.ecmp.flow.vo;

import java.io.Serializable;

public class StartFlowVo implements Serializable {

    private String businessModelCode;

    private String businessKey;

    private String opinion;

    private String typeId;

    private String flowDefKey;

    private String taskList;

    private String anonymousNodeId;


    public String getBusinessModelCode() {
        return businessModelCode;
    }

    public void setBusinessModelCode(String businessModelCode) {
        this.businessModelCode = businessModelCode;
    }

    public String getBusinessKey() {
        return businessKey;
    }

    public void setBusinessKey(String businessKey) {
        this.businessKey = businessKey;
    }

    public String getOpinion() {
        return opinion;
    }

    public void setOpinion(String opinion) {
        this.opinion = opinion;
    }

    public String getTypeId() {
        return typeId;
    }

    public void setTypeId(String typeId) {
        this.typeId = typeId;
    }

    public String getFlowDefKey() {
        return flowDefKey;
    }

    public void setFlowDefKey(String flowDefKey) {
        this.flowDefKey = flowDefKey;
    }

    public String getTaskList() {
        return taskList;
    }

    public void setTaskList(String taskList) {
        this.taskList = taskList;
    }

    public String getAnonymousNodeId() {
        return anonymousNodeId;
    }

    public void setAnonymousNodeId(String anonymousNodeId) {
        this.anonymousNodeId = anonymousNodeId;
    }
}
