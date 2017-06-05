package com.ecmp.flow.vo;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * *************************************************************************************************
 * <br>
 * 实现功能：
 * <br>
 * ------------------------------------------------------------------------------------------------
 * <br>
 * 版本          变更时间             变更人                     变更原因
 * <br>
 * ------------------------------------------------------------------------------------------------
 * <br>
 * 1.0.00      2017/5/27 9:55      詹耀(xxxlimit)                    新建
 * <br>
 * *************************************************************************************************<br>
 */
public class FlowHistoryInfoVO implements Serializable {

    /**
     * 流程当前处理状态
     */
    private Set<FlowHandleStatusVO> flowHandleStatusVOList;

    /**
     * 流程启动人
     */
    private String flowStarter;

    /**
     * 流程启动时间
     */
    private Date flowStartTime;

    /**
     * 流程处理历史
     */
    private Set<FlowHandleHistoryVO> flowHandleHistoryVOList;

    public Set<FlowHandleStatusVO> getFlowHandleStatusVOList() {
        return flowHandleStatusVOList;
    }

    public void setFlowHandleStatusVOList(Set<FlowHandleStatusVO> flowHandleStatusVOList) {
        this.flowHandleStatusVOList = flowHandleStatusVOList;
    }

    public String getFlowStarter() {
        return flowStarter;
    }

    public void setFlowStarter(String flowStarter) {
        this.flowStarter = flowStarter;
    }

    public Date getFlowStartTime() {
        return flowStartTime;
    }

    public void setFlowStartTime(Date flowStartTime) {
        this.flowStartTime = flowStartTime;
    }

    public Set<FlowHandleHistoryVO> getFlowHandleHistoryVOList() {
        return flowHandleHistoryVOList;
    }

    public void setFlowHandleHistoryVOList(Set<FlowHandleHistoryVO> flowHandleHistoryVOList) {
        this.flowHandleHistoryVOList = flowHandleHistoryVOList;
    }
}


