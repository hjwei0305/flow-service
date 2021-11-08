package com.ecmp.flow.vo;

import java.io.Serializable;
import java.util.List;

public class ReplaceSolidifyExecutorVo implements Serializable {

    /**
     * 需要被替换的用户ID
     */
    private String oldUserId;

    /**
     * 替换人的ID
     */
    private String newUserId;

    /**
     *  关联的业务ID集合
     */
    private List<String> businessIdList;


    public String getOldUserId() {
        return oldUserId;
    }

    public void setOldUserId(String oldUserId) {
        this.oldUserId = oldUserId;
    }

    public String getNewUserId() {
        return newUserId;
    }

    public void setNewUserId(String newUserId) {
        this.newUserId = newUserId;
    }

    public List<String> getBusinessIdList() {
        return businessIdList;
    }

    public void setBusinessIdList(List<String> businessIdList) {
        this.businessIdList = businessIdList;
    }
}
