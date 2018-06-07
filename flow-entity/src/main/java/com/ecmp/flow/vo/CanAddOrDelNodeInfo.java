package com.ecmp.flow.vo;

import javax.persistence.Column;
import java.io.Serializable;

/**
 * *************************************************************************************************
 * <p/>
 * 实现功能： 能够执行加减签-会签节点信息列表
 * 先实现会签发起人权限级别
 * <p>
 * ------------------------------------------------------------------------------------------------
 * 版本          变更时间             变更人                     变更原因
 * ------------------------------------------------------------------------------------------------
 * 1.0.00      2018/6/5 14:49      谭军(tanjun)                    新建
 * <p/>
 * *************************************************************************************************
 */
public class CanAddOrDelNodeInfo  implements Serializable {
    private String flowInstanceId;//流程实例Id
    private String nodeKey;//流程节点定义key
    private String nodeName;//流程节点名称==taskName
    private String businessId;//业务单据id
    private String businessName;//业务单据名称
    private String businessModelRemark;//业务摘要(工作说明)
    private String flowName;//流程名称
    private String flowDefKey;//流程定义key


    public String getFlowInstanceId() {
        return flowInstanceId;
    }

    public void setFlowInstanceId(String flowInstanceId) {
        this.flowInstanceId = flowInstanceId;
    }

    public String getNodeKey() {
        return nodeKey;
    }

    public void setNodeKey(String nodeKey) {
        this.nodeKey = nodeKey;
    }

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public String getBusinessId() {
        return businessId;
    }

    public void setBusinessId(String businessId) {
        this.businessId = businessId;
    }

    public String getBusinessName() {
        return businessName;
    }

    public void setBusinessName(String businessName) {
        this.businessName = businessName;
    }

    public String getBusinessModelRemark() {
        return businessModelRemark;
    }

    public void setBusinessModelRemark(String businessModelRemark) {
        this.businessModelRemark = businessModelRemark;
    }

    public String getFlowName() {
        return flowName;
    }

    public void setFlowName(String flowName) {
        this.flowName = flowName;
    }

    public String getFlowDefKey() {
        return flowDefKey;
    }

    public void setFlowDefKey(String flowDefKey) {
        this.flowDefKey = flowDefKey;
    }
}
