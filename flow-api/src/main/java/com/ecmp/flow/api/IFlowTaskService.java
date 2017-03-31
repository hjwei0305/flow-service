package com.ecmp.flow.api;

import com.ecmp.flow.entity.FlowServiceUrl;
import com.ecmp.flow.entity.FlowTask;
import io.swagger.annotations.Api;

import javax.ws.rs.Path;

/**
 * *************************************************************************************************
 * <p/>
 * 实现功能：流程服务地址服务API接口定义
 * <p>
 * ------------------------------------------------------------------------------------------------
 * 版本          变更时间             变更人                     变更原因
 * ------------------------------------------------------------------------------------------------
 * 1.0.00      2017/3/31 11:39      谭军(tanjun)               新建
 * <p/>
 * *************************************************************************************************
 */
@Path("flowTask")
@Api(value = "IFlowTaskService 流程服务地址服务API接口")
public interface IFlowTaskService extends IBaseService<FlowTask, String>{
}
