package com.ecmp.flow.api;

import com.ecmp.flow.basic.vo.Executor;
import com.ecmp.core.search.PageResult;
import com.ecmp.core.search.Search;
import com.ecmp.flow.api.common.api.IBaseService;
import com.ecmp.flow.entity.DefaultBusinessModel3;
import com.ecmp.vo.OperateResultWithData;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

/**
 * *************************************************************************************************
 * <p>
 * 实现功能：销售业务表单服务API接口定义
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
@Path("defaultBusinessModel3")
@Api(value = "IDefaultBusinessModel3Service 销售业务表单服务API接口")
public interface IDefaultBusinessModel3Service extends IBaseService<DefaultBusinessModel3, String> {

//    /**
//     * 保存一个实体
//     * @param defaultBusinessModel3 实体
//     * @return 保存后的实体
//     */
//    @POST
//    @Path("save")
//    @Consumes(MediaType.APPLICATION_JSON)
//    @Produces(MediaType.APPLICATION_JSON)
//    @ApiOperation(value = "保存实体",notes = "测试 保存实体")
//    OperateResultWithData<DefaultBusinessModel3> save(DefaultBusinessModel3 defaultBusinessModel3);
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
//    PageResult<DefaultBusinessModel3> findByPage(Search searchConfig);

    /**
     * 测试自定义执行人选择
     * @param businessId 业务单据id
     * @param paramJson  json参数
     * @return 执行人列表
     */
    @POST
    @Path("getPersonToExecutorConfig")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "根据组织Id获取excutor",notes = "测试 根据组织Id获取excutor")
    public List<Executor> getPersonToExecutorConfig(@QueryParam("businessId") String businessId, @QueryParam("paramJson")String paramJson);

}
