package com.ecmp.flow.api;

import com.ecmp.core.api.IBaseEntityService;
import com.ecmp.core.api.IFindByPageService;
import com.ecmp.core.search.PageResult;
import com.ecmp.core.search.Search;
import com.ecmp.flow.basic.vo.Executor;
import com.ecmp.flow.entity.DefaultBusinessModel;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

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
public interface IDefaultBusinessModelService extends IBaseEntityService<DefaultBusinessModel>,IFindByPageService<DefaultBusinessModel>{


   /**
     * 测试事前
     * @param id 单据id
    *  @param    paramJson json参数
     * @return 执行结果
     */
    @POST
    @Path("changeCreateDepict")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "测试事件", notes = "测试事件")
    public String changeCreateDepict(@QueryParam("id") String id, @QueryParam("paramJson") String paramJson);

    /**
     * 测试事后
     * @param id 单据id
     * @param    paramJson json参数
     * @return 执行结果
     */
    @POST
    @Path("changeCompletedDepict")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "测试事件", notes = "测试事件")
    public String  changeCompletedDepict(@QueryParam("id") String id,@QueryParam("paramJson") String paramJson);

    /**
     * 测试自定义执行人选择
     * @param businessId 单据id
     * @param    paramJson json参数
     * @return 执行结果
     */
    @POST
    @Path("getPersonToExecutorConfig")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "根据组织Id获取excutor",notes = "测试 根据组织Id获取excutor")
    public List<Executor> getPersonToExecutorConfig(@QueryParam("businessId") String businessId,@QueryParam("paramJson")String paramJson);


 /**
  * 接收任务测试接口
  * @param id  业务单据id
  * @param changeText   参数文本
  * @return 结果
  */
 @POST
 @Path("testReceiveCall")
 @Produces(MediaType.APPLICATION_JSON)
 @Consumes(MediaType.APPLICATION_JSON)
 @ApiOperation(value = "测试ReceiveCall",notes = "测试ReceiveCall")
 public boolean testReceiveCall(@QueryParam("id")String id,@QueryParam("paramJson")String changeText);

//
// /**
//  * 分页查询业务实体
//  *
//  * @param search 查询参数
//  * @return 分页查询结果
//  */
// @POST
// @Produces(MediaType.APPLICATION_JSON)
// @Consumes(MediaType.APPLICATION_JSON)
// @Path("findByPage")
// @ApiOperation(value = "分页查询业务实体", notes = "分页查询业务实体")
//public PageResult<DefaultBusinessModel> findByPage(Search search);

}
