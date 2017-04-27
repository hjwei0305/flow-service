package com.ecmp.flow.api.common.api;

import com.ecmp.flow.entity.IConditionPojo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

/**
 * *************************************************************************************************
 * <p/>
 * 实现功能： 流程条件API定义
 * <p>
 * ------------------------------------------------------------------------------------------------
 * 版本          变更时间             变更人                     变更原因
 * ------------------------------------------------------------------------------------------------
 * 1.0.00      2017/4/26 13:19      谭军(tanjun)                    新建
 * <p/>
 * *************************************************************************************************
 */
@Path("condition")
@Api(value = "IConditionServer 条件通用服务API接口")
public interface IConditionServer {

    /**
     * 获取条件POJO属性说明
     * @param conditonPojoClassName 条件POJO类名
     * @return  POJO属性说明Map
     * @throws ClassNotFoundException
     */
    @GET
    @Path("properties/{conditonPojoClassName}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "获取条件POJO属性说明",notes = "测试")
    public Map<String,String> getPropertiesForConditionPojo(@PathParam("conditonPojoClassName") String conditonPojoClassName) throws ClassNotFoundException;

    /**
     * 获取条件POJO属性说明及初始化值
     * @param conditonPojoClassName
     * @return  获取条件POJO属性说明及初始化值Map
     * @throws ClassNotFoundException
     * @throws InvocationTargetException
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    @GET
    @Path("propertiesAndValues/{conditonPojoClassName}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "获取条件POJO属性及初始化值说明",notes = "测试")
    public Map<String,Object> getPropertiesAndValues(@PathParam("conditonPojoClassName") String conditonPojoClassName) throws ClassNotFoundException, InvocationTargetException, InstantiationException, IllegalAccessException, NoSuchMethodException;


    /**
     * 通过Id获取条件POJO的值
     * @param conditonPojoClassName 条件pojo类名
     * @param daoBeanName  对应的dao层BeanName
     * @param id  业务ID
     * @return POJO的值
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     * @throws ClassNotFoundException
     * @throws InstantiationException
     */
    @GET
    @Path("conditonPojoMap/{conditonPojoClassName}/{daoBeanName}/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "获取条件POJO的值",notes = "测试")
    public Map<String,Object> getConditonPojoMap(@PathParam("conditonPojoClassName") String conditonPojoClassName,@PathParam("daoBeanName") String daoBeanName,@PathParam("id") String id) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, ClassNotFoundException, InstantiationException;


}
