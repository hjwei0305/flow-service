package com.ecmp.flow.vo;

import com.ecmp.flow.entity.FlowHistory;
import com.ecmp.flow.entity.FlowInstance;
import com.ecmp.flow.entity.FlowTask;

import java.io.Serializable;
import java.util.List;

/**
 * *************************************************************************************************
 * <p/>
 * 实现功能：流程跟踪VO对象
 * <p>
 * ------------------------------------------------------------------------------------------------
 * 版本          变更时间             变更人                     变更原因
 * ------------------------------------------------------------------------------------------------
 * 1.0.00      2017/6/16 11:00      谭军(tanjun)                    新建
 * <p/>
 * *************************************************************************************************
 */
public class ProcessTrackVO implements Serializable {
   private FlowInstance flowInstance;
   private List<FlowHistory>  flowHistoryList;
   private List<FlowTask> flowTaskList;

    public FlowInstance getFlowInstance() {
        return flowInstance;
    }

    public void setFlowInstance(FlowInstance flowInstance) {
        this.flowInstance = flowInstance;
    }

    public List<FlowHistory> getFlowHistoryList() {
        return flowHistoryList;
    }

    public void setFlowHistoryList(List<FlowHistory> flowHistoryList) {
        this.flowHistoryList = flowHistoryList;
    }

    public List<FlowTask> getFlowTaskList() {
        return flowTaskList;
    }

    public void setFlowTaskList(List<FlowTask> flowTaskList) {
        this.flowTaskList = flowTaskList;
    }
}
