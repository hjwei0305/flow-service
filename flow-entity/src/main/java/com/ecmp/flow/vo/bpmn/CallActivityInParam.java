package com.ecmp.flow.vo.bpmn;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

/**
 * *************************************************************************************************
 * <p/>
 * 实现功能：
 * <p>
 * ------------------------------------------------------------------------------------------------
 * 版本          变更时间             变更人                     变更原因
 * ------------------------------------------------------------------------------------------------
 * 1.0.00      2017/8/16 14:11      谭军(tanjun)                    新建
 * <p/>
 * *************************************************************************************************
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "activiti:in")
public class CallActivityInParam {
    @XmlAttribute
    private String source;
    @XmlAttribute
    private String target;
    @XmlAttribute
    private String sourceExpression;


    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getSourceExpression() {
        return sourceExpression;
    }

    public void setSourceExpression(String sourceExpression) {
        this.sourceExpression = sourceExpression;
    }
}
