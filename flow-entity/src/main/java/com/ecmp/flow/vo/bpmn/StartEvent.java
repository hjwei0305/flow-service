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
@XmlType(name = "startEvent")
public class StartEvent extends BaseFlowNode implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 流程启动人
     */
    @XmlAttribute(name = "activiti:initiator")
    private String initiator = "startUserId";

    public String getInitiator() {
        return initiator;
    }

    public void setInitiator(String initiator) {
        this.initiator = initiator;
    }
}
