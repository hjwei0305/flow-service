package com.ecmp.flow.api;

import com.ecmp.flow.common.api.IBaseService;
import com.ecmp.flow.entity.FlowInstance;
import com.ecmp.vo.OperateResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * *************************************************************************************************
 * <p/>
 * 实现功能：流程实例服务API接口定义
 * <p>
 * ------------------------------------------------------------------------------------------------
 * 版本          变更时间             变更人                     变更原因
 * ------------------------------------------------------------------------------------------------
 * 1.0.00      2017/3/31 11:39      谭军(tanjun)               新建
 * <p/>
 * *************************************************************************************************
 */
@Path("flowInstance")
@Api(value = "IFlowInstanceService 流程实例服务API接口")
public interface IFlowInstanceService extends IBaseService<FlowInstance, String> {

    /**
     * 将流程实例挂起
     * @param id
     */
    @POST
    @Path("suspend")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "将流程实例暂停",notes = "测试")
    public OperateResult suspend(String id);
}
