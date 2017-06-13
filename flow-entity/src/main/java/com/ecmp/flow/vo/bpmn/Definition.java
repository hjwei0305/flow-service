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

//    @XmlAttribute
//    private String xs = "http://www.w3.org/2001/XMLSchema";

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

    /**
     * 优先级
     */
    @XmlTransient
    private Integer priority;


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
        Process process = new Process();
        process.setName("测试");
        process.setId("1122");
        process.setStartUEL("23sfsfsf");
        df.setDefJson("sdfsdfs");
        JSONObject nodes = JSONObject.fromObject("{\"StartEvent_0\":{\"type\":\"StartEvent\",\"x\":188,\"y\":136,\"id\":\"StartEvent_0\",\"target\":[{\"targetId\":\"UserTask_2\",\"uel\":\"\"}],\"name\":\"开始\",\"nodeConfig\":{}},\"EndEvent_1\":{\"type\":\"EndEvent\",\"x\":704,\"y\":232,\"id\":\"EndEvent_1\",\"target\":[],\"name\":\"结束\",\"nodeConfig\":{}},\"UserTask_2\":{\"type\":\"UserTask\",\"x\":438,\"y\":185,\"id\":\"UserTask_2\",\"target\":[{\"targetId\":\"EndEvent_1\",\"uel\":\"\"}],\"name\":\"普通任务\",\"nodeConfig\":{\"normal\":{\"name\":\"普通任务\",\"executeTime\":\"44\",\"workPageName\":\"默认审批页面\",\"workPageUrl\":\"http://localhost:8081/lookApproveBill/show\",\"allowTerminate\":true,\"allowPreUndo\":true,\"allowReject\":true},\"executor\":{\"userType\":\"AnyOne\"},\"event\":{\"beforeExcuteService\":\"\",\"afterExcuteService\":\"\",\"afterExcuteServiceId\":\"\"},\"notify\":null}}}");
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
