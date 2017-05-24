package com.ecmp.flow.vo.bpmn;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
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
 * <p/>
 * *************************************************************************************************
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "extensionElements")
public class ExtensionElement {
    private List<TaskListener> taskListener;
    private List<ExecutionListener> executionListener;

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
}
