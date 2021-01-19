package com.ecmp.flow.constant;


import java.io.Serializable;

/**
 * 预警状态枚举类
 */
public enum EarlyWarningStatus implements Serializable {

    NORMAL("normal", "正常"),

    WARNING("warning", "预警"),

    TIMEOUT("timeout", "超时");

    private String code;

    private String name;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    EarlyWarningStatus(String code, String name) {
        this.code = code;
        this.name = name;
    }

}
