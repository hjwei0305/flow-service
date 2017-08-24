package com.ecmp.flow.api;

import com.ecmp.core.search.PageResult;
import com.ecmp.core.search.Search;
import com.ecmp.flow.api.common.api.IBaseService;
import com.ecmp.flow.constant.FlowDefinationStatus;
import com.ecmp.flow.entity.FlowDefVersion;
import com.ecmp.flow.vo.bpmn.Definition;
import com.ecmp.vo.OperateResultWithData;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBException;

/**
 * *************************************************************************************************
 * <p>
 * 实现功能：流程定义版本服务API接口定义
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
@Path("flowDefVersion")
@Api(value = "IFlowDefVersionService 流程定义版本服务API接口")
public interface IFlowDefVersionService extends IBaseService<FlowDefVersion, String> {

    /**
     * 保存一个实体
     * @param flowDefVersion 实体
     * @return 保存后的实体
     */
    @POST
    @Path("save")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "保存实体",notes = "测试 保存实体")
    OperateResultWithData<FlowDefVersion> save(FlowDefVersion flowDefVersion);

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
    PageResult<FlowDefVersion> findByPage(Search searchConfig);

    /**
     * 通过json流程定义数据，保存流程版本定义
     * @param definition json对象实体
     * @throws    JAXBException  jaxb解析异常
     * @throws    CloneNotSupportedException 复制异常
     * @return 保存后的流程版本定义实体
     */
    @POST
    @Path("jsonSave")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "json流程定义保存实体",notes = "测试 json流程定义保存实体")
    public OperateResultWithData<FlowDefVersion> save(Definition definition) throws JAXBException, CloneNotSupportedException;


    /**
     * 切换版本状态
     * @param id 单据id
     * @param status 状态
     * @return 结果
     */
    @POST
    @Path("changeStatus/{id}/{status}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "json流程定义保存实体",notes = "测试 json流程定义保存实体")
    public OperateResultWithData<FlowDefVersion> changeStatus(@PathParam("id") String id,@PathParam("status") FlowDefinationStatus status);

}
