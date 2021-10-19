package com.ecmp.flow.vo;


import java.io.Serializable;
import java.util.List;

public class TaskTrustInfoVo implements Serializable {


    /**
     * 被委托的任务ID
     */
    private String taskId;


    /**
     * 选择的用户ID集合
     */
    private List<String>  userIds;

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public List<String> getUserIds() {
        return userIds;
    }

    public void setUserIds(List<String> userIds) {
        this.userIds = userIds;
    }
}
