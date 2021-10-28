package com.ecmp.flow.dto;

import java.io.Serializable;
import java.util.Date;

/**
 * 可以到达的历史节点
 */
public class CanToHistoryNode implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 历史ID
     */
    private String id;

    /**
     * 流程名称
     */
    private String flowName;

    /**
     * 流程任务名
     */
    private String flowTaskName;

    /**
     * 执行人Id
     */
    private String executorId;

    /**
     * 执行人账号
     */
    private String executorAccount;

    /**
     * 执行人名称
     */
    private String executorName;

    /**
     * 执行人处理意见
     */
    private String depict;

    /**
     * 执行时间
     */
    private Date createdDate;


    /**
     * 工作池池代码
     */
    private String poolTaskCode;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFlowName() {
        return flowName;
    }

    public void setFlowName(String flowName) {
        this.flowName = flowName;
    }

    public String getFlowTaskName() {
        return flowTaskName;
    }

    public void setFlowTaskName(String flowTaskName) {
        this.flowTaskName = flowTaskName;
    }

    public String getExecutorId() {
        return executorId;
    }

    public void setExecutorId(String executorId) {
        this.executorId = executorId;
    }

    public String getExecutorAccount() {
        return executorAccount;
    }

    public void setExecutorAccount(String executorAccount) {
        this.executorAccount = executorAccount;
    }

    public String getExecutorName() {
        return executorName;
    }

    public void setExecutorName(String executorName) {
        this.executorName = executorName;
    }

    public String getDepict() {
        return depict;
    }

    public void setDepict(String depict) {
        this.depict = depict;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public String getPoolTaskCode() {
        return poolTaskCode;
    }

    public void setPoolTaskCode(String poolTaskCode) {
        this.poolTaskCode = poolTaskCode;
    }
}
