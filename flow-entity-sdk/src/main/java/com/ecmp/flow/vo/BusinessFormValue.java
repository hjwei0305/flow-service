package com.ecmp.flow.vo;

import java.io.Serializable;
import java.util.Map;

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

    private Map<String, Object> son;


    public BusinessFormValue(){}

    public BusinessFormValue(Object value){
        this.value = value;
    }

    public BusinessFormValue(Object value, Map<String, Object> son){
        this.value = value;
        this.son = son;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public boolean isHasSon() {
        return this.son == null || this.son.isEmpty() ? false:true;
    }

    public Map<String, Object> getSon() {
        return son;
    }

    public void setSon(Map<String, Object> son) {
        this.son = son;
    }
}
