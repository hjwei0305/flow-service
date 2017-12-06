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
 * 1.0.00      2017/12/5 17:23      谭军(tanjun)                    新建
 * <p/>
 * *************************************************************************************************
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "documentation")
public class Documentation {
    private static final long serialVersionUID = 1L;

    @XmlValue
    private String value;

    public Documentation(){

    }

    public Documentation(String value){
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
