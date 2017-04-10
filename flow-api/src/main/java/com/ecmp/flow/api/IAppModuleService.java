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
 * 1.0.02      2017/3/31 10:39      谭军(tanjun)               接口变更
 * <p/>
 * *************************************************************************************************
 */
@Path("appModule")
@Api(value = "IAppModuleService 应用模块服务API接口")
public interface IAppModuleService extends IBaseService<AppModule, String>{
//    @GET
//    @Path("hello")
//    @Produces(MediaType.APPLICATION_JSON)
//    @Consumes(MediaType.APPLICATION_JSON)
//    @ApiOperation(value = "test1",notes = "测试1")
//    public String hello();
//
//    @GET
//    @Path("hello2")
//    @Produces(MediaType.APPLICATION_JSON)
//    @Consumes(MediaType.APPLICATION_JSON)
//    @ApiOperation(value = "test2",notes = "测试2")
//    public String hello(String v);

}
