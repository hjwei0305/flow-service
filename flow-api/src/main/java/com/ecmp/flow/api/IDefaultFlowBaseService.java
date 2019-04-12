package com.ecmp.flow.api;


import com.ecmp.annotation.IgnoreCheckAuth;
import com.ecmp.vo.ResponseData;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

/**
 * *************************************************************************************************
 * <p/>
 * 实现功能：
 * 流程的默认服务接口（原FlowBaseController的方法提供成接口）
 * <p>
 * ------------------------------------------------------------------------------------------------
 * 版本          变更时间             变更人                     变更原因
 * ------------------------------------------------------------------------------------------------
 * 1.0.00      2018/11/30            何灿坤                      新建
 * <p/>
 * *************************************************************************************************
 */

@Path("defaultFlowBase")
@Api(value = "IDefaultFlowBaseService 流程的默认服务接口")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface IDefaultFlowBaseService {


    /**
     * 通过流程定义key启动流程
     *
     * @param businessModelCode 业务实体code
     * @param businessKey       业务实体key
     * @param opinion           审批意见
     * @param typeId            流程类型id
     * @param flowDefKey        流程定义key
     * @param taskList          任务完成传输对象
     * @param anonymousNodeId   传输对象为anonymous时传入的节点id
     * @return
     */
    @POST
    @Path("startFlow")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "通过流程定义key启动流程", notes = "通过流程定义key启动流程")
    @IgnoreCheckAuth
    ResponseData startFlow(@QueryParam("businessModelCode") String businessModelCode,
                           @QueryParam("businessKey") String businessKey,
                           @QueryParam("opinion") String opinion,
                           @QueryParam("typeId") String typeId,
                           @QueryParam("flowDefKey") String flowDefKey,
                           @QueryParam("taskList") String taskList,
                           @QueryParam("anonymousNodeId") String anonymousNodeId) throws NoSuchMethodException, SecurityException;


    /**
     * 签收任务
     *
     * @param taskId 任务id
     * @return
     */
    @POST
    @Path("listFlowTask")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "签收任务", notes = "签收任务")
    @IgnoreCheckAuth
    ResponseData claimTask(@QueryParam("taskId") String taskId);


    /**
     * 完成任务
     *
     * @param taskId         节点ID
     * @param businessId     业务表单ID
     * @param opinion        审批意见
     * @param taskList       任务完成传输对象
     * @param endEventId
     * @param manualSelected
     * @param approved
     * @param loadOverTime
     * @return 操作结果
     */
    @POST
    @Path("completeTask")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "完成任务", notes = "完成任务")
    @IgnoreCheckAuth
    ResponseData completeTask(@QueryParam("taskId") String taskId,
                              @QueryParam("businessId") String businessId,
                              @QueryParam("opinion") String opinion,
                              @QueryParam("taskList") String taskList,
                              @QueryParam("endEventId") String endEventId,
                              @QueryParam("manualSelected") boolean manualSelected,
                              @QueryParam("approved") String approved,
                              @QueryParam("loadOverTime") Long loadOverTime) throws Exception;

    /**
     * 回退（撤销）任务
     *
     * @param preTaskId 上一个任务ID
     * @param opinion   意见
     * @return 操作结果
     */
    @POST
    @Path("cancelTask")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = " 回退（撤销）任务", notes = " 回退（撤销）任务")
    @IgnoreCheckAuth
    ResponseData rollBackTo(@QueryParam("preTaskId") String preTaskId, @QueryParam("opinion") String opinion) throws CloneNotSupportedException;


    /**
     * 任务驳回
     *
     * @param taskId  任务ID
     * @param opinion 意见
     * @return 操作结果
     */
    @POST
    @Path("rejectTask")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "任务驳回", notes = "任务驳回")
    @IgnoreCheckAuth
    ResponseData rejectTask(@QueryParam("taskId") String taskId, @QueryParam("opinion") String opinion) throws Exception;


    /**
     * 获取当前审批任务的决策信息
     *
     * @param taskId
     * @return 操作结果
     */
    @POST
    @Path("nextNodesInfo")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "获取当前审批任务的决策信息", notes = "获取当前审批任务的决策信息")
    @IgnoreCheckAuth
    ResponseData nextNodesInfo(@QueryParam("taskId") String taskId) throws NoSuchMethodException;


    /**
     * 获取下一步的节点信息任务
     *
     * @param taskId
     * @return 操作结果
     */
    @POST
    @Path("getSelectedNodesInfo")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "获取下一步的节点信息任务", notes = "获取下一步的节点信息任务")
    @IgnoreCheckAuth
    ResponseData getSelectedNodesInfo(@QueryParam("taskId") String taskId,
                                      @QueryParam("approved") String approved,
                                      @QueryParam("includeNodeIdsStr") String includeNodeIdsStr,
                                      @QueryParam("solidifyFlow") Boolean solidifyFlow) throws NoSuchMethodException;


    /**
     * 获取下一步的节点信息任务(带用户信息)
     *
     * @param taskId
     * @return 操作结果
     */
    @POST
    @Path("nextNodesInfoWithUser")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "获取下一步的节点信息任务(带用户信息)", notes = "获取下一步的节点信息任务(带用户信息)")
    @IgnoreCheckAuth
    ResponseData nextNodesInfoWithUser(@QueryParam("taskId") String taskId) throws NoSuchMethodException;


    /**
     * 获取任务抬头信息信息任务
     *
     * @param taskId
     * @return 操作结果
     */
    @POST
    @Path("getApprovalHeaderInfo")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "获取任务抬头信息信息任务", notes = "获取任务抬头信息信息任务")
    @IgnoreCheckAuth
    ResponseData getApprovalHeaderInfo(@QueryParam("taskId") String taskId);


    /**
     * 通过业务单据Id获取待办任务（中泰要求新增的功能:不包含子流程信息）
     *
     * @param businessId 业务单据id
     * @return 待办任务集合
     */
    @POST
    @Path("findTasksByBusinessId")
    @ApiOperation(value = "获取待办任务", notes = "通过业务单据Id获取待办任务（不包含子流程）")
    @IgnoreCheckAuth
    ResponseData findTasksByBusinessId(@QueryParam("businessId") String businessId);


}
