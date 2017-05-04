package com.ecmp.flow.api;

import com.ecmp.core.search.PageResult;
import com.ecmp.core.search.Search;
import com.ecmp.flow.api.common.api.IBaseService;
import com.ecmp.flow.entity.FlowDefination;
import com.ecmp.flow.entity.FlowInstance;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.UnsupportedEncodingException;
import java.util.Map;

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
public interface IFlowDefinationService extends IBaseService<FlowDefination, String> {


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
    PageResult<FlowDefination> findByPage(Search searchConfig);
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

    /**
     * 通过ID启动流程实体
     * @param id 流程id
     * @param businessKey 业务KEY
     * @param variables  其他参数
     * @return 流程实例
     */
    @POST
    @Path("startById/{id}/{businessKey}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "通过ID启动流程实体,附加启动ID",notes = "测试")
    public FlowInstance startById(@PathParam("id") String id,@PathParam("businessKey")String businessKey, Map<String, Object> variables);

    /**
     * 通过ID启动流程实体
     * @param id 流程id
     * @param startUserId 流程启动人
     * @param businessKey 业务KEY
     * @param variables  其他参数
     * @return 流程实例
     */
    @POST
    @Path("startByIdWithStartUserId/{id}/{startUserId}/{businessKey}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "通过ID启动流程实体",notes = "测试")
    public FlowInstance startById(@PathParam("id")String id,@PathParam("startUserId")String startUserId,@PathParam("businessKey")String businessKey, Map<String, Object> variables);

    /**
     * 通过Key启动流程实体
     * @param key 定义Key
     * @param businessKey 业务KEY
     * @param variables  其他参数
     * @return 流程实例
     */
    @POST
    @Path("startByKey/{key}/{businessKey}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "通过Key启动流程实体",notes = "测试")
    public FlowInstance startByKey(@PathParam("key") String key,@PathParam("businessKey")String businessKey, Map<String, Object> variables);

    /**
     * 通过Key启动流程实体
     * @param key 定义Key
     * @param startUserId 流程启动人
     * @param businessKey 业务KEY
     * @param variables  其他参数
     * @return 流程实例
     */
    @POST
    @Path("startByKeyWithStartUserId/{key}/{startUserId}/{businessKey}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "通过Key启动流程实体,附加启动用户ID",notes = "测试")
    public FlowInstance startByKey(@PathParam("key") String key,@PathParam("startUserId") String startUserId,@PathParam("businessKey")String businessKey, Map<String, Object> variables);

}
