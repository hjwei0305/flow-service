package com.ecmp.flow.constant;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * *************************************************************************************************
 * <p/>
 * 实现功能：条件表达式自定义注解
 * <p>
 * ------------------------------------------------------------------------------------------------
 * 版本          变更时间             变更人                     变更原因
 * ------------------------------------------------------------------------------------------------
 * 1.0.00      2017/4/26 13:41      谭军(tanjun)                    新建
 * <p/>
 * *************************************************************************************************
 */
//@Target(ElementType.FIELD) //字段、枚举的常量
@Target(ElementType.METHOD) //方法
@Retention(RetentionPolicy.RUNTIME)
@Deprecated
public @interface ConditionAnnotaion {
    /**
     * 字段中文名称
     * @return
     */
    public String name();

    /**
     * 排序，从大到小
     * @return
     */
    public int rank() default 0;

    /**
     * 前端表达式是否可见
     * @return
     */
    public boolean canSee() default true;
}
