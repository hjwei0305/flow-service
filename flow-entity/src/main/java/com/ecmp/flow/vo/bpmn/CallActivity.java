package com.ecmp.flow.vo.bpmn;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;

/**
 * *************************************************************************************************
 * <p/>
 * 实现功能：调用子流程
 * <p>
 * ------------------------------------------------------------------------------------------------
 * 版本          变更时间             变更人                     变更原因
 * ------------------------------------------------------------------------------------------------
 * 1.0.00      2017/08/16 9:51      谭军(tj)                  新建
 * <p/>
 * *************************************************************************************************
 */
@XmlType(name = "callActivity")
public class CallActivity extends BaseFlowNode implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 调用的流程id
     */
    @XmlAttribute
    private String calledElement;

    public String getCalledElement() {
        return calledElement;
    }

    public void setCalledElement(String calledElement) {
        this.calledElement = calledElement;
    }
}
