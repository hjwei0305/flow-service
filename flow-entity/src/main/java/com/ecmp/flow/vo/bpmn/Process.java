package com.ecmp.flow.vo.bpmn;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import javax.xml.bind.annotation.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * *************************************************************************************************
 * <p/>
 * 实现功能：流程类
 * <p>
 * ------------------------------------------------------------------------------------------------
 * 版本          变更时间             变更人                     变更原因
 * ------------------------------------------------------------------------------------------------
 * 1.0.00      2017/4/10 9:52      陈飞(fly)                  新建
 * <p/>
 * *************************************************************************************************
 */
@XmlType(name = "process")
@XmlAccessorType(XmlAccessType.FIELD)
public class Process extends BaseNode implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 流程key
     */
    @XmlTransient
    private String key;

    /**
     * 流程名
     */
    @XmlAttribute
    private String name;
    /**
     * 是否可执行
     */
    @XmlAttribute(name = "isExecutable")
    private boolean executable = true;

    /**
     * 启动条件
     */
    @XmlTransient
    private String startUEL;

    @XmlTransient
    private JSONObject nodes;

    private List<StartEvent> startEvent;
    private List<EndEvent> endEvent;
    private List<UserTask> userTask;
    private List<MailTask> mailTask;
    private List<ManualTask> manualTask;
    private List<ScriptTask> scriptTask;
    private List<ServiceTask> serviceTask;
    private List<ExclusiveGateway> exclusiveGateway;
    private List<InclusiveGateway> inclusiveGateway;
    private List<ParallelGateway> parallelGateway;
    private List<EventGateway> eventGateway;
    private List<SequenceFlow> sequenceFlow;

    @XmlTransient
    private int lineCount = 0;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStartUEL() {
        return startUEL;
    }

    public void setStartUEL(String startUEL) {
        this.startUEL = startUEL;
    }

    public JSONObject getNodes() {
        return nodes;
    }

    public boolean isExecutable() {
        return executable;
    }

    public void setExecutable(boolean executable) {
        this.executable = executable;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public void setNodes(JSONObject nodes) {
        this.nodes = nodes;
        if (nodes != null) {
            startEvent = new ArrayList<StartEvent>();
            endEvent = new ArrayList<EndEvent>();
            userTask = new ArrayList<UserTask>();
            mailTask = new ArrayList<MailTask>();
            manualTask = new ArrayList<ManualTask>();
            scriptTask = new ArrayList<ScriptTask>();
            serviceTask = new ArrayList<ServiceTask>();
            exclusiveGateway = new ArrayList<ExclusiveGateway>();
            inclusiveGateway = new ArrayList<InclusiveGateway>();
            parallelGateway = new ArrayList<ParallelGateway>();
            eventGateway = new ArrayList<EventGateway>();
            sequenceFlow = new ArrayList<SequenceFlow>();
            lineCount = 0;
            Iterator iterator = nodes.keys();
            while (iterator.hasNext()) {
                String key = (String) iterator.next();
                JSONObject node = nodes.getJSONObject(key);
                JSONArray targets = node.getJSONArray("target");
                switch (node.getString("type")) {
                    case "StartEvent":
                        startEvent.add((StartEvent) JSONObject.toBean(node, StartEvent.class));
                        break;
                    case "EndEvent":
                        endEvent.add((EndEvent) JSONObject.toBean(node, EndEvent.class));
                        break;
                    case "UserTask":
                        userTask.add((UserTask) JSONObject.toBean(node, UserTask.class));
                        break;
                    case "MailTask":
                        mailTask.add((MailTask) JSONObject.toBean(node, MailTask.class));
                        break;
                    case "ManualTask":
                        manualTask.add((ManualTask) JSONObject.toBean(node, ManualTask.class));
                        break;
                    case "ScriptTask":
                        scriptTask.add((ScriptTask) JSONObject.toBean(node, ScriptTask.class));
                        break;
                    case "ServiceTask":
                        serviceTask.add((ServiceTask) JSONObject.toBean(node, ServiceTask.class));
                        break;
                    case "ExclusiveGateway":
                        exclusiveGateway.add((ExclusiveGateway) JSONObject.toBean(node, ExclusiveGateway.class));
                        break;
                    case "InclusiveGateway":
                        inclusiveGateway.add((InclusiveGateway) JSONObject.toBean(node, InclusiveGateway.class));
                        break;
                    case "ParallelGateway":
                        parallelGateway.add((ParallelGateway) JSONObject.toBean(node, ParallelGateway.class));
                        break;
                    case "EventGateway":
                        eventGateway.add((EventGateway) JSONObject.toBean(node, EventGateway.class));
                        break;
                    default:
                        break;
                }
                addSequenceFlow(key, targets);
            }
        }
    }

    private void addSequenceFlow(String sourceId, JSONArray targets) {
        for (int i = 0; i < targets.size(); i++) {
            JSONObject target = targets.getJSONObject(i);
            String id = "flow" + (++lineCount);
            String targetId = target.getString("targetId");
            String uel = target.getString("uel");
            SequenceFlow sf = new SequenceFlow(id, sourceId, targetId, uel);
            sequenceFlow.add(sf);
        }
    }
}
