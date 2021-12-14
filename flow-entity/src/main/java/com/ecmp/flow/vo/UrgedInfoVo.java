package com.ecmp.flow.vo;

import java.io.Serializable;
import java.util.List;

public class UrgedInfoVo implements Serializable {


    /**
     * 流程实例ID
     */
    private String flowInstanceId;


    /**
     * 催办类型集合
     */
    private List<String> urgedTypeList;


    /**
     * 催办内容
     */
    private String urgedInfo;


    public String getFlowInstanceId() {
        return flowInstanceId;
    }

    public void setFlowInstanceId(String flowInstanceId) {
        this.flowInstanceId = flowInstanceId;
    }

    public List<String> getUrgedTypeList() {
        return urgedTypeList;
    }

    public void setUrgedTypeList(List<String> urgedTypeList) {
        this.urgedTypeList = urgedTypeList;
    }

    public String getUrgedInfo() {
        return urgedInfo;
    }

    public void setUrgedInfo(String urgedInfo) {
        this.urgedInfo = urgedInfo;
    }
}
