package com.ecmp.flow.vo.bpmn;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;

/**
 * *************************************************************************************************
 * <p/>
 * 实现功能：
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
    private String assignee = "admin";
    /**
     * 候选人id结合，以","间隔
     */
    @XmlAttribute(name = "activiti:candidateUsers")
    private String candidateUsers;

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
}
