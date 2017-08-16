package com.ecmp.flow.vo.bpmn;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.List;

/**
 * *************************************************************************************************
 * <p/>
 * 实现功能：
 * <p>
 * ------------------------------------------------------------------------------------------------
 * 版本          变更时间             变更人                     变更原因
 * ------------------------------------------------------------------------------------------------
 * 1.0.00      2017/5/24 9:33      谭军(tanjun)                    新建
 * 2.0.00      2017/8/16 14:33     谭军(tanjun)                    扩展调用任务输入输出参数
 * <p/>
 * *************************************************************************************************
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "extensionElements")
public class ExtensionElement {
    @XmlElement(name = "activiti:taskListener")
    private List<TaskListener> taskListener;

    @XmlElement(name = "activiti:executionListener")
    private List<ExecutionListener> executionListener;

    @XmlElement(name = "activiti:in")
    private List<CallActivityInParam> callActivityInParam;

    @XmlElement(name = "activiti:out")
    private List<CallActivityOutParam> callActivityOutParam;

    public List<TaskListener> getTaskListener() {
        return taskListener;
    }

    public void setTaskListener(List<TaskListener> taskListener) {
        this.taskListener = taskListener;
    }

    public List<ExecutionListener> getExecutionListener() {
        return executionListener;
    }

    public void setExecutionListener(List<ExecutionListener> executionListener) {
        this.executionListener = executionListener;
    }

    public List<CallActivityInParam> getCallActivityInParam() {
        return callActivityInParam;
    }

    public void setCallActivityInParam(List<CallActivityInParam> callActivityInParam) {
        this.callActivityInParam = callActivityInParam;
    }

    public List<CallActivityOutParam> getCallActivityOutParam() {
        return callActivityOutParam;
    }

    public void setCallActivityOutParam(List<CallActivityOutParam> callActivityOutParam) {
        this.callActivityOutParam = callActivityOutParam;
    }
}
