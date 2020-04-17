package com.ecmp.core.entity;

/**
 * 实现功能：冻结属性特征接口
 *
 * @author 马超(Vision.Mac)
 * @version 1.0.00  2017/6/6 14:04
 */
public interface IFrozen {

    String FROZEN = "frozen";

    Boolean getFrozen();

    void setFrozen(Boolean frozen);
}
