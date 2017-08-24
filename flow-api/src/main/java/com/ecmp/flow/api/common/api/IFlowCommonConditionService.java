package com.ecmp.flow.api.common.api;

import com.ecmp.flow.clientapi.ICommonConditionService;
import com.ecmp.flow.constant.FlowStatus;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

/**
 * *************************************************************************************************
 * <p>
 * 实现功能：条件通用服务API接口
 * </p>
 * <p>
 * ------------------------------------------------------------------------------------------------
 * </p>
 * <p>
 * 版本          变更时间             变更人                     变更原因
 * </p>
 * <p>
 * ------------------------------------------------------------------------------------------------
 * </p>
 * <p>
 * 1.0.00      2017/3/31 11:39      谭军(tanjun)                新建
 * </p>
 * *************************************************************************************************
 */
@Path("condition")
@Api(value = "IFlowCommonConditionService 条件通用服务API接口")
public interface IFlowCommonConditionService extends ICommonConditionService {
}
