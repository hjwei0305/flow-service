package com.ecmp.core.entity;

/**
 * @author 马超(Vision.Mac)
 * @version 1.0.1 2018/5/8 10:48
 */
public interface IDataValue<ID, V> {
    /**
     * id
     *
     * @return 返回id
     */
    ID getId();

    /**
     * 名称
     *
     * @return 返回名称
     */
    String getName();

    /**
     * 值
     *
     * @return 返回值
     */
    V getValue();
}
