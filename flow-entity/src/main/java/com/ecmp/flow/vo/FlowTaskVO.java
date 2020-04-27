package com.ecmp.flow.vo;

import java.io.Serializable;

/**
 * *************************************************************************************************
 * <p/>
 * 实现功能：任务vo对象
 * <p>
 * ------------------------------------------------------------------------------------------------
 * 版本          变更时间             变更人                     变更原因
 * ------------------------------------------------------------------------------------------------
 * 1.0.00      2018/5/28 14:48      谭军(tanjun)                    新建
 * <p/>
 * *************************************************************************************************
 */
public class FlowTaskVO implements Serializable {
    /**
     * 任务id
     */
    private String id;
    /**
     * 任务名称
     */
    private String name;
    /**
     * 工作页面
     */
    private String workPageUrl;

    /**
     * 流程实例ID
     */
    private String instanceId;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getWorkPageUrl() {
        return workPageUrl;
    }

    public void setWorkPageUrl(String workPageUrl) {
        this.workPageUrl = workPageUrl;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }
}
