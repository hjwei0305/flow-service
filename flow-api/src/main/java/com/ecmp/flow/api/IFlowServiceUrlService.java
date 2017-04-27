package com.ecmp.flow.api;

import com.ecmp.core.search.PageResult;
import com.ecmp.core.search.Search;
import com.ecmp.flow.common.api.IBaseService;
import com.ecmp.flow.entity.FlowServiceUrl;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * *************************************************************************************************
 * <p/>
 * 实现功能：流程历史服务API接口定义
 * <p>
 * ------------------------------------------------------------------------------------------------
 * 版本          变更时间             变更人                     变更原因
 * ------------------------------------------------------------------------------------------------
 * 1.0.00      2017/3/31 11:39      谭军(tanjun)               新建
 * <p/>
 * *************************************************************************************************
 */
@Path("flowServiceUrl")
@Api(value = "IFlowServiceUrlService 流程服务地址服务API接口")
public interface IFlowServiceUrlService extends IBaseService<FlowServiceUrl, String> {

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
    PageResult<FlowServiceUrl> findByPage(Search searchConfig);
}
