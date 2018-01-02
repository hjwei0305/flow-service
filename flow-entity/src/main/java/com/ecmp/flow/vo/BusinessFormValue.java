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
     * 是否有关联对象
     */
    private boolean hasSon = false;

    public BusinessFormValue(){}

    public BusinessFormValue(Object value){
        this.value = value;
    }

    public BusinessFormValue( Object value,boolean hasSon){
        this.value = value;
        this.hasSon = hasSon;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public boolean isHasSon() {
        return hasSon;
    }

    public void setHasSon(boolean hasSon) {
        this.hasSon = hasSon;
    }
}
