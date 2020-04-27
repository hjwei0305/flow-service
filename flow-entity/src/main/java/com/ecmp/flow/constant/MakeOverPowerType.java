package com.ecmp.flow.constant;

import java.io.Serializable;

public enum MakeOverPowerType implements Serializable {

    // 协办：授权期间你和被授权人都可以看到待办，都可以处理。
    SAMETOSEE("sameToSee", "协办"),


    // 转办：你授权期间看不到到自己的待办，流程执行人是被授权人，待办由被授权人处理。
    TURNTODO("turnToDo", "转办");

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


    MakeOverPowerType(String code, String name) {
        this.code = code;
        this.name = name;
    }


}
