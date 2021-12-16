package com.ecmp.flow.dto;

import java.io.Serializable;

public class RejectParam implements Serializable {

    /**
     *  任务ID
     */
    private String id;

    /**
     *  驳回意见
     */
    private String opinion;


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
}
