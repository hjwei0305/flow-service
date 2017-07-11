package com.ecmp.flow.api;

import com.ecmp.basic.entity.Employee;
import com.ecmp.basic.entity.vo.Executor;
import com.ecmp.core.search.PageResult;
import com.ecmp.core.search.Search;
import com.ecmp.flow.api.common.api.IBaseService;
import com.ecmp.flow.entity.DefaultBusinessModel;
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
 * 实现功能：默认业务表单服务API接口定义
 * <p>
 * ------------------------------------------------------------------------------------------------
 * 版本          变更时间             变更人                     变更原因
 * ------------------------------------------------------------------------------------------------
 * 1.0.00      2017/3/26 10:39      谭军(tanjun)               新建
 * <p/>
 * *************************************************************************************************
 */
@Path("defaultBusinessModel")
@Api(value = "IDefaultBusinessModelService 默认业务表单服务API接口")
public interface IDefaultBusinessModelService extends IBaseService<DefaultBusinessModel, String> {

//    /**
//     * 保存一个实体
//     * @param defaultBusinessModel 实体
//     * @return 保存后的实体
//     */
//    @POST
//    @Path("save")
//    @Consumes(MediaType.APPLICATION_JSON)
//    @Produces(MediaType.APPLICATION_JSON)
//    @ApiOperation(value = "保存实体",notes = "测试 保存实体")
//    OperateResultWithData<DefaultBusinessModel> save(DefaultBusinessModel defaultBusinessModel);
//
//    /**
//     * 获取分页数据
//     *
//     * @return 实体清单
//     */
//    @POST
//    @Path("findByPage")
//    @Produces(MediaType.APPLICATION_JSON)
//    @Consumes(MediaType.APPLICATION_JSON)
//    @ApiOperation(value = "获取分页数据", notes = "测试 获取分页数据")
//    PageResult<DefaultBusinessModel> findByPage(Search searchConfig);

   /**
     * 测试事前
     *
     * @return 执行结果
     */
    @GET
    @Path("changeCreateDepict/{id}/{changeText}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "获取分页数据", notes = "测试 获取分页数据")
    public boolean changeCreateDepict(@PathParam("id") String id,@PathParam("changeText") String changeText);

    /**
     * 测试事后
     *
     * @return 执行结果
     */
    @GET
    @Path("changeCompletedDepict/{id}/{changeText}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "获取分页数据", notes = "测试 获取分页数据")
    public boolean changeCompletedDepict(@PathParam("id") String id,@PathParam("changeText") String changeText);

    /**
     * 测试自定义执行人选择
     *
     * @return 执行结果
     */
    @POST
    @Path("getPersonToExecutorConfig")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "根据组织Id获取excutor",notes = "测试 根据组织Id获取excutor")
    public List<Executor> getPersonToExecutorConfig(@QueryParam("businessId") String businessId,@QueryParam("paramJson")String paramJson);

}
