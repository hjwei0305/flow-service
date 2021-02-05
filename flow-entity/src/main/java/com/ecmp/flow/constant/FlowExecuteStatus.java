package com.ecmp.flow.constant;


import java.io.Serializable;


/**
 * *************************************************************************************************
 * <br>
 * 实现功能：流程节点执行状态
 * <br>
 * ------------------------------------------------------------------------------------------------
 * <br>
 * 版本          变更时间             变更人                     变更原因
 * <br>
 * ------------------------------------------------------------------------------------------------
 * <br>
 * 1.0.00      2019/2/26 9:58       何灿坤                      新建
 * <br>
 * *************************************************************************************************
 */
public enum FlowExecuteStatus implements Serializable {

    SUBMIT("submit", "提交"),

    AGREE("agree", "同意"),

    DISAGREE("disagree", "不同意"),

    TURNTODO("turntodo", "转办"),

    ENTRUST("entrust", "委托"),

    RECALL("recall", "撤回"),

    REJECT("reject", "驳回"),

    END("end", "终止"),

    AUTO("auto", "自动执行"),

    HAVEREAD("haveRead", "已阅");


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

    FlowExecuteStatus(String code, String name) {
        this.code = code;
        this.name = name;
    }

    /**
     * 根据code值得到中文字段
     *
     * @param code
     * @return
     */
    public static String getNameByCode(String code) {
        for (FlowExecuteStatus flowCode : FlowExecuteStatus.values()) {
            if (flowCode.equals(code)) {
                return flowCode.getName();
            }
        }
        //如果匹配不上，默认返回自动执行
        return FlowExecuteStatus.AUTO.getName();
    }

}
