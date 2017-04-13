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

    public Process getProcess() {
        return process;
    }

    public void setProcess(Process process) {
        this.process = process;
    }

    public static void main(String[] args) throws JAXBException {
        Definition df = new Definition();
        Process process = new Process();
        process.setName("测试");
        process.setId("1122");
        process.setStartUEL("23sfsfsf");
        JSONObject nodes = JSONObject.fromObject("{\"StartEvent_0\":{\"type\":\"StartEvent\",\"x\":67,\"y\":156,\"id\":\"StartEvent_0\",\"target\":[\"UserTask_1\"],\"name\":\"开始\"},\"UserTask_1\":{\"type\":\"UserTask\",\"x\":246,\"y\":259,\"id\":\"UserTask_1\",\"target\":[\"ExclusiveGateway_2\"],\"name\":\"审批任务\"},\"ExclusiveGateway_2\":{\"type\":\"ExclusiveGateway\",\"x\":529,\"y\":238,\"id\":\"ExclusiveGateway_2\",\"target\":[\"UserTask_3\",\"UserTask_4\"],\"name\":\"排他网关\"},\"UserTask_3\":{\"type\":\"UserTask\",\"x\":716,\"y\":120,\"id\":\"UserTask_3\",\"target\":[\"EndEvent_5\"],\"name\":\"审批任务\"},\"UserTask_4\":{\"type\":\"UserTask\",\"x\":674,\"y\":386,\"id\":\"UserTask_4\",\"target\":[\"EndEvent_5\"],\"name\":\"审批任务\"},\"EndEvent_5\":{\"type\":\"EndEvent\",\"x\":965,\"y\":223,\"id\":\"EndEvent_5\",\"target\":[],\"name\":\"结束\"}}");
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
