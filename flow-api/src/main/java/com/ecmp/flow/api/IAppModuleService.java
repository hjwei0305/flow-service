package com.ecmp.flow.api;

import com.ecmp.core.api.IBaseEntityService;
import com.ecmp.core.api.IFindAllService;
import com.ecmp.flow.entity.AppModule;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

/**
 * <p>
 * *************************************************************************************************
 * </p><p>
 * 实现功能：应用模块管理
 * </p><p>
 * ------------------------------------------------------------------------------------------------
 * </p><p>
 * 版本          变更时间             变更人                     变更原因
 * </p><p>
 * ------------------------------------------------------------------------------------------------
 * </p><p>
 * 1.0.00      2017/09/06 11:39      谭军(tanjun)                新建
 * </p><p>
 * *************************************************************************************************
 * </p>
 */
@Path("appModule")
@Api(value = "IAppModuleService 应用模块的服务接口")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public interface IAppModuleService extends IBaseEntityService<AppModule>,IFindAllService<AppModule> {
    /**
     * 获取分页数据
     *
     * @return 实体清单
     */
    @GET
    @Path("findAllByAuth")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "通过当前用户筛选有权限的数据", notes = "通过当前用户筛选有权限的数据")
    public List<AppModule> findAllByAuth();
}
