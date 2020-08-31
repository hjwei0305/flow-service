package com.ecmp.flow.api;

import com.ecmp.core.api.IBaseEntityService;
import com.ecmp.flow.entity.CommonContactPeople;
import com.ecmp.vo.ResponseData;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;


/**
 * *************************************************************************************************
 * <p/>
 * 实现功能：常用联系人
 * <p>
 * ------------------------------------------------------------------------------------------------
 * 版本          变更时间             变更人                     变更原因
 * ------------------------------------------------------------------------------------------------
 * 1.0.00      2020/08/24          何灿坤(AK)                  新建
 * <p/>
 * *************************************************************************************************
 */
@Path("commonContactPeople")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Api(value = "ICommonContactPeopleService 常用联系人服务API接口")
public interface ICommonContactPeopleService extends IBaseEntityService<CommonContactPeople> {



    /**
     * 通过常用联系组请求常用联系人
     *
     * @return 实体清单
     */
    @GET
    @Path("findCommonContactPeopleByGroupId")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "通过常用联系组请求常用联系人", notes = "通过常用联系组请求常用联系人")
    List<CommonContactPeople> findCommonContactPeopleByGroupId(@QueryParam("commonContactGroupId") String commonContactGroupId);



    /**
     * 保存业务实体集合
     *
     * @param list 业务实体集合
     * @return 操作结果
     */
    @POST
    @Path("saveList")
    @ApiOperation(value = "保存业务实体集合", notes = "保存业务实体集合")
    ResponseData saveList(List<CommonContactPeople> list);

}
