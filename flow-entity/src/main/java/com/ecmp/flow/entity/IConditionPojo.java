package com.ecmp.flow.entity;

import net.sf.json.JSONObject;

import java.io.Serializable;

/**
 * 条件Pojo接口
 * 注：条件属性需要提供默认值，以供表达式验证
 * @author tanjun
 *
 */
public interface IConditionPojo extends Serializable{
//    public JSONObject toJsonObject();

    /**
     * 条件表达式初始化，提供给表达式做初始化验证，
     * 结合具体业务实际
     */
    public  void init();
}
