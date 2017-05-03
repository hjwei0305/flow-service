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


    public static void main(String[] args) throws JAXBException {
        Definition df = new Definition();
        Process process = new Process();
        process.setName("测试");
        process.setId("1122");
        process.setStartUEL("23sfsfsf");
        df.setDefJson("sdfsdfs");
        JSONObject nodes = JSONObject.fromObject("{\"StartEvent_0\":{\"type\":\"StartEvent\",\"x\":256,\"y\":45,\"id\":\"StartEvent_0\",\"target\":[{\"targetId\":\"UserTask_1\",\"uel\":\"sdf\"}],\"name\":\"开始\"},\"UserTask_1\":{\"type\":\"UserTask\",\"x\":402,\"y\":93,\"id\":\"UserTask_1\",\"target\":[{\"targetId\":\"UserTask_2\",\"uel\":\"\"}],\"name\":\"审批任务\"},\"UserTask_2\":{\"type\":\"UserTask\",\"x\":639,\"y\":63,\"id\":\"UserTask_2\",\"target\":[{\"targetId\":\"EndEvent_3\",\"uel\":\"\"}],\"name\":\"审批任务\"},\"EndEvent_3\":{\"type\":\"EndEvent\",\"x\":688,\"y\":243,\"id\":\"EndEvent_3\",\"target\":[],\"name\":\"结束\"}}");
        process.setNodes(nodes);
        df.setProcess(process);
        JAXBContext context = JAXBContext.newInstance(df.getClass());
        Marshaller marshaller = context.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
        StringWriter writer = new StringWriter();
        marshaller.marshal(df, writer);
        System.out.println(writer.toString());
    }
}
