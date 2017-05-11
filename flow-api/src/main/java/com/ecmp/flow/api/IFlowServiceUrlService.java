package com.ecmp.flow.api;

import com.ecmp.core.search.PageResult;
import com.ecmp.core.search.Search;
import com.ecmp.flow.api.common.api.IBaseService;
import com.ecmp.flow.entity.FlowServiceUrl;
import com.ecmp.vo.OperateResultWithData;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

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
public interface IFlowServiceUrlService extends IBaseService<FlowServiceUrl, String> {

    /**
     * 保存一个实体
     * @param flowServiceUrl 实体
     * @return 保存后的实体
     */
    @POST
    @Path("save")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "保存实体",notes = "测试 保存实体")
    OperateResultWithData<FlowServiceUrl> save(FlowServiceUrl flowServiceUrl);

    /**
     * 获取分页数据
     *
     * @return 实体清单
     */
    @POST
    @Path("findByPage")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "获取分页数据", notes = "测试 获取分页数据")
    PageResult<FlowServiceUrl> findByPage(Search searchConfig);

    /**
     * 通过流程类型id查找拥有的服务方法
     * @param flowTypeId 流程类型id
     * @return 服务方法list
     */
    @GET
    @Path("findByFlowTypeId")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "通过流程类型id查找拥有的服务方法",notes = "测试")
    public List<FlowServiceUrl> findByFlowTypeId(String flowTypeId);
}
