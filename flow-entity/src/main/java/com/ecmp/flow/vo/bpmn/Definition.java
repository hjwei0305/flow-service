package com.ecmp.flow.vo.bpmn;

import net.sf.json.JSONObject;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.*;
import java.io.Serializable;
import java.io.StringWriter;

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

    @XmlAttribute(name = "xmlns:xsd")
    private String xsd = "http://www.w3.org/2001/XMLSchema";

    @XmlAttribute
    private String expressionLanguage = "http://www.w3.org/1999/XPath";

    @XmlAttribute
    private String typeLanguage = "http://www.w3.org/2001/XMLSchema";

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
     * 流程类型Id
     */
    @XmlTransient
    private String flowTypeId;
    /**
     * 流程类型Name
     */
    @XmlTransient
    private String flowTypeName;

    /**
     * 组织机构ID
     */
    @XmlTransient
    private String orgId;

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

    public String getOrgId() {
        return orgId;
    }

    public void setOrgId(String orgId) {
        this.orgId = orgId;
    }

    public String getFlowTypeName() {
        return flowTypeName;
    }

    public void setFlowTypeName(String flowTypeName) {
        this.flowTypeName = flowTypeName;
    }

    public static void main(String[] args) throws JAXBException {
        Definition df = new Definition();
        JSONObject json = JSONObject.fromObject("{\"flowTypeId\":\"C35B0B09-3640-11E7-9617-3C970EA9E0F7\",\"orgId\":\"c0a80171-5bcd-1066-815b-cd83f5a20002\",\"id\"\n" +
                ":null,\"process\":{\"name\":\"test0607\",\"id\":\"test0607\",\"isExecutable\":true,\"nodes\":{\"StartEvent_0\":{\"type\"\n" +
                ":\"StartEvent\",\"x\":71,\"y\":107,\"id\":\"StartEvent_0\",\"target\":[{\"targetId\":\"UserTask_1\",\"uel\":\"\"}],\"name\"\n" +
                ":\"开始\",\"nodeConfig\":{}},\"UserTask_1\":{\"type\":\"UserTask\",\"x\":189,\"y\":104,\"id\":\"UserTask_1\",\"nodeType\":\"Normal\"\n" +
                ",\"target\":[{\"targetId\":\"EndEvent_5\",\"uel\":\"\"}],\"name\":\"普通任务\",\"nodeConfig\":{\"normal\":{\"name\":\"普通任务\",\"executeTime\"\n" +
                ":\"\",\"workPageName\":\"默认审批页面\",\"workPageUrl\":\"http://localhost:8081/lookApproveBill/show\",\"allowTerminate\"\n" +
                ":false,\"allowPreUndo\":false,\"allowReject\":false},\"executor\":{\"userType\":\"StartUser\"},\"event\":{\"beforeExcuteService\"\n" +
                ":\"\",\"beforeExcuteServiceId\":\"\",\"afterExcuteService\":\"\",\"afterExcuteServiceId\":\"\"},\"notify\":\"\"}},\"EndEvent_5\"\n" +
                ":{\"type\":\"EndEvent\",\"x\":780,\"y\":94,\"id\":\"EndEvent_5\",\"target\":[],\"name\":\"结束\",\"nodeConfig\":{}}}}}\n");
        df = (Definition) JSONObject.toBean(json, Definition.class);
        JAXBContext context = JAXBContext.newInstance(df.getClass());
        Marshaller marshaller = context.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
        StringWriter writer = new StringWriter();
        marshaller.marshal(df, writer);
        System.out.println(writer.toString());
    }
}
