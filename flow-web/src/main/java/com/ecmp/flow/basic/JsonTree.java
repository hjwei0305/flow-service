package com.ecmp.flow.basic;

/**
 * *************************************************************************************************
 * <p/>
 * 实现功能：前台树形结构Json数据
 * <p>
 * ------------------------------------------------------------------------------------------------
 * 版本          变更时间             变更人                     变更原因
 * ------------------------------------------------------------------------------------------------
 * 1.0.00      2017/4/25 16:15      秦有宝                     新建
 * <p/>
 * *************************************************************************************************
 */
public class JsonTree {
    private Object[] data;
    private boolean success;

    public JsonTree(Object data, boolean success) {
        this.setData(new Object[]{data});
        this.success = success;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object[] data) {
        this.data = data;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
}
