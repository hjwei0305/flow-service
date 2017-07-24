package com.ecmp.flow.vo;

import com.ecmp.flow.entity.FlowInstance;
import com.ecmp.flow.entity.FlowType;

import javax.xml.soap.Node;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * *************************************************************************************************
 * <p/>
 * 实现功能： 任务启动时的引擎返回的对象
 * <p>
 * ------------------------------------------------------------------------------------------------
 * 版本          变更时间             变更人                     变更原因
 * ------------------------------------------------------------------------------------------------
 * 1.0.00      2017/6/12 17:33      谭军(tanjun)                    新建
 * <p/>
 * *************************************************************************************************
 */
public class FlowStartResultVO implements Serializable{

    private com.ecmp.flow.entity.FlowInstance flowInstance;//流程启动实例

    private com.ecmp.flow.entity.FlowDefination flowDefination;//选择的流程类型

    private List<FlowType> flowTypeList;//流程类型选择（一个流程实体存在多个流程类型的情况下）
    private List<NodeInfo> nodeInfoList;//启动时节点信息

    /**
     * 额外参数
     */
    private Map<String, Object> variables;


    public FlowInstance getFlowInstance() {
        return flowInstance;
    }

    public void setFlowInstance(FlowInstance flowInstance) {
        this.flowInstance = flowInstance;
    }

    public List<FlowType> getFlowTypeList() {
        return flowTypeList;
    }

    public void setFlowTypeList(List<FlowType> flowTypeList) {
        this.flowTypeList = flowTypeList;
    }

    public List<NodeInfo> getNodeInfoList() {
        return nodeInfoList;
    }

    public void setNodeInfoList(List<NodeInfo> nodeInfoList) {
        this.nodeInfoList = nodeInfoList;
    }

    public Map<String, Object> getVariables() {
        return variables;
    }

    public void setVariables(Map<String, Object> variables) {
        this.variables = variables;
    }
}
