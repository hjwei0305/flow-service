package com.ecmp.flow.api;

import com.ecmp.flow.entity.FlowHistory;
import com.ecmp.flow.entity.FlowServiceUrl;
import io.swagger.annotations.Api;

import javax.ws.rs.Path;

/**
 * *************************************************************************************************
 * <p/>
 * 实现功能：流程历史服务API接口定义
 * <p>
 * ------------------------------------------------------------------------------------------------
 * 版本          变更时间             变更人                     变更原因
 * ------------------------------------------------------------------------------------------------
 * 1.0.00      2017/3/31 11:39      谭军(tanjun)               新建
 * <p/>
 * *************************************************************************************************
 */
@Path("flowServiceUrl")
@Api(value = "IFlowServiceUrlService 流程服务地址服务API接口")
public interface IFlowServiceUrlService extends IBaseService<FlowServiceUrl, String>{
}
