package com.ecmp.flow.api;

import com.ecmp.flow.entity.FlowHistory;
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
@Path("flowHistory")
@Api(value = "IFlowHistoryService 流程历史服务API接口")
public interface IFlowHistoryService extends IBaseService<FlowHistory, String>{
}
