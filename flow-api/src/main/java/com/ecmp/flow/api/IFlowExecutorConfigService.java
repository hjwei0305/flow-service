package com.ecmp.flow.api;

import com.ecmp.core.search.PageResult;
import com.ecmp.core.search.Search;
import com.ecmp.flow.api.common.api.IBaseService;
import com.ecmp.flow.entity.FlowExecutorConfig;
import com.ecmp.vo.OperateResultWithData;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

/**
 * *************************************************************************************************
 * <p/>
 * 实现功能：默认业务表单服务API接口定义
 * <p>
 * ------------------------------------------------------------------------------------------------
 * 版本          变更时间             变更人                     变更原因
 * ------------------------------------------------------------------------------------------------
 * 1.0.00      2017/3/26 10:39      谭军(tanjun)               新建
 * <p/>
 * *************************************************************************************************
 */
@Path("executorConfig")
@Api(value = "IFlowExecutorConfigService 自定义执行人API接口")
public interface IFlowExecutorConfigService {
//
    /**
     * 保存一个实体
     * @param flowExecutorConfig 实体
     * @return 保存后的实体
     */
    @POST
    @Path("save")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "保存实体",notes = "测试 保存实体")
    OperateResultWithData<FlowExecutorConfig> save(FlowExecutorConfig flowExecutorConfig);

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
    PageResult<FlowExecutorConfig> findByPage(Search searchConfig);


    /**
     * 通过Id获取实体
     * @param id
     * @return 实体
     */
    @GET
    @Path("getById/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "通过Id获取实体",notes = "测试 通过Id获取实体")
    FlowExecutorConfig findOne(@PathParam("id")String id);

    /**
     * 获取所有实体
     * @return 实体清单
     */
    @GET
    @Path("getAll")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "获取所有实体",notes = "测试 获取所有实体")
    List<FlowExecutorConfig> findAll();
}
