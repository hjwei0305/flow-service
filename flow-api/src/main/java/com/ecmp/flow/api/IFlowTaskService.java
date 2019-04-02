package com.ecmp.flow.api;

import com.ecmp.annotation.IgnoreCheckAuth;
import com.ecmp.core.search.PageResult;
import com.ecmp.core.search.Search;
import com.ecmp.flow.api.common.api.IBaseService;
import com.ecmp.flow.basic.vo.Executor;
import com.ecmp.flow.constant.FlowStatus;
import com.ecmp.flow.entity.FlowTask;
import com.ecmp.flow.vo.*;
import com.ecmp.flow.vo.phone.FlowTaskPhoneVo;
import com.ecmp.vo.OperateResult;
import com.ecmp.vo.OperateResultWithData;
import com.ecmp.vo.ResponseData;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.Map;

/**
 * *************************************************************************************************
 * <p>
 * 实现功能：流程任务服务API接口定义
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
@Path("flowTask")
@Api(value = "IFlowTaskService 流程任务服务API接口")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface IFlowTaskService extends IBaseService<FlowTask, String> {
    /**
     * 任务签收
     * @param id 任务id
     * @param userId 用户账号
     * @return 操作结果
     */
    @POST
    @Path("claim/{id}/{userId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "签收任务",notes = "测试")
    public OperateResult claim(@PathParam("id") String id, @PathParam("userId") String userId);


    /**
     * 任务签收（移动端专用）
     * @param taskId 任务id
     * @return 操作结果
     */
    @POST
    @Path("claimTaskOfPhone")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "任务签收（移动端专用）",notes = "任务签收（移动端专用）")
    ResponseData claimTaskOfPhone(@QueryParam("taskId") String taskId);

//    /**
//     * 完成任务
//     * @param id 任务id
//     * @param variables 参数
//     * @return 操作结果
//     */
//    @POST
//    @Path("complete/{id}")
//    @Produces(MediaType.APPLICATION_JSON)
//    @Consumes(MediaType.APPLICATION_JSON)
//    @ApiOperation(value = "完成任务",notes = "测试")
//    public OperateResult complete(@PathParam("id") String id, Map<String, Object> variables);

    /**
     * 完成任务
     * @param flowTaskCompleteVO 任务传输对象
     * @return 操作结果
     */
    @POST
    @Path("complete")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "完成任务",notes = "测试")
    public OperateResultWithData<FlowStatus> complete(FlowTaskCompleteVO flowTaskCompleteVO) throws Exception;

    /**
     * 批量处理指定版本节点的任务
     * @param flowTaskBatchCompleteVO 任务传输对象
     * @return 操作结果
     */
    @POST
    @Path("completeBatch")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "完成任务",notes = "测试")
    public OperateResultWithData<Integer> completeBatch(FlowTaskBatchCompleteVO flowTaskBatchCompleteVO) ;


    /**
     * 批量处理（移动端）
     * @param flowTaskBatchCompleteWebVoStrs
     * @return 操作结果
     */
    @POST
    @Path("completeTaskBatchOfPhone")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "批量处理（移动端）",notes = "批量处理（移动端）")
    ResponseData completeTaskBatchOfPhone(@QueryParam("flowTaskBatchCompleteWebVoStrs")String flowTaskBatchCompleteWebVoStrs) ;



    /**
     * 撤回到指定任务节点
     * @param id 任务id
     * @param opinion 意见
     * @throws    CloneNotSupportedException 不能复制对象
     * @return 操作结果
     */
    @POST
    @Path("rollBackTo/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "撤回任务",notes = "测试")
    public  OperateResult rollBackTo(@PathParam("id") String id, String opinion) throws CloneNotSupportedException;

    /**
     * 撤回到指定任务节点(移动端专用)
     * @param preTaskId 任务id
     * @param opinion 意见
     * @throws CloneNotSupportedException 不能复制对象
     * @return 操作结果
     */
    @POST
    @Path("rollBackToOfPhone")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "撤回任务(移动端专用)",notes = "撤回任务(移动端专用)")
    ResponseData rollBackToOfPhone(@QueryParam("preTaskId") String preTaskId, @QueryParam("opinion")String opinion) throws CloneNotSupportedException;

    /**
     * 驳回任务（动态驳回）
     * @param id 任务id
     * @param opinion 意见
     * @param variables 参数
     * @return 操作结果
     */
    @POST
    @Path("reject/{id}/{opinion}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "驳回任务（动态驳回）",notes = "测试")
    public OperateResult taskReject(@PathParam("id") String id, @PathParam("opinion") String opinion, Map<String, Object> variables) throws  Exception;

    /**
     * 驳回任务（移动端）
     * @param taskId 任务id
     * @param opinion 意见
     * @return 操作结果
     */
    @POST
    @Path("rejectTaskOfPhone")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "驳回任务（移动端）",notes = "驳回任务（移动端）")
    ResponseData rejectTaskOfPhone(@QueryParam("taskId") String taskId, @QueryParam("opinion") String opinion) throws  Exception;

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
    PageResult<FlowTask> findByPage(Search searchConfig);

//    /**
//     * 通过任务Id检查当前任务的出口节点是否存在条件表达式
//     * @param id 任务id
//     * @return 结果
//     */
//    @GET
//    @Path("checkHasConditon/{id}")
//    @Produces(MediaType.APPLICATION_JSON)
//    @Consumes(MediaType.APPLICATION_JSON)
//    @ApiOperation(value = "检查当前任务的出口节点是否存在条件表达式",notes = "测试")
//    public boolean checkHasConditon(@PathParam("id")String id);


    /**
     * 选择下一步执行的节点信息
     * @param id 任务ID
     * @param businessId 业务ID
     * @return 下一步执行的节点信息
     * @throws NoSuchMethodException 方法找不到异常
     */
    @POST
    @Path("findNextNodesWithBusinessId/{id}/{businessId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "选择下一步执行的节点信息",notes = "测试")
    public List<NodeInfo> findNextNodes(@PathParam("id") String id, @PathParam("businessId") String businessId) throws NoSuchMethodException;

    /**
     * 根据流程实例ID查询待办
     * @param instanceId 实例id
     * @return 待办列表
     */
    @POST
    @Path("findByInstanceId")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "查询待办", notes = "查询待办")
    List<FlowTask> findByInstanceId(String instanceId);

    /**
     * 选择下一步执行的节点信息
     * @param id 任务ID
     * @return 下一步执行的节点信息
     * @throws NoSuchMethodException 方法找不到异常
     */
    @GET
    @Path("findNextNodes/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "选择下一步执行的节点信息",notes = "测试")
    public List<NodeInfo> findNextNodes(@PathParam("id") String id) throws NoSuchMethodException;


//    /**
//     * 通过任务ID与业务ID选择下一步带用户信息的执行的节点信息
//     * @param id 任务ID
//     * @param businessId 业务ID
//     * @return
//     * @throws NoSuchMethodException
//     */
//    @GET
//    @Path("findNexNodesByIdAndBusinessIdWithUserSet/{id}/{businessId}/{approved}")
//    @Produces(MediaType.APPLICATION_JSON)
//    @Consumes(MediaType.APPLICATION_JSON)
//    @ApiOperation(value = "通过任务ID与业务ID选择下一步执行的节点信息(带用户信息)",notes = "测试")
//    public List<NodeInfo> findNexNodesWithUserSet(@PathParam("id")String id, @PathParam("businessId")String businessId, @PathParam("approved")String approved) throws NoSuchMethodException;


    /**
     * 只通过任务ID选择下一步带用户信息的执行的节点信息
     * @param id 任务ID
     * @return 下一步执行的节点信息(带用户信息)
     * @throws NoSuchMethodException 找不到方法异常
     */
    @GET
    @Path("findNexNodesByIdWithUserSet/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "只通过任务ID选择下一步执行的节点信息(带用户信息)",notes = "测试")
    public List<NodeInfo> findNexNodesWithUserSet(@PathParam("id") String id) throws NoSuchMethodException;


    /**
     * 只通过任务ID选择下一步带用户信息的执行的节点信息
     * @param id 任务ID
     * @param approved 是否同意
     * @param includeNodeIds 只包含此节点
     * @return 下一步执行的节点信息(带用户信息)
     * @throws NoSuchMethodException 找不到方法异常
     */
    @POST
    @Path("findNexNodesByIdWithUserSetAndNodeIds/{id}/{approved}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "通过任务ID选择下一步执行的节点信息(带用户信息)",notes = "测试")
    public List<NodeInfo> findNexNodesWithUserSet(@PathParam("id") String id, @PathParam("approved") String approved, List<String> includeNodeIds) throws NoSuchMethodException;


    /**
     * 只通过任务ID选择下一步带用户信息的执行的节点信息
     * @param taskIds 任务IDs

     * @return 下一步执行的节点信息(带用户信息)
     * @throws NoSuchMethodException 找不到方法异常
     */
    @GET
    @Path("findNexNodesWithUserSetCanBatch")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "通过任务IDs选择下一步可批量审批执行的节点信息(带用户信息)",notes = "测试")
    public List<NodeInfo> findNexNodesWithUserSetCanBatch(@QueryParam("taskIds") String taskIds) throws NoSuchMethodException;

    /**
     * 获取当前流程抬头信息
     * @param id 任务id
     * @return  当前任务流程抬头信息
     */
    @GET
    @Path("getApprovalHeaderVO/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "选择当前流程抬头信息",notes = "测试")
   public ApprovalHeaderVO getApprovalHeaderVO(@PathParam("id") String id);


    /**
     * 查询流程待办和任务汇总列表
     * @return  ResponseData.data是 List<TodoBusinessSummaryVO>
     */
    @POST
    @Path("listFlowTaskHeader")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "查询流程待办和任务汇总列表",notes = "查询流程待办和任务汇总列表")
    ResponseData  listFlowTaskHeader();

    /**
     * 获取待办汇总信息
     * @param appSign 应用标识
     * @return 待办汇总信息
     */
    @GET
    @Path("findTaskSumHeader")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "获取待办汇总信息",notes = "测试")
    public List<TodoBusinessSummaryVO> findTaskSumHeader(@QueryParam("appSign") String appSign);


    /**
     * 获取待办汇总信息(移动端专用)
     * @return 待办汇总信息
     */
    @POST
    @Path("findTaskSumHeaderOfPhone")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "获取待办汇总信息(移动端专用)",notes = "测试")
    ResponseData findTaskSumHeaderOfPhone();

    /**
     * 获取待办汇总信息-可批量审批
     * @param batchApproval 是批量审批
     * @param appSign 应用标识
     * @return 待办汇总信息
     */
    @GET
    @Path("findCommonTaskSumHeader")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "获取待办汇总信息-可批量审批",notes = "测试")
    public List<TodoBusinessSummaryVO> findCommonTaskSumHeader(@QueryParam("batchApproval") Boolean batchApproval, @QueryParam("appSign") String appSign);

    /**
     * 获取待办汇总信息-可批量审批(移动端)
     * @return 待办汇总信息
     */
    @GET
    @Path("findTaskSumHeaderCanBatchApprovalOfPhone")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "获取待办汇总信息-可批量审批(移动端)",notes = "测试")
    ResponseData findTaskSumHeaderCanBatchApprovalOfPhone();


    /**
     * 获取待办信息（租户管理员）
     * @param appModuleId 应用模块id
     * @param businessModelId 业务实体id
     * @param flowTypeId 流程类型id
     * @return 待办汇总信息
     */
    @POST
    @Path("findAllByTenant")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "获取待办信息（租户管理员）",notes = "测试")
    public PageResult<FlowTask> findAllByTenant(@QueryParam("appModuleId") String appModuleId, @QueryParam("businessModelId") String businessModelId, @QueryParam("flowTypeId") String flowTypeId, Search searchConfig);

    /**
     * 获取待办汇总信息
     * @param businessModelId 业务实体id
     * @param appSign 应用标识
     * @param searchConfig 查询条件
     * @return 待办汇总信息
     */
    @POST
    @Path("findByBusinessModelId")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "获取待办汇总信息",notes = "测试")
    public PageResult<FlowTask> findByBusinessModelId(@QueryParam("businessModelId") String businessModelId, @QueryParam("appSign") String appSign, Search searchConfig);


    /**
     * 获取可批量审批待办信息
     * @param searchConfig 查询条件
     * @return 可批量审批待办信息
     */
    @POST
    @Path("findByPageCanBatchApproval")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "获取可批量审批待办信息",notes = "测试")
    public PageResult<FlowTask> findByPageCanBatchApproval(Search searchConfig);


    /**
     * 获取可批量审批待办信息
     * @param searchConfig 查询条件
     * @return 可批量审批待办信息
     */
    @POST
    @Path("findByPageCanBatchApprovalByBusinessModelId")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "获取可批量审批待办信息",notes = "测试")
    public PageResult<FlowTask> findByPageCanBatchApprovalByBusinessModelId(@QueryParam("businessModelId") String businessModelId, Search searchConfig);

    /**
     * 获取可批量审批待办信息(移动端)
     * @param property 需要排序的字段
     * @param direction 排序规则
     * @param page 当前页数
     * @param rows 每页条数
     * @param quickValue 模糊查询字段内容
     * @return 可批量审批待办信息
     */
    @POST
    @Path("findByPageCanBatchApprovalOfPhone")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "获取可批量审批待办信息(移动端)",notes = "测试")
    PageResult<FlowTask> findByPageCanBatchApprovalOfPhone(
            @QueryParam("businessModelId") String businessModelId,
            @QueryParam("property") String property,
            @QueryParam("direction") String direction,
            @QueryParam("page") int page,
            @QueryParam("rows") int rows,
            @QueryParam("quickValue") String quickValue);


    /**
     * 查询流程任务列表,带用户所有待办总数
     * @param searchConfig 搜索对象
     */
    @POST
    @Path("listFlowTaskWithAllCount")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "查询流程任务列表,带用户所有待办总数",notes = "查询流程任务列表,带用户所有待办总数")
    ResponseData  listFlowTaskWithAllCount(Search searchConfig,@QueryParam("modelId")String modelId);


    /**
     * 获取可批量审批待办信息
     * @param searchConfig 查询条件
     * @param businessModelId 为空查询全部
     * @param appSign 应用标识
     * @return 可批量审批待办信息
     */
    @POST
    @Path("findByBusinessModelIdWithAllCount")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "获取待办信息",notes = "测试")
    public FlowTaskPageResultVO<FlowTask> findByBusinessModelIdWithAllCount(@QueryParam("businessModelId") String businessModelId, @QueryParam("appSign") String appSign, Search searchConfig);



    /**
     * 获取可批量审批待办信息(最新移动端专用)
     * @param businessModelId 为空查询全部
     * @param page 当前页数
     * @param rows 每页条数
     * @param quickValue 模糊查询字段内容
     * @return 可批量审批待办信息
     */
    @POST
    @Path("findByBusinessModelIdWithAllCountOfMobile")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "获取待办信息(最新移动端专用)",notes = "获取待办信息(最新移动端专用)")
    FlowTaskPageResultVO<FlowTaskPhoneVo>  findByBusinessModelIdWithAllCountOfMobile(
            @QueryParam("businessModelId") String businessModelId,
            @QueryParam("page") int page,
            @QueryParam("rows") int rows,
            @QueryParam("quickValue") String quickValue);


    /**
     * 获取可批量审批待办信息(移动端专用)
     * @param businessModelId 为空查询全部
     * @param property 需要排序的字段
     * @param direction 排序规则
     * @param page 当前页数
     * @param rows 每页条数
     * @param quickValue 模糊查询字段内容
     * @return 可批量审批待办信息
     */
    @POST
    @Path("findByBusinessModelIdWithAllCountOfPhone")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "获取待办信息(移动端专用)",notes = "获取待办信息(移动端专用)")
    FlowTaskPageResultVO<FlowTask> findByBusinessModelIdWithAllCountOfPhone(
            @QueryParam("businessModelId") String businessModelId,
            @QueryParam("property") String property,
            @QueryParam("direction") String direction,
            @QueryParam("page") int page,
            @QueryParam("rows") int rows,
            @QueryParam("quickValue") String quickValue);


//    @POST
//    @Path("findByModelEntity")
//    @Produces(MediaType.APPLICATION_JSON)
//    @Consumes(MediaType.APPLICATION_JSON)
//    @ApiOperation(value = "获取待办汇总信息",notes = "测试")
//    public PageResult<FlowTask> findByBusinessModelId(FlowTaskQueryParam flowTaskQueryParam);


    /**
     * 获取批量审批明细信息
     * @param taskIdArray 任务id列表
     * @return 可批量审批待办信息
     */
    @POST
    @Path("getBatchApprovalFlowTasks")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "获取可批量审批待办信息",notes = "测试")
    public List<BatchApprovalFlowTaskGroupVO> getBatchApprovalFlowTasks(List<String> taskIdArray) throws NoSuchMethodException;


    /**
     * 批量完成任务
     * @param flowTaskCompleteVOList 任务传输对象列表
     * @return 操作结果
     */
    @POST
    @Path("completeBatchApproval")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "完成任务",notes = "测试")
    public OperateResultWithData<FlowStatus> completeBatchApproval(List<FlowTaskCompleteVO> flowTaskCompleteVOList) throws Exception;


    /**
     * 取得一一步的执行人信息
     * @param taskId 任务ID
     * @param   approved 是否同意
     * @param    includeNodeIdsStr 包含节点
     * @return 操作结果
     */
    @GET
    @Path("getSelectedNodesInfo")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "完成任务",notes = "测试")
    public OperateResultWithData getSelectedNodesInfo(@QueryParam("taskId") String taskId, @QueryParam("approved") String approved, @QueryParam("includeNodeIdsStr") String includeNodeIdsStr) throws NoSuchMethodException;


    /**
     * 通过任务IDs选择下一步带用户信息的执行的节点信息
     * @param taskIds 任务IDs

     * @return 下一步执行的节点信息(带用户信息),带分组
     * @throws NoSuchMethodException 找不到方法异常
     */
    @GET
    @Path("findNextNodesGroupWithUserSetCanBatch")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "通过任务IDs选择下一步可批量审批执行的节点信息(带用户信息)",notes = "测试")
    public List<NodeGroupInfo> findNexNodesGroupWithUserSetCanBatch(@QueryParam("taskIds") String taskIds)  throws NoSuchMethodException;

    /**
     * 通过任务IDs选择下一步带用户信息的执行的节点信息
     * @param taskIds 任务IDs

     * @return 下一步执行的节点信息(带用户信息),带分组
     * @throws NoSuchMethodException 找不到方法异常
     */
    @GET
    @Path("findNextNodesByVersionGroupWithUserSetCanBatch")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "通过任务IDs选择下一步可批量审批执行的节点信息(带用户信息与版本分组)",notes = "测试")
    public List<NodeGroupByFlowVersionInfo> findNexNodesGroupByVersionWithUserSetCanBatch(@QueryParam("taskIds") String taskIds)  throws NoSuchMethodException;


    /**
     * 获取下一步的节点信息任务(移动端)
     * @param taskIds 任务IDs
     * @throws NoSuchMethodException 找不到方法异常
     */
    @POST
    @Path("getSelectedCanBatchNodesInfoOfPhone")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "获取下一步的节点信息任务(移动端)",notes = "获取下一步的节点信息任务(移动端)")
    ResponseData getSelectedCanBatchNodesInfoOfPhone(@QueryParam("taskIds") String taskIds)  throws NoSuchMethodException;


    /**
     * 将任务转办给指定用户
     * @param taskId 任务ID
     * @param  userId 用户id
     * @return 操作结果
     */
    @POST
    @Path("taskTurnToDo")
    @ApiOperation(value = "将任务转办给指定用户",notes = "测试")
    public OperateResult taskTurnToDo(@QueryParam("taskId") String taskId, @QueryParam("userId") String userId);

    /**
     * 将任务委托给指定用户
     * @param taskId 任务ID
     * @param  userId 用户id
     * @return 操作结果
     */
    @POST
    @Path("taskTrustToDo")
    @ApiOperation(value = "将任务委托给指定用户",notes = "测试")
    public OperateResult taskTrustToDo(@QueryParam("taskId") String taskId, @QueryParam("userId") String userId) throws Exception;

    /**
     * 会签任务加签
     * @param actInstanceId 流程实例实际ID
     * @param  taskActKey 任务key
     * @param  userIds 用户id,以“，”分割
     * @return 操作结果
     */
    @POST
    @Path("counterSignAdd")
    @ApiOperation(value = "会签加签",notes = "测试")
    public OperateResult counterSignAdd(@QueryParam("actInstanceId") String actInstanceId, @QueryParam("taskActKey") String taskActKey, @QueryParam("userIds") String userIds) throws Exception;


    /**
     * 会签任务加签
     * @param actInstanceId 流程实例实际ID
     * @param  taskActKey 任务key
     * @param  userIds 用户id,以“，”分割
     * @return 操作结果
     */
    @POST
    @Path("counterSignDel")
    @ApiOperation(value = "会签减签",notes = "测试")
    OperateResult counterSignDel(@QueryParam("actInstanceId") String actInstanceId, @QueryParam("taskActKey") String taskActKey, @QueryParam("userIds") String userIds) throws Exception;

    /**
     * @return 操作结果
     */
    @GET
    @Path("getAllCanAddNodeInfoList")
    @ApiOperation(value = "获取当前用户所有可执行会签加签的操作节点列表",notes = "测试")
    public List<CanAddOrDelNodeInfo> getAllCanAddNodeInfoList() throws Exception;

    /**
     * @return 操作结果
     */
    @GET
    @Path("getAllCanDelNodeInfoList")
    @ApiOperation(value = "获取当前用户所有可执行会签减签的操作节点列表",notes = "测试")
    public List<CanAddOrDelNodeInfo> getAllCanDelNodeInfoList() throws Exception;

    /**
     * 委托任务完成后返回给委托人
     * @param taskId 任务ID
     * @return 操作结果
     */
    @POST
    @Path("taskTrustToReturn")
    @ApiOperation(value = "委托任务完成后返回给委托人",notes = "测试")
    public OperateResult taskTrustToReturn(@QueryParam("taskId") String taskId, @QueryParam("opinion") String opinion) throws Exception;

    /**
     *
     * @param actInstanceId 流程实例id
     * @param taskActKey 节点定义key
     * @return 执行人列表
     * @throws Exception
     */
    @GET
    @Path("getCounterSignExecutorList")
    @ApiOperation(value = "通过当前流程实例和对应节点key获取会签执行人列表",notes = "测试")
    public List<Executor> getCounterSignExecutorList(@QueryParam("actInstanceId") String actInstanceId, @QueryParam("taskActKey") String taskActKey) throws Exception;

    @POST
    @Path("reminding")
    @ApiOperation(value = "催办提醒定时任务接口",notes = "测试")
    public OperateResult reminding();

    /**
     * 获取指定用户的待办工作数量
     * @param executorId 执行人用户Id
     * @param searchConfig 查询参数
     * @return 待办工作的数量
     */
    @POST
    @Path("findCountByExecutorId")
    @ApiOperation(value = "获取指定用户的待办工作数量", notes = "通过执行人的用户Id获取待办工作数量，可以通过查询条件过滤")
    int findCountByExecutorId(@QueryParam("executorId") String executorId, Search searchConfig);

    /**
     * 通过Id获取一个待办任务(设置了办理任务URL)
     *
     * @param taskId 待办任务Id
     * @return 待办任务
     */
    @GET
    @Path("findTaskById")
    @ApiOperation(value = "获取一个待办任务", notes = "通过Id获取一个待办任务(设置了办理任务URL)")
    FlowTask findTaskById(@QueryParam("taskId") String taskId);



    /**
     * 通过业务模块code和业务模块id推送待办到业务模块
     *
     * @param businessModelCode 业务单据modelCode（非必填）
     * @param businessId 业务单据id（必填）
     * @param taskId     当前已办ID
     */
    @POST
    @Path("pushTaskToBusinessModel")
    @ApiOperation(value = "推送待办到业务模块", notes = "通过业务模块code和业务模块id推送待办到业务模块")
    @IgnoreCheckAuth
    void pushTaskToBusinessModel(@QueryParam("businessModelCode") String businessModelCode,@QueryParam("businessId") String businessId,@QueryParam("taskId") String taskId);


    @POST
    @Path("getExecutorsByRequestExecutorsVo")
    @ApiOperation(value = "获取执行人", notes = "通过执行人参数VO获取执行人")
    ResponseData  getExecutorsByRequestExecutorsVo(List<RequestExecutorsVo> requestExecutorsVos,@QueryParam("businessModelCode") String businessModelCode,@QueryParam("businessId") String businessId);


    /**
     * 推送待办到basic模块
     *
     * @param taskList 需要推送的待办
     */
    @POST
    @Path("pushNewTaskToBasic")
    @ApiOperation(value = "推送新生成的待办到basic模块", notes = "推送新生成的待办到basic模块")
    @IgnoreCheckAuth
    void pushNewTaskToBasic(List<FlowTask> taskList);

    /**
     * 推送新的已办到basic模块
     *
     * @param taskList 需要推送的已办（刚执行完成的）
     */
    @POST
    @Path("pushOldTaskToBasic")
    @ApiOperation(value = "推送新的已办到basic模块", notes = "推送新的已办到basic模块")
    @IgnoreCheckAuth
    void pushOldTaskToBasic(List<FlowTask> taskList);

    /**
     * 推送需要删除的待办到basic模块
     *
     * @param taskList 需要删除的待办
     */
    @POST
    @Path("pushDelTaskToBasic")
    @ApiOperation(value = "推送需要删除的待办到basic模块", notes = "推送需要删除的待办到basic模块")
    @IgnoreCheckAuth
    void pushDelTaskToBasic(List<FlowTask> taskList);

    /**
     * 推送需要归档（终止）的任务到basic模块
     *
     * @param task 需要终止的任务
     */
    @POST
    @Path("pushEndTaskToBasic")
    @ApiOperation(value = "推送需要归档（终止）的任务到basic模块", notes = "推送需要归档（终止）的任务到basic模块")
    @IgnoreCheckAuth
    void pushEndTaskToBasic(FlowTask task);

}
