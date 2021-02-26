package com.ecmp.flow.api;

import com.ecmp.core.api.IBaseEntityService;
import com.ecmp.core.api.IFindByPageService;
import com.ecmp.flow.constant.FlowStatus;
import com.ecmp.flow.entity.DefaultBusinessModel;
import com.ecmp.flow.vo.FlowInvokeParams;
import com.ecmp.flow.vo.FlowOperateResult;
import com.ecmp.vo.ResponseData;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;


@Path("defaultBusinessModel")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Api(value = "IDefaultBusinessModelService 默认业务表单服务API接口")
public interface IDefaultBusinessModelService extends IBaseEntityService<DefaultBusinessModel>, IFindByPageService<DefaultBusinessModel> {


    /**
     * 获取条件POJO属性说明
     *
     * @param businessModelCode 业务实体代码
     * @param all               是否查询全部
     * @return POJO属性说明Map
     * @throws ClassNotFoundException 类找不到异常
     */
    @GET
    @Path("properties")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "通过业务实体代码获取条件POJO属性说明", notes = "测试")
    ResponseData properties(@QueryParam("businessModelCode") String businessModelCode, @QueryParam("all") Boolean all) throws ClassNotFoundException;


    /**
     * 获取条件属性的备注说明
     *
     * @param businessModelCode 业务实体代码
     * @throws ClassNotFoundException 类找不到异常
     */
    @GET
    @Path("propertiesRemark")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "获取条件属性的备注说明", notes = "获取条件属性的备注说明")
    ResponseData propertiesRemark(@QueryParam("businessModelCode") String businessModelCode) throws ClassNotFoundException;


    /**
     * 获取条件POJO属性初始化值键值对
     *
     * @param businessModelCode 业务实体代码
     * @return POJO属性说明Map
     * @throws ClassNotFoundException    类找不到异常
     * @throws InvocationTargetException 目标类解析异常
     * @throws InstantiationException    实例异常
     * @throws IllegalAccessException    访问异常
     * @throws NoSuchMethodException     没有方法异常
     */
    @GET
    @Path("initPropertiesAndValues")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "通过业务实体代码获取条件POJO属性初始化值键值对", notes = "测试")
    ResponseData initPropertiesAndValues(@QueryParam("businessModelCode") String businessModelCode) throws ClassNotFoundException, InvocationTargetException, InstantiationException, IllegalAccessException, NoSuchMethodException;


    /**
     * 获取条件POJO属性键值对
     *
     * @param businessModelCode 业务实体代码
     * @param id                单据id
     * @return POJO属性说明Map
     * @throws ClassNotFoundException    类找不到异常
     * @throws InvocationTargetException 目标类解析异常
     * @throws InstantiationException    实例异常
     * @throws IllegalAccessException    访问异常
     * @throws NoSuchMethodException     没有方法异常
     */
    @GET
    @Path("propertiesAndValues")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "通过业务实体代码,业务ID获取条件POJO属性键值对", notes = "测试")
    ResponseData propertiesAndValues(@QueryParam("businessModelCode") String businessModelCode, @QueryParam("id") String id, @QueryParam("all") Boolean all) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, ClassNotFoundException, InstantiationException;


    /**
     * 重置单据状态
     *
     * @param businessModelCode 业务实体代码
     * @param id                单据id
     * @param status            状态
     * @return 返回结果
     * @throws ClassNotFoundException    类找不到异常
     * @throws InvocationTargetException 目标类解析异常
     * @throws InstantiationException    实例异常
     * @throws IllegalAccessException    访问异常
     * @throws NoSuchMethodException     没有方法异常
     */
    @POST
    @Path("resetState")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "通过业务实体代码及单据ID重置业务单据流程状态", notes = "测试")
    ResponseData resetState(@QueryParam("businessModelCode") String businessModelCode, @QueryParam("id") String id,
                            @QueryParam("status") FlowStatus status) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, ClassNotFoundException, InstantiationException;


    /**
     * 测试事前
     */
    @POST
    @Path("changeCreateDepict")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "测试事件", notes = "测试事件")
    String changeCreateDepict(@QueryParam("id") String id, @QueryParam("paramJson") String paramJson);

    /**
     * 测试事后
     */
    @POST
    @Path("changeCompletedDepict")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "测试事件", notes = "测试事件")
    String changeCompletedDepict(@QueryParam("id") String id, @QueryParam("paramJson") String paramJson);

    /**
     * 测试自定义执行人选择
     */
    @POST
    @Path("getPersonToExecutorConfig")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "测试自定义执行人选择", notes = "测试自定义执行人选择")
    ResponseData getPersonToExecutorConfig(FlowInvokeParams flowInvokeParams);


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


    @GET
    @Path("checkStartFlow")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "测试checkStartFlow", notes = "测试checkStartFlow")
    boolean checkStartFlow(@QueryParam("id") String id);

    @POST
    @Path("endCall")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "测试endCall", notes = "endCall")
    void endCall(@QueryParam("id") String id);

    @POST
    @Path("newServiceCall")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "测试endCall", notes = "endCall")
    FlowOperateResult newServiceCall(FlowInvokeParams flowInvokeParams);


    @POST
    @Path("newServiceCallFailure")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "测试异常服务", notes = "failureCall")
    ResponseData newServiceCallFailure(FlowInvokeParams flowInvokeParams);

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
    @ApiOperation(value = "测试changeCreateDepictNew", notes = "changeCreateDepictNew")
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
    @ApiOperation(value = "测试changeCompletedDepictNew", notes = "changeCompletedDepictNew")
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
    @ApiOperation(value = "changeProperties", notes = "changeProperties")
    ResponseData changeProperties(FlowInvokeParams flowInvokeParams);


    @GET
    @Path("testPJoin")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "testPJoin", notes = "testPJoin")
    Map<String, Object> businessPropertiesAndValues(@QueryParam("businessModelCode") String businessModelCode, @QueryParam("id") String id) throws Exception;

    @POST
    @Path("testPoolTaskComplete")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "测试testPoolTaskComplete", notes = "testPoolTaskComplete")
    FlowOperateResult testPoolTaskComplete(FlowInvokeParams flowInvokeParams);

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
    @ApiOperation(value = "测试testPoolTaskSignal", notes = "testPoolTaskSignal")
    ResponseData testPoolTaskSignal(FlowInvokeParams flowInvokeParams);


    @POST
    @Path("testPoolTaskCreatePool")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "testPoolTaskCreatePool", notes = "testPoolTaskCreatePool")
    FlowOperateResult testPoolTaskCreatePool(FlowInvokeParams flowInvokeParams);



}
