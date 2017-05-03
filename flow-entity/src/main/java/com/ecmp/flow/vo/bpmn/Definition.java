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
        JSONObject nodes = JSONObject.fromObject("{\"StartEvent_0\":{\"type\":\"StartEvent\",\"x\":28,\"y\":85,\"id\":\"StartEvent_0\",\"target\":[\"UserTask_1\"],\"name\":\"开始\"},\"UserTask_1\":{\"type\":\"UserTask\",\"x\":130,\"y\":204,\"id\":\"UserTask_1\",\"target\":[\"UserTask_2\",\"ExclusiveGateway_5\"],\"name\":\"审批任务\"},\"EndEvent_3\":{\"type\":\"EndEvent\",\"x\":782,\"y\":451,\"id\":\"EndEvent_3\",\"target\":[],\"name\":\"结束\"},\"UserTask_4\":{\"type\":\"UserTask\",\"x\":517,\"y\":151,\"id\":\"UserTask_4\",\"target\":[\"UserTask_10\"],\"name\":\"审批任务\"},\"ExclusiveGateway_5\":{\"type\":\"ExclusiveGateway\",\"x\":362,\"y\":268,\"id\":\"ExclusiveGateway_5\",\"target\":[\"UserTask_4\",\"UserTask_6\",\"UserTask_8\"],\"name\":\"排他网关\"},\"UserTask_6\":{\"type\":\"UserTask\",\"x\":520,\"y\":287,\"id\":\"UserTask_6\",\"target\":[\"UserTask_10\"],\"name\":\"审批任务\"},\"UserTask_8\":{\"type\":\"UserTask\",\"x\":518,\"y\":397,\"id\":\"UserTask_8\",\"target\":[\"EndEvent_3\"],\"name\":\"审批任务\"},\"UserTask_10\":{\"type\":\"UserTask\",\"x\":756,\"y\":215,\"id\":\"UserTask_10\",\"target\":[\"EndEvent_3\"],\"name\":\"审批任务\"}}");
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
