package com.ecmp.flow.api;

import com.ecmp.core.api.IBaseEntityService;
import com.ecmp.core.api.IFindAllService;
import com.ecmp.flow.entity.AppModule;
import io.swagger.annotations.Api;

import javax.ws.rs.Consumes;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

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
}
