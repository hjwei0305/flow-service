package com.ecmp.flow.api;

import com.ecmp.core.search.PageResult;
import com.ecmp.core.search.Search;
import com.ecmp.flow.api.common.api.IBaseService;
import com.ecmp.flow.constant.FlowStatus;
import com.ecmp.flow.entity.FlowTask;
import com.ecmp.flow.vo.ApprovalHeaderVO;
import com.ecmp.flow.vo.FlowTaskCompleteVO;
import com.ecmp.flow.vo.NodeInfo;
import com.ecmp.flow.vo.TodoBusinessSummaryVO;
import com.ecmp.vo.OperateResult;
import com.ecmp.vo.OperateResultWithData;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.Map;

/**
 * *************************************************************************************************
 * <p/>
 * 实现功能：流程服务地址服务API接口定义
 * <p>
 * ------------------------------------------------------------------------------------------------
 * 版本          变更时间             变更人                     变更原因
 * ------------------------------------------------------------------------------------------------
 * 1.0.00      2017/3/31 11:39      谭军(tanjun)               新建
 * <p/>
 * *************************************************************************************************
 */
@Path("flowTask")
@Api(value = "IFlowTaskService 流程任务服务API接口")
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
    public OperateResult claim(@PathParam("id") String id, @PathParam("userId")String userId);

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
    public OperateResultWithData<FlowStatus> complete(FlowTaskCompleteVO flowTaskCompleteVO);


    /**
     * 撤回到指定任务节点
     * @param id
     * @return
     */
    @POST
    @Path("rollBackTo")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "撤回任务",notes = "测试")
    public  OperateResult rollBackTo(String id);

    /**
     * 驳回任务（动态驳回）
     * @param id 任务id
     * @param variables 参数
     * @return 操作结果
     */
    @POST
    @Path("reject/{id}/{opinion}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "驳回任务（动态驳回）",notes = "测试")
    public OperateResult taskReject(@PathParam("id")String id,@PathParam("opinion")String opinion, Map<String, Object> variables);

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

    /**
     * 通过任务Id检查当前任务的出口节点是否存在条件表达式
     * @param id 任务id
     * @return 结果
     */
    @GET
    @Path("checkHasConditon/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "检查当前任务的出口节点是否存在条件表达式",notes = "测试")
    public boolean checkHasConditon(@PathParam("id")String id);


    /**
     * 选择下一步执行的节点信息
     * @param id 任务ID
     * @param businessId 业务ID
     * @return
     * @throws NoSuchMethodException
     */
    @POST
    @Path("findNextNodesWithBusinessId/{id}/{businessId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "选择下一步执行的节点信息",notes = "测试")
    public List<NodeInfo> findNextNodes(@PathParam("id")String id, @PathParam("businessId")String businessId) throws NoSuchMethodException;

    /**
     * 根据流程实例ID查询待办
     * @param instanceId
     * @return
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
     * @return
     * @throws NoSuchMethodException
     */
    @GET
    @Path("findNextNodes/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "选择下一步执行的节点信息",notes = "测试")
    public List<NodeInfo> findNextNodes(@PathParam("id")String id) throws NoSuchMethodException;


    /**
     * 通过任务ID与业务ID选择下一步带用户信息的执行的节点信息
     * @param id 任务ID
     * @param businessId 业务ID
     * @return
     * @throws NoSuchMethodException
     */
    @GET
    @Path("findNexNodesByIdAndBusinessIdWithUserSet/{id}/{businessId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "通过任务ID与业务ID选择下一步执行的节点信息(带用户信息)",notes = "测试")
    public List<NodeInfo> findNexNodesWithUserSet(@PathParam("id")String id, @PathParam("businessId")String businessId) throws NoSuchMethodException;


    /**
     * 只通过任务ID选择下一步带用户信息的执行的节点信息
     * @param id 任务ID
     * @return
     * @throws NoSuchMethodException
     */
    @GET
    @Path("findNexNodesByIdWithUserSet/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "只通过任务ID选择下一步执行的节点信息(带用户信息)",notes = "测试")
    public List<NodeInfo> findNexNodesWithUserSet(@PathParam("id")String id) throws NoSuchMethodException;


    /**
     * 只通过任务ID选择下一步带用户信息的执行的节点信息
     * @param id 任务ID
     * @param includeNodeIds 只包含此节点
     * @return
     * @throws NoSuchMethodException
     */
    @POST
    @Path("findNexNodesByIdWithUserSetAndNodeIds/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "通过任务ID选择下一步执行的节点信息(带用户信息)",notes = "测试")
    public List<NodeInfo> findNexNodesWithUserSet(@PathParam("id")String id,List<String> includeNodeIds) throws NoSuchMethodException;

    /**
     *
     * @param id
     * @return
     */
    @GET
    @Path("getApprovalHeaderVO/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "选择当前流程抬头信息",notes = "测试")
   public ApprovalHeaderVO getApprovalHeaderVO(@PathParam("id")String id);

    @GET
    @Path("findTaskSumHeader")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "获取待办汇总信息",notes = "测试")
    public List<TodoBusinessSummaryVO> findTaskSumHeader();

    @POST
    @Path("findByBusinessModelId/{businessModelId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "获取待办汇总信息",notes = "测试")
    public PageResult<FlowTask> findByBusinessModelId(@PathParam("businessModelId") String businessModelId, Search searchConfig);
}
