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
@Path("businessWorkPageUrl")
@Api(value = "IBusinessWorkPageUrlService 业务实体工作界面配置管理服务API接口")
public interface IBusinessWorkPageUrlService extends IBaseService<BusinessWorkPageUrl, String> {

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
     * 通过ID启动流程实体
     * @param startUserId 流程启动人
     * @param businessKey 业务KEY
     * @param variables  其他参数
     * @param id 流程id
     * @return 流程实例
     */
    @POST
    @Path("saveBusinessWorkPageUrlByIds/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "通过ID启动流程实体",notes = "测试")
    public void saveBusinessWorkPageUrlByIds(@PathParam("id")String id, String[] selectWorkPageIds);

}
