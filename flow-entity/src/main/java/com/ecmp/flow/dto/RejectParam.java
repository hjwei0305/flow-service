package com.ecmp.flow.dto;

import java.io.Serializable;

public class RejectParam implements Serializable {

    /**
     *  任务ID
     */
    private String id;

    /**
     * 驳回意见3.0
     */
    private String opinion;

    /**
     * 驳回意见6.0
     */
    private String remark;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOpinion() {
        return opinion;
    }

    public void setOpinion(String opinion) {
        this.opinion = opinion;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
