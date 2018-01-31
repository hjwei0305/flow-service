package com.ecmp.flow.vo;

import java.io.Serializable;

/**
 * *************************************************************************************************
 * <p/>
 * 实现功能：
 * <p>
 * ------------------------------------------------------------------------------------------------
 * 版本          变更时间             变更人                     变更原因
 * ------------------------------------------------------------------------------------------------
 * 1.0.00      2018/1/30 21:21      谭军(tanjun)                    新建
 * <p/>
 * *************************************************************************************************
 */
public class StartFlowTypeVO implements Serializable {
    private String id;
    private String name;
    private String flowDefName;//流程定义名称
    private String flowDefKey;//流程定义Key

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

    public String getFlowDefName() {
        return flowDefName;
    }

    public void setFlowDefName(String flowDefName) {
        this.flowDefName = flowDefName;
    }

    public String getFlowDefKey() {
        return flowDefKey;
    }

    public void setFlowDefKey(String flowDefKey) {
        this.flowDefKey = flowDefKey;
    }
}
