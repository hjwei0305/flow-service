package com.ecmp.flow.vo;

import com.ecmp.flow.entity.FlowTask;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * *************************************************************************************************
 * <p/>
 * 实现功能：
 * <p>
 * ------------------------------------------------------------------------------------------------
 * 版本          变更时间             变更人                     变更原因
 * ------------------------------------------------------------------------------------------------
 * 1.0.00      2017/10/26 18:46      谭军(tanjun)                    新建
 * <p/>
 * *************************************************************************************************
 */
public class BatchApprovalFlowTaskGroupVO implements Serializable{
     private String key;//流程定义key+版本号+节点KEY
     Map<FlowTask,List<NodeInfo>> flowTaskNextNodesInfo = new HashMap<FlowTask,List<NodeInfo>>();//任务id,下一步的节点信息
     private String taskName;//任务名称
     private String taskKey;//任务key
     private String definationKey;//流程定义Key


    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }


    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public String getTaskKey() {
        return taskKey;
    }

    public void setTaskKey(String taskKey) {
        this.taskKey = taskKey;
    }

    public String getDefinationKey() {
        return definationKey;
    }

    public void setDefinationKey(String definationKey) {
        this.definationKey = definationKey;
    }


    @Override
    public boolean equals(Object obj) {
        if (null == obj) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        if (!getClass().equals(obj.getClass())) {
            return false;
        }
        BatchApprovalFlowTaskGroupVO that = (BatchApprovalFlowTaskGroupVO) obj;
        return null != this.getKey() && this.getKey().equals(that.getKey());
    }

    @Override
    public int hashCode() {
        int hashCode = 17;
        hashCode += ((null == getKey()) ? 0 : getKey().hashCode()) * 31;
        return hashCode;
    }

    public Map<FlowTask, List<NodeInfo>> getFlowTaskNextNodesInfo() {
        return flowTaskNextNodesInfo;
    }

    public void setFlowTaskNextNodesInfo(Map<FlowTask, List<NodeInfo>> flowTaskNextNodesInfo) {
        this.flowTaskNextNodesInfo = flowTaskNextNodesInfo;
    }
}
