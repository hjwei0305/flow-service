package com.ecmp.flow.vo.bpmn;

import javax.xml.bind.annotation.*;
import java.io.Serializable;

/**
 * *************************************************************************************************
 * <p/>
 * 实现功能：会签任务参数配置类
 * <p>
 * ------------------------------------------------------------------------------------------------
 * 版本          变更时间             变更人                     变更原因
 * ------------------------------------------------------------------------------------------------
 * 1.0.00      2017/4/10 9:51      陈飞(fly)                  新建
 * <p/>
 * *************************************************************************************************
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "multiInstanceLoopCharacteristics")
public class MultiInstanceConfig implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 是否串行，默认false
     */
    @XmlAttribute
    private boolean isSequential = false;
    /**
     * 会签人id集合，以","间隔
     */
    @XmlAttribute(name = "activiti:collection")
    private String userIds;

    @XmlAttribute(name = "activiti:elementVariable")
    private String variable;

    @XmlElement
    private String loopCardinality;//设定的循环次数

    @XmlElement
    private String completionCondition="${nrOfCompletedInstances/nrOfInstances == 1}";//审批完成的条件，暂时定义为需要全部人审批

    @XmlTransient
    private Integer ballot;//赞成票数

    @XmlTransient
    private Byte votingType=0;//投票类型，0代表百分比，1代表绝对赞成票数

    @XmlTransient
    private Byte percentApproval;//赞成百分比，前端默认为100%

    public String getUserIds() {
        return userIds;
    }

    public void setUserIds(String userIds) {
        this.userIds = userIds;
    }

    public boolean isSequential() {
        return isSequential;
    }

    public void setSequential(boolean sequential) {
        isSequential = sequential;
    }

    public String getVariable() {
        return variable;
    }

    public void setVariable(String variable) {
        this.variable = variable;
    }

    public String getLoopCardinality() {
        return loopCardinality;
    }

    public void setLoopCardinality(String loopCardinality) {
        this.loopCardinality = loopCardinality;
    }

    public String getCompletionCondition() {
        return completionCondition;
    }

    public void setCompletionCondition(String completionCondition) {
        this.completionCondition = completionCondition;
    }

    public Integer getBallot() {
        return ballot;
    }

    public void setBallot(Integer ballot) {
        this.ballot = ballot;
    }

    public Byte getVotingType() {
        return votingType;
    }

    public void setVotingType(Byte votingType) {
        this.votingType = votingType;
    }

    public Byte getPercentApproval() {
        return percentApproval;
    }

    public void setPercentApproval(Byte percentApproval) {
        this.percentApproval = percentApproval;
    }
}
