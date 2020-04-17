package com.ecmp.config.util;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.Provider;

/**
 * *************************************************************************************************
 * <p/>
 * 实现功能：平台API服务使用的JSON序列化提供类
 * <p>
 * ------------------------------------------------------------------------------------------------
 * 版本          变更时间             变更人                     变更原因
 * ------------------------------------------------------------------------------------------------
 * 1.0.00      2017-03-31 15:49      王锦光(wangj)                新建
 * <p/>
 * *************************************************************************************************
 */
@Provider
@Consumes({MediaType.APPLICATION_JSON, "application/json", "text/json"})
// NOTE: required to support "non-standard" JSON variants
@Produces({MediaType.APPLICATION_JSON, "application/json", "text/json"})
public class ApiRestJsonProvider extends JacksonJsonProvider {

    //重载构造函数
    public ApiRestJsonProvider() {
        super(ApiJsonUtils.mapper());
    }
}
