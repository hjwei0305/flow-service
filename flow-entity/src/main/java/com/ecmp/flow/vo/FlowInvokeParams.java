package com.ecmp.flow.vo;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * *************************************************************************************************
 * <p/>
 * 实现功能：服务、事件输入参数VO
 * <p>
 * ------------------------------------------------------------------------------------------------
 * 版本          变更时间             变更人                     变更原因
 * ------------------------------------------------------------------------------------------------
 * 1.0.00      2017/11/9 17:25      谭军(tanjun)                    新建
 * <p/>
 * *************************************************************************************************
 */
public class FlowInvokeParams implements Serializable{
    private String id;//业务单据id
    private Map<String,String> params;//其他参数
    private Boolean isAgree;  //是否同意
    private Boolean isFinalAgree;//是否最终同意，针对会签
    private String taskActDefId;//针对接收任务\用户池任务等，传递任务key
    private Boolean reject=false;//是否是被驳回的任务,用于业务接口判断任务驳回时是否需要调用业务处理逻辑
    private List<String> callActivitySonPaths;//调用子流程路径

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Map<String, String> getParams() {
        return params;
    }

    public void setParams(Map<String, String> params) {
        this.params = params;
    }

    public Boolean getAgree() {
        return isAgree;
    }

    public void setAgree(Boolean agree) {
        isAgree = agree;
    }

    public Boolean getFinalAgree() {
        return isFinalAgree;
    }

    public void setFinalAgree(Boolean finalAgree) {
        isFinalAgree = finalAgree;
    }

    public String getTaskActDefId() {
        return taskActDefId;
    }

    public void setTaskActDefId(String taskActDefId) {
        this.taskActDefId = taskActDefId;
    }

    public Boolean getReject() {
        return reject;
    }

    public void setReject(Boolean reject) {
        this.reject = reject;
    }

    public List<String> getCallActivitySonPaths() {
        return callActivitySonPaths;
    }

    public void setCallActivitySonPaths(List<String> callActivitySonPaths) {
        this.callActivitySonPaths = callActivitySonPaths;
    }
}
