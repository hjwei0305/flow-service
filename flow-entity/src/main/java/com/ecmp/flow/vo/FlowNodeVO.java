package com.ecmp.flow.vo;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import java.io.Serializable;

public class FlowNodeVO implements Serializable {


    private String id;
    /**
     * 节点类型
     */
    private String type;
    /**
     * 节点x坐标
     */
    private int x;
    /**
     * 节点y坐标
     */
    private int y;

    /**
     * 任务类型
     */
    private String nodeType;

    private JSONArray target;

    /**
     * 节点名
     */
    private String name;

    private JSONObject nodeConfig;
    /**
     * 网关类型
     */
    private String busType;

    /**
     * 所处分支位置信息
     */
    private String flowNewPosition;

    public FlowNodeVO() {
    }

    public FlowNodeVO(String id, String name, String nodeType) {
        this.id = id;
        this.name = name;
        this.nodeType = nodeType;
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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

    public String getNodeType() {
        return nodeType;
    }

    public void setNodeType(String nodeType) {
        this.nodeType = nodeType;
    }

    public JSONArray getTarget() {
        return target;
    }

    public void setTarget(JSONArray target) {
        this.target = target;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public String getFlowNewPosition() {
        return flowNewPosition;
    }

    public void setFlowNewPosition(String flowNewPosition) {
        this.flowNewPosition = flowNewPosition;
    }
}
