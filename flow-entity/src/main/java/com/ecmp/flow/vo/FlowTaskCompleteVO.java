package com.ecmp.flow.vo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * *************************************************************************************************
 * <p/>
 * 实现功能：执行任务完成时的传输对象
 * <p>
 * ------------------------------------------------------------------------------------------------
 * 版本          变更时间             变更人                     变更原因
 * ------------------------------------------------------------------------------------------------
 * 1.0.00      2017/5/22 17:33      谭军(tanjun)                    新建
 * <p/>
 * *************************************************************************************************
 */
public class FlowTaskCompleteVO implements Serializable{

    /**
     * 任务id
     */
    private String taskId;
    /**
     * 手动选择出口分支节点的节点ID
     */
    private List<String> manualSelectedNodeIds ;

    /**
     * 审批意见
     */
    private String opinion;
    /**
     * 额外参数
     */
    Map<String, Object> variables;



    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public List<String> getManualSelectedNodeIds() {
        return manualSelectedNodeIds;
    }

    public void setManualSelectedNodeIds(List<String> manualSelectedNodeIds) {
        this.manualSelectedNodeIds = manualSelectedNodeIds;
    }
    public Map<String, Object> getVariables() {
        return variables;
    }

    public void setVariables(Map<String, Object> variables) {
        this.variables = variables;
    }

    public String getOpinion() {
        return opinion;
    }

    public void setOpinion(String opinion) {
        this.opinion = opinion;
    }
}
