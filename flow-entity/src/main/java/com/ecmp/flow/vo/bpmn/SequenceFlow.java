package com.ecmp.flow.vo.bpmn;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;

/**
 * *************************************************************************************************
 * <p/>
 * 实现功能：bmpn连线类
 * <p>
 * ------------------------------------------------------------------------------------------------
 * 版本          变更时间             变更人                     变更原因
 * ------------------------------------------------------------------------------------------------
 * 1.0.00      2017/4/10 14:58      陈飞(fly)                  新建
 * <p/>
 * *************************************************************************************************
 */
@XmlType(name = "sequenceFlow")
public class SequenceFlow extends BaseNode implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 源节点
     */
    @XmlAttribute
    private String sourceRef;
    /**
     * 目标节点
     */
    @XmlAttribute
    private String targetRef;

    /**
     * UEL
     */
    @XmlElement(name = "conditionExpression xsi:type=\"tFormalExpression\"")
    private String uel;

    public SequenceFlow(String id, String sourceRef, String targetRef, String conditionExpression) {
        this.id = id;
        this.sourceRef = sourceRef;
        this.targetRef = targetRef;
        this.uel = conditionExpression;
    }

    public String getSourceRef() {
        return sourceRef;
    }

    public void setSourceRef(String sourceRef) {
        this.sourceRef = sourceRef;
    }

    public String getTargetRef() {
        return targetRef;
    }

    public void setTargetRef(String targetRef) {
        this.targetRef = targetRef;
    }

    public String getUel() {
        return uel;
    }

    public void setUel(String uel) {
        this.uel = uel;
    }
}
