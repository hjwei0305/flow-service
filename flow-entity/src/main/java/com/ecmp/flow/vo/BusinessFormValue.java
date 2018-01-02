package com.ecmp.flow.vo;

import java.io.Serializable;

/**
 * *************************************************************************************************
 * <p/>
 * 实现功能： 业务表单值
 * <p>
 * ------------------------------------------------------------------------------------------------
 * 版本          变更时间             变更人                     变更原因
 * ------------------------------------------------------------------------------------------------
 * 1.0.00      2018/1/2 15:51      谭军(tanjun)                    新建
 * <p/>
 * *************************************************************************************************
 */
public class BusinessFormValue implements Serializable {
    /**
     * 值
     */
    private Object value;
    /**
     * 排序，DESC
     */
    private int rank = 0;
    /**
     * 是否有关联对象
     */
    private boolean hasSon = false;

    public BusinessFormValue(){}

    public BusinessFormValue(Object value){
        this.value = value;
    }

    public BusinessFormValue( Object value,int rank){
        this.value = value;
        this.rank = rank;
    }

    public BusinessFormValue( Object value,int rank,boolean hasSon){
        this.value = value;
        this.rank = rank;
        this.hasSon = hasSon;
    }



    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public boolean isHasSon() {
        return hasSon;
    }

    public void setHasSon(boolean hasSon) {
        this.hasSon = hasSon;
    }
}
