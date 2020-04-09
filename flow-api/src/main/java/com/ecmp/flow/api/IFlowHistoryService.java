package com.ecmp.flow.api;

import com.ecmp.core.search.PageResult;
import com.ecmp.core.search.Search;
import com.ecmp.flow.api.common.api.IBaseService;
import com.ecmp.flow.entity.FlowHistory;
import com.ecmp.flow.vo.phone.FlowHistoryPhoneVo;
import com.ecmp.vo.OperateResultWithData;
import com.ecmp.vo.ResponseData;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

/**
 * *************************************************************************************************
 * <p>
 * 实现功能：流程历史服务API接口定义
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
@Path("flowHistory")
@Api(value = "IFlowHistoryService 流程历史服务API接口")
public interface IFlowHistoryService extends IBaseService<FlowHistory, String> {

    /**
     * 保存一个实体
     *
     * @param flowHistory 实体
     * @return 保存后的实体
     */
    @POST
    @Path("save")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "保存实体", notes = "测试 保存实体")
    OperateResultWithData<FlowHistory> save(FlowHistory flowHistory);

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
    PageResult<FlowHistory> findByPage(Search searchConfig);

    /**
     * 根据流程实例ID查询流程历史
     *
     * @param instanceId 实例id
     * @return 执行历史
     */
    @POST
    @Path("findByInstanceId")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "查询流程历史", notes = "查询流程历史")
    List<FlowHistory> findByInstanceId(String instanceId);

    /**
     * 获取分页数据
     *
     * @return 实体清单
     */
    @POST
    @Path("findByPageAndUser")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "获取分页数据", notes = "测试 获取分页数据")
    PageResult<FlowHistory> findByPageAndUser(Search searchConfig);


    /**
     * 查询流程已办汇总列表
     *
     * @return ResponseData.data是 List<TodoBusinessSummaryVO>
     */
    @POST
    @Path("listFlowHistoryHeader")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "查询流程已办汇总列表", notes = "查询流程已办汇总列表")
    ResponseData listFlowHistoryHeader(@QueryParam("dataType") String dataType);

    /**
     * 获取已办信息
     *
     * @param businessModelId 业务实体id
     * @param searchConfig    查询条件
     * @return 已办汇总信息
     */
    @POST
    @Path("listFlowHistory")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "已办汇总信息", notes = "已办汇总信息")
    ResponseData listFlowHistory(@QueryParam("businessModelId") String businessModelId, Search searchConfig);


    /**
     * 获取已办信息(关联待办执行人)
     *
     * @param businessModelId 业务实体id
     * @param searchConfig    查询条件
     * @return 已办汇总信息
     */
    @POST
    @Path("listFlowHistoryAndExecutor")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "已办汇总信息（关联当前执行人）", notes = "已办汇总信息（关联当前执行人）")
    ResponseData listFlowHistoryAndExecutor(@QueryParam("businessModelId") String businessModelId, Search searchConfig);


    /**
     * 获取待办汇总信息
     *
     * @param businessModelId 业务实体id
     * @param searchConfig    查询条件
     * @return 待办汇总信息
     */
    @POST
    @Path("findByBusinessModelId")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "获取待办汇总信息", notes = "测试")
    public PageResult<FlowHistory> findByBusinessModelId(@QueryParam("businessModelId") String businessModelId, Search searchConfig);


    /**
     * 获取已办汇总信息（最新移动端专用）
     *
     * @param businessModelId 业务实体id
     * @param property        需要排序的字段
     * @param direction       排序规则
     * @param page            当前页数
     * @param rows            每页条数
     * @param quickValue      模糊查询字段内容
     * @return 待办汇总信息
     */
    @POST
    @Path("findByBusinessModelIdOfMobile")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "获取已办汇总信息（最新移动端专用）", notes = "获取已办汇总信息（最新移动端专用）")
    PageResult<FlowHistoryPhoneVo> findByBusinessModelIdOfMobile(
            @QueryParam("businessModelId") String businessModelId,
            @QueryParam("property") String property,
            @QueryParam("direction") String direction,
            @QueryParam("page") int page,
            @QueryParam("rows") int rows,
            @QueryParam("quickValue") String quickValue);


    /**
     * 获取已办汇总信息（移动端专用）
     *
     * @param businessModelId 业务实体id
     * @param property        需要排序的字段
     * @param direction       排序规则
     * @param page            当前页数
     * @param rows            每页条数
     * @param quickValue      模糊查询字段内容
     * @return 待办汇总信息
     */
    @POST
    @Path("findByBusinessModelIdOfPhone")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "获取已办汇总信息（移动端专用）", notes = "获取已办汇总信息（移动端专用）")
    PageResult<FlowHistory> findByBusinessModelIdOfPhone(
            @QueryParam("businessModelId") String businessModelId,
            @QueryParam("property") String property,
            @QueryParam("direction") String direction,
            @QueryParam("page") int page,
            @QueryParam("rows") int rows,
            @QueryParam("quickValue") String quickValue);


}
