package com.ecmp.flow.api;

import com.ecmp.core.search.PageResult;
import com.ecmp.core.search.Search;
import com.ecmp.flow.api.common.api.IBaseService;
import com.ecmp.flow.entity.WorkPageUrl;
import com.ecmp.vo.OperateResultWithData;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

/**
 * *************************************************************************************************
 * <p/>
 * 实现功能：工作界面API接口定义
 * <p>
 * ------------------------------------------------------------------------------------------------
 * 版本          变更时间             变更人                     变更原因
 * ------------------------------------------------------------------------------------------------
 * 1.0.00      2017/3/31 11:39      谭军(tanjun)                新建
 * <p/>
 * *************************************************************************************************
 */
@Path("workPageUrl")
@Api(value = "IWorkPageUrlService工作界面配置管理服务API接口")
public interface IWorkPageUrlService extends IBaseService<WorkPageUrl, String> {

    /**
     * 保存一个实体
     * @param workPageUrl 实体
     * @return 保存后的实体
     */
    @POST
    @Path("save")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "保存实体",notes = "测试 保存实体")
    OperateResultWithData<WorkPageUrl> save(WorkPageUrl workPageUrl);

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
    PageResult<WorkPageUrl> findByPage(Search searchConfig);

    /**
     * 根据应用模块id查询业务实体
     *
     * @return 实体清单
     */
    @POST
    @Path("findByAppModuleId")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "获取分页数据", notes = "测试 获取分页数据")
    List<WorkPageUrl> findByAppModuleId(@QueryParam("appModuleId") String appModuleId);


    /**
     * 查看对应业务实体已选中的工作界面
     * @param appModuleId  业务模块Id
     * @param businessModelId  业务实体ID
     * @return 已选中的工作界面
     */
    @GET
    @Path("findSelectEdByAppModuleId/{appModuleId}/{businessModelId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "查看对应业务实体已选中的工作界面",notes = "测试")
    public List<WorkPageUrl> findSelectEdByAppModuleId(@PathParam("appModuleId")String appModuleId,@PathParam("businessModelId")String businessModelId);

    /**
     * 查看对应业务实体未选中的工作界面
     * @param appModuleId  业务模块Id
     * @param businessModelId  业务实体ID
     * @return 已选中的工作界面
     */
    @GET
    @Path("findNotSelectEdByAppModuleId/{appModuleId}/{businessModelId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "查看对应业务实体未选中的工作界面",notes = "测试")
    public List<WorkPageUrl> findNotSelectEdByAppModuleId(@PathParam("appModuleId")String appModuleId,@PathParam("businessModelId")String businessModelId);


    /**
     * 通过流程类型id查找拥有的工作界面
     * @param flowTypeId 流程类型id
     * @return 工作界面list
     */
    @GET
    @Path("findByFlowTypeId")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "通过流程类型id查找拥有的工作界面",notes = "测试")
    public List<WorkPageUrl> findByFlowTypeId(String flowTypeId);


    /**
     * 查看对应业务实体已选中的工作界面
     * @param businessModelId  业务实体ID
     * @return 已选中的工作界面
     */
    @GET
    @Path("findSelectEdByBusinessModelId/{businessModelId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "通过业务实体ID查看业务实体已选中的工作界面",notes = "测试")
    public List<WorkPageUrl> findSelectEdByBusinessModelId(@PathParam("businessModelId")String businessModelId);
}
