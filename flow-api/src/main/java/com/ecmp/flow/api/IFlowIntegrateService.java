package com.ecmp.flow.api;

import com.ecmp.flow.dto.PortalFlowHistory;
import com.ecmp.flow.dto.PortalFlowTask;
import com.ecmp.flow.dto.PortalFlowTaskParam;
import com.ecmp.flow.vo.DefaultStartParam;
import com.ecmp.vo.OperateResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

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

    /**
     * 获取当前用户门户待办信息
     * @param portalFlowTaskParam 门户待办查询参数
     * @return 门户待办信息清单
     */
    @POST
    @Path("getPortalFlowTask")
    @ApiOperation(value = "获取当前用户门户待办信息", notes = "获取当前用户门户待办信息，只返回输入参数指定的条目数")
    List<PortalFlowTask> getPortalFlowTask(PortalFlowTaskParam portalFlowTaskParam);

    /**
     * 获取当前用户门户已办信息
     * @param recordCount 获取条目数
     * @return 门户已办信息清单
     */
    @GET
    @Path("getPortalFlowHistory")
    @ApiOperation(value = "获取当前用户门户已办信息", notes = "获取当前用户门户已办信息，只返回输入参数指定的条目数")
    List<PortalFlowHistory> getPortalFlowHistory(Integer recordCount);
}
