package com.ecmp.flow.api;

import com.ecmp.core.api.IBaseEntityService;
import com.ecmp.core.api.IFindByPageService;
import com.ecmp.flow.basic.vo.Executor;
import com.ecmp.flow.constant.FlowStatus;
import com.ecmp.flow.entity.DefaultBusinessModel;
import com.ecmp.flow.vo.FlowInvokeParams;
import com.ecmp.vo.ResponseData;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.Map;


@Path("defaultBusinessModel")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Api(value = "IDefaultBusinessModelService 默认业务表单服务API接口")
public interface IDefaultBusinessModelService extends IBaseEntityService<DefaultBusinessModel>, IFindByPageService<DefaultBusinessModel> {


    /**
     * 获取业务实体条件属性说明
     *
     * @param businessModelCode 业务实体类路径
     * @param all               是否查询全部
     * @return 条件属性说明
     */
    @GET
    @Path("properties")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "获取业务实体条件属性说明", notes = "获取业务实体条件属性说明")
    ResponseData<Map<String, String>> properties(@QueryParam("businessModelCode") String businessModelCode, @QueryParam("all") Boolean all);


    /**
     * 获取业务实体条件属性值
     *
     * @param businessModelCode 业务实体类路径
     * @param id                单据id
     * @param all               是否查询全部
     * @return 条件属性值
     */
    @GET
    @Path("propertiesAndValues")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "获取业务实体条件属性值", notes = "获取业务实体条件属性值")
    ResponseData<Map<String, Object>> propertiesAndValues(@QueryParam("businessModelCode") String businessModelCode, @QueryParam("id") String id, @QueryParam("all") Boolean all);


    /**
     * 获取业务实体条件属性初始值
     *
     * @param businessModelCode 业务实体类路径
     * @return 条件属性初始值
     */
    @GET
    @Path("initPropertiesAndValues")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "获取业务实体条件属性初始值", notes = "获取业务实体条件属性初始值")
    ResponseData<Map<String, Object>> initPropertiesAndValues(@QueryParam("businessModelCode") String businessModelCode);


    /**
     * 重置业务单据状态
     *
     * @param businessModelCode 业务实体类路径
     * @param id                单据id
     * @param status            状态（init:初始化状态、inProcess：流程中、completed：流程处理完成）
     * @return 返回结果
     */
    @POST
    @Path("resetState")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "重置业务单据状态", notes = "重置业务单据状态")
    ResponseData<Boolean> resetState(@QueryParam("businessModelCode") String businessModelCode, @QueryParam("id") String id, @QueryParam("status") FlowStatus status);


    /**
     * 获取条件属性的备注说明
     *
     * @param businessModelCode 业务实体类路径
     * @return 条件属性备注说明
     */
    @GET
    @Path("propertiesRemark")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "获取条件属性的备注说明", notes = "获取条件属性的备注说明")
    ResponseData<Map<String, String>> propertiesRemark(@QueryParam("businessModelCode") String businessModelCode);















    /**
     * 流程事前事件测试
     *
     * @param flowInvokeParams
     * @return
     */
    @POST
    @Path("changeCreateDepictNew")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "流程事前事件测试", notes = "流程事前事件测试")
    ResponseData changeCreateDepictNew(FlowInvokeParams flowInvokeParams);


    /**
     * 流程事后事件测试
     *
     * @param flowInvokeParams
     * @return
     */
    @POST
    @Path("changeCompletedDepictNew")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "流程事后事件测试", notes = "流程事后事件测试")
    ResponseData changeCompletedDepictNew(FlowInvokeParams flowInvokeParams);




    /**
     * 改变属性
     *
     * @param flowInvokeParams
     * @return
     */
    @POST
    @Path("changeProperties")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "改变属性", notes = "改变属性")
    ResponseData changeProperties(FlowInvokeParams flowInvokeParams);


    /**
     * 报异常的方法
     * @param flowInvokeParams
     * @return
     */
    @POST
    @Path("newServiceCallFailure")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "报异常的方法", notes = "报异常的方法")
    ResponseData newServiceCallFailure(FlowInvokeParams flowInvokeParams);


    /**
     * 工作池任务测试
     *
     * @param flowInvokeParams
     * @return
     */
    @POST
    @Path("testPoolTaskSignal")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "工作池任务测试", notes = "工作池任务测试")
    ResponseData testPoolTaskSignal(FlowInvokeParams flowInvokeParams);


    /**
     * 接收任务（不触发）
     */
    @POST
    @Path("testReceiveCall")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "接收任务（不触发）", notes = "接收任务（不触发）")
    ResponseData testReceiveCall(FlowInvokeParams flowInvokeParams);

    /**
     * 接收任务（触发）
     */
    @POST
    @Path("testReceiveCallNew")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "接收任务（触发）", notes = "接收任务（触发）")
    ResponseData testReceiveCallNew(FlowInvokeParams flowInvokeParams);

    /**
     * 测试自定义执行人选择
     */
    @POST
    @Path("getPersonToExecutorConfig")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "测试自定义执行人选择", notes = "测试自定义执行人选择")
    ResponseData<List<Executor>> getPersonToExecutorConfig(FlowInvokeParams flowInvokeParams);


    /**
     * 表单明细接口（移动端）
     * @param businessModelCode
     * @param id
     * @return
     */
    @GET
    @Path("testPJoin")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "表单明细接口（移动端）", notes = "表单明细接口（移动端）")
    ResponseData<Map<String, Object>> businessPropertiesAndValues(@QueryParam("businessModelCode") String businessModelCode, @QueryParam("id") String id);



}
