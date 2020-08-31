package com.ecmp.flow.entity;


import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;


/**
 * *************************************************************************************************
 * <p/>
 * 实现功能：常用联系组
 * <p>
 * ------------------------------------------------------------------------------------------------
 * 版本          变更时间             变更人                     变更原因
 * ------------------------------------------------------------------------------------------------
 * 1.0.00      2020/08/31          何灿坤(AK)                  新建
 * <p/>
 * *************************************************************************************************
 */
@Entity
@Table(name = "common_contact_group")
public class CommonContactGroup  extends com.ecmp.core.entity.BaseAuditableEntity{

    /**
     * 常用联系组名称
     */
    @Column(name = "name")
    private String name;


    /**
     * 排序
     */
    @Column(name = "rank")
    private Integer rank = 0;



    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getRank() {
        return rank;
    }

    public void setRank(Integer rank) {
        this.rank = rank;
    }

}
