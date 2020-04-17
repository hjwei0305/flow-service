package com.ecmp.core.entity;

/**
 * 实现功能：代码唯一的业务实体特征接口
 *
 * @author 王锦光(wangj)
 * @version 1.0.00  2017-06-14 10:17
 */
public interface ICodeUnique {
    String CODE_FIELD = "code";

    String getCode();

    void setCode(String code);
}
