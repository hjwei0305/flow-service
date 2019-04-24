package com.ecmp.flow.api;

import com.ecmp.core.search.PageResult;
import com.ecmp.core.search.Search;
import com.ecmp.flow.api.common.api.IBaseService;
import com.ecmp.flow.entity.BusinessModel;
import com.ecmp.flow.vo.ConditionVo;
import com.ecmp.vo.ResponseData;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.Map;

/**
 * *************************************************************************************************
 * <p>
 * 实现功能：业务实体服务API接口定义
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
@Path("businessModel")
@Api(value = "IBusinessModelService 业务实体服务API接口")
public interface IBusinessModelService extends IBaseService<BusinessModel, String> {

//    /**
//     * 保存一个实体
//     * @param businessModel 实体
//     * @return 保存后的实体
//     */
//    @POST
//    @Path("save")
//    @Consumes(MediaType.APPLICATION_JSON)
//    @Produces(MediaType.APPLICATION_JSON)
//    @ApiOperation(value = "保存实体",notes = "测试 保存实体")
//    OperateResultWithData<BusinessModel> save(BusinessModel businessModel);

    /**
     * 获取分页数据
     *
     * @return 实体清单
     */
    @POST
    @Path("findByPage")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "获取分页数据", notes = "测试 获取分页数据")
    PageResult<BusinessModel> findByPage(Search searchConfig);

    /**
     * 根据应用模块id查询业务实体
     *
     * @param appModuleId 业务模块id
     * @return 实体清单
     */
    @POST
    @Path("findByAppModuleId")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "获取业务实体数据", notes = "测试 获取业务实体数据")
    List<BusinessModel> findByAppModuleId(@QueryParam("appModuleId") String appModuleId);

    /**
     * 根据应用模块id查询业务实体
     *
     * @param classNmae 业务模块代码
     * @return 实体对象
     */
    @POST
    @Path("findByClassName")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "获取业务实体数据", notes = "测试 获取业务实体数据")
    public BusinessModel findByClassName(@QueryParam("classNmae") String classNmae);

    /**
     * 获取当前用户权限范围所有
     *
     * @return 实体清单
     */
    @GET
    @Path("findAllByAuth")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "通过当前用户筛选有权限的数据", notes = "通过当前用户筛选有权限的数据")
    public List<BusinessModel> findAllByAuth();


    /**
     * 查询条件属性
     *
     * @param businessModelCode 业务实体代码
     * @return 实体对象
     * @throws ClassNotFoundException
     */
    @POST
    @Path("getProperties")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "查询条件属性", notes = "查询条件属性")
    ResponseData getProperties(@QueryParam("businessModelCode") String businessModelCode)throws ClassNotFoundException;


    /**
     * 查询条件属性
     *
     * @param businessModelCode 业务实体代码
     * @return 实体对象
     * @throws ClassNotFoundException
     */
    @POST
    @Path("getPropertiesForConditionPojo")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "查询条件属性", notes = "查询条件属性")
    List<ConditionVo> getPropertiesForConditionPojo(@QueryParam("businessModelCode") String businessModelCode) throws ClassNotFoundException;


    /**
     * 获取表单明细（移动端专用）
     *
     */
    @POST
    @Path("getPropertiesByUrlOfModile")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "获取表单明细（移动端专用）", notes = "获取表单明细（移动端专用）")
    ResponseData getPropertiesByUrlOfModile(@QueryParam("url")String url, @QueryParam("businessModelCode") String businessModelCode, @QueryParam("id") String id);

}
