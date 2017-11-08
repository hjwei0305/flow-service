package com.ecmp.flow.api;

import com.ecmp.core.api.IBaseEntityService;
import com.ecmp.core.api.IFindByPageService;
import com.ecmp.flow.basic.vo.Executor;
import com.ecmp.flow.entity.DefaultBusinessModel3;
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
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Api(value = "IDefaultBusinessModel3Service 销售业务表单服务API接口")
public interface IDefaultBusinessModel3Service extends IBaseEntityService<DefaultBusinessModel3>,IFindByPageService<DefaultBusinessModel3> {

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
