package com.ecmp.flow.api;

import com.ecmp.flow.entity.FlowDefination;
import com.ecmp.vo.OperateResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.UnsupportedEncodingException;
import java.util.List;

/**
 * *************************************************************************************************
 * <p/>
 * 实现功能：流程定义服务API接口定义
 * <p>
 * ------------------------------------------------------------------------------------------------
 * 版本          变更时间             变更人                     变更原因
 * ------------------------------------------------------------------------------------------------
 * 1.0.00      2017/3/30 10:39      谭军(tanjun)               新建
 * <p/>
 * *************************************************************************************************
 */
@Path("flowDefination")
@Api(value = "IFlowDefinationService 流程定义服务API接口")
public interface IFlowDefinationService extends IBaseService<FlowDefination, String>{

    /**
     * 通过流程定义ID发布最新版本的流程
     * @param id 实体
     * @return 发布id
     */
    @POST
    @Path("deployById")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "通过流程定义ID发布最新版本的流程",notes = "测试")
    public String deployById(String id) throws UnsupportedEncodingException;

    /**
     * 通过流程版本ID发布指定版本的流程
     * @param id 实体
     * @return 发布id
     */
    @POST
    @Path("deployByVersionId")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "通过流程版本ID发布指定版本的流程",notes = "测试")
    public String deployByVersionId(String id) throws UnsupportedEncodingException;
}
