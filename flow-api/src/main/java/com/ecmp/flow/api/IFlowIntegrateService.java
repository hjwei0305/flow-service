package com.ecmp.flow.api;

import com.ecmp.flow.vo.DefaultStartParam;
import com.ecmp.vo.OperateResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * <strong>实现功能:</strong>
 * <p>工作流业务集成服务API接口</p>
 *
 * @author 王锦光 wangj
 * @version 1.0.1 2018-12-19 10:29
 */
@Path("flowIntegrate")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Api(value = "IFlowIntegrateService", description = "工作流业务集成服务API接口")
public interface IFlowIntegrateService {
    /**
     * 使用默认值启动业务流程
     * @param startParam 启动参数
     * @return 操作结果
     */
    @POST
    @Path("startDefaultFlow")
    @ApiOperation(value = "使用默认值启动流程", notes = "使用默认流程类型和第一节点执行人来启动流程")
    OperateResult startDefaultFlow(DefaultStartParam startParam);
}
