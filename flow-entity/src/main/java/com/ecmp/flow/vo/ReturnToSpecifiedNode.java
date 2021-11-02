package com.ecmp.flow.vo;

import java.io.Serializable;

public class ReturnToSpecifiedNode implements Serializable {

    /**
     * 指定退回的节点ID
     */
    private String nodeId;


    /**
     * 当前任务ID
     */
    private String taskId;


    /**
     * 处理后是否返回我审批
     */
    private Boolean allowJumpBack;


    /**
     * 退回原因或意见
     */
    private String opinion;


    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public Boolean getAllowJumpBack() {
        return allowJumpBack;
    }

    public void setAllowJumpBack(Boolean allowJumpBack) {
        this.allowJumpBack = allowJumpBack;
    }

    public String getOpinion() {
        return opinion;
    }

    public void setOpinion(String opinion) {
        this.opinion = opinion;
    }
}
