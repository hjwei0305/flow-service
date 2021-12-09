package com.ecmp.flow.dto;

import com.ecmp.flow.entity.FlowInstance;
import com.ecmp.flow.entity.FlowTask;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

/**
 * 实现功能: 门户待办信息
 *
 * @author 王锦光 wangjg
 * @version 2020-06-04 17:29
 */
public class PortalFlowTask implements Serializable {
    /**
     * 待办任务Id
     */
    private String id;

    /**
     * 任务表单URL
     */
    private String taskFormUrl;

    /**
     * 流程实例Id
     */
    private String flowInstanceId;

    /**
     * 流程实例中业务单据Id
     */
    private String flowInstanceBusinessId;

    /**
     * 流程实例中业务单据编号
     */
    private String flowInstanceBusinessCode;

    /**
     * 流程实例中的业务说明
     */
    private String businessModelRemark;

    /**
     * 委托状态：被转办的状态0，委托状态，1发起委托的任务，2被委托的任务,非委托状态为null,委托完成为3
     */
    private Integer trustState;

    /**
     * 任务说明
     */
    private String taskName;

    /**
     * 优先级
     */
    private Integer priority = 0;

    /**
     * 任务到达时间
     */
    private Date createdDate;

    /**
     * 任务额定工时（小时）
     */
    private Double timing;

    /**
     * 预警状态
     * EarlyWarningStatus.code
     * ("normal", "正常")
     * ("warning", "预警")
     * ("timeout", "超时");
     */
    private String warningStatus;


    /**
     * 标注原因
     */
    private String labelReason;


    /**
     * 默认构造函数
     */
    public PortalFlowTask() {
    }

    /**
     * 通过工作流任务构造
     *
     * @param flowTask 工作流任务
     */
    public PortalFlowTask(FlowTask flowTask) {
        id = flowTask.getId();
        taskFormUrl = flowTask.getTaskFormUrl();
        FlowInstance flowInstance = flowTask.getFlowInstance();
        if (Objects.nonNull(flowInstance)) {
            flowInstanceId = flowInstance.getId();
            flowInstanceBusinessId = flowInstance.getBusinessId();
            flowInstanceBusinessCode = flowInstance.getBusinessCode();
            businessModelRemark = flowInstance.getBusinessModelRemark();
        }
        trustState = flowTask.getTrustState();
        taskName = flowTask.getTaskName();
        priority = flowTask.getPriority();
        createdDate = flowTask.getCreatedDate();
        timing = flowTask.getTiming();
        warningStatus = flowTask.getWarningStatus();
        labelReason = flowTask.getLabelReason();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Double getTiming() {
        return timing;
    }

    public void setTiming(Double timing) {
        this.timing = timing;
    }

    public String getWarningStatus() {
        return warningStatus;
    }

    public void setWarningStatus(String warningStatus) {
        this.warningStatus = warningStatus;
    }

    public String getTaskFormUrl() {
        return taskFormUrl;
    }

    public void setTaskFormUrl(String taskFormUrl) {
        this.taskFormUrl = taskFormUrl;
    }

    public String getFlowInstanceId() {
        return flowInstanceId;
    }

    public void setFlowInstanceId(String flowInstanceId) {
        this.flowInstanceId = flowInstanceId;
    }

    public String getFlowInstanceBusinessId() {
        return flowInstanceBusinessId;
    }

    public void setFlowInstanceBusinessId(String flowInstanceBusinessId) {
        this.flowInstanceBusinessId = flowInstanceBusinessId;
    }

    public String getFlowInstanceBusinessCode() {
        return flowInstanceBusinessCode;
    }

    public void setFlowInstanceBusinessCode(String flowInstanceBusinessCode) {
        this.flowInstanceBusinessCode = flowInstanceBusinessCode;
    }

    public String getBusinessModelRemark() {
        return businessModelRemark;
    }

    public void setBusinessModelRemark(String businessModelRemark) {
        this.businessModelRemark = businessModelRemark;
    }

    public Integer getTrustState() {
        return trustState;
    }

    public void setTrustState(Integer trustState) {
        this.trustState = trustState;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public String getLabelReason() {
        return labelReason;
    }

    public void setLabelReason(String labelReason) {
        this.labelReason = labelReason;
    }
}
