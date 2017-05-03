package com.ecmp.flow.vo.bpmn;

import javax.xml.bind.annotation.*;
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
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "conditionExpression")
public class ConditionExpression implements Serializable {

    private static final long serialVersionUID = 1L;

    @XmlAttribute(name = "xsi:type")
    private String type = "tFormalExpression";

    @XmlValue
    private String uel;

    public String getUel() {
        return uel;
    }

    public void setUel(String uel) {
        this.uel = uel;
    }

    public ConditionExpression(String uel) {
        this.uel = uel;
    }

    public ConditionExpression(){}
}
