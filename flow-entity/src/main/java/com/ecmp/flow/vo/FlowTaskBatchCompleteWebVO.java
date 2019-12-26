package com.ecmp.flow.vo;

import java.io.Serializable;
import java.util.List;

/**
 * *************************************************************************************************
 * <p/>
 * 实现功能：执行任务完成时的传输对象-web端
 * <p>
 * ------------------------------------------------------------------------------------------------
 * 版本          变更时间             变更人                     变更原因
 * ------------------------------------------------------------------------------------------------
 * 1.0.00      2017/5/22 17:33      谭军(tanjun)                    新建
 * <p/>
 * *************************************************************************************************
 */
public class FlowTaskBatchCompleteWebVO implements Serializable{

    /**
     * 任务id
     */
    private List<String> taskIdList;

    private List<FlowTaskCompleteWebVO> flowTaskCompleteList;

    private Boolean  solidifyFlow;

    public List<String> getTaskIdList() {
        return taskIdList;
    }

    public void setTaskIdList(List<String> taskIdList) {
        this.taskIdList = taskIdList;
    }

    public List<FlowTaskCompleteWebVO> getFlowTaskCompleteList() {
        return flowTaskCompleteList;
    }

    public void setFlowTaskCompleteList(List<FlowTaskCompleteWebVO> flowTaskCompleteList) {
        this.flowTaskCompleteList = flowTaskCompleteList;
    }

    public Boolean getSolidifyFlow() {
        return solidifyFlow;
    }

    public void setSolidifyFlow(Boolean solidifyFlow) {
        this.solidifyFlow = solidifyFlow;
    }
}
