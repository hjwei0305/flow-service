package com.ecmp.flow.vo;

import java.io.Serializable;

public class SolidifyStartExecutorVo implements Serializable {

    /**
     * 节点名称
     */
    private String actTaskDefKey;

    /**
     * 执行人ids
     */
    private String executorIds;

    /**
     * 紧急状态
     */
    private boolean instancyStatus = false;

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

    public String getExecutorIds() {
        return executorIds;
    }

    public void setExecutorIds(String executorIds) {
        this.executorIds = executorIds;
    }

    public boolean isInstancyStatus() {
        return instancyStatus;
    }

    public void setInstancyStatus(boolean instancyStatus) {
        this.instancyStatus = instancyStatus;
    }

    public String getNodeType() {
        return nodeType;
    }

    public void setNodeType(String nodeType) {
        this.nodeType = nodeType;
    }
}
