package com.ecmp.flow.vo.bpmn;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;

/**
 * *************************************************************************************************
 * <p/>
 * 实现功能：服务任务
 * <p>
 * ------------------------------------------------------------------------------------------------
 * 版本          变更时间             变更人                     变更原因
 * ------------------------------------------------------------------------------------------------
 * 1.0.00      2017/4/10 9:51      陈飞(fly)                  新建
 * 2.0.00      2017/8/12 10:51     谭军(tj)                   增加服务配置
 * <p/>
 * *************************************************************************************************
 */
@XmlType(name = "serviceTask")
public class ServiceTask extends BaseFlowNode implements Serializable {
    private static final long serialVersionUID = 1L;

    @XmlAttribute(name = "activiti:delegateExpression")
    private String delegateExpression;

    @XmlAttribute(name = "activiti:expression")
    private String expression;

    @XmlAttribute(name = "activiti:class")
    private String classStr;

    public String getDelegateExpression() {
        return delegateExpression;
    }

    public void setDelegateExpression(String delegateExpression) {
        this.delegateExpression = delegateExpression;
    }

    public String getExpression() {
        return expression;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }

    public String getClassStr() {
        return classStr;
    }

    public void setClassStr(String classStr) {
        this.classStr = classStr;
    }
}
