package com.ecmp.flow.vo;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * *************************************************************************************************
 * <p/>
 * 实现功能： 任务启动时的传输对象
 * <p>
 * ------------------------------------------------------------------------------------------------
 * 版本          变更时间             变更人                     变更原因
 * ------------------------------------------------------------------------------------------------
 * 1.0.00      2017/5/22 17:33      谭军(tanjun)                    新建
 * <p/>
 * *************************************************************************************************
 */
public class FlowStartVO implements Serializable{

    /**
     * 业务实体代码
     */
    private String businessModelCode;

    /**
     * 启动用户id
     */
    private String startUserId;

    /**
     * 业务id
     */
    private  String businessKey;

    /**
     * 选择的流程类型id
     */
    private String flowTypeId;

    /**
     * 手动选择出口分支节点的节点ID
     */
    private List<String> manualSelectedNodeIds ;

    /**
     * 启动说明
     */
    private String opinion;

    /**
     * 额外参数
     */
    private Map<String, Object> variables;


     /**
     * 启动时，下一步的用户
     */
    private Map<String, Object> userMap;



    public String getBusinessModelCode() {
        return businessModelCode;
    }

    public void setBusinessModelCode(String businessModelCode) {
        this.businessModelCode = businessModelCode;
    }

    public String getStartUserId() {
        return startUserId;
    }

    public void setStartUserId(String startUserId) {
        this.startUserId = startUserId;
    }

    public String getBusinessKey() {
        return businessKey;
    }

    public void setBusinessKey(String businessKey) {
        this.businessKey = businessKey;
    }

    public Map<String, Object> getVariables() {
        return variables;
    }

    public void setVariables(Map<String, Object> variables) {
        this.variables = variables;
    }

    public String getFlowTypeId() {
        return flowTypeId;
    }

    public void setFlowTypeId(String flowTypeId) {
        this.flowTypeId = flowTypeId;
    }

    public List<String> getManualSelectedNodeIds() {
        return manualSelectedNodeIds;
    }

    public void setManualSelectedNodeIds(List<String> manualSelectedNodeIds) {
        this.manualSelectedNodeIds = manualSelectedNodeIds;
    }

    public String getOpinion() {
        return opinion;
    }

    public void setOpinion(String opinion) {
        this.opinion = opinion;
    }

    public Map<String, Object> getUserMap() {
        return userMap;
    }

    public void setUserMap(Map<String, Object> userMap) {
        this.userMap = userMap;
    }
}
