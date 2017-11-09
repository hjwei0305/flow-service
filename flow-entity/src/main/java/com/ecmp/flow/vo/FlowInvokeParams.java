package com.ecmp.flow.vo;

import java.io.Serializable;
import java.util.Map;

/**
 * *************************************************************************************************
 * <p/>
 * 实现功能：事件输入参数VO
 * <p>
 * ------------------------------------------------------------------------------------------------
 * 版本          变更时间             变更人                     变更原因
 * ------------------------------------------------------------------------------------------------
 * 1.0.00      2017/11/9 17:25      谭军(tanjun)                    新建
 * <p/>
 * *************************************************************************************************
 */
public class FlowInvokeParams implements Serializable{
    private String id;
    private Map<String,String> params;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Map<String, String> getParams() {
        return params;
    }

    public void setParams(Map<String, String> params) {
        this.params = params;
    }
}
