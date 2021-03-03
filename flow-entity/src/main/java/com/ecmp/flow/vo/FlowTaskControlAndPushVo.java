package com.ecmp.flow.vo;

import java.io.Serializable;

public class FlowTaskControlAndPushVo implements Serializable {

    /**
     * 关系表id
     */
    private String id;

    /**
     * 推送任务控制表（父实体）id
     */
    private String controlId;

    /**
     * 推送任务记录表（子实体）id
     */
    private String pushId;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public FlowTaskControlAndPushVo(String id, String controlId, String pushId) {
        this.id = id;
        this.controlId = controlId;
        this.pushId = pushId;
    }

    public String getControlId() {
        return controlId;
    }

    public void setControlId(String controlId) {
        this.controlId = controlId;
    }

    public String getPushId() {
        return pushId;
    }

    public void setPushId(String pushId) {
        this.pushId = pushId;
    }
}
