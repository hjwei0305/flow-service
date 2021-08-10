package com.ecmp.flow.api;

import com.ecmp.core.search.PageResult;
import com.ecmp.core.search.Search;
import com.ecmp.flow.api.common.api.IBaseService;
import com.ecmp.flow.constant.FlowStatus;
import com.ecmp.flow.dto.UserFlowBillsQueryParam;
import com.ecmp.flow.entity.FlowHistory;
import com.ecmp.flow.entity.FlowInstance;
import com.ecmp.flow.entity.FlowTask;
import com.ecmp.flow.vo.*;
import com.ecmp.flow.vo.phone.MyBillPhoneVO;
import com.ecmp.vo.OperateResult;
import com.ecmp.vo.OperateResultWithData;
import com.ecmp.vo.ResponseData;
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
     *
     * @param flowInstance 实体
     * @return 保存后的实体
     */
    @POST
    @Path("save")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "保存实体", notes = "测试 保存实体")
    OperateResultWithData<FlowInstance> save(FlowInstance flowInstance);


    /**
     * 将流程实例挂起
     *
     * @param id 实例id
     * @return 操作结果
     */
    @POST
    @Path("suspend")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "将流程实例暂停", notes = "测试")
    OperateResult suspend(@QueryParam("id") String id);

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
     *
     * @param id 实例id
     * @return 当前激动节点
     */
    @GET
    @Path("currentNodeIds/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "获取流程实例在线任务节点定义ID列表", notes = "用于流程跟踪图")
    Map<String, String> currentNodeIds(@PathParam("id") String id);

    /**
     * 通过业务单据id获取单据生命周期所有任务历史记录
     *
     * @param businessId 业务单据id
     * @return 流程执行历史
     */
    @GET
    @Path("findAllByBusinessId")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "通过业务单据id获取单据生命周期所有任务历史记录", notes = "测试")
    List<FlowHistory> findAllByBusinessId(@QueryParam("businessId") String businessId);

    /**
     * 通过业务单据id获取单据最近一次流程实例的流程历史记录
     *
     * @param businessId 业务单据id
     * @return 流程执行历史
     */
    @GET
    @Path("findLastHisByBusinessId")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "通过业务单据id获取单据最近一次流程实例的流程历史记录", notes = "通过业务单据id获取单据最近一次流程实例的流程历史记录")
    List<FlowHistory> findLastHisByBusinessId(@QueryParam("businessId") String businessId);


    /**
     * 通过业务单据id获取单据最近一次流程实例
     *
     * @param businessId 业务单据id
     * @return 流程实例
     */
    @GET
    @Path("findLastInstanceByBusinessId/{businessId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "通过业务单据id获取单据最近一次流程实例", notes = "测试")
    FlowInstance findLastInstanceByBusinessId(@PathParam("businessId") String businessId);

    /**
     * 通过业务单据id获取单据最近一次待办列表
     *
     * @param businessId 业务单据id
     * @return 流程实例
     */
    @GET
    @Path("findCurrentTaskByBusinessId/{businessId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "通过业务单据id获取单据最近一次待办列表", notes = "测试")
    List<FlowTask> findCurrentTaskByBusinessId(@PathParam("businessId") String businessId);


    /**
     * 通过业务单据id获取最新流程实例待办任务id列表
     *
     * @param businessId 业务单据id
     * @return 最新流程实例待办任务id列表
     */
    @GET
    @Path("getLastNodeIdsByBusinessId/{businessId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "通过业务单据id获取最新流程实例待办任务id列表", notes = "测试")
    Set<String> getLastNodeIdsByBusinessId(@PathParam("businessId") String businessId);

    /**
     * 通过单据id，获取流程实例及关联待办及任务历史
     *
     * @param businessId 业务单据id
     * @return 流程实例及关联待办及任务历史
     */
    @POST
    @Path("getProcessTrackVO")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "通过单据id，获取流程实例及关联待办及任务历史", notes = "测试")
    List<ProcessTrackVO> getProcessTrackVO(@QueryParam("businessId") String businessId);


    /**
     * 通过当前任务id，获取流程实例及关联待办及任务历史
     *
     * @param taskId 当前任务id（必须在待办中）
     * @return 流程实例及关联待办及任务历史
     */
    @POST
    @Path("getProcessTrackVOByTaskId")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "通过当前任务id，获取流程实例及关联待办及任务历史", notes = "测试")
    List<ProcessTrackVO> getProcessTrackVOByTaskId(@QueryParam("taskId") String taskId);

    /**
     * 通过流程实例id，获取流程实例及关联待办及任务历史
     *
     * @param instanceId id
     * @return 流程实例及关联待办及任务历史
     */
    @POST
    @Path("getProcessTrackVOById")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "通过实例id，获取流程实例及关联待办及任务历史", notes = "测试")
    List<ProcessTrackVO> getProcessTrackVOById(@QueryParam("instanceId") String instanceId);


    /**
     * 获取流程实例任务历史id列表，以完成时间升序排序
     *
     * @param id 业务单据id
     * @return 流程实例及关联待办及任务历史
     */
    @GET
    @Path("getNodeHistoryIds/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "通过单据id，获取流程实例及关联待办及任务历史", notes = "主要用于流程图跟踪")
    List<String> nodeHistoryIds(@PathParam("id") String id);


    /**
     * 检查当前实例是否允许执行终止流程实例操作
     *
     * @param id 业务单据id
     * @return 当前实例是否允许执行终止流程实例操作
     */
    @GET
    @Path("checkCanEnd/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "检查当前流程是否可以终止", notes = "实例终止")
    Boolean checkCanEnd(@PathParam("id") String id);


    /**
     * 检查实例集合是否允许执行终止流程实例操作
     *
     * @param ids 待操作数据ID集合
     * @return 集合是否可以终止列表
     */
    @POST
    @Path("checkIdsCanEnd")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "检查流程集合是否可以终止", notes = "实例终止")
    List<Boolean> checkIdsCanEnd(List<String> ids);


    /**
     * 撤销流程实例
     * 清除有关联的流程版本及对应的流程引擎数据
     *
     * @param id 待操作数据ID
     * @return 操作结果
     */
    @POST
    @Path("end/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "终止流程实例", notes = "终止")
    OperateResult end(@PathParam("id") String id);


    /**
     * 撤销流程实例（网关支持的模式）
     * 清除有关联的流程版本及对应的流程引擎数据
     *
     * @param id 待操作数据ID
     * @return 操作结果
     */
    @POST
    @Path("endByFlowInstanceId")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "终止流程实例", notes = "终止")
    OperateResult endByFlowInstanceId(@QueryParam("id") String id);


    /**
     * 通过单据ID终止流程实例（带流程配置权限检查）
     * 列表组件使用
     *
     * @param businessId 业务单据id
     * @return 操作结果
     */
    @POST
    @Path("checkAndEndByBusinessId")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "通过单据ID终止流程实例（权限检查）", notes = "通过单据ID终止流程实例（权限检查）")
    ResponseData checkAndEndByBusinessId(@QueryParam("businessId") String businessId);

    /**
     * 撤销流程实例
     * 清除有关联的流程版本及对应的流程引擎数据
     *
     * @param businessId 业务单据id
     * @return 操作结果
     */
    @POST
    @Path("endByBusinessId/{businessId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "通过单据终止目前在线的流程实例", notes = "终止")
    OperateResult endByBusinessId(@PathParam("businessId") String businessId);


    /**
     * 通过单据ID集合批量终止（.NET项目专用）
     *
     * @param businessIds 需要终止的单据ID集合
     * @return
     */
    @POST
    @Path("endByBusinessIdList")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "通过单据ID集合批量终止", notes = "通过单据ID批量终止")
    ResponseData endByBusinessIdList(List<String> businessIds);


    /**
     * 激活ReceiveTask（接收任务）
     *
     * @param businessId          业务单据id
     * @param receiveTaskActDefId 实际节点id
     * @param v                   其他参数值Map
     * @return 操作结果
     */
    @POST
    @Path("signalByBusinessId")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "通过单据id和接收任务节点id激活流程", notes = "通过单据id和接收任务节点id激活流程")
    OperateResult signalByBusinessId(@QueryParam("businessId") String businessId, @QueryParam("receiveTaskActDefId") String receiveTaskActDefId, Map<String, Object> v);


    /**
     * 强制中止流程实例
     * 清除有关联的流程版本及对应的流程引擎数据
     *
     * @param id 待操作数据ID
     * @return 操作结果
     */
    @POST
    @Path("endForce/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "终止流程实例", notes = "终止")
    OperateResult endForce(@PathParam("id") String id);

    /**
     * 工作池任务确定执行人
     *
     * @param businessId       业务单据id
     * @param poolTaskActDefId 工作池任务实际流程定义key
     * @param userId           接收人id
     * @param v                其他变量
     * @return 操作结果
     */
    @POST
    @Path("signalPoolTaskByBusinessId")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "工作池任务确定执行人", notes = "工作池任务确定执行人")
    OperateResult signalPoolTaskByBusinessId(@QueryParam("businessId") String businessId, @QueryParam("poolTaskActDefId") String poolTaskActDefId, @QueryParam("userId") String userId, Map<String, Object> v);


    /**
     * 工作池任务确定多执行人（新）
     *
     * @return 操作结果
     */
    @POST
    @Path("signalPoolTaskByBusinessIdAndUserList")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "工作池任务确定多执行人", notes = "工作池任务确定多执行人")
    ResponseData signalPoolTaskByBusinessIdAndUserList(SignalPoolTaskVO signalPoolTaskVO);


    /**
     * 工作池任务确定执行人并完成任务
     *
     * @param businessId         业务单据id
     * @param poolTaskActDefId   工作池任务实际流程定义key
     * @param userId             接收人id
     * @param flowTaskCompleteVO 完成任务VO对象
     * @return 操作结果状态
     */
    @POST
    @Path("completePoolTask")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "工作池任务确定执行人并完成任务", notes = "说明")
    OperateResultWithData<FlowStatus> completePoolTask(@QueryParam("businessId") String businessId, @QueryParam("poolTaskActDefId") String poolTaskActDefId, @QueryParam("userId") String userId, FlowTaskCompleteVO flowTaskCompleteVO) throws Exception;


    /**
     * 通过业务单据id,流程节点定义key获取任务
     *
     * @param businessId   业务单据id
     * @param taskActDefId 流程节点定义key
     * @return 操作结果状态
     */
    @POST
    @Path("findTaskByBusinessIdAndActTaskKey")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "通过业务单据id,流程节点定义key获取任务", notes = "说明")
    FlowTask findTaskByBusinessIdAndActTaskKey(@QueryParam("businessId") String businessId, @QueryParam("taskActDefId") String taskActDefId);

    /**
     * 通过业务单据id,流程节点定义key获取任务
     *
     * @param businessId   业务单据id
     * @param taskActDefId 流程节点定义key
     * @return 操作结果状态
     */
    @POST
    @Path("findTaskVOByBusinessIdAndActTaskKey")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "通过业务单据id,流程节点定义key获取任务VO对象", notes = "说明")
    FlowTaskVO findTaskVOByBusinessIdAndActTaskKey(@QueryParam("businessId") String businessId, @QueryParam("taskActDefId") String taskActDefId);


    /**
     * 查询我的单据汇总列表
     *
     * @return ResponseData.data是 List<TodoBusinessSummaryVO>
     */
    @POST
    @Path("listMyBillsHeader")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "查询我的单据汇总列表", notes = "查询我的单据汇总列表")
    ResponseData listMyBillsHeader(MyBillsHeaderVo myBillsHeaderVo);


    /**
     * 查询我的单据汇总列表
     *
     * @return ResponseData.data是 List<TodoBusinessSummaryVO>
     */
    @GET
    @Path("listAllMyBillsHeader")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "查询我的单据汇总列表", notes = "查询我的单据汇总列表")
    ResponseData listAllMyBillsHeader();


    /**
     * 获取我的单据
     *
     * @param searchConfig 查询条件
     * @return
     */
    @POST
    @Path("getMyBills")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "获取我的单据", notes = "获取我的单据")
    ResponseData getMyBills(Search searchConfig);

    /**
     * 获取我的单据
     *
     * @param queryParam 查询参数
     * @return
     */
    @POST
    @Path("getAllMyBills")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "获取我的单据", notes = "获取我的单据")
    ResponseData getAllMyBills(UserFlowBillsQueryParam queryParam);


    /**
     * 根据业务实体获取我的单据
     *
     * @param modelId      业务实体ID
     * @param searchConfig 查询条件
     * @return
     */
    @POST
    @Path("getMyBillsByModeId")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "根据业务实体获取我的单据", notes = "根据业务实体获取我的单据")
    ResponseData getMyBillsByModeId(@QueryParam("modelId") String modelId, Search searchConfig);


    /**
     * 根据业务实体获取我的单据(关联待办执行人)
     *
     * @param appModelCode 应用模块code
     * @param modelId      业务实体ID
     * @param searchConfig 查询条件
     * @return
     */
    @POST
    @Path("getMyBillsAndExecutorByModeId")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "根据业务实体获取我的单据(关联待办执行人)", notes = "根据业务实体获取我的单据(关联待办执行人)")
    ResponseData getMyBillsAndExecutorByModeId(@QueryParam("appModelCode") String appModelCode, @QueryParam("modelId") String modelId, Search searchConfig);


    /**
     * 获取我的单据（已办/待办）
     *
     * @param page       当前页数
     * @param rows       每页条数
     * @param quickValue 模糊查询字段内容
     * @param ended      是否完成
     * @return
     */
    @POST
    @Path("getMyBillsOfMobile")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "获取我的单据(最新移动端专用)", notes = "获取我的单据(最新移动端专用)")
    PageResult<MyBillPhoneVO> getMyBillsOfMobile(
            @QueryParam("page") int page,
            @QueryParam("rows") int rows,
            @QueryParam("quickValue") String quickValue,
            @QueryParam("ended") boolean ended);

    /**
     * 获取我的单据（已办/待办）
     *
     * @param property   需要排序的字段
     * @param direction  排序规则
     * @param page       当前页数
     * @param rows       每页条数
     * @param quickValue 模糊查询字段内容
     * @return
     */
    @POST
    @Path("getMyBillsOfPhone")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "获取我的单据(移动端专用)", notes = "获取我的单据(移动端专用)")
    PageResult<MyBillVO> getMyBillsOfPhone(
            @QueryParam("property") String property,
            @QueryParam("direction") String direction,
            @QueryParam("page") int page,
            @QueryParam("rows") int rows,
            @QueryParam("quickValue") String quickValue,
            @QueryParam("startDate") String startDate,
            @QueryParam("endDate") String endDate,
            @QueryParam("ended") boolean ended);

    /**
     * 历史详情(移动端)
     *
     * @param businessId 业务id
     * @param instanceId 实例id
     * @return 可批量审批待办信息
     */
    @POST
    @Path("getFlowHistoryInfoOfPhone")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "历史详情(移动端)", notes = "历史详情(移动端)")
    ResponseData getFlowHistoryInfoOfPhone(
            @QueryParam("businessId") String businessId,
            @QueryParam("instanceId") String instanceId
    );


    /**
     * 得到我的流程汇总信息（我的待办，我的未完结单据）
     */
    @GET
    @Path("getMyFlowCollectInfo")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "得到我的流程汇总信息", notes = "得到我的流程汇总信息")
    ResponseData getMyFlowCollectInfo(@QueryParam("userId") String userId);


    @POST
    @Path("updateRemarkByBusinessId")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "通过单据ID修改流程实例说明", notes = "通过单据ID修改流程实例说明")
    ResponseData updateRemarkByBusinessId(UpdateInstanceRemarkVo updateInstanceRemarkVo);


    @POST
    @Path("updateRemarkByInstaceId")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "通过流程实例ID修改流程实例说明", notes = "通过流程实例ID修改流程实例说明")
    ResponseData updateRemarkByInstaceId(UpdateRemarkByInstanceVo updateRemarkByInstanceVo);



    @GET
    @Path("taskFailTheCompensation")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "待办生成失败的补偿机制接口", notes = "待办生成失败的补偿机制接口")
    ResponseData taskFailTheCompensation(@QueryParam("instanceId") String instanceId);


    /**
     * 检查并获取流程中可以跳转节点信息
     *
     * @param instanceId 流程实例id
     * @return 可以跳转的节点信息集合
     */
    @GET
    @Path("checkAndGetCanJumpNodeInfos")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "检查并获取流程中可以跳转节点信息", notes = "检查并获取流程中可以跳转节点信息")
    ResponseData<List<FlowNodeVO>> checkAndGetCanJumpNodeInfos(@QueryParam("instanceId") String instanceId);


    /**
     * 获取跳转目标节点信息
     *
     * @param instanceId   流程实例ID
     * @param targetNodeId 目标节点ID
     * @return 目标节点信息
     */
    @GET
    @Path("getTargetNodeInfo")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "获取跳转目标节点信息", notes = "获取跳转目标节点信息")
    ResponseData<TargetNodeInfoVo> getTargetNodeInfo(@QueryParam("instanceId") String instanceId, @QueryParam("targetNodeId") String targetNodeId);


    /**
     * 跳转至目标节点
     *
     * @return 操作结果
     */
    @POST
    @Path("jumpToTargetNode")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "跳转至目标节点", notes = "跳转至目标节点")
    ResponseData jumpToTargetNode(JumpTaskVo jumpTaskVo);

}
