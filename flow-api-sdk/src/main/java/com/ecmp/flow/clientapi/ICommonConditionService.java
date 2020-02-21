package com.ecmp.flow.clientapi;

import com.ecmp.flow.constant.FlowStatus;
import com.ecmp.flow.entity.FlowTask;
import io.swagger.annotations.ApiOperation;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;

/**
 * *************************************************************************************************
 * <p>
 * 实现功能：业务系统条件通用服务API接口定义
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
public interface ICommonConditionService {

    /**
     * 获取条件POJO属性说明
     * @param businessModelCode 业务实体代码
     * @param  all  是否查询全部
     * @return  POJO属性说明Map
     * @throws ClassNotFoundException 类找不到异常
     */
    @GET
    @Path("properties")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "通过业务实体代码获取条件POJO属性说明",notes = "测试")
    public Map<String, String> properties(@QueryParam("businessModelCode") String businessModelCode, @QueryParam("all") Boolean all) throws ClassNotFoundException;


    /**
     * 获取条件POJO属性初始化值键值对
     * @param businessModelCode 业务实体代码
     * @return  POJO属性说明Map
     * @throws ClassNotFoundException 类找不到异常
     * @throws InvocationTargetException 目标类解析异常
     * @throws InstantiationException 实例异常
     * @throws IllegalAccessException 访问异常
     * @throws NoSuchMethodException 没有方法异常
     */
    @GET
    @Path("initPropertiesAndValues")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "通过业务实体代码获取条件POJO属性初始化值键值对",notes = "测试")
    public Map<String, Object> initPropertiesAndValues(@QueryParam("businessModelCode") String businessModelCode) throws ClassNotFoundException, InvocationTargetException, InstantiationException, IllegalAccessException, NoSuchMethodException;


    /**
     * 获取条件POJO属性键值对
     * @param businessModelCode 业务实体代码
     * @param id 单据id
     * @return  POJO属性说明Map
     * @throws ClassNotFoundException 类找不到异常
     * @throws InvocationTargetException 目标类解析异常
     * @throws InstantiationException 实例异常
     * @throws IllegalAccessException 访问异常
     * @throws NoSuchMethodException 没有方法异常
     */
    @GET
    @Path("propertiesAndValues")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "通过业务实体代码,业务ID获取条件POJO属性键值对",notes = "测试")
    public Map<String,Object> propertiesAndValues(@QueryParam("businessModelCode") String businessModelCode, @QueryParam("id") String id, @QueryParam("all") Boolean all) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, ClassNotFoundException, InstantiationException;



    /**
     * 重置单据状态
     * @param businessModelCode   业务实体代码
     * @param id   单据id
     * @param status   状态
     * @return 返回结果
     * @throws ClassNotFoundException 类找不到异常
     * @throws InvocationTargetException 目标类解析异常
     * @throws InstantiationException 实例异常
     * @throws IllegalAccessException 访问异常
     * @throws NoSuchMethodException 没有方法异常
     */
    @POST
    @Path("resetState")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "通过业务实体代码及单据ID重置业务单据流程状态",notes = "测试")
    public Boolean resetState(@QueryParam("businessModelCode") String businessModelCode, @QueryParam("id") String id,
                              @QueryParam("status") FlowStatus status) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, ClassNotFoundException, InstantiationException;


    /**
     * 获取条件POJO属性键值对
     * @param businessModelCode 业务实体代码
     * @param id 单据id
     * @return  POJO属性说明Map
     */
    @GET
    @Path("formPropertiesAndValues")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "通过业务实体代码,业务ID获取POJO属性键值对",notes = "测试")
    public Map<String,Object> businessPropertiesAndValues(@QueryParam("businessModelCode") String businessModelCode, @QueryParam("id") String id) throws Exception;



    /**
     * 推送待办
     * @param  list 待办信息
     */
    @POST
    @Path("pushTasksToDo")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "推送待办",notes = "推送待办")
    default  String pushTasksToDo(List<FlowTask> list){
        return "推送成功！";
    }
}
