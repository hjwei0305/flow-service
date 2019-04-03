package com.ecmp.flow.vo.phone;

import java.io.Serializable;
import java.util.Date;


/**
 * 批量审批待办VO（移动端专用）
 * 新建：何灿坤
 */
public class FlowTaskBatchPhoneVO implements Serializable {


    /**
     * 待办id
     */
    private String taskId;

    /**
     * 流程名称
     */
    private String flowName;

    /**
     * 待办名称
     */
    private String taskName;

    /**
     * 创建时间
     */
    private Date createdDate;

    /**
     * 流程任务引擎实际的任务签收时间
     */
    private Date actClaimTime;

    /**
     * 移动端能否
     */
    private Boolean canMobile;

    /**
     * 流程节点任务类型
     * JSON.parse(taskJsonDef).nodeType
     */
    private String nodeType;

    /**
     * 业务单号
     * flowInstance.businessCode
     */
    private String flowInstanceBusinessCode;

    /**
     * 流程类型名称
     * flowInstance.flowDefVersion.flowDefination.flowType.name
     */
    private String flowTypeName;

    /**
     * 提交地址
     *  ContextUtil.getGlobalProperty(flowInstance.flowDefVersion.flowDefination.flowType.businessModel.appModule.webBaseAddress) +
     *  flowInstance.flowDefVersion.flowDefination.flowType.businessModel.completeTaskServiceUrl
     */
    private String completeTaskUrl;


    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
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

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public Date getActClaimTime() {
        return actClaimTime;
    }

    public void setActClaimTime(Date actClaimTime) {
        this.actClaimTime = actClaimTime;
    }

    public Boolean getCanMobile() {
        return canMobile;
    }

    public void setCanMobile(Boolean canMobile) {
        this.canMobile = canMobile;
    }

    public String getNodeType() {
        return nodeType;
    }

    public void setNodeType(String nodeType) {
        this.nodeType = nodeType;
    }

    public String getFlowInstanceBusinessCode() {
        return flowInstanceBusinessCode;
    }

    public void setFlowInstanceBusinessCode(String flowInstanceBusinessCode) {
        this.flowInstanceBusinessCode = flowInstanceBusinessCode;
    }

    public String getFlowTypeName() {
        return flowTypeName;
    }

    public void setFlowTypeName(String flowTypeName) {
        this.flowTypeName = flowTypeName;
    }

    public String getCompleteTaskUrl() {
        return completeTaskUrl;
    }

    public void setCompleteTaskUrl(String completeTaskUrl) {
        this.completeTaskUrl = completeTaskUrl;
    }
}
