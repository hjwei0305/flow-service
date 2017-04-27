package com.ecmp.flow.vo.bpmn;

import javax.xml.bind.annotation.*;
import java.io.Serializable;

/**
 * *************************************************************************************************
 * <p/>
 * 实现功能：bmpn节点基类
 * <p>
 * ------------------------------------------------------------------------------------------------
 * 版本          变更时间             变更人                     变更原因
 * ------------------------------------------------------------------------------------------------
 * 1.0.00      2017/4/7 16:38      陈飞(fly)                  新建
 * 1.0.01      2017/4/18 14:38      谭军(tanjun)               增加ID
 * <p/>
 * *************************************************************************************************
 */
@XmlRootElement(name = "definitions")
@XmlAccessorType(XmlAccessType.FIELD)
public class Definition implements Serializable {
    private static final long serialVersionUID = 1L;

    @XmlAttribute
    private String xmlns = "http://www.omg.org/spec/BPMN/20100524/MODEL";

    @XmlAttribute
    private String typeLanguage = "http://www.w3.org/2001/XMLSchema";

    @XmlAttribute
    private String expressionLanguage = "http://www.w3.org/1999/XPath";

    @XmlAttribute
    private String targetNamespace = "bpmn";

    @XmlElement
    private Process process;

    /**
     * 前端设计json文本
     */
    @XmlTransient
    private String defJson;

    /**
     * 流程类型
     */
    @XmlTransient
    private String flowTypeId;

    /**
     * 流程定义ID
     */
    @XmlTransient
    private String id;


    public Process getProcess() {
        return process;
    }

    public void setProcess(Process process) {
        this.process = process;
    }

    public String getDefJson() {
        return defJson;
    }

    public void setDefJson(String defJson) {
        this.defJson = defJson;
    }

    public String getFlowTypeId() {
        return flowTypeId;
    }

    public void setFlowTypeId(String flowTypeId) {
        this.flowTypeId = flowTypeId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

}
