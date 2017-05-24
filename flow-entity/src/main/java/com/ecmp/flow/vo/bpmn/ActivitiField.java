package com.ecmp.flow.vo.bpmn;

import javax.xml.bind.annotation.*;

/**
 * *************************************************************************************************
 * <p/>
 * 实现功能：
 * <p>
 * ------------------------------------------------------------------------------------------------
 * 版本          变更时间             变更人                     变更原因
 * ------------------------------------------------------------------------------------------------
 * 1.0.00      2017/5/24 9:49      谭军(tanjun)                    新建
 * <p/>
 * *************************************************************************************************
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "activiti:field")
public class ActivitiField {

    @XmlAttribute()
    private String name;
    @XmlElement(name = "activiti:string")
    private String activitiString;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getActivitiString() {
        return activitiString;
    }

    public void setActivitiString(String activitiString) {
        this.activitiString = activitiString;
    }
}
