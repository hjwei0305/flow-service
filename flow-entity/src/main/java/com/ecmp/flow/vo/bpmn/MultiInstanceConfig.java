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
    private String loopCardinality;

    @XmlElement
    private String completionCondition;


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
}
