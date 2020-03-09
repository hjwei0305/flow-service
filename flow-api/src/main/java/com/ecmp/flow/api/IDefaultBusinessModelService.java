package com.ecmp.flow.api;

import com.ecmp.core.api.IBaseEntityService;
import com.ecmp.core.api.IFindByPageService;
import com.ecmp.flow.basic.vo.Executor;
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
import java.util.List;
import java.util.Map;

/**
 * *************************************************************************************************
 * <p>
 * 实现功能： 默认业务表单服务API接口定义
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
     *
     * @param id        单据id
     * @param paramJson json参数
     * @return 执行结果
     */
    @POST
    @Path("changeCreateDepict")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "测试事件", notes = "测试事件")
    String changeCreateDepict(@QueryParam("id") String id, @QueryParam("paramJson") String paramJson);

    /**
     * 测试事后
     *
     * @param id        单据id
     * @param paramJson json参数
     * @return 执行结果
     */
    @POST
    @Path("changeCompletedDepict")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "测试事件", notes = "测试事件")
    String changeCompletedDepict(@QueryParam("id") String id, @QueryParam("paramJson") String paramJson);

    /**
     * 测试自定义执行人选择
     *
     * @param flowInvokeParams 流程参数
     * @return 执行结果
     */
    @POST
    @Path("getPersonToExecutorConfig")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "自定义获取excutor", notes = "测试 自定义获取excutor")
    List<Executor> getPersonToExecutorConfig(FlowInvokeParams flowInvokeParams);


    /**
     * 接收任务测试接口
     *
     * @param id         业务单据id
     * @param changeText 参数文本
     * @return 结果
     */
    @POST
    @Path("testReceiveCall")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "测试ReceiveCall", notes = "测试ReceiveCall")
    boolean testReceiveCall(@QueryParam("id") String id, @QueryParam("paramJson") String changeText);


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
    FlowOperateResult newServiceCallFailure(FlowInvokeParams flowInvokeParams);

    @POST
    @Path("changeCreateDepictNew")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "测试changeCreateDepictNew", notes = "changeCreateDepictNew")
    FlowOperateResult changeCreateDepictNew(FlowInvokeParams flowInvokeParams);

    @POST
    @Path("changeCompletedDepictNew")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "测试changeCompletedDepictNew", notes = "changeCompletedDepictNew")
    FlowOperateResult changeCompletedDepictNew(FlowInvokeParams flowInvokeParams);


    @POST
    @Path("testReceiveCallNew")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "测试testReceiveCallNew", notes = "testReceiveCallNew")
    FlowOperateResult testReceiveCallNew(FlowInvokeParams flowInvokeParams);

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

    @POST
    @Path("testPoolTaskSignal")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "测试testPoolTaskSignal", notes = "testPoolTaskSignal")
    FlowOperateResult testPoolTaskSignal(FlowInvokeParams flowInvokeParams);


    @POST
    @Path("testPoolTaskCreatePool")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "testPoolTaskCreatePool", notes = "testPoolTaskCreatePool")
    FlowOperateResult testPoolTaskCreatePool(FlowInvokeParams flowInvokeParams);

}
