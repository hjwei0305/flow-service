package com.ecmp.flow.api;

import com.ecmp.core.api.IBaseEntityService;
import com.ecmp.flow.entity.CommonContactGroup;
import com.ecmp.vo.OperateResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;


/**
 * *************************************************************************************************
 * <p/>
 * 实现功能：常用联系组
 * <p>
 * ------------------------------------------------------------------------------------------------
 * 版本          变更时间             变更人                     变更原因
 * ------------------------------------------------------------------------------------------------
 * 1.0.00      2020/08/24          何灿坤(AK)                  新建
 * <p/>
 * *************************************************************************************************
 */
@Path("commonContactGroup")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Api(value = "ICommonContactGroupService 常用联系组服务API接口")
public interface ICommonContactGroupService extends IBaseEntityService<CommonContactGroup> {


    /**
     * 获取当前用户所有常用联系组
     *
     * @return 实体清单
     */
    @GET
    @Path("getAllGroupByUser")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "获取当前用户所有常用联系组", notes = "获取当前用户所有常用联系组")
    List<CommonContactGroup> getAllGroupByUser();


    /**
     * 删除常用联系组
     *
     * @param id 业务实体Id
     * @return 操作结果
     */
    @DELETE
    @Path("delete")
    @ApiOperation(value = "删除常用联系组", notes = "删除常用联系组")
    OperateResult delete(String id);



}
