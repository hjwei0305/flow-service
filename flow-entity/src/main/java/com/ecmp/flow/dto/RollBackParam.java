package com.ecmp.flow.dto;

import java.io.Serializable;


/**
 * 实现功能: 撤回接口参数
 *
 * @author 何灿坤  AK
 * @version 2020-06-11 09:17
 */
public class RollBackParam implements Serializable {

    /**
     *  撤回指定节点的历史ID
     */
   private String id;

    /**
     *  撤回意见
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
