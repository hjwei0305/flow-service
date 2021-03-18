package com.ecmp.flow.vo;

import java.io.Serializable;

public class TargetNodeInfoVo implements Serializable {

    /**
     * 是否固化流程
     */
    private boolean solidifyFlow;

    /**
     * 目标节点信息
     */
    private NodeInfo targetNodeInfo;

    public TargetNodeInfoVo() {
    }

    public TargetNodeInfoVo(boolean solidifyFlow, NodeInfo targetNodeInfo) {
        this.solidifyFlow = solidifyFlow;
        this.targetNodeInfo = targetNodeInfo;
    }

    public boolean isSolidifyFlow() {
        return solidifyFlow;
    }

    public void setSolidifyFlow(boolean solidifyFlow) {
        this.solidifyFlow = solidifyFlow;
    }

    public NodeInfo getTargetNodeInfo() {
        return targetNodeInfo;
    }

    public void setTargetNodeInfo(NodeInfo targetNodeInfo) {
        this.targetNodeInfo = targetNodeInfo;
    }
}
