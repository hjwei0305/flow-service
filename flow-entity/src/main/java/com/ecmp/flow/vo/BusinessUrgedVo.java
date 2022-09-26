package com.ecmp.flow.vo;

import java.io.Serializable;
import java.util.List;

public class BusinessUrgedVo implements Serializable {


    /**
     * 业务单据ID
     */
    private String businessId;


    /**
     * 催办类型集合
     */
    private List<String> urgedTypeList;


    /**
     * 催办内容
     */
    private String urgedInfo;


    public String getBusinessId() {
        return businessId;
    }

    public void setBusinessId(String businessId) {
        this.businessId = businessId;
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
