package com.ecmp.flow.api;

import com.ecmp.core.search.PageResult;
import com.ecmp.core.search.Search;
import com.ecmp.flow.api.common.api.IBaseService;
import com.ecmp.flow.entity.FlowHistory;
import com.ecmp.flow.entity.FlowInstance;
import com.ecmp.flow.entity.FlowTask;
import com.ecmp.vo.OperateResult;
import com.ecmp.vo.OperateResultWithData;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.Set;

/**
 * *************************************************************************************************
 * <p/>
 * 实现功能：流程实例服务API接口定义
 * <p>
 * ------------------------------------------------------------------------------------------------
 * 版本          变更时间             变更人                     变更原因
 * ------------------------------------------------------------------------------------------------
 * 1.0.00      2017/3/31 11:39      谭军(tanjun)               新建
 * <p/>
 * *************************************************************************************************
 */
@Path("flowInstance")
@Api(value = "IFlowInstanceService 流程实例服务API接口")
public interface IFlowInstanceService extends IBaseService<FlowInstance, String> {

    /**
     * 保存一个实体
     * @param flowInstance 实体
     * @return 保存后的实体
     */
    @POST
    @Path("save")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "保存实体",notes = "测试 保存实体")
    OperateResultWithData<FlowInstance> save(FlowInstance flowInstance);


    /**
     * 将流程实例挂起
     * @param id
     */
    @POST
    @Path("suspend")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "将流程实例暂停",notes = "测试")
    public OperateResult suspend(String id);

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
    PageResult<FlowInstance> findByPage(Search searchConfig);


    /**
     * 获取流程实例在线任务id列表
     * @param id
     */
    @GET
    @Path("currentNodeIds")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "获取流程实例在线任务节点定义ID列表", notes = "用于流程跟踪图")
    public Set<String> currentNodeIds(String id);

    /**
     * 通过业务单据id获取单据生命周期所有任务历史记录
     * @param businessId
     */
    @GET
    @Path("findAllByBusinessId")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "通过业务单据id获取单据生命周期所有任务历史记录", notes = "测试")
    public List<FlowHistory> findAllByBusinessId(String businessId);

    /**
     * 通过业务单据id获取单据最近一次流程实例流程历史记录
     * @param businessId
     */
    @GET
    @Path("findLastByBusinessId")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "通过业务单据id获取单据最近一次流程实例流程历史记录", notes = "测试")
    public List<FlowHistory>  findLastByBusinessId(String businessId);


    /**
     * 通过业务单据id获取单据最近一次流程实例
     * @param businessId
     */
    @GET
    @Path("findLastInstanceByBusinessId")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "通过业务单据id获取单据最近一次流程实例", notes = "测试")
    public FlowInstance  findLastInstanceByBusinessId(String businessId);

    /**
     * 通过业务单据id获取单据最近一次流程实例
     * @param businessId
     */
    @GET
    @Path("findCurrentTaskByBusinessId")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "通过业务单据id获取单据最近一次待办列表", notes = "测试")
    public List<FlowTask>  findCurrentTaskByBusinessId(String businessId);
}
