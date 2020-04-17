package com.ecmp.core.search;

import com.ecmp.annotation.Remark;
import com.ecmp.util.ConverterUtils;

import java.io.Serializable;
import java.util.Objects;

/**
 * *************************************************************************************************
 * <p/>
 * 实现功能：查询字段
 * <p>
 * ------------------------------------------------------------------------------------------------
 * 版本          变更时间             变更人                     变更原因
 * ------------------------------------------------------------------------------------------------
 * 1.0.00      2017/4/5 14:42      陈飞(fly)                  新建
 * <p/>
 * *************************************************************************************************
 */
public class SearchFilter implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final String EMPTY_VALUE = "EMPTY";
    public static final String NO_EMPTY_VALUE = "NOEMPTY";
    public static final String NULL_VALUE = "NULL";
    public static final String NO_NULL_VALUE = "NONULL";

    /**
     * 查询操作枚举
     */
    public enum Operator {
        @Remark(value = "等于", comments = " = ")
        EQ,

        @Remark(value = "不等于", comments = " != ")
        NE,

        @Remark(value = "模糊查询", comments = " LIKE %abc% ")
        LK,

        @Remark(value = "不包含", comments = " NOT LIKE %abc% ")
        NC,

        @Remark(value = "左匹配", comments = " LIKE abc% ")
        LLK,

        @Remark(value = "不以什么开头", comments = " NOT LIKE abc% ")
        BN,

        @Remark(value = "右匹配", comments = " LIKE %abc ")
        RLK,

        @Remark(value = "不以什么结尾", comments = " NOT LIKE %abc ")
        EN,

        @Remark(value = "BETWEEN", comments = " BETWEEN 1 AND 2 ")
        BT,

        @Remark(value = "大于", comments = " > ")
        GT,

        @Remark(value = "小于", comments = " < ")
        LT,

        @Remark(value = "大等于", comments = " >= ")
        GE,

        @Remark(value = "小等于", comments = " <= ")
        LE,

        @Remark(value = "在其中", comments = "IN ( )")
        IN,

        @Remark(value = "为空", comments = "IS NULL OR ==''")
        BK,

        @Remark(value = "不为空", comments = "IS NOT NULL AND !=''")
        NB,

        @Remark(value = "为NULL", comments = "IS NULL")
        NU,

        @Remark(value = "不为NULL", comments = "IS NOT NULL")
        NN,

        @Remark(value = "属性比较", comments = "属性1 < 属性2")
        PLT,

        @Remark(value = "属性比较", comments = "属性1 <= 属性2")
        PLE
    }

    /**
     * 查询字段名
     */
    private String fieldName;
    /**
     * 查询字段对应值
     */
    private Object value;
    /**
     * 查询操作
     */
    private Operator operator;

    /**
     * 字段类型
     */
    private String fieldType;

    public SearchFilter() {
    }

    public SearchFilter(String fieldName, Object value) {
        this(fieldName, value, Operator.EQ, null);
    }

    public SearchFilter(String fieldName, Object value, Operator operator) {
        this(fieldName, value, operator, null);
    }

    public SearchFilter(String fieldName, Object value, Operator operator, String fieldType) {
        this.fieldName = fieldName;
        this.value = value;
        this.operator = operator;
        this.fieldType = fieldType;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public Object getValue() {
        return ConverterUtils.convert(getFieldType(), this.value);
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public Operator getOperator() {
        return operator;
    }

    public void setOperator(Operator operator) {
        this.operator = operator;
    }

    public String getFieldType() {
        return fieldType;
    }

    public void setFieldType(String fieldType) {
        this.fieldType = fieldType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SearchFilter that = (SearchFilter) o;
        return Objects.equals(fieldName, that.fieldName) &&
                Objects.equals(value, that.value) &&
                operator == that.operator &&
                Objects.equals(fieldType, that.fieldType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fieldName, value, operator, fieldType);
    }
}
