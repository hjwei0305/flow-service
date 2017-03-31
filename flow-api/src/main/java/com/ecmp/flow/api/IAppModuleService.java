package com.ecmp.flow.api;

import com.ecmp.flow.entity.AppModule;
import com.ecmp.flow.entity.FlowDefination;
import com.ecmp.vo.OperateResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

/**
 * *************************************************************************************************
 * <p/>
 * 实现功能：应用模块服务API接口定义
 * <p>
 * ------------------------------------------------------------------------------------------------
 * 版本          变更时间             变更人                     变更原因
 * ------------------------------------------------------------------------------------------------
 * 1.0.00      2017/3/23 22:39      谭军(tanjun)               新建
 * <p/>
 * *************************************************************************************************
 */
@Path("appModule")
@Api(value = "IAppModuleService 应用模块服务API接口")
public interface IAppModuleService {

    /**
     * 获取所有实体
     * @return 实体清单
     */
    @GET
    @Path("getAll")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "获取所有实体",notes = "测试 获取所有实体")
    List<AppModule> findAll();

    /**
     * 通过Id获取实体
     * @param id
     * @return 实体
     */
    @GET
    @Path("getById")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "通过Id获取实体",notes = "测试 通过Id获取实体")
    AppModule findOne(String id);

    /**
     * 保存一个实体
     * @param entity 实体
     * @return 保存后的实体
     */
    @POST
    @Path("save")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "保存实体",notes = "测试 保存实体")
    OperateResult<AppModule> save(AppModule entity);

}
