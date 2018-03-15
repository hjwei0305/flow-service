package com.ecmp.flow.api;

import com.ecmp.core.search.PageResult;
import com.ecmp.core.search.Search;
import com.ecmp.flow.api.common.api.IBaseService;
import com.ecmp.flow.constant.FlowStatus;
import com.ecmp.flow.entity.FlowHistory;
import com.ecmp.flow.entity.FlowInstance;
import com.ecmp.flow.entity.FlowTask;
import com.ecmp.flow.vo.FlowTaskCompleteVO;
import com.ecmp.flow.vo.ProcessTrackVO;
import com.ecmp.vo.OperateResult;
import com.ecmp.vo.OperateResultWithData;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * *************************************************************************************************
 * <p>
 * 实现功能：流程实例服务API接口定义
 * </p>
 * <p>
 * ------------------------------------------------------------------------------------------------
 * </p>
 * <p>
 * 版本          变更时间             变更人                     变更原因
 * </p>
 * <p>
 * ------------------------------------------------------------------------------------------------
 * </p>
 * <p>
 * 1.0.00      2017/3/31 11:39      谭军(tanjun)                新建
 * </p>
 * *************************************************************************************************
 */
@Path("flowInstance")
@Api(value = "IFlowInstanceService 流程实例服务API接口")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
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
     * @param id 实例id
     *  @return 操作结果
     */
    @POST
    @Path("suspend")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "将流程实例暂停",notes = "测试")
    public OperateResult suspend(@QueryParam("id") String id);

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
     * @param id 实例id
     * @return 当前激动节点
     */
    @GET
    @Path("currentNodeIds/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "获取流程实例在线任务节点定义ID列表", notes = "用于流程跟踪图")
    public Map<String,String> currentNodeIds(@PathParam("id") String id);

    /**
     * 通过业务单据id获取单据生命周期所有任务历史记录
     * @param businessId 业务单据id
     * @return 流程执行历史
     */
    @GET
    @Path("findAllByBusinessId")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "通过业务单据id获取单据生命周期所有任务历史记录", notes = "测试")
    public List<FlowHistory> findAllByBusinessId(@QueryParam("businessId") String businessId);

    /**
     * 通过业务单据id获取单据最近一次流程实例流程历史记录
     * @param businessId 业务单据id
     * @return 流程执行历史
     */
    @GET
    @Path("findLastByBusinessId/{businessId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "通过业务单据id获取单据最近一次流程实例流程历史记录", notes = "测试")
    public List<FlowHistory>  findLastByBusinessId(@PathParam("businessId")String businessId);


    /**
     * 通过业务单据id获取单据最近一次流程实例
     * @param businessId 业务单据id
     * @return 流程实例
     */
    @GET
    @Path("findLastInstanceByBusinessId/{businessId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "通过业务单据id获取单据最近一次流程实例", notes = "测试")
    public FlowInstance  findLastInstanceByBusinessId(@PathParam("businessId")String businessId);

    /**
     * 通过业务单据id获取单据最近一次流程实例
     * @param businessId 业务单据id
     * @return 流程实例
     */
    @GET
    @Path("findCurrentTaskByBusinessId/{businessId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "通过业务单据id获取单据最近一次待办列表", notes = "测试")
    public List<FlowTask>  findCurrentTaskByBusinessId(@PathParam("businessId")String businessId);


    /**
     * 通过业务单据id获取最新流程实例待办任务id列表
     * @param businessId 业务单据id
     * @return 最新流程实例待办任务id列表
     */
    @GET
    @Path("getLastNodeIdsByBusinessId/{businessId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "通过业务单据id获取最新流程实例待办任务id列表", notes = "测试")
    public Set<String>  getLastNodeIdsByBusinessId(@PathParam("businessId")String businessId);

    /**
     * 通过单据id，获取流程实例及关联待办及任务历史
     * @param businessId 业务单据id
     * @return 流程实例及关联待办及任务历史
     */
    @GET
    @Path("getProcessTrackVO/{businessId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "通过单据id，获取流程实例及关联待办及任务历史", notes = "测试")
    public List<ProcessTrackVO> getProcessTrackVO(@PathParam("businessId")String businessId);

    /**
     * 通过流程实例id，获取流程实例及关联待办及任务历史
     * @param instanceId id
     * @return 流程实例及关联待办及任务历史
     */
    @GET
    @Path("getProcessTrackVOById/{instanceId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "通过实例id，获取流程实例及关联待办及任务历史", notes = "测试")
    public List<ProcessTrackVO> getProcessTrackVOById(@PathParam("instanceId")String instanceId);


    /**
     * 获取流程实例任务历史id列表，以完成时间升序排序
     * @param id 业务单据id
     * @return 流程实例及关联待办及任务历史
     */
    @GET
    @Path("getNodeHistoryIds/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "通过单据id，获取流程实例及关联待办及任务历史", notes = "主要用于流程图跟踪")
    public List<String>  nodeHistoryIds(@PathParam("id")String id);


    /**
     * 检查当前实例是否允许执行终止流程实例操作
     * @param id 业务单据id
     * @return 当前实例是否允许执行终止流程实例操作
     */
    @GET
    @Path("checkCanEnd/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "检查当前流程是否可以终止", notes = "实例终止")
    public Boolean checkCanEnd(@PathParam("id") String id);


    /**
     * 检查实例集合是否允许执行终止流程实例操作
     * @param ids 待操作数据ID集合
     * @return 集合是否可以终止列表
     */
    @POST
    @Path("checkIdsCanEnd")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "检查流程集合是否可以终止", notes = "实例终止")
    public List<Boolean> checkIdsCanEnd(List<String> ids);


    /**
     * 撤销流程实例
     * 清除有关联的流程版本及对应的流程引擎数据
     * @param id 待操作数据ID
     * @return 操作结果
     */
    @POST
    @Path("end/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "终止流程实例", notes = "终止")
    public OperateResult end(@PathParam("id") String id);

    /**
     * 撤销流程实例
     * 清除有关联的流程版本及对应的流程引擎数据
     * @param businessId 业务单据id
     * @return 操作结果
     */
    @POST
    @Path("endByBusinessId/{businessId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "通过单据终止目前在线的流程实例", notes = "终止")
    public OperateResult endByBusinessId(@PathParam("businessId")String businessId);


    /**
     * 激活ReceiveTask
     *
     * @param businessId 业务单据id
     * @param receiveTaskActDefId 实际节点id
     * @param v 其他参数值Map
     * @return 操作结果
     */
    @POST
    @Path("signalByBusinessId")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "通过单据终止目前在线的流程实例", notes = "终止")
    public OperateResult signalByBusinessId(@QueryParam("businessId")String businessId,@QueryParam("receiveTaskActDefId")String receiveTaskActDefId,Map<String,Object> v);


    /**
     * 强制中止流程实例
     * 清除有关联的流程版本及对应的流程引擎数据
     * @param id 待操作数据ID
     * @return 操作结果
     */
    @POST
    @Path("endForce/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "终止流程实例", notes = "终止")
    public OperateResult endForce(@PathParam("id")String id);

    /**
     * 工作池任务确定执行人
     * @param businessId 业务单据id
     * @param poolTaskActDefId 工作池任务实际流程定义key
     * @param userId 接收人id
     * @param v 其他变量
     * @return 操作结果
     */
    @POST
    @Path("signalPoolTaskByBusinessId")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "工作池任务确定执行人", notes = "工作池任务确定执行人")
    public OperateResultWithData<FlowTask> signalPoolTaskByBusinessId(@QueryParam("businessId") String businessId,@QueryParam("poolTaskActDefId") String poolTaskActDefId,@QueryParam("userId")String userId,Map<String,Object> v);

    /**
     * 工作池任务确定执行人并完成任务
     * @param businessId 业务单据id
     * @param poolTaskActDefId 工作池任务实际流程定义key
     * @param userId 接收人id
     * @param flowTaskCompleteVO 完成任务VO对象
     * @return 操作结果状态
     */
    @POST
    @Path("completePoolTask")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "工作池任务确定执行人并完成任务", notes = "说明")
    public OperateResultWithData<FlowStatus> completePoolTask(@QueryParam("businessId") String businessId,@QueryParam("poolTaskActDefId") String poolTaskActDefId,@QueryParam("userId")String userId, FlowTaskCompleteVO flowTaskCompleteVO) throws Exception;
}
