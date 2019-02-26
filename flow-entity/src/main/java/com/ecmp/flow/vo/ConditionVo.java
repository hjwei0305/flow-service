package com.ecmp.flow.vo;

import java.io.Serializable;


/**
 * <strong>实现功能:</strong>
 * <p>条件属性的VO对象</p>
 *
 * @author 何灿坤
 * @version 1.0.0 2019-2-26 13:49
 */
public class ConditionVo  implements Serializable {

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
}
