package com.ecmp.flow.vo;

import java.io.Serializable;


/**
 * 标注待办原因VO
 */
public class LabelTaskReasonVo implements Serializable {

    /**
     * 任务ID
     */
    private String taskId;


    /**
     * 标注待办原因
     */
    private String labelReason;


    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getLabelReason() {
        return labelReason;
    }

    public void setLabelReason(String labelReason) {
        this.labelReason = labelReason;
    }
}
