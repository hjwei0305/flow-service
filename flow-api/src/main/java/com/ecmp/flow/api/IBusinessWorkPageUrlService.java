package com.ecmp.flow.api;

import com.ecmp.core.search.PageResult;
import com.ecmp.core.search.Search;
import com.ecmp.flow.api.common.api.IBaseService;
import com.ecmp.flow.entity.BusinessWorkPageUrl;
import com.ecmp.vo.OperateResultWithData;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

/**
 * *************************************************************************************************
 * <p>
 * 实现功能：业务实体工作界面配置管理服务API接口定义
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
@Path("businessWorkPageUrl")
@Api(value = "IBusinessWorkPageUrlService 业务实体工作界面配置管理服务API接口")
public interface IBusinessWorkPageUrlService extends IBaseService<BusinessWorkPageUrl, String> {

    /**
     * 保存一个实体
     * @param businessWorkPageUrl 实体
     * @return 保存后的实体
     */
    @POST
    @Path("save")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "保存实体",notes = "测试 保存实体")
    OperateResultWithData<BusinessWorkPageUrl> save(BusinessWorkPageUrl businessWorkPageUrl);

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
    PageResult<BusinessWorkPageUrl> findByPage(Search searchConfig);

    /**
     * 保存设置的工作界面
     * @param id 业务实体id
     * @param selectWorkPageIds 工作界面的所有id
     * @param id 流程id
     */
    @POST
    @Path("saveBusinessWorkPageUrlByIds/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "保存设置的工作界面",notes = "测试")
    public void saveBusinessWorkPageUrlByIds(@PathParam("id")String id, String selectWorkPageIds);


}
