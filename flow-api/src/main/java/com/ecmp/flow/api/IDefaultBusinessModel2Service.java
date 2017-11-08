package com.ecmp.flow.api;

import com.ecmp.core.api.IBaseEntityService;
import com.ecmp.flow.basic.vo.Executor;
import com.ecmp.flow.entity.DefaultBusinessModel2;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

/**
 * *************************************************************************************************
 * <p>
 * 实现功能：采购业务表单服务API接口定义
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
@Path("defaultBusinessModel2")
@Api(value = "IDefaultBusinessModel2Service 采购业务表单服务API接口")
public interface IDefaultBusinessModel2Service extends IBaseEntityService<DefaultBusinessModel2> {
//
//    /**
//     * 保存一个实体
//     * @param defaultBusinessModel2 实体
//     * @return 保存后的实体
//     */
//    @POST
//    @Path("save")
//    @Consumes(MediaType.APPLICATION_JSON)
//    @Produces(MediaType.APPLICATION_JSON)
//    @ApiOperation(value = "保存实体",notes = "测试 保存实体")
//    OperateResultWithData<DefaultBusinessModel2> save(DefaultBusinessModel2 defaultBusinessModel2);
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
//    PageResult<DefaultBusinessModel2> findByPage(Search searchConfig);

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
    @ApiOperation(value = "根据业务单据Id获取执行人",notes = "测试 根据业务单据Id获取执行人")
    public List<Executor> getPersonToExecutorConfig(@QueryParam("businessId") String businessId, @QueryParam("paramJson")String paramJson);

}
