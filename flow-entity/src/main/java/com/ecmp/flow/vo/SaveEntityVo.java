package com.ecmp.flow.vo;

import java.io.Serializable;

public class SaveEntityVo implements Serializable {


    /**
     * 流程设计json
     */
    private String def;

    /**
     * 是否发布
     */
    private Boolean deploy;


    /**
     * 用于数据同步后统一发布
     */
    private String flowDefinationId;


    public Boolean getDeploy() {
        return deploy;
    }

    public void setDeploy(Boolean deploy) {
        this.deploy = deploy;
    }

    public String getDef() {
        return def;
    }

    public void setDef(String def) {
        this.def = def;
    }

    public String getFlowDefinationId() {
        return flowDefinationId;
    }

    public void setFlowDefinationId(String flowDefinationId) {
        this.flowDefinationId = flowDefinationId;
    }
}
