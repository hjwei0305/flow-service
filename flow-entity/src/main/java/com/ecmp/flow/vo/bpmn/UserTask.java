package com.ecmp.flow.vo.bpmn;

import net.sf.json.JSONObject;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;

/**
 * *************************************************************************************************
 * <p/>
 * 实现功能：审批任务
 * <p>
 * ------------------------------------------------------------------------------------------------
 * 版本          变更时间             变更人                     变更原因
 * ------------------------------------------------------------------------------------------------
 * 1.0.00      2017/4/10 9:51      陈飞(fly)                  新建
 * <p/>
 * *************************************************************************************************
 */
@XmlType(name = "userTask")
public class UserTask extends BaseFlowNode implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 任务参与人id
     */
    @XmlAttribute(name = "activiti:assignee")
    private String assignee;
    /**
     * 候选人id集合，以","间隔
     */
    @XmlAttribute(name = "activiti:candidateUsers")
    private String candidateUsers;


    /**
     * activti扩展属性，任务监听器之类
     */
    @XmlElement(name = "extensionElements")
    private ExtensionElement extensionElement;


    /**
     * 会签配置
     */
    @XmlElement(name = "multiInstanceLoopCharacteristics")
    private MultiInstanceConfig config;

//    /**
//     * 是否是审批任务类型
//     */
//    @XmlTransient
//    private Boolean approval=false;

    /**
     * 前端节点类型
     */
    @XmlTransient
    private String nodeType;//Normal,SingleSign,CounterSign

//    /**
//     * 执行人配置信息
//     */
//    @XmlTransient
//    private JSONObject executor;
//
//    /**
//     * 事件
//     */
//    @XmlTransient
//    private JSONObject event;
//
//    /**
//     * 通知
//     */
//    @XmlTransient
//    private JSONObject notify;


    public String getAssignee() {
        return assignee;
    }

    public void setAssignee(String assignee) {
        this.assignee = assignee;
    }

    public String getCandidateUsers() {
        return candidateUsers;
    }

    public void setCandidateUsers(String candidateUsers) {
        this.candidateUsers = candidateUsers;
    }

    public MultiInstanceConfig getConfig() {
        return config;
    }

    public void setConfig(MultiInstanceConfig config) {
        this.config = config;
    }

    public String getNodeType() {
        return nodeType;
    }

    public void setNodeType(String nodeType) {
        this.nodeType = nodeType;
    }

    public ExtensionElement getExtensionElement() {
        return extensionElement;
    }

    public void setExtensionElement(ExtensionElement extensionElement) {
        this.extensionElement = extensionElement;
    }

}
