package com.ecmp.flow.dto;

import com.ecmp.flow.entity.FlowHistory;
import com.ecmp.flow.entity.FlowInstance;
import com.ecmp.flow.entity.FlowTask;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

/**
 * 实现功能: 门户已办信息
 *
 * @author 王锦光 wangjg
 * @version 2020-06-04 17:29
 */
public class PortalFlowHistory implements Serializable {
    /**
     * 待办任务Id
     */
    private String id;

    /**
     * 流程实例Id
     */
    private String flowInstanceId;

    /**
     * 是否允许撤销任务
     */
    private Boolean canCancel;

    /**
     * 任务状态
     */
    private String taskStatus;

    /**
     * 流程实例是否结束
     */
    private Boolean flowInstanceEnded;

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
     * 流程实例中的创建人账号
     */
    private String flowInstanceCreatorAccount;

    /**
     * 查看单据的URL
     */
    private String flowInstanceLookUrl;

    /**
     * Web基地址绝对路径
     */
    private String webBaseAddressAbsolute;

    /**
     * 流程任务名称
     */
    private String flowTaskName;

    /**
     * 流程任务实际完成时间
     */
    private Date actEndTime;

    /**
     * 默认构造函数
     */
    public PortalFlowHistory() {
    }

    /**
     * 通过工作流已办任务构造
     * @param flowHistory 工作流已办任务
     */
    public PortalFlowHistory(FlowHistory flowHistory) {
        id = flowHistory.getId();
        canCancel = flowHistory.getCanCancel();
        taskStatus = flowHistory.getTaskStatus();
        FlowInstance flowInstance = flowHistory.getFlowInstance();
        if (Objects.nonNull(flowInstance)) {
            flowInstanceId = flowInstance.getId();
            flowInstanceEnded = flowInstance.isEnded();
            flowInstanceBusinessId = flowInstance.getBusinessId();
            flowInstanceBusinessCode = flowInstance.getBusinessCode();
            businessModelRemark = flowInstance.getBusinessModelRemark();
            flowInstanceCreatorAccount = flowInstance.getCreatorAccount();
            flowInstanceLookUrl = flowInstance.getFlowDefVersion().getFlowDefination().getFlowType().getLookUrl();
            if (StringUtils.isBlank(flowInstanceLookUrl)) {
                flowInstanceLookUrl = flowInstance.getFlowDefVersion().getFlowDefination().getFlowType().getBusinessModel().getLookUrl();
            }
        }
        webBaseAddressAbsolute = flowHistory.getWebBaseAddressAbsolute();
        flowTaskName = flowHistory.getFlowTaskName();
        actEndTime = flowHistory.getActEndTime();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public Boolean getCanCancel() {
        return canCancel;
    }

    public void setCanCancel(Boolean canCancel) {
        this.canCancel = canCancel;
    }

    public String getTaskStatus() {
        return taskStatus;
    }

    public void setTaskStatus(String taskStatus) {
        this.taskStatus = taskStatus;
    }

    public Boolean getFlowInstanceEnded() {
        return flowInstanceEnded;
    }

    public void setFlowInstanceEnded(Boolean flowInstanceEnded) {
        this.flowInstanceEnded = flowInstanceEnded;
    }

    public String getFlowInstanceCreatorAccount() {
        return flowInstanceCreatorAccount;
    }

    public void setFlowInstanceCreatorAccount(String flowInstanceCreatorAccount) {
        this.flowInstanceCreatorAccount = flowInstanceCreatorAccount;
    }

    public String getFlowInstanceLookUrl() {
        return flowInstanceLookUrl;
    }

    public void setFlowInstanceLookUrl(String flowInstanceLookUrl) {
        this.flowInstanceLookUrl = flowInstanceLookUrl;
    }

    public String getWebBaseAddressAbsolute() {
        return webBaseAddressAbsolute;
    }

    public void setWebBaseAddressAbsolute(String webBaseAddressAbsolute) {
        this.webBaseAddressAbsolute = webBaseAddressAbsolute;
    }

    public String getFlowTaskName() {
        return flowTaskName;
    }

    public void setFlowTaskName(String flowTaskName) {
        this.flowTaskName = flowTaskName;
    }

    public Date getActEndTime() {
        return actEndTime;
    }

    public void setActEndTime(Date actEndTime) {
        this.actEndTime = actEndTime;
    }
}
