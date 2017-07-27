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
 * <p/>
 * 实现功能： 流程条件API定义
 * <p>
 * ------------------------------------------------------------------------------------------------
 * 版本          变更时间             变更人                     变更原因
 * ------------------------------------------------------------------------------------------------
 * 1.0.00      2017/4/26 13:19      谭军(tanjun)                    新建
 * <p/>
 * *************************************************************************************************
 */
@Path("condition")
@Api(value = "IFlowCommonConditionService 条件通用服务API接口")
public interface IFlowCommonConditionService extends ICommonConditionService {

}
