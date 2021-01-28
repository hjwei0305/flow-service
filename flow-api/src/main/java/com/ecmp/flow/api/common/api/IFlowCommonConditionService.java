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
@Deprecated
public interface IFlowCommonConditionService extends ICommonConditionService {

    /**
     * 获取条件属性的备注说明
     *
     * @param businessModelCode 业务实体代码
     * @throws ClassNotFoundException 类找不到异常
     */
    @GET
    @Path("propertiesRemark")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "获取条件属性的备注说明", notes = "获取条件属性的备注说明")
    Map<String, String> propertiesRemark(@QueryParam("businessModelCode") String businessModelCode) throws ClassNotFoundException;

}
