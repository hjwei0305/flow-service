package com.ecmp.flow.service;

/**
 * *************************************************************************************************
 * <p/>
 * 实现功能：
 * <p>
 * ------------------------------------------------------------------------------------------------
 * 版本          变更时间             变更人                     变更原因
 * ------------------------------------------------------------------------------------------------
 * 1.0.00      2017/4/28 14:22      谭军(tanjun)                    新建
 * <p/>
 * *************************************************************************************************
 */
public enum TestEnum {
    RED("红色", 1,"test"), GREEN("绿色", 2), BLANK("白色", 3), YELLO("黄色", 4);

    // 成员变量
    private String name;
    private int index;
    private String t;
    // 构造方法
    private TestEnum(String name, int index) {
        this.name = name;
        this.index = index;
    }
    // 构造方法
    private TestEnum(String name, int index,String t) {
        this.name = name;
        this.index = index;
        this.t = t;
    }
    }
