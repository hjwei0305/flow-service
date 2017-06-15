package com.ecmp.flow.api;

import com.ecmp.core.search.PageResult;
import com.ecmp.core.search.Search;
import com.ecmp.flow.api.common.api.IBaseService;
import com.ecmp.flow.entity.DefaultBusinessModel2;
import com.ecmp.vo.OperateResultWithData;
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
 * 实现功能：默认业务表单服务API接口定义
 * <p>
 * ------------------------------------------------------------------------------------------------
 * 版本          变更时间             变更人                     变更原因
 * ------------------------------------------------------------------------------------------------
 * 1.0.00      2017/3/26 10:39      谭军(tanjun)               新建
 * <p/>
 * *************************************************************************************************
 */
@Path("defaultBusinessModel2")
@Api(value = "IDefaultBusinessModel2Service 采购业务表单服务API接口")
public interface IDefaultBusinessModel2Service extends IBaseService<DefaultBusinessModel2, String> {
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



}
