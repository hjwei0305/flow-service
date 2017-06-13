package com.ecmp.flow.vo.bpmn;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.*;
import java.io.Serializable;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

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
@XmlAccessorType(XmlAccessType.FIELD)
public class BaseFlowNode extends BaseNode implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 节点名
     */
    @XmlAttribute
    private String name;
    /**
     * 节点类型
     */
    @XmlTransient
    private String type;
    /**
     * 扩展节点类型
     */
    @XmlTransient
    private String busType;
    /**
     * 节点x坐标
     */
    @XmlTransient
    private int x;
    /**
     * 节点y坐标
     */
    @XmlTransient
    private int y;
    /**
     * 节点连接目标节点集合
     */
    @XmlTransient
    private List<BaseFlowNode> targetNodes;

    @XmlTransient
    private JSONArray target;

    @XmlTransient
    private JSONObject nodeConfig;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public List<BaseFlowNode> getTargetNodes() {
        return targetNodes;
    }

    public void setTargetNodes(List<BaseFlowNode> targetNodes) {
        this.targetNodes = targetNodes;
    }

    public JSONArray getTarget() {
        return target;
    }

    public void setTarget(JSONArray target) {
        this.target = target;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public JSONObject getNodeConfig() {
        return nodeConfig;
    }

    public void setNodeConfig(JSONObject nodeConfig) {
        this.nodeConfig = nodeConfig;
    }

    public String getBusType() {
        return busType;
    }

    public void setBusType(String busType) {
        this.busType = busType;
    }

    public static void main(String[] args) {
        String result = null;
        BaseFlowNode obj = new BaseFlowNode();
        obj.setId("sdfsdd");
        obj.setName("审批任务");
        List<BaseFlowNode> test = new ArrayList<BaseFlowNode>();
        BaseFlowNode obj2 = new BaseFlowNode();
        obj2.setId("sdfsdd");
        obj2.setName("审批任务");

        test.add(obj2);
        obj.setTargetNodes(test);

        try {
            JAXBContext context = JAXBContext.newInstance(obj.getClass());
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");

            StringWriter writer = new StringWriter();
            marshaller.marshal(obj, writer);
            result = writer.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println(result);

    }


}
