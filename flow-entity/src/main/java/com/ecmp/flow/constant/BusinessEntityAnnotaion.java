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
@Target(ElementType.TYPE) //类
@Retention(RetentionPolicy.RUNTIME)
@Deprecated
public @interface BusinessEntityAnnotaion {
    /**
     * 转换对象全路径
     * @return
     */
    public String conditionBean();

    /**
     * 数据访问对象,spring管理的bean名称
     * @return
     */
    public String daoBean();
}
