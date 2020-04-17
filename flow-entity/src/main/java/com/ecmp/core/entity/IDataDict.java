package com.ecmp.core.entity;

/**
 * 实现功能：数据字典特征接口
 *
 * @author 马超(Vision.Mac)
 * @version 1.0.00  2019-05-16 18:22
 */
public interface IDataDict {
    /**
     * 字典分类code
     */
    String getCategoryCode();

    /**
     * 代码
     */
    String getCode();

    /**
     * 值
     */
    String getValue();

    /**
     * 值名称
     */
    String getValueName();

}
