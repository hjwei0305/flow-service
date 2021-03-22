package com.ecmp.flow.vo;

import java.io.Serializable;

public class JumpTaskVo implements Serializable {

    /**
     * 流程实例ID
     */
    private String instanceId;

    /**
     * 跳转节点ID
     */
    private String targetNodeId;

    /**
     * 是否执行当前节点事后事件
     */
    private boolean currentNodeAfterEvent;

    /**
     * 是否执行目标节点事前事件
     */
    private boolean targetNodeBeforeEvent;

    /**
     * 跳转理由
     */
    private String jumpDepict;


    /**
     * 目标节点taskList：同completeTask方法中的
     */
    private String taskList;


    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    public String getTargetNodeId() {
        return targetNodeId;
    }

    public void setTargetNodeId(String targetNodeId) {
        this.targetNodeId = targetNodeId;
    }

    public boolean isCurrentNodeAfterEvent() {
        return currentNodeAfterEvent;
    }

    public void setCurrentNodeAfterEvent(boolean currentNodeAfterEvent) {
        this.currentNodeAfterEvent = currentNodeAfterEvent;
    }

    public boolean isTargetNodeBeforeEvent() {
        return targetNodeBeforeEvent;
    }

    public void setTargetNodeBeforeEvent(boolean targetNodeBeforeEvent) {
        this.targetNodeBeforeEvent = targetNodeBeforeEvent;
    }

    public String getJumpDepict() {
        return jumpDepict;
    }

    public void setJumpDepict(String jumpDepict) {
        this.jumpDepict = jumpDepict;
    }

    public String getTaskList() {
        return taskList;
    }

    public void setTaskList(String taskList) {
        this.taskList = taskList;
    }
}
