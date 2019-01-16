package com.ecmp.flow.vo;

import java.io.Serializable;

public class FlowSolidifyExecutorVO implements Serializable {

    /**
     * 任务定义KEY
     */
    private String  actTaskDefKey;

    /**
     * 是否紧急
     */
    private Boolean instancyStatus = false;

    /**
     * 执行人ids
     */
    private String  executorIds;

    /**
     * 任务类型
     */
    private String nodeType;


    public String getActTaskDefKey() {
        return actTaskDefKey;
    }

    public void setActTaskDefKey(String actTaskDefKey) {
        this.actTaskDefKey = actTaskDefKey;
    }

    public Boolean getInstancyStatus() {
        return instancyStatus;
    }

    public void setInstancyStatus(Boolean instancyStatus) {
        this.instancyStatus = instancyStatus;
    }

    public String getExecutorIds() {
        return executorIds;
    }

    public void setExecutorIds(String executorIds) {
        this.executorIds = executorIds;
    }

    public String getNodeType() {
        return nodeType;
    }

    public void setNodeType(String nodeType) {
        this.nodeType = nodeType;
    }
}
