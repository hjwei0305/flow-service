package com.ecmp.flow.vo;

import java.io.Serializable;

public class TaskTurnInfoVo  implements Serializable {

    /**
     * 被转办的任务ID
     */
    private String taskId;


    /**
     * 转办人ID
     */
    private String userId;


    /**
     * 转办时输入的意见
     */
    private String opinion;


    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getOpinion() {
        return opinion;
    }

    public void setOpinion(String opinion) {
        this.opinion = opinion;
    }
}
